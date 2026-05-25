package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 4 P04 scenario-grade responsibility binding gate.
 *
 * <p>This keeps the existing responsibility model honest across the combined
 * campaign flow: overlapping geographic responsibility, activity scoping,
 * mid-campaign reassignment, live-sync contraction/expansion, and role-action
 * authority across the reassignment boundary.
 */
class ResponsibilityBindingScenarioIntegrationTest extends AbstractIntegrationTest {

    private static final UUID ADMIN = TEST_ACTOR_ID;
    private static final UUID WORKER_A = UUID.fromString("aaaa4000-0000-0000-0000-000000000001");
    private static final UUID WORKER_B = UUID.fromString("bbbb4000-0000-0000-0000-000000000002");
    private static final UUID SUPERVISOR = UUID.fromString("cccc4000-0000-0000-0000-000000000003");

    private static final UUID DEVICE_A = UUID.fromString("dddd4000-0000-0000-0000-000000000001");
    private static final UUID DEVICE_B = UUID.fromString("dddd4000-0000-0000-0000-000000000002");
    private static final UUID DEVICE_SUPERVISOR = UUID.fromString("dddd4000-0000-0000-0000-000000000003");
    private static final UUID DEVICE_ADMIN = UUID.fromString("dddd4000-0000-0000-0000-000000000004");

    private static final String CAMPAIGN_ACTIVITY = "campaign_capture";
    private static final String INVENTORY_ACTIVITY = "inventory";

    @Autowired private TestRestTemplate rest;
    @Autowired private AssignmentService assignmentService;
    @Autowired private LocationRepository locationRepository;
    @Autowired private ActorTokenRepository actorTokenRepository;
    @Autowired private ObjectMapper objectMapper;

    private UUID region;
    private UUID districtA;
    private UUID districtB;
    private UUID villageA1;
    private UUID villageA2;
    private UUID villageB1;
    private String tokenA;
    private String tokenB;
    private String supervisorToken;
    private int seqCounter;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("DELETE FROM actor_tokens");
        jdbcTemplate.execute("DELETE FROM subject_locations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM device_sync_state");
        jdbcTemplate.execute("DELETE FROM activities");
        jdbcTemplate.execute("DELETE FROM locations");
        provisionTestToken();

        region = UUID.randomUUID();
        districtA = UUID.randomUUID();
        districtB = UUID.randomUUID();
        villageA1 = UUID.randomUUID();
        villageA2 = UUID.randomUUID();
        villageB1 = UUID.randomUUID();
        locationRepository.insert(region, "Region", null, "region");
        locationRepository.insert(districtA, "District A", region, "district");
        locationRepository.insert(districtB, "District B", region, "district");
        locationRepository.insert(villageA1, "Village A1", districtA, "village");
        locationRepository.insert(villageA2, "Village A2", districtA, "village");
        locationRepository.insert(villageB1, "Village B1", districtB, "village");

