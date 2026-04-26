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
 * Ship-2 scenario acceptance. Drives the real server over HTTP with a coordinator and CHV(s)
 * to prove W-3 (reactive merge after identity_conflict, ADR-002 §S6+§S13), W-4 (wrong-merge
 * correction via multi-successor split, §S7+§S8 + schema arity edit), and W-5 (lineage DAG
 * enforcement, §S9) from {@code docs/ships/ship-2.md §6.4}.
 *
 * <p>Sibling of {@link WalkthroughAcceptanceTest} (Ship-1). Same package, same SpringBootTest
 * harness, same @BeforeEach truncate so Ship-1 walkthroughs remain untouched and green.
 *
 * <h3>Duplicate-construction strategy: B (single-CHV, clean)</h3>
 * Both colliding captures are authored by CHV-A inside village-A, so only {@code identity_conflict}
 * fires — no incidental {@code scope_violation} to filter through. Strategy A (cross-CHV) is also
 * acceptable per spec §6.4 but would force every W-3/W-4/W-5 flag count to first subtract the
 * cross-village scope violation. Merge/split semantics are independent of how the duplicate was
 * detected, so the cleaner strategy is preferred.
 *
 * <h3>Discriminator discipline (F-A2 / F-A4 / F-B4)</h3>
 * Every flag/event assertion below filters by {@code shape_ref}, never envelope {@code type}.
 * Authorship asserted by {@code actor_id} UUID-shape (merge/split) vs {@code system:}-prefix
 * (ConflictDetector), never by envelope {@code type}.
 */
