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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ship-3 scenario acceptance — drives the real server over HTTP for the four shape-evolution
 * walkthroughs from {@code docs/ships/ship-3.md §6.4}:
 *
 * <ul>
 *   <li>W-6 — additive happy path; FP-009 closure assertion (mixed v1/v2 identity-conflict
 *       detection without modifying ConflictDetector);</li>
 *   <li>W-7 — deprecation: deprecated field on v2 still accepted and stored verbatim;
 *       v1 captures continue to work; both render via /admin/events;</li>
 *   <li>W-8 — unknown {@code shape_ref} rejection (HTTP 400 with 'shape_unknown' marker);</li>
 *   <li>W-10 — backward-compat read: pre-existing v1 events remain readable after v2 lands,
 *       and /admin/events renders the mixed-version timeline with version-specific fidelity.</li>
 * </ul>
 *
 * <p>Sibling of {@link WalkthroughAcceptanceTest} (Ship-1) and
 * {@link Ship2WalkthroughAcceptanceTest} (Ship-2) — same SpringBootTest harness, same
 * {@code @BeforeEach} truncate; prior Ship walkthroughs remain green and untouched.
 *
 * <h3>Discriminator discipline (F-A2)</h3>
 * Every assertion below filters by {@code shape_ref}, never by envelope {@code type}.
 *
 * <h3>FP-009 closure note</h3>
 * {@link dev.datarun.ship1.integrity.ConflictDetector} hard-codes the v1 entry guard at the
 * detector entry. Identity-key field names ({@code village_ref}, {@code household_name}) are
 * preserved verbatim in v2 per Ship-3 §6.1 sub-decision 2, so the prefix scan
 * ({@code findByShapeRefPrefix("household_observation/")}) finds v2 priors when a v1 capture
 * arrives. The mixed-version assertion in W-6 therefore targets the v1-current + v2-prior
 * direction (where the detector activates and the prior-scan crosses versions). The
 * v2-current direction is intentionally not asserted in this Ship — see SHIP-3 BUILD REPORT
 * 'Surprises / deviations' for the discussion.
 */
