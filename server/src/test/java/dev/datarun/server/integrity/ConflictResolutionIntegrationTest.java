package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.config.FlagSeverityConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 1c quality gate tests — Conflict Resolution (server-side slice).
 * Key quality gate: QG3 — Admin resolves flag → conflict_resolved event created
 * → flagged event now included in state → projection re-derived.
 */
class ConflictResolutionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FlagSeverityConfigService flagSeverityConfigService;

    private static final UUID DEVICE_A = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID DEVICE_B = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID ACTOR_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID WORKER_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000101");
    private static final UUID OTHER_ACTOR_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000202");
    private static final String OTHER_TOKEN = "other_actor_token_000000000000000000000000000000000000";
    private static final UUID SUBJECT_X = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
    private static final UUID SUBJECT_Y = UUID.fromString("8d0f7780-8536-41ef-a55c-f18ad2a01bf8");

    @BeforeEach
    void cleanDb() {
        jdbc.execute("DELETE FROM actor_tokens");
        jdbc.execute("DELETE FROM deployment_config");
        jdbc.execute("DELETE FROM subject_locations");
        jdbc.execute("DELETE FROM events");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbc.execute("DELETE FROM device_sync_state");
        jdbc.execute("DELETE FROM subject_aliases");
        provisionTestToken();
        provisionToken(OTHER_TOKEN, OTHER_ACTOR_ID);
    }

    // --- QG3: Resolve flag (accepted) → event re-included in projection ---

    @Test
    void resolveAccepted_eventReincludedInProjection() {
        // Create a concurrent scenario → flag raised
        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEvents(List.of(eventA), DEVICE_A, 0);
        var eventB = buildEvent(SUBJECT_X, DEVICE_B, 1);
        pushEvents(List.of(eventB), DEVICE_B, 0);

        // Find the flagged event and the flag
        UUID flagId = findFlagEventId();
        assertThat(flagId).isNotNull();

        // Before resolution: projection should exclude flagged event
        int countBefore = getProjectedEventCount(SUBJECT_X);

        // Resolve with "accepted"
        var resolveResponse = resolveFlag(flagId, "accepted", null);
        assertThat(resolveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resolveResponse.getBody().get("resolution").asText()).isEqualTo("accepted");

        // After resolution: projection should now include the previously-flagged event
        int countAfter = getProjectedEventCount(SUBJECT_X);
        assertThat(countAfter).isEqualTo(countBefore + 1);
    }

    // --- Resolve flag (rejected) → event stays excluded ---

    @Test
    void resolveRejected_eventStaysExcluded() {
        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEvents(List.of(eventA), DEVICE_A, 0);
        var eventB = buildEvent(SUBJECT_X, DEVICE_B, 1);
        pushEvents(List.of(eventB), DEVICE_B, 0);

        UUID flagId = findFlagEventId();
        int countBefore = getProjectedEventCount(SUBJECT_X);

        // Resolve with "rejected" — flagged event stays permanently excluded
        var resolveResponse = resolveFlag(flagId, "rejected", null);
        assertThat(resolveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Projection should NOT re-include the rejected event
        int countAfter = getProjectedEventCount(SUBJECT_X);
        assertThat(countAfter).isEqualTo(countBefore);
    }

    // --- Resolve flag (reclassified) → event re-attributed to different subject ---

    @Test
    void resolveReclassified_eventReattributed() {
        // Push event for Subject X from Device A
        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEvents(List.of(eventA), DEVICE_A, 0);

        // Create manual identity conflict flag
        UUID sourceEventId = findDomainEventId(SUBJECT_X);
        var flagResponse = createIdentityConflict(sourceEventId);
        assertThat(flagResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UUID flagId = UUID.fromString(flagResponse.getBody().get("event_id").asText());

        // Resolve with "reclassified" to Subject Y
        var resolveResponse = resolveFlag(flagId, "reclassified", SUBJECT_Y);
        assertThat(resolveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resolveResponse.getBody().get("resolution").asText()).isEqualTo("reclassified");

        // Verify conflict_resolved event payload contains reclassified_subject_id
        var pullResponse = pullEvents(0, 100);
        JsonNode resolvedEvent = null;
        for (JsonNode e : pullResponse.getBody().get("events")) {
            if ("conflict_resolved/v1".equals(e.get("shape_ref").asText())) {
                resolvedEvent = e;
                break;
            }
        }
        assertThat(resolvedEvent).isNotNull();
        assertThat(resolvedEvent.get("payload").get("reclassified_subject_id").asText())
                .isEqualTo(SUBJECT_Y.toString());
    }

    // --- conflict_resolved event has valid 11-field envelope ---

    @Test
    void conflictResolvedEvent_validEnvelope() {
        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEvents(List.of(eventA), DEVICE_A, 0);
        var eventB = buildEvent(SUBJECT_X, DEVICE_B, 1);
        pushEvents(List.of(eventB), DEVICE_B, 0);

        UUID flagId = findFlagEventId();
        resolveFlag(flagId, "accepted", null);

        // Pull all events — find the conflict_resolved event
        var pullResponse = pullEvents(0, 100);
        JsonNode resolvedEvent = null;
        for (JsonNode e : pullResponse.getBody().get("events")) {
            if ("conflict_resolved/v1".equals(e.get("shape_ref").asText())) {
                resolvedEvent = e;
                break;
            }
        }
        assertThat(resolvedEvent).isNotNull();

        // Verify envelope per ADR-007 S2: manual resolution is type=review.
        assertThat(resolvedEvent.get("type").asText()).isEqualTo("review");
        assertThat(resolvedEvent.get("shape_ref").asText()).isEqualTo("conflict_resolved/v1");
        assertThat(resolvedEvent.get("subject_ref").get("type").asText()).isEqualTo("subject");
        assertThat(resolvedEvent.get("subject_ref").get("id").asText()).isEqualTo(SUBJECT_X.toString());
        assertThat(resolvedEvent.get("actor_ref").get("type").asText()).isEqualTo("actor");
        assertThat(resolvedEvent.get("actor_ref").get("id").asText()).isEqualTo(ACTOR_ID.toString());
        assertThat(resolvedEvent.get("device_id")).isNotNull();
        assertThat(resolvedEvent.get("device_seq")).isNotNull();
        assertThat(resolvedEvent.get("sync_watermark").asLong()).isGreaterThan(0);
        assertThat(resolvedEvent.get("timestamp")).isNotNull();

        // Verify payload
        JsonNode payload = resolvedEvent.get("payload");
        assertThat(payload.get("flag_event_id").asText()).isEqualTo(flagId.toString());
        assertThat(payload.get("source_event_id").asText()).isNotEmpty();
        assertThat(payload.get("resolution").asText()).isEqualTo("accepted");
    }

    // --- Double-resolve → 400 (idempotency guard) ---

    @Test
    void doubleResolve_returns400() {
        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEvents(List.of(eventA), DEVICE_A, 0);
        var eventB = buildEvent(SUBJECT_X, DEVICE_B, 1);
        pushEvents(List.of(eventB), DEVICE_B, 0);

        UUID flagId = findFlagEventId();
        resolveFlag(flagId, "accepted", null);

        // Second resolve → should fail
        var response = resolveFlag(flagId, "rejected", null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error").asText()).isEqualTo("precondition_failed");
    }

    // --- Resolve non-existent flag → 400 ---

    @Test
    void resolveNonExistentFlag_returns400() {
        UUID fakeId = UUID.randomUUID();
        var response = resolveFlag(fakeId, "accepted", null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // --- List unresolved flags ---

    @Test
    void listFlags_showsUnresolved_hidesResolved() {
        // Create a concurrent scenario → flag raised
        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEvents(List.of(eventA), DEVICE_A, 0);
        var eventB = buildEvent(SUBJECT_X, DEVICE_B, 1);
        pushEvents(List.of(eventB), DEVICE_B, 0);

        // List flags — should have 1 unresolved
        var listResponse = listConflicts(TEST_TOKEN);
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody().get("flags").size()).isEqualTo(1);

        // Resolve it
        UUID flagId = findFlagEventId();
        resolveFlag(flagId, "accepted", null);

        // List again — should be empty
        var listAfter = listConflicts(TEST_TOKEN);
        assertThat(listAfter.getBody().get("flags").size()).isEqualTo(0);
    }

    @Test
    void listFlags_includesEffectiveSeverity_withoutChangingResolvability() throws Exception {
        JsonNode overrides = objectMapper.readTree("""
                {"concurrent_state_change": "informational"}
                """);
        assertThat(flagSeverityConfigService.updateOverrides(overrides, null)).isEmpty();

        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEvents(List.of(eventA), DEVICE_A, 0);
        var eventB = buildEvent(SUBJECT_X, DEVICE_B, 1);
        pushEvents(List.of(eventB), DEVICE_B, 0);

        var listResponse = listConflicts(TEST_TOKEN);
        JsonNode flag = listResponse.getBody().get("flags").get(0);
        assertThat(flag.get("flag_category").asText()).isEqualTo("concurrent_state_change");
        assertThat(flag.get("severity").asText()).isEqualTo("informational");
        assertThat(getProjectedEventCount(SUBJECT_X)).isEqualTo(1);

        String resolvability = jdbc.queryForObject("""
                SELECT payload->>'resolvability'
                FROM events
                WHERE shape_ref = 'conflict_detected/v1'
                  AND payload->>'flag_category' = 'concurrent_state_change'
                LIMIT 1
                """, String.class);
        assertThat(resolvability).isEqualTo("manual_only");
    }

    // --- Manual identity_conflict flag creation ---

    @Test
    void createManualIdentityConflict_flagCreated() {
        // Push a domain event
        var event = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEvents(List.of(event), DEVICE_A, 0);

        UUID sourceEventId = findDomainEventId(SUBJECT_X);

        // Create manual identity_conflict
        var response = createIdentityConflict(sourceEventId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("flag_category").asText()).isEqualTo("identity_conflict");

        // Flag should appear in list
        var listResponse = listConflicts(TEST_TOKEN);
        JsonNode flags = listResponse.getBody().get("flags");
        assertThat(flags.size()).isEqualTo(1);
        assertThat(flags.get(0).get("flag_category").asText()).isEqualTo("identity_conflict");

        // Event should be excluded from projection
        int projected = getProjectedEventCount(SUBJECT_X);
        assertThat(projected).isEqualTo(0);
    }

    // --- Duplicate manual identity_conflict → 400 ---

    @Test
    void duplicateManualIdentityConflict_returns400() {
        var event = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEvents(List.of(event), DEVICE_A, 0);
        UUID sourceEventId = findDomainEventId(SUBJECT_X);

        createIdentityConflict(sourceEventId);

        // Second attempt → should fail (deterministic ID collision)
        var response = createIdentityConflict(sourceEventId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void conflictApi_requiresBearerToken() throws Exception {
        var listMissing = rest.getForEntity("/api/conflicts", JsonNode.class);
        assertThat(listMissing.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        HttpHeaders invalid = new HttpHeaders();
        invalid.setBearerAuth("not-a-valid-token");
        var listInvalid = rest.exchange(
                "/api/conflicts", HttpMethod.GET, new HttpEntity<>(invalid), JsonNode.class);
        assertThat(listInvalid.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(postWithoutTokenStatus("/api/conflicts/" + UUID.randomUUID() + "/resolve"))
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(postWithoutTokenStatus("/api/conflicts/identity"))
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void unauthorizedResolution_persistsAuditDoesNotClearAndRaisesScopeViolation() {
        pushEvents(List.of(buildEvent(SUBJECT_X, DEVICE_A, 1)), DEVICE_A, 0);
        pushEvents(List.of(buildEvent(SUBJECT_X, DEVICE_B, 1)), DEVICE_B, 0);

        UUID flagId = findFlagEventId();
        int countBefore = getProjectedEventCount(SUBJECT_X);

        var response = resolveFlag(
                flagId, "accepted", null, OTHER_TOKEN, ACTOR_ID);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UUID resolutionEventId = UUID.fromString(response.getBody().get("event_id").asText());

        String resolutionActor = jdbc.queryForObject("""
                SELECT actor_ref->>'id'
                FROM events
                WHERE id = ?::uuid
                """, String.class, resolutionEventId.toString());
        assertThat(resolutionActor).isEqualTo(OTHER_ACTOR_ID.toString());
        assertThat(getProjectedEventCount(SUBJECT_X)).isEqualTo(countBefore);
        assertThat(canonicalResolutionCount(flagId)).isZero();

        Map<String, Object> unauthorizedFlag = jdbc.queryForMap("""
                SELECT payload
                FROM events
                WHERE shape_ref = 'conflict_detected/v1'
                  AND payload->>'flag_category' = 'scope_violation'
                  AND payload->>'source_event_id' = ?
                """, resolutionEventId.toString());
        String payload = unauthorizedFlag.get("payload").toString();
        assertThat(payload).contains("\"designated_resolver\": {\"id\": \"" + ACTOR_ID + "\"");

        var adminQueue = listConflicts(TEST_TOKEN);
        assertThat(adminQueue.getBody().get("flags").size()).isEqualTo(2);
    }

    @Test
    void bodySpoofedActorId_doesNotGrantCanonicalResolution() {
        pushEvents(List.of(buildEvent(SUBJECT_X, DEVICE_A, 1)), DEVICE_A, 0);
        pushEvents(List.of(buildEvent(SUBJECT_X, DEVICE_B, 1)), DEVICE_B, 0);

        UUID flagId = findFlagEventId();
        int countBefore = getProjectedEventCount(SUBJECT_X);

        var response = resolveFlag(flagId, "accepted", null, OTHER_TOKEN, ACTOR_ID);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(canonicalResolutionCount(flagId)).isZero();
        assertThat(getProjectedEventCount(SUBJECT_X)).isEqualTo(countBefore);
    }

    @Test
    void legacyFlagWithoutDesignatedResolver_isNotCanonicalResolved() {
        var event = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEvents(List.of(event), DEVICE_A, 0);
        UUID sourceEventId = UUID.fromString(event.get("id").toString());
        UUID flagId = insertLegacyFlagWithoutResolver(sourceEventId, SUBJECT_X);

        assertThat(getProjectedEventCount(SUBJECT_X)).isZero();

        var response = resolveFlag(flagId, "accepted", null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(canonicalResolutionCount(flagId)).isZero();
        assertThat(getProjectedEventCount(SUBJECT_X)).isZero();
    }

    // --- Helpers ---

    private Map<String, Object> buildEvent(UUID subjectId, UUID deviceId, int seq) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "capture");
        event.put("shape_ref", "basic_capture/v1");
        event.put("activity_ref", null);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", WORKER_ID.toString()));
        event.put("device_id", deviceId.toString());
        event.put("device_seq", seq);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", Map.of(
                "name", "Site " + seq,
                "category", "urban",
                "notes", "Test event " + seq,
                "date", "2026-04-18",
                "value", seq * 10
        ));
        return event;
    }

    private ResponseEntity<JsonNode> pushEvents(List<?> events, UUID deviceId, long lastPullWatermark) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("events", events);
        request.put("device_id", deviceId.toString());
        request.put("last_pull_watermark", lastPullWatermark);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return rest.exchange("/api/sync/push", HttpMethod.POST, entity, JsonNode.class);
    }

    private ResponseEntity<JsonNode> pullEvents(long sinceWatermark, int limit) {
        Map<String, Object> request = Map.of("since_watermark", sinceWatermark, "limit", limit);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, authHeaders());
        return rest.exchange("/api/sync/pull", HttpMethod.POST, entity, JsonNode.class);
    }

    private ResponseEntity<JsonNode> resolveFlag(UUID flagId, String resolution, UUID reclassifiedSubjectId) {
        return resolveFlag(flagId, resolution, reclassifiedSubjectId, TEST_TOKEN, ACTOR_ID);
    }

    private ResponseEntity<JsonNode> resolveFlag(UUID flagId, String resolution,
                                                 UUID reclassifiedSubjectId,
                                                 String bearerToken,
                                                 UUID bodyActorId) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("resolution", resolution);
        request.put("actor_id", bodyActorId.toString());
        if (reclassifiedSubjectId != null) {
            request.put("reclassified_subject_id", reclassifiedSubjectId.toString());
        }
        request.put("reason", "Test resolution: " + resolution);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return rest.exchange("/api/conflicts/" + flagId + "/resolve",
                HttpMethod.POST, entity, JsonNode.class);
    }

    private ResponseEntity<JsonNode> createIdentityConflict(UUID sourceEventId) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("source_event_id", sourceEventId.toString());
        request.put("actor_id", ACTOR_ID.toString());
        request.put("reason", "Suspected wrong subject attribution");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TEST_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return rest.exchange("/api/conflicts/identity", HttpMethod.POST, entity, JsonNode.class);
    }

    private ResponseEntity<JsonNode> listConflicts(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        return rest.exchange("/api/conflicts", HttpMethod.GET,
                new HttpEntity<>(headers), JsonNode.class);
    }

    private int postWithoutTokenStatus(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(rest.getRootUri() + path))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.discarding())
                .statusCode();
    }

    private UUID findFlagEventId() {
        return jdbc.queryForObject(
                "SELECT id FROM events WHERE shape_ref LIKE 'conflict_detected/%' ORDER BY sync_watermark LIMIT 1",
                UUID.class);
    }

    private UUID findDomainEventId(UUID subjectId) {
        return jdbc.queryForObject("""
                SELECT id FROM events
                WHERE type = 'capture'
                  AND subject_ref->>'id' = ?
                ORDER BY sync_watermark LIMIT 1
                """,
                UUID.class, subjectId.toString());
    }

    private int getProjectedEventCount(UUID subjectId) {
        var response = rest.getForEntity("/api/subjects", JsonNode.class);
        JsonNode subjects = response.getBody().get("subjects");
        for (JsonNode s : subjects) {
            if (subjectId.toString().equals(s.get("id").asText())) {
                return s.get("event_count").asInt();
            }
        }
        return 0;
    }

    private void provisionToken(String token, UUID actorId) {
        jdbc.update("""
                INSERT INTO actor_tokens (token, actor_id, revoked)
                VALUES (?, ?::uuid, FALSE)
                ON CONFLICT (token) DO NOTHING
                """, token, actorId.toString());
    }

    private int canonicalResolutionCount(UUID flagId) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM events cr
                JOIN events cd ON cd.id::text = cr.payload->>'flag_event_id'
                WHERE cr.shape_ref = 'conflict_resolved/v1'
                  AND cr.payload->>'flag_event_id' = ?
                  AND cr.actor_ref->>'type' = cd.payload->'designated_resolver'->>'type'
                  AND cr.actor_ref->>'id' = cd.payload->'designated_resolver'->>'id'
                """, Integer.class, flagId.toString());
        return count != null ? count : 0;
    }

    private UUID insertLegacyFlagWithoutResolver(UUID sourceEventId, UUID subjectId) {
        UUID flagId = UUID.randomUUID();
        UUID serverDeviceId = jdbc.queryForObject(
                "SELECT device_id FROM server_identity LIMIT 1", UUID.class);
        long seq = jdbc.queryForObject("SELECT nextval('server_device_seq')", Long.class);
        jdbc.update("""
                INSERT INTO events (id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                                    device_id, device_seq, timestamp, payload)
                VALUES (?::uuid, 'alert', 'conflict_detected/v1', NULL,
                        ?::jsonb, ?::jsonb, ?::uuid, ?, NOW()::timestamptz, ?::jsonb)
                """,
                flagId.toString(),
                "{\"type\":\"subject\",\"id\":\"" + subjectId + "\"}",
                "{\"type\":\"actor\",\"id\":\"system:conflict_detector/concurrent_state_change\"}",
                serverDeviceId.toString(),
                seq,
                "{\"source_event_id\":\"" + sourceEventId + "\"," +
                        "\"flag_category\":\"concurrent_state_change\"," +
                        "\"resolvability\":\"manual_only\"," +
                        "\"reason\":\"Legacy flag without resolver\"}");
        return flagId;
    }
}