@SpringBootTest(classes = DatarunApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class Ship2WalkthroughAcceptanceTest {

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
     * W-3 — reactive merge after {@code identity_conflict} (ADR-002 §S6, §S13).
     *
     * <p>Two captures fire {@code identity_conflict}; coordinator merges {@code S_X → S_Y};
     * a follow-up merge {@code S_Y → S_Z} proves eager transitive closure.
     */
    @Test
    void walkthrough_W3_reactive_merge_after_identity_conflict() throws Exception {
        Bootstrap b = bootstrap();
        UUID deviceA = UUID.randomUUID();
        UUID subjectX = UUID.randomUUID();
        UUID subjectY = UUID.randomUUID();
        UUID subjectZ = UUID.randomUUID();

        // Strategy B setup: CHV-A writes "Khan" in village-A twice with different subject_ids.
        ObjectNode captureX = buildHouseholdCapture(
                subjectX, b.chvAActor, deviceA, 1, b.villageA, "Khan household", 5);
        ObjectNode captureY = buildHouseholdCapture(
                subjectY, b.chvAActor, deviceA, 2, b.villageA, "Khan household", 6);
        push(pushBody(captureX));
        HttpResponse<String> respY = push(pushBody(captureY));
        assertThat(mapper.readTree(respY.body).path("flags_raised").asInt())
                .describedAs("Strategy B: only identity_conflict, no scope_violation")
                .isEqualTo(1);

        // Coordinator decides S_X and S_Y are the same household; merges X (retired) into Y (surviving).
        HttpResponse<String> mergeResp = postAsCoordinator(b.coordinatorToken, "/admin/subjects/merge",
                "{\"surviving_id\":\"" + subjectY + "\",\"retired_id\":\"" + subjectX + "\"}");
        assertThat(mergeResp.statusCode).isEqualTo(200);
        String mergeEventId = mapper.readTree(mergeResp.body).path("event_id").asText();

        // Assert: subjects_merged/v1 event persisted with correct envelope discipline (§6 commitments 3+7).
        var mergeRow = jdbc.queryForMap(
                "SELECT type, shape_ref, actor_id, device_id, sync_watermark, payload::text AS payload " +
                        "FROM events WHERE id = ?::uuid", mergeEventId);
        assertThat(mergeRow.get("type"))
                .describedAs("envelope type=capture (F2 — vocabulary closed at six)")
                .isEqualTo("capture");
        assertThat(mergeRow.get("shape_ref")).isEqualTo("subjects_merged/v1");
        assertThat(mergeRow.get("actor_id"))
                .describedAs("F-B4: human-authored — coordinator UUID, NOT system: prefix")
                .isEqualTo(b.coordinatorActor.toString());
        assertThat(UUID.fromString(mergeRow.get("actor_id").toString()))
                .describedAs("actor_id parses as a UUID (not a system:component/id string)")
                .isEqualTo(b.coordinatorActor);
        assertThat(mergeRow.get("device_id").toString())
                .describedAs("server-authored: reserved server device UUID (§6 commitment 7)")
                .isEqualTo("00000000-0000-0000-0000-000000000001");

        // Assert: historical capture for S_X remains present with original subject_id (raw, not rewritten — §S6).
        Integer historicalX = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE shape_ref = 'household_observation/v1' AND subject_id = ?",
                Integer.class, subjectX);
        assertThat(historicalX)
                .describedAs("§S6: historical events keep their original subject_id; alias is a parallel projection")
                .isEqualTo(1);

        // Assert: GET /admin/subjects/{S_X}/canonical resolves S_X → S_Y.
        JsonNode canonicalAfterFirst = getJson(b.coordinatorToken, "/admin/subjects/" + subjectX + "/canonical");
        assertThat(canonicalAfterFirst.path("canonical_id").asText()).isEqualTo(subjectY.toString());
        assertThat(canonicalAfterFirst.path("alias_chain_length").asInt()).isGreaterThanOrEqualTo(1);

        // Eager closure: a follow-up merge S_Y → S_Z. After this, querying S_X must return S_Z directly.
        HttpResponse<String> mergeResp2 = postAsCoordinator(b.coordinatorToken, "/admin/subjects/merge",
                "{\"surviving_id\":\"" + subjectZ + "\",\"retired_id\":\"" + subjectY + "\"}");
        assertThat(mergeResp2.statusCode).isEqualTo(200);

        JsonNode canonicalAfterSecond = getJson(b.coordinatorToken, "/admin/subjects/" + subjectX + "/canonical");
        assertThat(canonicalAfterSecond.path("canonical_id").asText())
                .describedAs("§S6 eager transitive closure: S_X → S_Y → S_Z resolves to S_Z in a single read")
                .isEqualTo(subjectZ.toString());
    }

    /**
     * W-4 — wrong-merge correction via multi-successor split (§S7 + §S8; exercises the
     * {@code successor_ids: array, minItems: 2} schema arity edit).
     */
    @Test
    void walkthrough_W4_wrong_merge_correction_via_multi_successor_split() throws Exception {
        Bootstrap b = bootstrap();
        UUID deviceA = UUID.randomUUID();
        UUID subjectX = UUID.randomUUID();
        UUID subjectY = UUID.randomUUID();
        UUID xPrime = UUID.randomUUID();
        UUID yPrime = UUID.randomUUID();

        // Setup mirrors W-3 (identity_conflict + merge), then coordinator realises X and Y were
        // genuinely distinct entities and corrects via multi-successor split.
        push(pushBody(buildHouseholdCapture(
                subjectX, b.chvAActor, deviceA, 1, b.villageA, "Khan household", 5)));
        push(pushBody(buildHouseholdCapture(
                subjectY, b.chvAActor, deviceA, 2, b.villageA, "Khan household", 6)));
        HttpResponse<String> mergeResp = postAsCoordinator(b.coordinatorToken, "/admin/subjects/merge",
                "{\"surviving_id\":\"" + subjectY + "\",\"retired_id\":\"" + subjectX + "\"}");
        assertThat(mergeResp.statusCode).isEqualTo(200);

        // Reject empty-array variant pre-validation (§S7: minItems=2 is structurally required).
        int eventsBeforeRejection = countAllEvents();
        HttpResponse<String> emptySplit = postAsCoordinator(b.coordinatorToken, "/admin/subjects/split",
                "{\"source_id\":\"" + subjectY + "\",\"successor_ids\":[]}");
        assertThat(emptySplit.statusCode)
                .describedAs("empty successor_ids: structural 4xx, NOT accept-and-flag")
                .isBetween(400, 499);
        assertThat(countAllEvents())
                .describedAs("4xx structural rejection persists nothing")
                .isEqualTo(eventsBeforeRejection);

        // Coordinator splits the (wrongly-merged) survivor S_Y into [yPrime, xPrime] atomically.
        HttpResponse<String> splitResp = postAsCoordinator(b.coordinatorToken, "/admin/subjects/split",
                "{\"source_id\":\"" + subjectY + "\",\"successor_ids\":[\"" + yPrime + "\",\"" + xPrime + "\"]}");
        assertThat(splitResp.statusCode).isEqualTo(200);
        String splitEventId = mapper.readTree(splitResp.body).path("event_id").asText();

        // Assert: subject_split/v1 event persisted with successor_ids array of length 2.
        JsonNode splitPayload = mapper.readTree(jdbc.queryForObject(
                "SELECT payload::text FROM events WHERE id = ?::uuid", String.class, splitEventId));
        assertThat(splitPayload.path("source_id").asText()).isEqualTo(subjectY.toString());
        JsonNode successorIdsArr = splitPayload.path("successor_ids");
        assertThat(successorIdsArr.isArray()).isTrue();
        assertThat(successorIdsArr.size())
                .describedAs("§S7 wrong-merge correction: minItems=2; arity-edited schema enforced")
                .isEqualTo(2);
        String splitShape = jdbc.queryForObject(
                "SELECT shape_ref FROM events WHERE id = ?::uuid", String.class, splitEventId);
        assertThat(splitShape).isEqualTo("subject_split/v1");

        // Assert source transitions to archived (proxy: another merge/split against S_Y returns 4xx).
        HttpResponse<String> remerge = postAsCoordinator(b.coordinatorToken, "/admin/subjects/merge",
                "{\"surviving_id\":\"" + UUID.randomUUID() + "\",\"retired_id\":\"" + subjectY + "\"}");
        assertThat(remerge.statusCode)
                .describedAs("§S8: archived source is terminal — re-merge rejected pre-write")
                .isBetween(400, 499);

        // Empty-successor invariant: original captures remain attributed to original subject_ids
        // (NOT to either successor). The original Khan capture for S_X stays attributed to S_X;
        // for S_Y, to S_Y (now archived). Neither yPrime nor xPrime has any payload-derived state.
        Integer xCaptures = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE shape_ref = 'household_observation/v1' AND subject_id = ?",
                Integer.class, subjectX);
        Integer yCaptures = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE shape_ref = 'household_observation/v1' AND subject_id = ?",
                Integer.class, subjectY);
        Integer yPrimeCaptures = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE shape_ref = 'household_observation/v1' AND subject_id = ?",
                Integer.class, yPrime);
        Integer xPrimeCaptures = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE shape_ref = 'household_observation/v1' AND subject_id = ?",
                Integer.class, xPrime);
        assertThat(xCaptures).describedAs("S_X capture stays at S_X").isEqualTo(1);
        assertThat(yCaptures).describedAs("S_Y capture stays at archived S_Y, not rewritten").isEqualTo(1);
        assertThat(yPrimeCaptures).describedAs("§S8 successors are bare identities").isEqualTo(0);
        assertThat(xPrimeCaptures).describedAs("§S8 successors are bare identities").isEqualTo(0);

        // Envelope discipline on the split event mirrors merge (§6 commitment 7).
        var splitRow = jdbc.queryForMap(
                "SELECT type, actor_id, device_id FROM events WHERE id = ?::uuid", splitEventId);
        assertThat(splitRow.get("type")).isEqualTo("capture");
        assertThat(splitRow.get("actor_id"))
                .describedAs("F-B4: split is human-authored — coordinator UUID, not system:")
                .isEqualTo(b.coordinatorActor.toString());
        assertThat(splitRow.get("device_id").toString())
                .isEqualTo("00000000-0000-0000-0000-000000000001");
    }

    /**
     * W-5 — lineage DAG enforcement (§S9). Operands must be active; archived is terminal.
     *
     * <p>Distinguishes pre-write structural rejection from §S14 accept-and-flag: NO event is
     * written for these rejections. {@code conflict_detected/v1} is for capture events with
     * problematic state, not for coordinator-action-as-bad-request.
     */
    @Test
    void walkthrough_W5_lineage_dag_enforcement() throws Exception {
        Bootstrap b = bootstrap();
        UUID deviceA = UUID.randomUUID();
        UUID subjectX = UUID.randomUUID();
        UUID subjectY = UUID.randomUUID();
        UUID xPrime = UUID.randomUUID();
        UUID yPrime = UUID.randomUUID();

        // Setup: get to a state where S_X is archived (via merge) and S_Y is archived (via split).
        push(pushBody(buildHouseholdCapture(
                subjectX, b.chvAActor, deviceA, 1, b.villageA, "Khan household", 5)));
        push(pushBody(buildHouseholdCapture(
                subjectY, b.chvAActor, deviceA, 2, b.villageA, "Khan household", 6)));
        // Merge S_X (retired, becomes archived) into S_Y (surviving, still active).
        HttpResponse<String> merge = postAsCoordinator(b.coordinatorToken, "/admin/subjects/merge",
                "{\"surviving_id\":\"" + subjectY + "\",\"retired_id\":\"" + subjectX + "\"}");
        assertThat(merge.statusCode).isEqualTo(200);
        // Split S_Y (becomes archived) into [yPrime, xPrime].
        HttpResponse<String> split = postAsCoordinator(b.coordinatorToken, "/admin/subjects/split",
                "{\"source_id\":\"" + subjectY + "\",\"successor_ids\":[\"" + yPrime + "\",\"" + xPrime + "\"]}");
        assertThat(split.statusCode).isEqualTo(200);

        int eventsBeforeRejections = countAllEvents();
        int conflictDetectedBefore = countByShape("conflict_detected/v1");
        int subjectsMergedBefore = countByShape("subjects_merged/v1");
        int subjectSplitBefore = countByShape("subject_split/v1");

        // Attempt to merge with archived S_Y as surviving operand.
        HttpResponse<String> r1 = postAsCoordinator(b.coordinatorToken, "/admin/subjects/merge",
                "{\"surviving_id\":\"" + subjectY + "\",\"retired_id\":\"" + UUID.randomUUID() + "\"}");
        assertThat(r1.statusCode).describedAs("§S9: surviving must be active").isBetween(400, 499);

        // Attempt to merge with archived S_Y as retired operand.
        HttpResponse<String> r2 = postAsCoordinator(b.coordinatorToken, "/admin/subjects/merge",
                "{\"surviving_id\":\"" + UUID.randomUUID() + "\",\"retired_id\":\"" + subjectY + "\"}");
        assertThat(r2.statusCode).describedAs("§S9: retired must be active").isBetween(400, 499);

        // Attempt to merge with archived S_X (archived by W-3-style merge).
        HttpResponse<String> r3 = postAsCoordinator(b.coordinatorToken, "/admin/subjects/merge",
                "{\"surviving_id\":\"" + subjectX + "\",\"retired_id\":\"" + UUID.randomUUID() + "\"}");
        assertThat(r3.statusCode).describedAs("§S9: archived (via prior merge) is terminal").isBetween(400, 499);

        // Attempt to split the already-archived S_Y again.
        HttpResponse<String> r4 = postAsCoordinator(b.coordinatorToken, "/admin/subjects/split",
                "{\"source_id\":\"" + subjectY + "\",\"successor_ids\":[\"" + UUID.randomUUID() + "\",\"" + UUID.randomUUID() + "\"]}");
        assertThat(r4.statusCode).describedAs("§S9: cannot split an archived source").isBetween(400, 499);

        // Distinguish from accept-and-flag: zero new events, zero new conflict_detected/v1, zero
        // new subjects_merged/v1, zero new subject_split/v1 across all four rejections.
        assertThat(countAllEvents())
                .describedAs("structural 4xx is pre-write — NOT §S14 accept-and-flag for capture events")
                .isEqualTo(eventsBeforeRejections);
        assertThat(countByShape("conflict_detected/v1"))
                .describedAs("no flag emitted for coordinator-action rejection")
                .isEqualTo(conflictDetectedBefore);
        assertThat(countByShape("subjects_merged/v1")).isEqualTo(subjectsMergedBefore);
        assertThat(countByShape("subject_split/v1")).isEqualTo(subjectSplitBefore);
    }

    // =========================================================================== helpers
    private record Bootstrap(UUID villageA, UUID villageB,
                             UUID chvAActor, String chvAToken,
                             UUID chvBActor, String chvBToken,
                             UUID coordinatorActor, String coordinatorToken) {}

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
                body.path("chv_b_token").asText(),
                UUID.fromString(body.path("coordinator_actor_id").asText()),
                body.path("coordinator_token").asText());
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

    private HttpResponse<String> postAsCoordinator(String coordinatorToken, String path, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + coordinatorToken);
        try {
            ResponseEntity<String> resp = http.exchange(url(path), HttpMethod.POST,
                    new HttpEntity<>(jsonBody, headers), String.class);
            return new HttpResponse<>(resp.getStatusCode().value(), resp.getBody());
        } catch (HttpClientErrorException ex) {
            return new HttpResponse<>(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    private JsonNode getJson(String coordinatorToken, String path) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + coordinatorToken);
        ResponseEntity<String> resp = http.exchange(url(path), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
        return mapper.readTree(resp.getBody());
    }

    private int countAllEvents() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM events", Integer.class);
        return n == null ? 0 : n;
    }

    private int countByShape(String shapeRef) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE shape_ref = ?", Integer.class, shapeRef);
        return n == null ? 0 : n;
    }

    private record HttpResponse<T>(int statusCode, T body) {}
}
