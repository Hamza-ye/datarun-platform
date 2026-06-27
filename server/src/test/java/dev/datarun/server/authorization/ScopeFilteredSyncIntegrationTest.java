package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.config.ActivityService;
import dev.datarun.server.config.ShapeService;
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
 * Phase 2a quality gate tests — Scope-Filtered Sync.
 * 
 * Tests the complete path: assignment creation → location hierarchy →
 * scope-filtered pull → correct event visibility.
 */
class ScopeFilteredSyncIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private ShapeService shapeService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ActorTokenRepository actorTokenRepository;

    // Actors
    private static final UUID ADMIN = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"); // same as TEST_ACTOR_ID
    private static final UUID ACTOR_A = UUID.fromString("aaaa0000-0000-0000-0000-000000000001");
    private static final UUID ACTOR_B = UUID.fromString("bbbb0000-0000-0000-0000-000000000002");
    private static final UUID ACTOR_NONE = UUID.fromString("cccc0000-0000-0000-0000-000000000003");

    // Devices
    private static final UUID DEVICE_A = UUID.fromString("dddd0000-0000-0000-0000-000000000001");

    private String tokenA;
    private String tokenB;
    private String tokenNone;

    // Locations: Region Y → District X, District Z
    private UUID regionY;
    private UUID districtX;
    private UUID districtZ;

    @BeforeEach
    void setup() {
        jdbc.execute("DELETE FROM actor_tokens");
        jdbc.execute("DELETE FROM subject_locations");
        jdbc.execute("DELETE FROM events");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbc.execute("DELETE FROM device_sync_state");
        jdbc.execute("DELETE FROM activities");
        jdbc.execute("DELETE FROM shapes");
        jdbc.execute("DELETE FROM locations");
        provisionTestToken(); // admin token for ADMIN actor

        // Build location hierarchy
        regionY = UUID.randomUUID();
        districtX = UUID.randomUUID();
        districtZ = UUID.randomUUID();
        locationRepository.insert(regionY, "Region Y", null, "region");
        locationRepository.insert(districtX, "District X", regionY, "district");
        locationRepository.insert(districtZ, "District Z", regionY, "district");

        // Create actor tokens
        tokenA = actorTokenRepository.createToken(ACTOR_A);
        tokenB = actorTokenRepository.createToken(ACTOR_B);
        tokenNone = actorTokenRepository.createToken(ACTOR_NONE);
    }

    /**
     * QG: Actor A scoped to District X → pull returns only District X events.
     */
    @Test
    void actorScopedToDistrict_pullReturnsOnlyDistrictEvents() {
        // Assign Actor A to District X
        assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtX, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        // Push events for subjects in District X and District Z
        UUID subjectInX = UUID.randomUUID();
        UUID subjectInZ = UUID.randomUUID();
        registerSubjectLocation(subjectInX, districtX);
        registerSubjectLocation(subjectInZ, districtZ);

        pushCaptureEvent(subjectInX, "Capture in X");
        pushCaptureEvent(subjectInZ, "Capture in Z");

        // Actor A pulls → should see only District X event + own assignment event
        ResponseEntity<JsonNode> response = pullEvents(tokenA, 0, 100);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode events = response.getBody().get("events");
        List<String> subjectIds = new ArrayList<>();
        for (JsonNode e : events) {
            String type = e.get("type").asText();
            if (type.equals("capture")) {
                subjectIds.add(e.get("subject_ref").get("id").asText());
            }
        }
        assertThat(subjectIds).containsExactly(subjectInX.toString());
        assertThat(subjectIds).doesNotContain(subjectInZ.toString());
    }

    /**
     * QG: Actor B scoped to Region Y (parent) → pull returns events for all districts in Region Y.
     */
    @Test
    void actorScopedToRegion_pullReturnsAllDistrictEvents() {
        // Assign Actor B to Region Y (parent of X and Z)
        assignmentService.createAssignment(ADMIN, ACTOR_B, "supervisor",
                regionY, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        // Push events in both districts
        UUID subjectInX = UUID.randomUUID();
        UUID subjectInZ = UUID.randomUUID();
        registerSubjectLocation(subjectInX, districtX);
        registerSubjectLocation(subjectInZ, districtZ);

        pushCaptureEvent(subjectInX, "Capture in X");
        pushCaptureEvent(subjectInZ, "Capture in Z");

        // Actor B pulls → should see events from both districts
        ResponseEntity<JsonNode> response = pullEvents(tokenB, 0, 100);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode events = response.getBody().get("events");
        List<String> subjectIds = new ArrayList<>();
        for (JsonNode e : events) {
            if (e.get("type").asText().equals("capture")) {
                subjectIds.add(e.get("subject_ref").get("id").asText());
            }
        }
        assertThat(subjectIds).containsExactlyInAnyOrder(
                subjectInX.toString(), subjectInZ.toString());
    }

    /**
     * QG: Actor with no active assignment → pull returns empty set.
     */
    @Test
    void actorWithNoAssignment_pullReturnsEmpty() {
        // Push some events
        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, districtX);
        pushCaptureEvent(subject, "Some capture");

        // Actor with no assignment pulls → empty
        ResponseEntity<JsonNode> response = pullEvents(tokenNone, 0, 100);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("events").size()).isEqualTo(0);
    }

    /**
     * QG: Assignment scope exceeding creator's scope → rejected (S5).
     */
    @Test
    void assignmentExceedingCreatorScope_rejected() {
        // Give Actor A scope to District X only
        assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtX, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        // Actor A tries to create assignment for Actor B scoped to Region Y (broader) → should fail
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenA);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "target_actor_id", ACTOR_B.toString(),
                "role", "field_worker",
                "geographic_id", regionY.toString(),
                "valid_from", OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).toString()
        );

        ResponseEntity<JsonNode> response = rest.exchange("/api/assignments",
                HttpMethod.POST, new HttpEntity<>(body, headers), JsonNode.class);

        // Should be rejected — 403 or 400
        assertThat(response.getStatusCode().value()).isGreaterThanOrEqualTo(400);
    }

    /**
     * QG: Create → end → create with different scope → pull reflects new scope.
     */
    @Test
    void assignmentLifecycle_newScopeReflectedInPull() {
        // Step 1: Assign Actor A to District X
        var created = assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtX, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        UUID assignmentId = UUID.fromString(created.subjectRef().get("id").asText());

        // Push events in X and Z
        UUID subjectInX = UUID.randomUUID();
        UUID subjectInZ = UUID.randomUUID();
        registerSubjectLocation(subjectInX, districtX);
        registerSubjectLocation(subjectInZ, districtZ);
        pushCaptureEvent(subjectInX, "Capture in X");
        pushCaptureEvent(subjectInZ, "Capture in Z");

        // Pull → sees only X
        ResponseEntity<JsonNode> pullBefore = pullEvents(tokenA, 0, 100);
        List<String> beforeSubjects = extractCaptureSubjectIds(pullBefore.getBody().get("events"));
        assertThat(beforeSubjects).containsExactly(subjectInX.toString());

        // Step 2: End assignment to District X
        assignmentService.endAssignment(assignmentId, ADMIN, "reassignment");

        // Pull → sees nothing (no active assignment)
        ResponseEntity<JsonNode> pullMid = pullEvents(tokenA, 0, 100);
        assertThat(pullMid.getBody().get("events").size()).isEqualTo(0);

        // Step 3: Assign Actor A to District Z
        assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtZ, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        // Pull → sees only Z
        ResponseEntity<JsonNode> pullAfter = pullEvents(tokenA, 0, 100);
        List<String> afterSubjects = extractCaptureSubjectIds(pullAfter.getBody().get("events"));
        assertThat(afterSubjects).containsExactly(subjectInZ.toString());
    }

    /**
     * FP-005 gate: normal live sync contracts at request time.
     * After reassignment away from District X, a later pull from the actor's
     * prior watermark must not deliver new events from the old scope.
     */
    @Test
    void liveSyncContraction_reassignedAway_doesNotDeliverNewOldScopeEvents() {
        var created = assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtX, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        UUID assignmentId = UUID.fromString(created.subjectRef().get("id").asText());

        UUID initialX = UUID.randomUUID();
        registerSubjectLocation(initialX, districtX);
        pushCaptureEvent(initialX, "Initial District X capture");

        ResponseEntity<JsonNode> initialPull = pullEvents(tokenA, 0, 100);
        long lastSeenBeforeReassignment = initialPull.getBody().get("latest_watermark").asLong();

        assignmentService.endAssignment(assignmentId, ADMIN, "reassignment");
        assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtZ, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        UUID oldScopeAfterReassignment = UUID.randomUUID();
        UUID newScopeAfterReassignment = UUID.randomUUID();
        registerSubjectLocation(oldScopeAfterReassignment, districtX);
        registerSubjectLocation(newScopeAfterReassignment, districtZ);
        pushCaptureEvent(oldScopeAfterReassignment, "New District X capture after reassignment");
        pushCaptureEvent(newScopeAfterReassignment, "New District Z capture after reassignment");

        ResponseEntity<JsonNode> pullAfterReassignment =
                pullEvents(tokenA, lastSeenBeforeReassignment, 100);
        List<String> visibleSubjects =
                extractCaptureSubjectIds(pullAfterReassignment.getBody().get("events"));

        assertThat(visibleSubjects).contains(newScopeAfterReassignment.toString());
        assertThat(visibleSubjects).doesNotContain(oldScopeAfterReassignment.toString());
    }

    @Test
    void subjectLocationUpdate_doesNotRewriteHistoricalEventLocationPath() {
        assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtX, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, ACTOR_B, "field_worker",
                districtZ, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        UUID subject = UUID.randomUUID();
        registerSubjectLocation(subject, districtX);
        UUID originalEvent = pushCaptureEvent(subject, "Capture before subject-location correction");
        String originalPath = eventLocationPath(originalEvent);
        assertThat(originalPath).isEqualTo(locationRepository.findPathById(districtX));

        registerSubjectLocation(subject, districtZ);

        assertThat(eventLocationPath(originalEvent)).isEqualTo(originalPath);
        UUID futureEvent = pushCaptureEvent(subject, "Capture after subject-location correction");
        assertThat(eventLocationPath(futureEvent)).isEqualTo(locationRepository.findPathById(districtZ));

        ResponseEntity<JsonNode> pullX = pullEvents(tokenA, 0, 100);
        ResponseEntity<JsonNode> pullZ = pullEvents(tokenB, 0, 100);

        assertThat(extractCaptureEventIds(pullX.getBody().get("events")))
                .contains(originalEvent.toString())
                .doesNotContain(futureEvent.toString());
        assertThat(extractCaptureEventIds(pullZ.getBody().get("events")))
                .contains(futureEvent.toString())
                .doesNotContain(originalEvent.toString());
    }

    /**
     * QG: Assignment events have valid 11-field envelope, type = assignment_changed.
     */
    @Test
    void assignmentEvent_validEnvelope() {
        var event = assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtX, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        assertThat(event.type()).isEqualTo("assignment_changed");
        assertThat(event.shapeRef()).isEqualTo("assignment_created/v1");
        assertThat(event.id()).isNotNull();
        assertThat(event.subjectRef()).isNotNull();
        assertThat(event.subjectRef().get("type").asText()).isEqualTo("assignment");
        assertThat(event.actorRef()).isNotNull();
        assertThat(event.actorRef().get("type").asText()).isEqualTo("actor");
        assertThat(event.deviceId()).isNotNull();
        assertThat(event.deviceSeq()).isNotNull();
        assertThat(event.timestamp()).isNotNull();
        assertThat(event.payload()).isNotNull();
        assertThat(event.payload().has("target_actor")).isTrue();
        assertThat(event.payload().has("role")).isTrue();
        assertThat(event.payload().has("scope")).isTrue();
    }

    /**
     * QG: Contract test C10 — pull returns exactly authorized events, no more, no less.
     */
    @Test
    void contractC10_pullReturnsExactlyAuthorizedEvents() {
        // Assign A to District X, B to Region Y
        assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtX, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, ACTOR_B, "supervisor",
                regionY, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        // Push 3 events: 1 in X, 1 in Z, 1 with no location
        UUID subjectInX = UUID.randomUUID();
        UUID subjectInZ = UUID.randomUUID();
        UUID subjectNoLoc = UUID.randomUUID();
        registerSubjectLocation(subjectInX, districtX);
        registerSubjectLocation(subjectInZ, districtZ);
        // subjectNoLoc has no location registered

        pushCaptureEvent(subjectInX, "In X");
        pushCaptureEvent(subjectInZ, "In Z");
        pushCaptureEvent(subjectNoLoc, "No location");

        // Actor A: sees only X event + own assignment (as E9 event)
        ResponseEntity<JsonNode> pullA = pullEvents(tokenA, 0, 100);
        List<String> aSubjects = extractCaptureSubjectIds(pullA.getBody().get("events"));
        assertThat(aSubjects).containsExactly(subjectInX.toString());

        // Actor B: sees X + Z events (region scope covers both districts) + own assignment
        ResponseEntity<JsonNode> pullB = pullEvents(tokenB, 0, 100);
        List<String> bSubjects = extractCaptureSubjectIds(pullB.getBody().get("events"));
        assertThat(bSubjects).containsExactlyInAnyOrder(
                subjectInX.toString(), subjectInZ.toString());

        // Neither A nor B should see the event with no location
        assertThat(aSubjects).doesNotContain(subjectNoLoc.toString());
        assertThat(bSubjects).doesNotContain(subjectNoLoc.toString());
    }

    @Test
    void subjectListAndActivityScope_paginatesPastFilteredRows() {
        UUID allowedSubject = UUID.randomUUID();
        UUID blockedSubject = UUID.randomUUID();
        registerSubjectLocation(allowedSubject, districtX);
        registerSubjectLocation(blockedSubject, districtX);
        assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                null, List.of(allowedSubject), List.of("survey"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        long cursor = latestWatermark();

        pushCaptureEvent(blockedSubject, "survey", "Blocked by subject list");
        pushCaptureEvent(allowedSubject, "vaccination", "Blocked by activity");
        pushCaptureEvent(allowedSubject, "survey", "Authorized");

        ResponseEntity<JsonNode> response = pullEvents(tokenA, cursor, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractCaptureSubjectActivityKeys(response.getBody().get("events")))
                .containsExactly(allowedSubject + "|survey");
        assertThat(response.getBody().get("has_more").asBoolean()).isTrue();
    }

    @Test
    void fieldAssetCandidateEvidenceSyncsWithinSubjectListScopeOnly() {
        publishFieldAssetConfig();
        UUID allowedAsset = UUID.randomUUID();
        UUID generatedCandidateSubject = UUID.randomUUID();
        assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtX, List.of(allowedAsset), List.of("field_asset_inspection"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, ACTOR_B, "supervisor",
                districtX, null, List.of("field_asset_inspection"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, ACTOR_NONE, "supervisor",
                districtZ, null, List.of("field_asset_inspection"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        long cursor = latestWatermark();

        UUID candidateEvent = pushFieldAssetEvidenceAs(
                tokenA, ACTOR_A, generatedCandidateSubject,
                "Unknown pump", "offline_saved_list");

        ResponseEntity<JsonNode> response = pullEvents(tokenB, cursor, 100);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractCaptureEventIds(response.getBody().get("events")))
                .contains(candidateEvent.toString());
        assertThat(extractCaptureSubjectActivityKeys(response.getBody().get("events")))
                .contains(generatedCandidateSubject + "|field_asset_inspection")
                .doesNotContain(allowedAsset + "|field_asset_inspection");
        assertThat(eventLocationPath(candidateEvent))
                .isEqualTo(locationRepository.findPathById(districtX));

        JsonNode pulled = eventById(response.getBody().get("events"),
                candidateEvent.toString());
        JsonNode evidence = pulled.path("payload").path("asset_candidate_evidence");
        assertThat(evidence.path("standing").asText()).isEqualTo("candidate");
        assertThat(evidence.path("display_label").asText()).isEqualTo("Unknown pump");
        assertThat(evidence.path("lookup_standing").path("state").asText())
                .isEqualTo("offline_saved_list");
        assertThat(pulled.path("payload").has("field_asset")).isFalse();
        assertThat(pulled.path("payload").has("subject_ref")).isFalse();
        assertThat(evidence.has("promoted")).isFalse();
        assertThat(evidence.has("rejected")).isFalse();
        assertThat(evidence.has("lifecycle_state")).isFalse();
        assertThat(evidence.has("duplicate_resolution")).isFalse();

        ResponseEntity<JsonNode> outOfScope = pullEvents(tokenNone, cursor, 100);
        assertThat(outOfScope.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractCaptureEventIds(outOfScope.getBody().get("events")))
                .doesNotContain(candidateEvent.toString());
    }

    @Test
    void scopeAxesComposeAndWithinAssignmentOrAcrossAssignments() {
        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();
        registerSubjectLocation(subjectA, districtX);
        registerSubjectLocation(subjectB, districtX);
        assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtX, List.of(subjectA), List.of("survey"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, ACTOR_A, "field_worker",
                districtX, List.of(subjectB), List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        long cursor = latestWatermark();

        pushCaptureEvent(subjectA, "survey", "First assignment permits this");
        pushCaptureEvent(subjectB, "vaccination", "Second assignment permits this");
        pushCaptureEvent(subjectA, "vaccination", "Subject from one assignment cannot borrow activity from another");
        pushCaptureEvent(subjectB, "survey", "Subject from one assignment cannot borrow activity from another");

        ResponseEntity<JsonNode> response = pullEvents(tokenA, cursor, 100);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractCaptureSubjectActivityKeys(response.getBody().get("events")))
                .containsExactlyInAnyOrder(
                        subjectA + "|survey",
                        subjectB + "|vaccination");
    }

    // --- Helpers ---

    private void registerSubjectLocation(UUID subjectId, UUID locationId) {
        jdbc.update("""
                INSERT INTO subject_locations (subject_id, location_id, path)
                VALUES (?::uuid, ?::uuid, (SELECT path FROM locations WHERE id = ?::uuid))
                ON CONFLICT (subject_id) DO UPDATE SET location_id = EXCLUDED.location_id, path = EXCLUDED.path
                """, subjectId.toString(), locationId.toString(), locationId.toString());
    }

    private void publishFieldAssetConfig() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("subject_binding", "field_asset");
        schema.putNull("uniqueness");
        ArrayNode fields = schema.putArray("fields");
        addField(fields, "field_asset", "subject_ref", true);
        addField(fields, "name", "text", true);
        assertThat(shapeService.createShape("asset_check", "standard", schema))
                .isEmpty();

        ObjectNode activityConfig = objectMapper.createObjectNode();
        activityConfig.putArray("shapes").add("asset_check/v1");
        activityConfig.putObject("roles").putArray("field_worker").add("capture");
        assertThat(activityService.createActivity(
                "field_asset_inspection", "standard", activityConfig))
                .isEmpty();
    }

    private void addField(ArrayNode fields, String name, String type, boolean required) {
        ObjectNode field = fields.addObject();
        field.put("name", name);
        field.put("type", type);
        field.put("required", required);
        field.put("deprecated", false);
        field.put("display_order", fields.size());
    }

    private UUID pushCaptureEvent(UUID subjectId, String notes) {
        return pushCaptureEvent(subjectId, null, notes);
    }

    private UUID pushCaptureEvent(UUID subjectId, String activityRef, String notes) {
        return pushCaptureEvent(subjectId, activityRef,
                Map.of("name", "Subject", "category", "test", "notes", notes),
                "basic_capture/v1");
    }

    private UUID pushFieldAssetEvidenceAs(String token, UUID actorId,
                                          UUID subjectId, String label,
                                          String lookupStanding) {
        UUID eventId = UUID.randomUUID();
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("standing", "candidate");
        evidence.put("review_label", "Candidate asset");
        evidence.put("display_label", label);
        evidence.put("candidate_standing",
                "Needs review before it can be used as a known asset.");
        evidence.put("activity_context", Map.of(
                "activity_ref", "field_asset_inspection",
                "shape_ref", "asset_check/v1"));
        evidence.put("lookup_standing", Map.of(
                "state", lookupStanding,
                "message", "You are using the last saved asset list.",
                "offline", true,
                "stale", false,
                "incomplete", true,
                "unavailable", false));
        evidence.put("actor_session_provenance", Map.of(
                "actor_id", ADMIN.toString(),
                "session", "local_actor_session"));
        evidence.put("assignment_scope_context", List.of(Map.of(
                "role", "field_worker",
                "activity_list", List.of("field_asset_inspection"))));
        evidence.put("capture_timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        evidence.put("original_submitted_record_ref", Map.of(
                "type", "event",
                "id", eventId.toString()));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "Candidate evidence record");
        payload.put("asset_candidate_evidence", evidence);
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", eventId.toString());
        event.put("type", "capture");
        event.put("shape_ref", "asset_check/v1");
        event.put("activity_ref", "field_asset_inspection");
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", actorId.toString()));
        event.put("device_id", DEVICE_A.toString());
        event.put("device_seq", (int) (System.nanoTime() % Integer.MAX_VALUE));
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", payload);

        Map<String, Object> request = Map.of("events", List.of(event));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JsonNode> response = rest.exchange("/api/sync/push",
                HttpMethod.POST, new HttpEntity<>(request, headers), JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return eventId;
    }

    private UUID pushCaptureEvent(UUID subjectId, String activityRef,
                                  Map<String, Object> payload,
                                  String shapeRef) {
        UUID eventId = UUID.randomUUID();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", eventId.toString());
        event.put("type", "capture");
        event.put("shape_ref", shapeRef);
        event.put("activity_ref", activityRef);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", ADMIN.toString()));
        event.put("device_id", DEVICE_A.toString());
        event.put("device_seq", (int) (System.nanoTime() % Integer.MAX_VALUE));
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", payload);

        Map<String, Object> request = Map.of("events", List.of(event));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JsonNode> response = rest.exchange("/api/sync/push",
                HttpMethod.POST, new HttpEntity<>(request, headers), JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return eventId;
    }

    private ResponseEntity<JsonNode> pullEvents(String token, long sinceWatermark, int limit) {
        Map<String, Object> request = Map.of("since_watermark", sinceWatermark, "limit", limit);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange("/api/sync/pull", HttpMethod.POST,
                new HttpEntity<>(request, headers), JsonNode.class);
    }

    private long latestWatermark() {
        Long watermark = jdbc.queryForObject(
                "SELECT COALESCE(MAX(sync_watermark), 0) FROM events",
                Long.class);
        return watermark == null ? 0 : watermark;
    }

    private List<String> extractCaptureSubjectIds(JsonNode events) {
        List<String> ids = new ArrayList<>();
        for (JsonNode e : events) {
            if (e.get("type").asText().equals("capture")) {
                ids.add(e.get("subject_ref").get("id").asText());
            }
        }
        return ids;
    }

    private List<String> extractCaptureEventIds(JsonNode events) {
        List<String> ids = new ArrayList<>();
        for (JsonNode e : events) {
            if (e.get("type").asText().equals("capture")) {
                ids.add(e.get("id").asText());
            }
        }
        return ids;
    }

    private String eventLocationPath(UUID eventId) {
        return jdbc.queryForObject(
                "SELECT location_path FROM events WHERE id = ?::uuid",
                String.class,
                eventId.toString());
    }

    private List<String> extractCaptureSubjectActivityKeys(JsonNode events) {
        List<String> keys = new ArrayList<>();
        for (JsonNode e : events) {
            if (e.get("type").asText().equals("capture")) {
                String subjectId = e.get("subject_ref").get("id").asText();
                JsonNode activityNode = e.get("activity_ref");
                String activityRef = activityNode == null || activityNode.isNull()
                        ? "null"
                        : activityNode.asText();
                keys.add(subjectId + "|" + activityRef);
            }
        }
        return keys;
    }

    private JsonNode eventById(JsonNode events, String eventId) {
        for (JsonNode e : events) {
            if (eventId.equals(e.get("id").asText())) {
                return e;
            }
        }
        throw new AssertionError("event not found: " + eventId);
    }
}