        tokenA = actorTokenRepository.createToken(WORKER_A);
        tokenB = actorTokenRepository.createToken(WORKER_B);
        supervisorToken = actorTokenRepository.createToken(SUPERVISOR);
        configureCampaignActivity();
        seqCounter = 1;
    }

    @Test
    void coordinatedCampaignReassignmentPreservesResponsibilityBinding() {
        Event workerAInitial = assignmentService.createAssignment(ADMIN, WORKER_A, "field_worker",
                villageA1, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, WORKER_B, "field_worker",
                villageA2, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, SUPERVISOR, "supervisor",
                districtA, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        long workerAKnowledge = latestWatermark(pullEvents(tokenA, 0, 100));
        long workerBKnowledge = latestWatermark(pullEvents(tokenB, 0, 100));
        long supervisorKnowledge = latestWatermark(pullEvents(supervisorToken, 0, 100));

        UUID subjectA1 = subjectAt(villageA1);
        UUID subjectA2 = subjectAt(villageA2);
        UUID subjectB1 = subjectAt(villageB1);
        UUID wrongActivityA1 = subjectAt(villageA1);

        UUID a1Initial = pushEvent(WORKER_A, DEVICE_A, subjectA1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "A1 baseline", workerAKnowledge);
        UUID a2Initial = pushEvent(WORKER_B, DEVICE_B, subjectA2, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "A2 baseline", workerBKnowledge);
        pushEvent(ADMIN, DEVICE_ADMIN, wrongActivityA1, INVENTORY_ACTIVITY,
                "capture", "basic_capture/v1", "wrong activity", 0);
        UUID b1BeforeReassignment = pushEvent(ADMIN, DEVICE_ADMIN, subjectB1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "outside district A", 0);

        assertVisibleCaptures(pullEvents(tokenA, 0, 100), a1Initial);
        assertVisibleCaptures(pullEvents(tokenB, 0, 100), a2Initial);
        assertVisibleCaptures(pullEvents(supervisorToken, 0, 100), a1Initial, a2Initial);

        UUID workerAInitialId = UUID.fromString(workerAInitial.subjectRef().get("id").asText());
        assignmentService.endAssignment(workerAInitialId, ADMIN, "campaign reassignment");
        assignmentService.createAssignment(ADMIN, WORKER_A, "field_worker",
                villageB1, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1), null);
        assignmentService.createAssignment(ADMIN, WORKER_B, "field_worker",
                villageA1, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1), null);

        long workerAAfterReassignment = latestWatermark(pullEvents(tokenA, workerAKnowledge, 100));
        long workerBAfterReassignment = latestWatermark(pullEvents(tokenB, workerBKnowledge, 100));
        long supervisorAfterReassignment = latestWatermark(
                pullEvents(supervisorToken, supervisorKnowledge, 100));

        UUID a1AfterReassignment = pushEvent(WORKER_B, DEVICE_B, subjectA1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "A1 after reassignment", workerBAfterReassignment);
        UUID b1AfterReassignment = pushEvent(WORKER_A, DEVICE_A, subjectB1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "B1 after reassignment", workerAAfterReassignment);
        UUID supervisorReview = pushEvent(SUPERVISOR, DEVICE_SUPERVISOR, subjectA1, CAMPAIGN_ACTIVITY,
                "review", "basic_review/v1", "Supervisor review", supervisorAfterReassignment);
        UUID workerAOfflineA1 = pushEvent(WORKER_A, DEVICE_A, subjectA1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "A1 offline after reassignment", workerAKnowledge);

        ResponseEntity<JsonNode> workerAExpandedPull = pullEvents(tokenA, workerAKnowledge, 100);
        assertCaptureContains(workerAExpandedPull, b1BeforeReassignment, b1AfterReassignment);
        assertCaptureExcludes(workerAExpandedPull, a1AfterReassignment);

        ResponseEntity<JsonNode> workerBExpandedPull = pullEvents(tokenB, workerBKnowledge, 100);
        assertCaptureContains(workerBExpandedPull, a1Initial, a2Initial, a1AfterReassignment);
        assertCaptureExcludes(workerBExpandedPull, b1AfterReassignment);

        ResponseEntity<JsonNode> supervisorDistrictPull = pullEvents(supervisorToken, supervisorKnowledge, 100);
        assertCaptureContains(supervisorDistrictPull, a1Initial, a2Initial, a1AfterReassignment);
        assertCaptureExcludes(supervisorDistrictPull, b1BeforeReassignment, b1AfterReassignment);

        assertThat(flagSources("temporal_authority_expired")).contains(workerAOfflineA1.toString());
        assertThat(flagSources("scope_violation")).doesNotContain(workerAOfflineA1.toString());
        assertThat(flagSources("role_stale")).contains(workerAOfflineA1.toString());
        assertThat(flagSources("role_stale")).doesNotContain(a1AfterReassignment.toString());
        assertThat(flagSources("role_stale")).doesNotContain(b1AfterReassignment.toString());
        assertThat(flagSources("role_stale")).doesNotContain(supervisorReview.toString());
    }

    private void configureCampaignActivity() {
        ObjectNode config = objectMapper.createObjectNode();
        config.putArray("shapes").add("basic_capture/v1").add("basic_review/v1");
        ObjectNode roles = config.putObject("roles");
        ArrayNode fieldWorker = roles.putArray("field_worker");
        fieldWorker.add("capture");
        ArrayNode supervisor = roles.putArray("supervisor");
        supervisor.add("capture");
        supervisor.add("review");
        jdbcTemplate.update("""
                INSERT INTO activities (name, config_json, status, sensitivity)
                VALUES (?, ?::jsonb, 'active', 'standard')
                """, CAMPAIGN_ACTIVITY, config.toString());
    }

    private UUID subjectAt(UUID locationId) {
        UUID subject = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO subject_locations (subject_id, location_id, path)
                VALUES (?::uuid, ?::uuid, (SELECT path FROM locations WHERE id = ?::uuid))
                """, subject.toString(), locationId.toString(), locationId.toString());
        return subject;
    }

    private UUID pushEvent(UUID actorId, UUID deviceId, UUID subjectId, String activityRef,
                           String type, String shapeRef, String notes, long lastPullWatermark) {
        UUID eventId = UUID.randomUUID();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", eventId.toString());
        event.put("type", type);
        event.put("shape_ref", shapeRef);
        event.put("activity_ref", activityRef);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", actorId.toString()));
        event.put("device_id", deviceId.toString());
        event.put("device_seq", seqCounter++);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", Map.of("name", "Subject", "category", "campaign", "notes", notes));

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("events", List.of(event));
        request.put("last_pull_watermark", lastPullWatermark);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JsonNode> response = rest.exchange("/api/sync/push",
                HttpMethod.POST, new HttpEntity<>(request, headers), JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("accepted").asInt()).isEqualTo(1);
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

    private long latestWatermark(ResponseEntity<JsonNode> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().get("latest_watermark").asLong();
    }

    private void assertVisibleCaptures(ResponseEntity<JsonNode> response, UUID... eventIds) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureEventIds(response.getBody().get("events")))
                .containsExactlyInAnyOrderElementsOf(
                        List.of(eventIds).stream().map(UUID::toString).toList());
    }

    private void assertCaptureContains(ResponseEntity<JsonNode> response, UUID... eventIds) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureEventIds(response.getBody().get("events")))
                .contains(List.of(eventIds).stream().map(UUID::toString).toArray(String[]::new));
    }

    private void assertCaptureExcludes(ResponseEntity<JsonNode> response, UUID... eventIds) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureEventIds(response.getBody().get("events")))
                .doesNotContain(List.of(eventIds).stream().map(UUID::toString).toArray(String[]::new));
    }

    private List<String> captureEventIds(JsonNode events) {
        List<String> ids = new ArrayList<>();
        for (JsonNode event : events) {
            if ("capture".equals(event.get("type").asText())) {
                ids.add(event.get("id").asText());
            }
        }
        return ids;
    }

    private List<String> flagSources(String category) {
        return jdbcTemplate.queryForList("""
                SELECT payload->>'source_event_id'
                FROM events
                WHERE shape_ref = 'conflict_detected/v1'
                  AND payload->>'flag_category' = ?
                ORDER BY sync_watermark ASC
                """, String.class, category);
    }
}