@SpringBootTest(classes = DatarunApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class Ship3WalkthroughAcceptanceTest {

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
     * W-6 — additive happy path. FP-009 closure assertion.
     *
     * <p>Step 1: two v1 captures with the same household_name in the same village fire
     * identity_conflict (Ship-1 baseline preserved).
     *
     * <p>Step 2: a v2 capture with a NEW household_name is accepted by the server (validates
     * against the v2 schema; uses the v2-only head_of_household_phone field).
     *
     * <p>Step 3: a v1 capture with that same NEW household_name in the same village arrives.
     * ConflictDetector activates (v1 entry guard satisfied), prefix-scans
     * household_observation/* and finds the v2 prior, sees identical normalized
     * household_name + same village_ref, and emits identity_conflict.
     *
     * <p>This proves the detector continues to fire across mixed v1/v2 events without code
     * changes — closure of FP-009 in the direction Ship-3 commits to.
     */
    @Test
    void walkthrough_W6_additive_happy_path_with_FP009_closure() throws Exception {
        Bootstrap b = bootstrap();
        UUID deviceA = UUID.randomUUID();
        UUID deviceB = UUID.randomUUID();

        // ---- Step 1: v1 + v1 baseline (Ship-1 W-1 reproduced) ----
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        push(pushBody(buildHouseholdCaptureV1(s1, b.chvAActor, deviceA, 1, b.villageA, "Khan household", 5)));
        HttpResponse<String> resp1 = push(pushBody(buildHouseholdCaptureV1(
                s2, b.chvAActor, deviceA, 2, b.villageA, "Khan household", 6)));
        assertThat(mapper.readTree(resp1.body).path("flags_raised").asInt())
                .describedAs("v1 baseline: identity_conflict still fires under Ship-3 multi-version registry")
                .isEqualTo(1);

        // ---- Step 2: v2 capture accepted ----
        UUID s3 = UUID.randomUUID();
        ObjectNode captureV2 = buildHouseholdCaptureV2(
                s3, b.chvAActor, deviceA, 3, b.villageA, "Sharif household", 4, "+1-555-0100");
        HttpResponse<String> respV2 = push(pushBody(captureV2));
        assertThat(respV2.statusCode).isEqualTo(200);
        JsonNode parsedV2 = mapper.readTree(respV2.body);
        assertThat(parsedV2.path("accepted").asInt()).isEqualTo(1);
        assertThat(parsedV2.path("flags_raised").asInt())
                .describedAs("Sharif is novel in village-A; no prior duplicate")
                .isEqualTo(0);

        // Assert v2 event landed in DB with the v2-only field preserved verbatim.
        var v2row = jdbc.queryForMap(
                "SELECT shape_ref, payload::text AS payload FROM events WHERE id = ?::uuid",
                captureV2.path("id").asText());
        assertThat(v2row.get("shape_ref")).isEqualTo("household_observation/v2");
        JsonNode v2payload = mapper.readTree(v2row.get("payload").toString());
        assertThat(v2payload.path("head_of_household_phone").asText()).isEqualTo("+1-555-0100");
        // FP-009 closure: identity-key fields preserved in v2 storage.
        assertThat(v2payload.path("village_ref").asText()).isEqualTo(b.villageA.toString());
        assertThat(v2payload.path("household_name").asText()).isEqualTo("Sharif household");

        // ---- Step 3: FP-009 closure — v1 capture finds v2 prior, detector fires ----
        UUID s4 = UUID.randomUUID();
        ObjectNode v1Mixed = buildHouseholdCaptureV1(
                s4, b.chvAActor, deviceA, 4, b.villageA, "Sharif household", 5);
        HttpResponse<String> respMixed = push(pushBody(v1Mixed));
        assertThat(respMixed.statusCode).isEqualTo(200);
        assertThat(mapper.readTree(respMixed.body).path("flags_raised").asInt())
                .describedAs("FP-009 closure: v1-current capture finds v2 prior via prefix scan; " +
                        "identity-key field names (village_ref, household_name) preserved across " +
                        "versions per §6.1 sub-decision 2; detector fires WITHOUT code changes.")
                .isEqualTo(1);

        // Assert: exactly one mixed-version identity_conflict flag references the v2 prior.
        JsonNode mixedFlags = adminListFlagsRelatedTo(captureV2.path("id").asText(), v1Mixed.path("id").asText());
        assertThat(mixedFlags.size())
                .describedAs("one identity_conflict references both the v2 prior and the v1 current")
                .isEqualTo(1);
        assertThat(mixedFlags.get(0).path("shape_ref").asText()).isEqualTo("conflict_detected/v1");
        assertThat(mixedFlags.get(0).path("type").asText()).isEqualTo("alert");
        assertThat(mixedFlags.get(0).path("actor_id").asText())
                .isEqualTo("system:conflict_detector/identity_conflict");

        // FP-009 closure final evidence: ConflictDetector fired across version boundary.
        // (Code-level evidence — that ConflictDetector.java is unchanged across Ship-3 — is
        //  asserted by the build report's `git diff ship-2 -- <path>` check.)
    }

    /**
     * W-7 — deprecation. A v2 capture with the deprecated visit_notes field present is stored
     * verbatim; a v1 capture continues to function unchanged; both events render correctly via
     * /admin/events with version-specific fidelity.
     */
    @Test
    void walkthrough_W7_deprecation_field_stored_verbatim_both_versions_render() throws Exception {
        Bootstrap b = bootstrap();
        UUID deviceA = UUID.randomUUID();
        UUID deviceB = UUID.randomUUID();

        // v2 capture with the deprecated visit_notes field present.
        UUID sV2 = UUID.randomUUID();
        ObjectNode v2 = buildHouseholdCaptureV2(
                sV2, b.chvAActor, deviceA, 1, b.villageA, "Ali household", 4, "+1-555-0200");
        ((ObjectNode) v2.path("payload")).put("visit_notes", "DEPRECATED-FIELD-MARKER-W7");
        HttpResponse<String> respV2 = push(pushBody(v2));
        assertThat(respV2.statusCode).isEqualTo(200);
        assertThat(mapper.readTree(respV2.body).path("accepted").asInt()).isEqualTo(1);

        // v1 capture still works.
        UUID sV1 = UUID.randomUUID();
        ObjectNode v1 = buildHouseholdCaptureV1(
                sV1, b.chvBActor, deviceB, 1, b.villageB, "Yusuf household", 3);
        HttpResponse<String> respV1 = push(pushBody(v1));
        assertThat(respV1.statusCode).isEqualTo(200);
        assertThat(mapper.readTree(respV1.body).path("accepted").asInt()).isEqualTo(1);

        // Stored verbatim — deprecated field present in DB exactly as sent (ADR-001 §S1 immutability).
        var stored = jdbc.queryForMap(
                "SELECT payload::text AS payload FROM events WHERE id = ?::uuid",
                v2.path("id").asText());
        JsonNode storedPayload = mapper.readTree(stored.get("payload").toString());
        assertThat(storedPayload.path("visit_notes").asText()).isEqualTo("DEPRECATED-FIELD-MARKER-W7");
        assertThat(storedPayload.path("head_of_household_phone").asText()).isEqualTo("+1-555-0200");

        // Both events render via /admin/events — version-specific rendering, fidelity preserved.
        String html = http.getForObject(url("/admin/events"), String.class);
        assertThat(html)
                .contains("household_observation v2")
                .contains("Ali household")
                .contains("DEPRECATED-FIELD-MARKER-W7")
                .contains("phone (v2-only)")
                .contains("+1-555-0200")
                .contains("household_observation v1")
                .contains("Yusuf household")
                .contains("(v1: head_of_household_phone field is not present)");
    }

    /**
     * W-8 — unknown shape_ref rejection. Server returns HTTP 400 with the {@code shape_unknown}
     * marker in the response body. Ship-1's strict-unknown choice preserved.
     */
    @Test
    void walkthrough_W8_unknown_shape_ref_rejected_400_shape_unknown() throws Exception {
        Bootstrap b = bootstrap();
        UUID device = UUID.randomUUID();
        UUID subject = UUID.randomUUID();

        // First case: future version of an existing shape.
        ObjectNode capture = buildHouseholdCaptureV1(subject, b.chvAActor, device, 1, b.villageA, "X", 1);
        capture.put("shape_ref", "household_observation/v9");
        HttpResponse<String> resp = push(pushBody(capture));
        assertThat(resp.statusCode).isEqualTo(400);
        assertThat(resp.body)
                .describedAs("body contains the shape_unknown marker")
                .contains("shape_unknown")
                .contains("household_observation/v9");

        // Second case: nonexistent shape entirely.
        ObjectNode capture2 = buildHouseholdCaptureV1(
                UUID.randomUUID(), b.chvAActor, device, 2, b.villageA, "Y", 1);
        capture2.put("shape_ref", "nonexistent_shape/v1");
        HttpResponse<String> resp2 = push(pushBody(capture2));
        assertThat(resp2.statusCode).isEqualTo(400);
        assertThat(resp2.body).contains("shape_unknown").contains("nonexistent_shape/v1");

        // Neither rejected event landed in the store (validation failure rejects whole batch).
        Integer rejectedRows = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE shape_ref IN ('household_observation/v9','nonexistent_shape/v1')",
                Integer.class);
        assertThat(rejectedRows).isEqualTo(0);
    }

    /**
     * W-10 — backward-compat read. Pre-existing v1 events are readable after v2 lands; the
     * /admin/events page renders a mixed-version timeline with payload fidelity intact for
     * each version.
     */
    @Test
    void walkthrough_W10_backward_compat_mixed_version_admin_render() throws Exception {
        Bootstrap b = bootstrap();
        UUID deviceA = UUID.randomUUID();
        UUID deviceB = UUID.randomUUID();

        // Pre-load: a v1 capture (the "Ship-1 fixture" — pre-Ship-3 event in this scope).
        UUID s1 = UUID.randomUUID();
        ObjectNode v1pre = buildHouseholdCaptureV1(
                s1, b.chvAActor, deviceA, 1, b.villageA, "PreShip3HouseholdV1", 5);
        push(pushBody(v1pre));

        // After v2 deploys (registry already has v2 from boot), a v2 capture lands.
        UUID s2 = UUID.randomUUID();
        ObjectNode v2new = buildHouseholdCaptureV2(
                s2, b.chvBActor, deviceB, 1, b.villageB, "PostShip3HouseholdV2", 7, "+1-555-0300");
        push(pushBody(v2new));

        // Both versions readable from the event store.
        Integer v1count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE shape_ref = 'household_observation/v1'", Integer.class);
        Integer v2count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE shape_ref = 'household_observation/v2'", Integer.class);
        assertThat(v1count).isEqualTo(1);
        assertThat(v2count).isEqualTo(1);

        // /admin/events renders the mixed-version timeline; each version's distinguishing
        // fields visible per its own version (per-event branching on shape_ref).
        String html = http.getForObject(url("/admin/events"), String.class);
        assertThat(html)
                .describedAs("v1 row: version-specific rendering")
                .contains("household_observation v1")
                .contains("PreShip3HouseholdV1")
                .contains("(v1: head_of_household_phone field is not present)");
        assertThat(html)
                .describedAs("v2 row: version-specific rendering — the v2-only field is shown")
                .contains("household_observation v2")
                .contains("PostShip3HouseholdV2")
                .contains("phone (v2-only)")
                .contains("+1-555-0300");
        // shape_ref discriminator visible (F-A2 / F-B4 — never by envelope type).
        assertThat(html).contains("v1").contains("v2");
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

    /** v1 capture envelope — identical shape to the Ship-1 helper for parity. */
    private ObjectNode buildHouseholdCaptureV1(UUID subjectId, UUID actorId, UUID deviceId,
                                               long deviceSeq, UUID villageId,
                                               String householdName, int householdSize) {
        ObjectNode env = baseCaptureEnvelope(
                subjectId, actorId, deviceId, deviceSeq, "household_observation/v1");
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

    /** v2 capture envelope — adds head_of_household_phone (v2-only). */
    private ObjectNode buildHouseholdCaptureV2(UUID subjectId, UUID actorId, UUID deviceId,
                                               long deviceSeq, UUID villageId,
                                               String householdName, int householdSize,
                                               String phone) {
        ObjectNode env = baseCaptureEnvelope(
                subjectId, actorId, deviceId, deviceSeq, "household_observation/v2");
        ObjectNode payload = env.putObject("payload");
        payload.put("household_name", householdName);
        payload.put("head_of_household_name", "HoH");
        payload.put("head_of_household_phone", phone);
        payload.put("household_size", householdSize);
        payload.put("village_ref", villageId.toString());
        payload.putNull("latitude");
        payload.putNull("longitude");
        payload.putNull("visit_notes");
        return env;
    }

    private ObjectNode baseCaptureEnvelope(UUID subjectId, UUID actorId, UUID deviceId,
                                           long deviceSeq, String shapeRef) {
        ObjectNode env = mapper.createObjectNode();
        env.put("id", UUID.randomUUID().toString());
        env.put("type", "capture");
        env.put("shape_ref", shapeRef);
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
        headers.set("Authorization", "Bearer " + issueAdminToken());
        try {
            ResponseEntity<String> resp = http.exchange(url("/api/sync/push"), HttpMethod.POST,
                    new HttpEntity<>(body.toString(), headers), String.class);
            return new HttpResponse<>(resp.getStatusCode().value(), resp.getBody());
        } catch (HttpClientErrorException ex) {
            return new HttpResponse<>(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    private String adminToken;
    private String issueAdminToken() {
        if (adminToken != null) return adminToken;
        adminToken = UUID.randomUUID().toString().replace("-", "");
        jdbc.update("INSERT INTO actor_tokens (token, actor_id) VALUES (?, ?)",
                adminToken, UUID.randomUUID());
        return adminToken;
    }

    /** All identity_conflict flags whose related_event_ids contain BOTH supplied event ids. */
    private JsonNode adminListFlagsRelatedTo(String eventA, String eventB) throws Exception {
        var rows = jdbc.queryForList(
                "SELECT id, type, shape_ref, actor_id, payload::text AS payload " +
                        "FROM events WHERE shape_ref LIKE 'conflict_detected/%' " +
                        "AND payload->>'flag_category' = 'identity_conflict' " +
                        "AND payload::text LIKE ? AND payload::text LIKE ? " +
                        "ORDER BY sync_watermark",
                "%" + eventA + "%", "%" + eventB + "%");
        ArrayNode out = mapper.createArrayNode();
        for (var row : rows) {
            ObjectNode n = mapper.createObjectNode();
            n.put("id", row.get("id").toString());
            n.put("type", row.get("type").toString());
            n.put("shape_ref", row.get("shape_ref").toString());
            n.put("actor_id", row.get("actor_id").toString());
            n.set("payload", mapper.readTree(row.get("payload").toString()));
            out.add(n);
        }
        return out;
    }

    private record HttpResponse<T>(int statusCode, T body) {}
}
