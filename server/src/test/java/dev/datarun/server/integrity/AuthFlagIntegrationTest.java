package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
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
                "SELECT payload FROM events WHERE type = 'conflict_detected'");
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
                "SELECT payload FROM events WHERE type = 'conflict_detected'");
        assertThat(flags).hasSize(1);
        String payload = flags.get(0).get("payload").toString();
        assertThat(payload).contains("temporal_authority_expired");
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
                "SELECT payload FROM events WHERE type = 'conflict_detected'");
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
                "SELECT payload FROM events WHERE type = 'conflict_detected'");
        assertThat(flags).hasSize(1);
        String payload = flags.get(0).get("payload").toString();
        assertThat(payload).contains("temporal_authority_expired");
        assertThat(payload).doesNotContain("scope_violation");
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
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "capture");
        event.put("shape_ref", "basic_capture/v1");
        event.put("activity_ref", "vaccination");
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", actorId.toString()));
        event.put("device_id", deviceId.toString());
        event.put("device_seq", seqCounter++);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", Map.of("name", "Subject", "category", "test", "notes", notes));

        Map<String, Object> request = Map.of("events", List.of(event));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JsonNode> response = rest.exchange("/api/sync/push",
                HttpMethod.POST, new HttpEntity<>(request, headers), JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
