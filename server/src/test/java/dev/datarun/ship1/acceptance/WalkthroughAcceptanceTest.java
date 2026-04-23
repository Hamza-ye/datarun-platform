package dev.datarun.ship1.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.DatarunApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ship-1 scenario acceptance. Drives the real server over HTTP as two simulated devices to prove
 * W-0 (happy path), W-1 (duplicate household across devices), and W-2 (out-of-scope capture) from
 * {@code docs/ships/ship-1.md §6.4}.
 *
 * <p>This is the CI gate per retro criterion #1. Manual two-emulator run is the retro demo.
 */
@SpringBootTest(classes = DatarunApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WalkthroughAcceptanceTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper mapper;

    private final RestTemplate http = new RestTemplate();

    @BeforeEach
    void cleanDb() {
        jdbc.update("TRUNCATE events, actor_tokens, villages RESTART IDENTITY CASCADE");
        jdbc.update("ALTER SEQUENCE server_device_seq RESTART WITH 1");
    }

    private String url(String path) { return "http://localhost:" + port + path; }

    /**
     * W-0 — a normal capture by CHV-A in village-1 completes with no flags.
     * Covers S00 + S01 + S03.
     */
    @Test
    void walkthrough_W0_happy_path() throws Exception {
        Bootstrap b = bootstrap();
        UUID deviceA = UUID.randomUUID();
        UUID subject = UUID.randomUUID();

        ObjectNode capture = buildHouseholdCapture(
                subject, b.chvAActor, deviceA, 1, b.villageA, "Khan household", 5);
        HttpResponse<String> resp = push(pushBody(capture));
        assertThat(resp.statusCode).isEqualTo(200);
        JsonNode parsed = mapper.readTree(resp.body);
        assertThat(parsed.path("accepted").asInt()).isEqualTo(1);
        assertThat(parsed.path("flags_raised").asInt()).isEqualTo(0);

        // CHV-A pulls — sees their own capture.
        JsonNode pull = pullJson(b.chvAToken, 0);
        ArrayNode events = (ArrayNode) pull.path("events");
        long captures = countByShape(events, "household_observation/v1");
        long flags = countByShape(events, "conflict_detected/v1");
        assertThat(captures).isEqualTo(1);
        assertThat(flags).isEqualTo(0);
    }

    /**
     * W-1 — duplicate household across devices emits one identity_conflict flag.
     * Covers S01 (duplicate-subject detection) and S03 (each CHV in their own scope path).
     *
     * <p>Spec §6.4 step 5 asserts "one identity_conflict flag". The spec's own seed (CHV-A→village-1,
     * CHV-B→village-2) also makes CHV-B's capture in village-1 a scope_violation; this test asserts
     * both flags are present. This interaction is documented in the Ship-1 retro, not an ADR.
     */
    @Test
    void walkthrough_W1_duplicate_household() throws Exception {
        Bootstrap b = bootstrap();
        UUID deviceA = UUID.randomUUID();
        UUID deviceB = UUID.randomUUID();

        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();

        // Step 2: CHV-A captures Khan (5 members) offline.
        ObjectNode captureA = buildHouseholdCapture(
                subjectA, b.chvAActor, deviceA, 1, b.villageA, "Khan household", 5);
        // Step 3: CHV-B captures Khan (6 members) offline, same village-A.
        ObjectNode captureB = buildHouseholdCapture(
                subjectB, b.chvBActor, deviceB, 1, b.villageA, "Khan household", 6);

        // Step 4: device-A syncs first.
        HttpResponse<String> respA = push(pushBody(captureA));
        assertThat(respA.statusCode).isEqualTo(200);
        assertThat(mapper.readTree(respA.body).path("flags_raised").asInt())
                .describedAs("first capture has no prior duplicate, no scope_violation")
                .isEqualTo(0);

        // Step 5: device-B syncs.
        HttpResponse<String> respB = push(pushBody(captureB));
        assertThat(respB.statusCode).isEqualTo(200);
        int flagsFromB = mapper.readTree(respB.body).path("flags_raised").asInt();
        // CHV-B's capture is in village-A (not B's assignment) → scope_violation AND
        // matches prior Khan-village-A capture → identity_conflict.
        assertThat(flagsFromB).isEqualTo(2);

        // Assert: two capture events exist.
        assertThat(countEvents("capture")).isEqualTo(2);

        // Assert: exactly one identity_conflict flag event, pointing at both captures.
        JsonNode idFlags = adminListFlags("identity_conflict");
        assertThat(idFlags.size()).isEqualTo(1);
        JsonNode idFlagPayload = idFlags.get(0).path("payload");
        JsonNode related = idFlagPayload.path("related_event_ids");
        assertThat(related.size()).isEqualTo(2);
        assertThat(related.toString())
                .contains(captureA.path("id").asText())
                .contains(captureB.path("id").asText());

        // Assert: flag was emitted with system actor in expected format (F-A3).
        String flagActor = idFlags.get(0).path("actor_id").asText();
        assertThat(flagActor).isEqualTo("system:conflict_detector/identity_conflict");

        // Assert: flag is shape-discriminated, not type-discriminated (F-A2, F-A4).
        assertThat(idFlags.get(0).path("shape_ref").asText()).isEqualTo("conflict_detected/v1");
        assertThat(idFlags.get(0).path("type").asText()).isEqualTo("alert");

        // Admin UI: the unresolved flags list includes this flag.
        String adminFlagsHtml = http.getForObject(url("/admin/flags"), String.class);
        assertThat(adminFlagsHtml).contains("identity_conflict");
    }

    /**
     * W-2 — out-of-scope capture: CHV-A submits a household whose village_ref is in village-B.
     * Server accepts it and emits a scope_violation flag. CHV-A's subsequent pull does NOT return
     * the out-of-scope subject's event timeline (scope filtering on read side holds).
     */
    @Test
    void walkthrough_W2_out_of_scope_capture() throws Exception {
        Bootstrap b = bootstrap();
        UUID deviceA = UUID.randomUUID();
        UUID outOfScopeSubject = UUID.randomUUID();

        // CHV-A captures a household in village-B (not their assigned scope).
        ObjectNode capture = buildHouseholdCapture(
                outOfScopeSubject, b.chvAActor, deviceA, 1, b.villageB, "Ahmed household", 4);
        HttpResponse<String> resp = push(pushBody(capture));
        assertThat(resp.statusCode).isEqualTo(200);
        JsonNode parsed = mapper.readTree(resp.body);
        assertThat(parsed.path("accepted").asInt())
                .describedAs("accept-and-flag: never rejected")
                .isEqualTo(1);
        assertThat(parsed.path("flags_raised").asInt()).isEqualTo(1);

        // Flag is scope_violation.
        JsonNode sv = adminListFlags("scope_violation");
        assertThat(sv.size()).isEqualTo(1);
        assertThat(sv.get(0).path("payload").path("source_event_id").asText())
                .isEqualTo(capture.path("id").asText());
        assertThat(sv.get(0).path("actor_id").asText())
                .isEqualTo("system:conflict_detector/scope_violation");

        // Pull-side scope filtering: CHV-A's pull does NOT contain the village-B capture.
        JsonNode pull = pullJson(b.chvAToken, 0);
        ArrayNode events = (ArrayNode) pull.path("events");
        for (JsonNode e : events) {
            if ("household_observation/v1".equals(e.path("shape_ref").asText())) {
                assertThat(e.path("subject_ref").path("id").asText())
                        .describedAs("CHV-A's pull must not expose the out-of-scope village-B subject")
                        .isNotEqualTo(outOfScopeSubject.toString());
            }
        }
    }

    // =========================================================================== helpers
    private record Bootstrap(UUID villageA, UUID villageB, UUID chvAActor, String chvAToken,
                             UUID chvBActor, String chvBToken) {}

    private Bootstrap bootstrap() throws Exception {
        ResponseEntity<String> resp = http.exchange(
                url("/dev/bootstrap"), HttpMethod.POST, HttpEntity.EMPTY, String.class);
        JsonNode body = mapper.readTree(resp.getBody());
        return new Bootstrap(
                UUID.fromString(body.path("village_a").asText()),
                UUID.fromString(body.path("village_b").asText()),
                UUID.fromString(body.path("chv_a_actor_id").asText()),
                body.path("chv_a_token").asText(),
                UUID.fromString(body.path("chv_b_actor_id").asText()),
                body.path("chv_b_token").asText());
    }

    private ObjectNode buildHouseholdCapture(UUID subjectId, UUID actorId, UUID deviceId,
                                             long deviceSeq, UUID villageId,
                                             String householdName, int householdSize) {
        ObjectNode env = mapper.createObjectNode();
        env.put("id", UUID.randomUUID().toString());
        env.put("type", "capture");
        env.put("shape_ref", "household_observation/v1");
        env.put("activity_ref", "household_observation");
        ObjectNode sref = env.putObject("subject_ref");
        sref.put("type", "subject");
        sref.put("id", subjectId.toString());
        ObjectNode aref = env.putObject("actor_ref");
        aref.put("type", "actor");
        aref.put("id", actorId.toString());
        env.put("device_id", deviceId.toString());
        env.put("device_seq", deviceSeq);
        env.putNull("sync_watermark");
        env.put("timestamp", OffsetDateTime.now().toString());
        ObjectNode payload = env.putObject("payload");
        payload.put("household_name", householdName);
        payload.put("head_of_household_name", "HoH");
        payload.put("household_size", householdSize);
        payload.put("village_ref", villageId.toString());
        payload.putNull("latitude");
        payload.putNull("longitude");
        payload.putNull("visit_notes");
        return env;
    }

    private ObjectNode pushBody(ObjectNode... events) {
        ObjectNode body = mapper.createObjectNode();
        ArrayNode arr = body.putArray("events");
        for (ObjectNode e : events) arr.add(e);
        return body;
    }

    private HttpResponse<String> push(ObjectNode body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Push is authenticated too (same interceptor) — use any valid token; device attribution
        // happens via device_id in the envelope, not via the token.
        headers.set("Authorization", "Bearer " + issueAdminToken());
        ResponseEntity<String> resp;
        try {
            resp = http.exchange(url("/api/sync/push"), HttpMethod.POST,
                    new HttpEntity<>(body.toString(), headers), String.class);
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            return new HttpResponse<>(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
        return new HttpResponse<>(resp.getStatusCode().value(), resp.getBody());
    }

    /** For push we need a token but we authorize on bearer only (any provisioned actor). */
    private String adminToken;
    private String issueAdminToken() {
        if (adminToken != null) return adminToken;
        adminToken = java.util.UUID.randomUUID().toString().replace("-", "");
        jdbc.update("INSERT INTO actor_tokens (token, actor_id) VALUES (?, ?)",
                adminToken, UUID.randomUUID());
        return adminToken;
    }

    private JsonNode pullJson(String token, long sinceWatermark) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        ObjectNode body = mapper.createObjectNode();
        body.put("since_watermark", sinceWatermark);
        body.put("limit", 500);
        ResponseEntity<String> resp = http.exchange(
                url("/api/sync/pull"), HttpMethod.POST,
                new HttpEntity<>(body.toString(), headers), String.class);
        return mapper.readTree(resp.getBody());
    }

    private JsonNode adminListFlags(String category) throws Exception {
        // Read directly from DB since no admin JSON API exists; mirrors the Thymeleaf view source.
        java.util.List<java.util.Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, type, shape_ref, subject_id, actor_id, payload::text AS payload " +
                        "FROM events WHERE shape_ref LIKE 'conflict_detected/%' " +
                        "AND payload->>'flag_category' = ? ORDER BY sync_watermark",
                category);
        ArrayNode arr = mapper.createArrayNode();
        for (var row : rows) {
            ObjectNode n = mapper.createObjectNode();
            n.put("id", row.get("id").toString());
            n.put("type", row.get("type").toString());
            n.put("shape_ref", row.get("shape_ref").toString());
            n.put("subject_id", row.get("subject_id").toString());
            n.put("actor_id", row.get("actor_id").toString());
            n.set("payload", mapper.readTree(row.get("payload").toString()));
            arr.add(n);
        }
        return arr;
    }

    private int countEvents(String type) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM events WHERE type = ?", Integer.class, type);
        return n == null ? 0 : n;
    }

    private long countByShape(ArrayNode events, String shapeRef) {
        long n = 0;
        for (JsonNode e : events) if (shapeRef.equals(e.path("shape_ref").asText())) n++;
        return n;
    }

    /** Tiny record to avoid depending on java.net.http types. */
    private record HttpResponse<T>(int statusCode, T body) {}
}
