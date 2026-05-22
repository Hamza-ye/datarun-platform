package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.authorization.AssignmentService;
import dev.datarun.server.authorization.ActorTokenRepository;
import dev.datarun.server.authorization.LocationRepository;
import dev.datarun.server.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 2c quality gate tests — Authorization Flags + Sync Hardening.
 *
 * Tests scope_violation, temporal_authority_expired, and role_stale flag detection.
 * Auth-flagged events must be excluded from state derivation.
 */
class AuthFlagIntegrationTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate rest;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private AssignmentService assignmentService;
    @Autowired private LocationRepository locationRepository;
    @Autowired private ActorTokenRepository actorTokenRepository;
    @Autowired private ObjectMapper objectMapper;

    private static final UUID ADMIN = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID WORKER = UUID.fromString("aaaa0000-0000-0000-0000-000000003001");
    private static final UUID DEVICE_W = UUID.fromString("dddd0000-0000-0000-0000-000000003001");

    private UUID region;
    private UUID districtX;
    private UUID districtY;
    private UUID villageX1;
    private UUID villageY1;
    private String workerToken;
    private int seqCounter = 1;

    @BeforeEach
    void setup() {
        jdbc.execute("DELETE FROM actor_tokens");
        jdbc.execute("DELETE FROM subject_locations");
        jdbc.execute("DELETE FROM events");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbc.execute("DELETE FROM device_sync_state");
        jdbc.execute("DELETE FROM activities");
        jdbc.execute("DELETE FROM locations");
        provisionTestToken();

        region = UUID.randomUUID();
        districtX = UUID.randomUUID();
        districtY = UUID.randomUUID();
        villageX1 = UUID.randomUUID();
        villageY1 = UUID.randomUUID();
        locationRepository.insert(region, "Region", null, "region");
        locationRepository.insert(districtX, "District X", region, "district");
        locationRepository.insert(districtY, "District Y", region, "district");
        locationRepository.insert(villageX1, "Village X1", districtX, "village");
        locationRepository.insert(villageY1, "Village Y1", districtY, "village");

        workerToken = actorTokenRepository.createToken(WORKER);
        seqCounter = 1;
    }

    /**
     * QG: Actor captures event for out-of-scope subject → pushes → scope_violation flag raised.
     */
    @Test
    void outOfScopeEvent_raiseScopeViolationFlag() {
        // Assign worker to Village X1 only
        assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        // Worker captures subject in Village Y1 (out of scope)
        UUID outOfScopeSubject = UUID.randomUUID();
        registerSubjectLocation(outOfScopeSubject, villageY1);
        pushCaptureEvent(outOfScopeSubject, WORKER, DEVICE_W, "Out of scope capture");

        // Verify scope_violation flag raised
        List<Map<String, Object>> flags = jdbc.queryForList(
                "SELECT payload FROM events WHERE shape_ref LIKE 'conflict_detected/%'");
        assertThat(flags).hasSize(1);
        String payload = flags.get(0).get("payload").toString();
        assertThat(payload).contains("scope_violation");
    }

    /**
     * QG: Actor's assignment ends while offline → continues → pushes →
     * temporal_authority_expired flags on events created after assignment end.
     */
    @Test
    void assignmentEndsOffline_temporalAuthorityExpiredFlagged() {
        // Create assignment, then end it
        Event created = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(2), null);
        UUID assignmentId = UUID.fromString(created.subjectRef().get("id").asText());
        assignmentService.endAssignment(assignmentId, ADMIN, "ended for test");

        // Worker was offline and captures an event for in-scope subject
        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, villageX1);
        pushCaptureEvent(subject, WORKER, DEVICE_W, "Captured after assignment ended");

        // Verify temporal_authority_expired flag raised
        List<Map<String, Object>> flags = jdbc.queryForList(
                "SELECT payload FROM events WHERE shape_ref LIKE 'conflict_detected/%'");
        assertThat(flags).hasSize(1);
        String payload = flags.get(0).get("payload").toString();
        assertThat(payload).contains("temporal_authority_expired");
    }

    /**
     * FP-006 gate: an ended assignment must not create a temporal flag after the
     * actor has synced a replacement covering assignment.
     */
    @Test
    void replacementAssignmentSynced_noTemporalAuthorityExpiredFromEndedAssignment() {
        Event assignmentA = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(2), null);
        UUID assignmentAId = UUID.fromString(assignmentA.subjectRef().get("id").asText());

        ResponseEntity<JsonNode> firstPull = pullEvents(workerToken, 0, 100);
        long syncedUnderA = firstPull.getBody().get("latest_watermark").asLong();
        assertThat(syncedUnderA).isGreaterThanOrEqualTo(syncWatermark(assignmentA.id()));

        assignmentService.endAssignment(assignmentAId, ADMIN, "replacement");
        Event assignmentB = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        ResponseEntity<JsonNode> replacementPull = pullEvents(workerToken, syncedUnderA, 100);
        long syncedPastB = replacementPull.getBody().get("latest_watermark").asLong();
        assertThat(syncedPastB).isGreaterThanOrEqualTo(syncWatermark(assignmentB.id()));

        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, villageX1);
        UUID pushedEvent = pushCaptureEvent(subject, WORKER, DEVICE_W,
                "Captured after replacement sync", syncedPastB);

        List<String> temporalSources = flagSources("temporal_authority_expired");
        assertThat(temporalSources).doesNotContain(pushedEvent.toString());
        assertThat(temporalSources).isEmpty();
    }

    /**
     * FP-006 gate: the real stale temporal case remains flagged when the actor
     * synced under A, A ended, and the actor did not sync the ending authority.
     */
    @Test
    void assignmentEndsAfterActorSync_withoutResync_temporalAuthorityExpiredFlagged() {
        Event assignmentA = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(2), null);
        UUID assignmentAId = UUID.fromString(assignmentA.subjectRef().get("id").asText());

        ResponseEntity<JsonNode> firstPull = pullEvents(workerToken, 0, 100);
        long syncedUnderA = firstPull.getBody().get("latest_watermark").asLong();
        assertThat(syncedUnderA).isGreaterThanOrEqualTo(syncWatermark(assignmentA.id()));

        assignmentService.endAssignment(assignmentAId, ADMIN, "ended while worker offline");

        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, villageX1);
        UUID pushedEvent = pushCaptureEvent(subject, WORKER, DEVICE_W,
                "Captured before syncing assignment end", syncedUnderA);

        assertThat(flagSources("temporal_authority_expired"))
                .containsExactly(pushedEvent.toString());
    }

    /**
     * QG: temporal_authority_expired flag carries auto_eligible resolvability.
     */
    @Test
    void temporalAuthorityExpired_carriesAutoEligibleResolvability() {
        Event created = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(2), null);
        UUID assignmentId = UUID.fromString(created.subjectRef().get("id").asText());
        assignmentService.endAssignment(assignmentId, ADMIN, "ended for test");

        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, villageX1);
        pushCaptureEvent(subject, WORKER, DEVICE_W, "Auto eligible check");

        List<Map<String, Object>> flags = jdbc.queryForList(
                "SELECT payload FROM events WHERE shape_ref LIKE 'conflict_detected/%'");
        assertThat(flags).isNotEmpty();
        String payload = flags.get(0).get("payload").toString();
        assertThat(payload).contains("auto_eligible");
    }

    /**
     * QG: Auth-flagged events are excluded from state derivation (SubjectProjection).
     */
    @Test
    void authFlaggedEvents_excludedFromStateDerivation() {
        assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        // Push in-scope event (should be clean)
        UUID inScopeSubject = UUID.randomUUID();
        registerSubjectLocation(inScopeSubject, villageX1);
        pushCaptureEvent(inScopeSubject, WORKER, DEVICE_W, "In scope");

        // Push out-of-scope event (should get scope_violation flag)
        UUID outOfScopeSubject = UUID.randomUUID();
        registerSubjectLocation(outOfScopeSubject, villageY1);
        pushCaptureEvent(outOfScopeSubject, WORKER, DEVICE_W, "Out of scope");

        // Verify out-of-scope subject has no state (flagged event excluded from projection)
        ResponseEntity<JsonNode> outResponse = rest.getForEntity(
                "/api/subjects/{id}", JsonNode.class, outOfScopeSubject.toString());
        // Should be 404 or empty state — flagged event excluded
        assertThat(outResponse.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.OK);
        if (outResponse.getStatusCode() == HttpStatus.OK) {
            // If 200, verify the timeline is empty (flagged events excluded)
            assertThat(outResponse.getBody().path("timeline").size()).isEqualTo(0);
        }
    }

    /**
     * QG: temporal_authority_expired checked BEFORE scope_violation — prevents
     * mis-classifying expired actors as scope violators.
     */
    @Test
    void temporalCheckedBeforeScope_noMisclassification() {
        // Create assignment for Village X1, then end it
        Event created = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(2), null);
        UUID assignmentId = UUID.fromString(created.subjectRef().get("id").asText());
        assignmentService.endAssignment(assignmentId, ADMIN, "ended for test");

        // Worker captures in Village X1 (was in scope before assignment ended)
        // This should be temporal_authority_expired, NOT scope_violation
        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, villageX1);
        pushCaptureEvent(subject, WORKER, DEVICE_W, "After assignment ended");

        List<Map<String, Object>> flags = jdbc.queryForList(
                "SELECT payload FROM events WHERE shape_ref LIKE 'conflict_detected/%'");
        assertThat(flags).hasSize(1);
        String payload = flags.get(0).get("payload").toString();
        assertThat(payload).contains("temporal_authority_expired");
        assertThat(payload).doesNotContain("scope_violation");
    }

    /**
     * Phase 4: field workers with capture permission can push capture cleanly.
     */
    @Test
    void roleAction_fieldWorkerCapturePermitted_noRoleStale() {
        configureVaccinationRoles(Map.of(
                "field_worker", List.of("capture"),
                "supervisor", List.of("review")));
        Event assignment = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        long knowledge = syncWatermark(assignment.id());

        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, villageX1);
        UUID event = pushCaptureEvent(subject, WORKER, DEVICE_W, "Allowed capture", knowledge);

        assertThat(flagSources("role_stale")).doesNotContain(event.toString());
        assertThat(flagSources("role_stale")).isEmpty();
        Integer flagCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE shape_ref = 'conflict_detected/v1'",
                Integer.class);
        assertThat(flagCount).isZero();
    }

    /**
     * Phase 4: field workers lacking review can still push review, but it is
     * accepted and flagged as role_stale.
     */
    @Test
    void roleAction_fieldWorkerReviewAcceptedAndRoleStale() {
        configureVaccinationRoles(Map.of(
                "field_worker", List.of("capture"),
                "supervisor", List.of("review")));
        Event assignment = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        long knowledge = syncWatermark(assignment.id());

        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, villageX1);
        UUID event = pushReviewEvent(subject, WORKER, DEVICE_W, "Unauthorized review", knowledge);

        assertThat(flagSources("role_stale")).containsExactly(event.toString());
    }

    /**
     * Phase 4: role label changes are not role_stale when both horizon and
     * current authority permit the attempted action.
     */
    @Test
    void roleAction_roleLabelChangeBothPermitAction_noRoleStale() {
        configureVaccinationRoles(Map.of(
                "field_worker", List.of("capture"),
                "supervisor", List.of("capture", "review")));
        Event oldAssignment = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(3), null);
        UUID oldAssignmentId = UUID.fromString(oldAssignment.subjectRef().get("id").asText());
        long knowledgeBeforeRoleChange = syncWatermark(oldAssignment.id());

        assignmentService.endAssignment(oldAssignmentId, ADMIN, "role change");
        assignmentService.createAssignment(ADMIN, WORKER, "supervisor",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC), null);

        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, villageX1);
        UUID event = pushCaptureEvent(subject, WORKER, DEVICE_W,
                "Created before role change, pushed after", knowledgeBeforeRoleChange);

        assertThat(flagSources("role_stale")).doesNotContain(event.toString());
    }

    /**
     * Phase 4: current role/action authority must still permit the event.
     */
    @Test
    void roleAction_currentRoleWithoutAction_roleStale() {
        configureVaccinationRoles(Map.of(
                "field_worker", List.of("capture"),
                "supervisor", List.of("review")));
        Event oldAssignment = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(3), null);
        UUID oldAssignmentId = UUID.fromString(oldAssignment.subjectRef().get("id").asText());
        long knowledgeBeforeRoleChange = syncWatermark(oldAssignment.id());

        assignmentService.endAssignment(oldAssignmentId, ADMIN, "role change");
        assignmentService.createAssignment(ADMIN, WORKER, "supervisor",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC), null);

        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, villageX1);
        UUID event = pushCaptureEvent(subject, WORKER, DEVICE_W,
                "Current role lacks capture", knowledgeBeforeRoleChange);

        assertThat(flagSources("role_stale")).contains(event.toString());
    }

    /**
     * FP-001 / IDR-021 gate: horizon authority uses the device knowledge
     * watermark, so a later promotion cannot authorize an older review event.
     */
    @Test
    void roleAction_horizonRoleWithoutAction_roleStaleEvenIfCurrentAllows() {
        configureVaccinationRoles(Map.of(
                "field_worker", List.of("capture"),
                "supervisor", List.of("review")));
        Event fieldWorkerAssignment = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(3), null);
        long knowledgeBeforePromotion = syncWatermark(fieldWorkerAssignment.id());

        assignmentService.createAssignment(ADMIN, WORKER, "supervisor",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC), null);

        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, villageX1);
        UUID event = pushReviewEvent(subject, WORKER, DEVICE_W,
                "Review created before promotion", knowledgeBeforePromotion);

        assertThat(flagSources("role_stale")).containsExactly(event.toString());
    }

    /**
     * Phase 4: permissions OR across covering assignments, but do not leak
     * outside an assignment's own scope.
     */
    @Test
    void roleAction_multipleAssignmentsOrOnlyInsideCoveringScopes() {
        configureVaccinationRoles(Map.of(
                "field_worker", List.of("capture"),
                "reviewer", List.of("review")));
        Event captureAssignment = assignmentService.createAssignment(ADMIN, WORKER, "field_worker",
                villageX1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        Event reviewAssignment = assignmentService.createAssignment(ADMIN, WORKER, "reviewer",
                villageY1, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        long knowledge = Math.max(syncWatermark(captureAssignment.id()), syncWatermark(reviewAssignment.id()));

        UUID subjectX = UUID.randomUUID();
        registerSubjectLocation(subjectX, villageX1);
        UUID reviewInCaptureScope = pushReviewEvent(subjectX, WORKER, DEVICE_W,
                "Review in capture-only scope", knowledge);

        UUID subjectY = UUID.randomUUID();
        registerSubjectLocation(subjectY, villageY1);
        UUID reviewInReviewScope = pushReviewEvent(subjectY, WORKER, DEVICE_W,
                "Review in review scope", knowledge);

        assertThat(flagSources("role_stale")).containsExactly(reviewInCaptureScope.toString());
        assertThat(flagSources("role_stale")).doesNotContain(reviewInReviewScope.toString());
    }

    // --- Helpers ---

    private void registerSubjectLocation(UUID subjectId, UUID locationId) {
        jdbc.update("""
                INSERT INTO subject_locations (subject_id, location_id, path)
                VALUES (?::uuid, ?::uuid, (SELECT path FROM locations WHERE id = ?::uuid))
                ON CONFLICT (subject_id) DO UPDATE SET location_id = EXCLUDED.location_id, path = EXCLUDED.path
                """, subjectId.toString(), locationId.toString(), locationId.toString());
    }

    private void pushCaptureEvent(UUID subjectId, UUID actorId, UUID deviceId, String notes) {
        pushCaptureEvent(subjectId, actorId, deviceId, notes, null);
    }

    private UUID pushCaptureEvent(UUID subjectId, UUID actorId, UUID deviceId, String notes,
                                  Long lastPullWatermark) {
        return pushEvent(subjectId, actorId, deviceId, "capture", "basic_capture/v1", notes, lastPullWatermark);
    }

    private UUID pushReviewEvent(UUID subjectId, UUID actorId, UUID deviceId, String notes,
                                 Long lastPullWatermark) {
        return pushEvent(subjectId, actorId, deviceId, "review", "basic_review/v1", notes, lastPullWatermark);
    }

    private UUID pushEvent(UUID subjectId, UUID actorId, UUID deviceId, String type, String shapeRef,
                           String notes, Long lastPullWatermark) {
        UUID eventId = UUID.randomUUID();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", eventId.toString());
        event.put("type", type);
        event.put("shape_ref", shapeRef);
        event.put("activity_ref", "vaccination");
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", actorId.toString()));
        event.put("device_id", deviceId.toString());
        event.put("device_seq", seqCounter++);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", Map.of("name", "Subject", "category", "test", "notes", notes));

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("events", List.of(event));
        if (lastPullWatermark != null) {
            request.put("last_pull_watermark", lastPullWatermark);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JsonNode> response = rest.exchange("/api/sync/push",
                HttpMethod.POST, new HttpEntity<>(request, headers), JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("accepted").asInt()).isEqualTo(1);
        return eventId;
    }

    private void configureVaccinationRoles(Map<String, List<String>> roles) {
        ObjectNode config = objectMapper.createObjectNode();
        config.putArray("shapes").add("basic_capture/v1").add("basic_review/v1");
        ObjectNode rolesNode = config.putObject("roles");
        roles.forEach((role, actions) -> {
            ArrayNode actionArray = rolesNode.putArray(role);
            actions.forEach(actionArray::add);
        });
        jdbc.update("""
                INSERT INTO activities (name, config_json, status, sensitivity)
                VALUES ('vaccination', ?::jsonb, 'active', 'standard')
                ON CONFLICT (name) DO UPDATE
                SET config_json = EXCLUDED.config_json, status = EXCLUDED.status, sensitivity = EXCLUDED.sensitivity
                """, config.toString());
    }

    private ResponseEntity<JsonNode> pullEvents(String token, long sinceWatermark, int limit) {
        Map<String, Object> request = Map.of("since_watermark", sinceWatermark, "limit", limit);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange("/api/sync/pull", HttpMethod.POST,
                new HttpEntity<>(request, headers), JsonNode.class);
    }

    private List<String> flagSources(String category) {
        return jdbc.queryForList("""
                SELECT payload->>'source_event_id'
                FROM events
                WHERE shape_ref = 'conflict_detected/v1'
                  AND payload->>'flag_category' = ?
                ORDER BY sync_watermark ASC
                """, String.class, category);
    }

    private long syncWatermark(UUID eventId) {
        return jdbc.queryForObject("SELECT sync_watermark FROM events WHERE id = ?::uuid",
                Long.class, eventId.toString());
    }
}
