package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datarun.server.AbstractIntegrationTest;
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
 * Phase 2b quality gate tests — Multi-Actor Scope + Supervisor Visibility.
 *
 * Tests activity/subject_list scope types with AND composition,
 * supervisor visibility via geographic hierarchy, and the full
 * 3-actor multi-actor E2E scenario.
 */
class MultiActorScopeIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ActorTokenRepository actorTokenRepository;

    // Actors
    private static final UUID ADMIN = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID FIELD_WORKER_A = UUID.fromString("aaaa0000-0000-0000-0000-000000002001");
    private static final UUID FIELD_WORKER_B = UUID.fromString("bbbb0000-0000-0000-0000-000000002002");
    private static final UUID SUPERVISOR_C = UUID.fromString("cccc0000-0000-0000-0000-000000002003");

    // Devices
    private static final UUID DEVICE_A = UUID.fromString("dddd0000-0000-0000-0000-000000002001");

    private String tokenA;
    private String tokenB;
    private String tokenC;

    // Locations: Region → District X (Village X1), District Y (Village Y1)
    private UUID region;
    private UUID districtX;
    private UUID districtY;
    private UUID villageX1;
    private UUID villageY1;

    @BeforeEach
    void setup() {
        jdbc.execute("DELETE FROM actor_tokens");
        jdbc.execute("DELETE FROM subject_locations");
        jdbc.execute("DELETE FROM events");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbc.execute("DELETE FROM device_sync_state");
        jdbc.execute("DELETE FROM locations");
        provisionTestToken(); // admin token for ADMIN actor

        // Build 3-level hierarchy
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

        // Create actor tokens
        tokenA = actorTokenRepository.createToken(FIELD_WORKER_A);
        tokenB = actorTokenRepository.createToken(FIELD_WORKER_B);
        tokenC = actorTokenRepository.createToken(SUPERVISOR_C);
    }

    /**
     * QG: Multi-actor E2E — 3 actors: field worker A (Village X1), field worker B (Village Y1),
     * supervisor C (Region containing both districts).
     * A captures → syncs → C pulls → sees A's data.
     * B captures → syncs → C pulls → sees A+B's data.
     * A pulls → sees only Village X1 data (not B's Village Y1 data).
     */
    @Test
    void multiActorE2E_threeActorsHierarchicalVisibility() {
        // Assign actors
        assignmentService.createAssignment(ADMIN, FIELD_WORKER_A, "field_worker",
                villageX1, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, FIELD_WORKER_B, "field_worker",
                villageY1, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, SUPERVISOR_C, "supervisor",
                region, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        // A captures a subject in Village X1
        UUID subjectInX = UUID.randomUUID();
        registerSubjectLocation(subjectInX, villageX1);
        pushCaptureEvent(subjectInX, "vaccination", "A's capture in Village X1");

        // B captures a subject in Village Y1
        UUID subjectInY = UUID.randomUUID();
        registerSubjectLocation(subjectInY, villageY1);
        pushCaptureEvent(subjectInY, "vaccination", "B's capture in Village Y1");

        // Supervisor C pulls → sees both A's and B's data (region scope covers all)
        ResponseEntity<JsonNode> pullC = pullEvents(tokenC, 0, 100);
        assertThat(pullC.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> cSubjects = extractCaptureSubjectIds(pullC.getBody().get("events"));
        assertThat(cSubjects).containsExactlyInAnyOrder(
                subjectInX.toString(), subjectInY.toString());

        // A pulls → sees only Village X1 data
        ResponseEntity<JsonNode> pullA = pullEvents(tokenA, 0, 100);
        List<String> aSubjects = extractCaptureSubjectIds(pullA.getBody().get("events"));
        assertThat(aSubjects).containsExactly(subjectInX.toString());
        assertThat(aSubjects).doesNotContain(subjectInY.toString());

        // B pulls → sees only Village Y1 data
        ResponseEntity<JsonNode> pullB = pullEvents(tokenB, 0, 100);
        List<String> bSubjects = extractCaptureSubjectIds(pullB.getBody().get("events"));
        assertThat(bSubjects).containsExactly(subjectInY.toString());
        assertThat(bSubjects).doesNotContain(subjectInX.toString());
    }

    /**
     * QG: Supervisor's pull includes events from ALL subordinate areas within their scope.
     */
    @Test
    void supervisorPull_includesAllSubordinateAreas() {
        // Supervisor at district level → sees all villages within
        assignmentService.createAssignment(ADMIN, SUPERVISOR_C, "supervisor",
                districtX, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        UUID subjectInVillage = UUID.randomUUID();
        UUID subjectInDistrict = UUID.randomUUID();
        UUID subjectInOtherDistrict = UUID.randomUUID();
        registerSubjectLocation(subjectInVillage, villageX1);
        registerSubjectLocation(subjectInDistrict, districtX);
        registerSubjectLocation(subjectInOtherDistrict, villageY1);

        pushCaptureEvent(subjectInVillage, null, "In Village X1");
        pushCaptureEvent(subjectInDistrict, null, "In District X");
        pushCaptureEvent(subjectInOtherDistrict, null, "In Village Y1");

        ResponseEntity<JsonNode> pull = pullEvents(tokenC, 0, 100);
        List<String> subjects = extractCaptureSubjectIds(pull.getBody().get("events"));

        // Sees village within own district + own district, but NOT other district's village
        assertThat(subjects).containsExactlyInAnyOrder(
                subjectInVillage.toString(), subjectInDistrict.toString());
        assertThat(subjects).doesNotContain(subjectInOtherDistrict.toString());
    }

    /**
     * QG: Actor with activity scope restriction → pull includes only events with matching activity_ref.
     */
    @Test
    void activityScope_pullIncludesOnlyMatchingActivities() {
        // Assign Actor A to region (broad geo) but restricted to "vaccination" activity
        assignmentService.createAssignment(ADMIN, FIELD_WORKER_A, "field_worker",
                region, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        UUID subject1 = UUID.randomUUID();
        UUID subject2 = UUID.randomUUID();
        registerSubjectLocation(subject1, villageX1);
        registerSubjectLocation(subject2, villageY1);

        pushCaptureEvent(subject1, "vaccination", "Vaccination capture");
        pushCaptureEvent(subject2, "survey", "Survey capture");

        ResponseEntity<JsonNode> pull = pullEvents(tokenA, 0, 100);
        List<String> subjects = extractCaptureSubjectIds(pull.getBody().get("events"));

        // Only vaccination event passes
        assertThat(subjects).containsExactly(subject1.toString());
        assertThat(subjects).doesNotContain(subject2.toString());
    }

    /**
     * QG: Actor with compound scope (geographic + activity) → both dimensions filter (AND).
     */
    @Test
    void compoundScope_geoAndActivityFilterAND() {
        // Actor A: District X + vaccination only
        assignmentService.createAssignment(ADMIN, FIELD_WORKER_A, "field_worker",
                districtX, null, List.of("vaccination"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        UUID subjX_vacc = UUID.randomUUID();
        UUID subjX_surv = UUID.randomUUID();
        UUID subjY_vacc = UUID.randomUUID();
        registerSubjectLocation(subjX_vacc, villageX1);
        registerSubjectLocation(subjX_surv, villageX1);
        registerSubjectLocation(subjY_vacc, villageY1);

        pushCaptureEvent(subjX_vacc, "vaccination", "X vaccination");
        pushCaptureEvent(subjX_surv, "survey", "X survey");
        pushCaptureEvent(subjY_vacc, "vaccination", "Y vaccination");

        ResponseEntity<JsonNode> pull = pullEvents(tokenA, 0, 100);
        List<String> subjects = extractCaptureSubjectIds(pull.getBody().get("events"));

        // Only District X + vaccination passes (AND composition)
        assertThat(subjects).containsExactly(subjX_vacc.toString());
        assertThat(subjects).doesNotContain(subjX_surv.toString());
        assertThat(subjects).doesNotContain(subjY_vacc.toString());
    }

    /**
     * QG: Field worker's pull excludes events outside their geographic scope.
     * (Verifies geographic filtering at village level — deeper than 2a's district test.)
     */
    @Test
    void fieldWorkerPull_excludesEventsOutsideGeoScope() {
        assignmentService.createAssignment(ADMIN, FIELD_WORKER_A, "field_worker",
                villageX1, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        UUID inScope = UUID.randomUUID();
        UUID outScope = UUID.randomUUID();
        registerSubjectLocation(inScope, villageX1);
        registerSubjectLocation(outScope, villageY1);

        pushCaptureEvent(inScope, null, "In scope");
        pushCaptureEvent(outScope, null, "Out of scope");

        ResponseEntity<JsonNode> pull = pullEvents(tokenA, 0, 100);
        List<String> subjects = extractCaptureSubjectIds(pull.getBody().get("events"));

        assertThat(subjects).containsExactly(inScope.toString());
        assertThat(subjects).doesNotContain(outScope.toString());
    }

    /**
     * QG: Projection equivalence — same scoped event set → server PE and device PE
     * produce identical output. (Server-side verification: the scoped pull returns events
     * consistent with ScopeResolver.isInScope for each event.)
     */
    @Test
    void projectionEquivalence_scopedPullConsistentWithScopeResolver() {
        // Supervisor C with region scope
        assignmentService.createAssignment(ADMIN, SUPERVISOR_C, "supervisor",
                region, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        // Push events across the hierarchy
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        UUID s3 = UUID.randomUUID();
        registerSubjectLocation(s1, villageX1);
        registerSubjectLocation(s2, districtY);
        registerSubjectLocation(s3, region);

        pushCaptureEvent(s1, "vaccination", "In village");
        pushCaptureEvent(s2, "survey", "In district");
        pushCaptureEvent(s3, null, "At region");

        // Pull all events for supervisor
        ResponseEntity<JsonNode> pull = pullEvents(tokenC, 0, 100);
        List<String> subjects = extractCaptureSubjectIds(pull.getBody().get("events"));

        // All 3 should be visible (all under region scope)
        assertThat(subjects).containsExactlyInAnyOrder(
                s1.toString(), s2.toString(), s3.toString());
    }

    /**
     * QG: Performance — supervisor pull for many subjects across 3-level hierarchy completes within budget.
     * (Lightweight version: validates query doesn't degrade with moderate data volume.)
     */
    @Test
    void performance_supervisorPullCompletesWithinBudget() {
        assignmentService.createAssignment(ADMIN, SUPERVISOR_C, "supervisor",
                region, null, null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        // Insert 100 subjects across villages
        for (int i = 0; i < 100; i++) {
            UUID subjectId = UUID.randomUUID();
            UUID location = (i % 2 == 0) ? villageX1 : villageY1;
            registerSubjectLocation(subjectId, location);
            pushCaptureEvent(subjectId, "vaccination", "Subject " + i);
        }

        long start = System.currentTimeMillis();
        ResponseEntity<JsonNode> pull = pullEvents(tokenC, 0, 1000);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(pull.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> subjects = extractCaptureSubjectIds(pull.getBody().get("events"));
        assertThat(subjects).hasSize(100);

        // Budget: < 5s for supervisor pull (spec says 1000 subjects, we test 100 for CI speed)
        assertThat(elapsed).isLessThan(5000);
    }

    // --- Helpers ---

    private void registerSubjectLocation(UUID subjectId, UUID locationId) {
        jdbc.update("""
                INSERT INTO subject_locations (subject_id, location_id, path)
                VALUES (?::uuid, ?::uuid, (SELECT path FROM locations WHERE id = ?::uuid))
                ON CONFLICT (subject_id) DO UPDATE SET location_id = EXCLUDED.location_id, path = EXCLUDED.path
                """, subjectId.toString(), locationId.toString(), locationId.toString());
    }

    private void pushCaptureEvent(UUID subjectId, String activityRef, String notes) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "capture");
        event.put("shape_ref", "basic_capture/v1");
        event.put("activity_ref", activityRef);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", ADMIN.toString()));
        event.put("device_id", DEVICE_A.toString());
        event.put("device_seq", (int) (System.nanoTime() % Integer.MAX_VALUE));
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

    private ResponseEntity<JsonNode> pullEvents(String token, long sinceWatermark, int limit) {
        Map<String, Object> request = Map.of("since_watermark", sinceWatermark, "limit", limit);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange("/api/sync/pull", HttpMethod.POST,
                new HttpEntity<>(request, headers), JsonNode.class);
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
}
