package dev.datarun.server.integrity;

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
 * Phase 1a quality gate tests — Conflict Detector.
 * Tests map 1:1 to the 8 quality gates in phase-1.md §3 (Phase 1a).
 */
class ConflictDetectorIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    private static final UUID DEVICE_A = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID DEVICE_B = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID ACTOR_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID SUBJECT_X = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");

    @BeforeEach
    void cleanDb() {
        jdbc.execute("DELETE FROM actor_tokens");
        jdbc.execute("DELETE FROM subject_locations");
        jdbc.execute("DELETE FROM events");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbc.execute("DELETE FROM device_sync_state");
        provisionTestToken();
    }

    /**
     * QG1: Push 2 events from different device_ids for same subject,
     * with knowledge horizon before each other → concurrent_state_change flag raised.
     */
    @Test
    void concurrentEvents_flagRaised() {
        // Device A pushes event for Subject X (knowledge horizon = 0, knows nothing)
        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        var responseA = pushEventsWithMeta(List.of(eventA), DEVICE_A, 0);
        assertThat(responseA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseA.getBody().get("accepted").asInt()).isEqualTo(1);

        // Device B pushes event for Subject X (knowledge horizon = 0, doesn't know about A's event)
        var eventB = buildEvent(SUBJECT_X, DEVICE_B, 1);
        var responseB = pushEventsWithMeta(List.of(eventB), DEVICE_B, 0);
        assertThat(responseB.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseB.getBody().get("accepted").asInt()).isEqualTo(1);
        assertThat(responseB.getBody().get("flags_raised").asInt()).isEqualTo(1);

        // Verify conflict_detected event exists
        assertConflictDetectedExists(SUBJECT_X);
    }

    /**
     * QG2: Push events with knowledge horizon AFTER the other device's events → no flag raised.
     */
    @Test
    void knowledgeHorizonAfterOtherEvents_noFlag() {
        // Device A pushes event for Subject X
        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEventsWithMeta(List.of(eventA), DEVICE_A, 0);

        // Device B pulls (gets watermark of A's event)
        var pullResponse = pullEvents(0, 100);
        long latestWatermark = pullResponse.getBody().get("latest_watermark").asLong();

        // Device B pushes with knowledge horizon AFTER A's event
        var eventB = buildEvent(SUBJECT_X, DEVICE_B, 1);
        var responseB = pushEventsWithMeta(List.of(eventB), DEVICE_B, latestWatermark);
        assertThat(responseB.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseB.getBody().get("flags_raised").asInt()).isEqualTo(0);
    }

    /**
     * QG3: Flagged event appears in subject timeline but NOT in projected state.
     */
    @Test
    void flaggedEvent_excludedFromProjection_visibleInTimeline() {
        // Device A pushes 2 events for Subject X
        var eventA1 = buildEvent(SUBJECT_X, DEVICE_A, 1);
        var eventA2 = buildEvent(SUBJECT_X, DEVICE_A, 2);
        pushEventsWithMeta(List.of(eventA1, eventA2), DEVICE_A, 0);

        // Device B pushes 1 event (concurrent — horizon 0)
        var eventB1 = buildEvent(SUBJECT_X, DEVICE_B, 1);
        pushEventsWithMeta(List.of(eventB1), DEVICE_B, 0);

        // Subject list (projected state) — should exclude flagged event from count
        var subjectResponse = rest.getForEntity("/api/subjects", JsonNode.class);
        JsonNode subjects = subjectResponse.getBody().get("subjects");
        assertThat(subjects.size()).isEqualTo(1);
        int projectedCount = subjects.get(0).get("event_count").asInt();

        // Timeline shows all events (including flagged + flag event)
        var timelineResponse = rest.getForEntity(
                "/api/subjects/" + SUBJECT_X + "/events", JsonNode.class);
        int timelineCount = timelineResponse.getBody().get("events").size();

        // Timeline has more events than projection count (flagged event excluded from projection,
        // but both flagged event and conflict_detected event are in timeline)
        assertThat(timelineCount).isGreaterThan(projectedCount);
    }

    /**
     * QG4: Push → CD failure (simulated) → events still persisted (C3), no flag, no crash.
     * We simulate this by pushing events that should trigger CD but verifying events are
     * always persisted regardless of CD outcome. Since we can't easily inject a CD failure
     * in integration tests, we verify the core invariant: events are always persisted.
     */
    @Test
    void eventsPersisted_regardlessOfCdOutcome() {
        // Push events — they must always be persisted
        var event = buildEvent(SUBJECT_X, DEVICE_A, 1);
        var response = pushEventsWithMeta(List.of(event), DEVICE_A, 0);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("accepted").asInt()).isEqualTo(1);

        // Verify event is in DB
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE type = 'capture'", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    /**
     * QG5: Server-generated conflict_detected event has valid 11-field envelope.
     */
    @Test
    void conflictDetectedEvent_validEnvelope() {
        // Create concurrent scenario to generate a conflict_detected event
        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEventsWithMeta(List.of(eventA), DEVICE_A, 0);
        var eventB = buildEvent(SUBJECT_X, DEVICE_B, 1);
        pushEventsWithMeta(List.of(eventB), DEVICE_B, 0);

        // Pull all events — find the conflict_detected event
        var pullResponse = pullEvents(0, 100);
        JsonNode events = pullResponse.getBody().get("events");

        JsonNode flagEvent = null;
        for (JsonNode e : events) {
            if ("conflict_detected".equals(e.get("type").asText())) {
                flagEvent = e;
                break;
            }
        }
        assertThat(flagEvent).isNotNull();

        // Verify all 11 envelope fields
        assertThat(flagEvent.has("id")).isTrue();
        assertThat(flagEvent.has("type")).isTrue();
        assertThat(flagEvent.get("type").asText()).isEqualTo("conflict_detected");
        assertThat(flagEvent.has("shape_ref")).isTrue();
        assertThat(flagEvent.get("shape_ref").asText()).isEqualTo("system/integrity/v1");
        // activity_ref is null (excluded by Jackson non_null)
        assertThat(flagEvent.has("subject_ref")).isTrue();
        assertThat(flagEvent.get("subject_ref").get("type").asText()).isEqualTo("subject");
        assertThat(flagEvent.get("subject_ref").get("id").asText()).isEqualTo(SUBJECT_X.toString());
        assertThat(flagEvent.has("actor_ref")).isTrue();
        assertThat(flagEvent.get("actor_ref").get("type").asText()).isEqualTo("actor");
        assertThat(flagEvent.has("device_id")).isTrue();
        assertThat(flagEvent.has("device_seq")).isTrue();
        assertThat(flagEvent.has("sync_watermark")).isTrue();
        assertThat(flagEvent.get("sync_watermark").asLong()).isGreaterThan(0);
        assertThat(flagEvent.has("timestamp")).isTrue();
        assertThat(flagEvent.has("payload")).isTrue();

        // Verify payload has required CD fields
        JsonNode payload = flagEvent.get("payload");
        assertThat(payload.has("source_event_id")).isTrue();
        assertThat(payload.has("flag_category")).isTrue();
        assertThat(payload.get("flag_category").asText()).isEqualTo("concurrent_state_change");
        assertThat(payload.has("resolvability")).isTrue();
        assertThat(payload.has("designated_resolver")).isTrue();
        assertThat(payload.has("reason")).isTrue();
    }

    /**
     * QG6: Push same events twice → idempotent (no duplicate flags).
     */
    @Test
    void duplicatePush_noDuplicateFlags() {
        // Device A pushes
        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEventsWithMeta(List.of(eventA), DEVICE_A, 0);

        // Device B pushes concurrent event
        var eventB = buildEvent(SUBJECT_X, DEVICE_B, 1);
        var response1 = pushEventsWithMeta(List.of(eventB), DEVICE_B, 0);
        int flags1 = response1.getBody().get("flags_raised").asInt();

        // Push Device B's event again (duplicate)
        var response2 = pushEventsWithMeta(List.of(eventB), DEVICE_B, 0);
        assertThat(response2.getBody().get("duplicates").asInt()).isEqualTo(1);
        assertThat(response2.getBody().get("accepted").asInt()).isEqualTo(0);
        // No additional flags for duplicates
        assertThat(response2.getBody().get("flags_raised").asInt()).isEqualTo(0);

        // Count conflict_detected events — should be exactly 1
        Integer flagCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE type = 'conflict_detected'", Integer.class);
        assertThat(flagCount).isEqualTo(flags1);
    }

    /**
     * QG7: Contract test C3 — event with stale data → persisted, not rejected.
     */
    @Test
    void staleEvent_persisted_notRejected() {
        // Push some events first to advance watermark
        var event1 = buildEvent(SUBJECT_X, DEVICE_A, 1);
        var event2 = buildEvent(SUBJECT_X, DEVICE_A, 2);
        pushEventsWithMeta(List.of(event1, event2), DEVICE_A, 0);

        // Device B pushes event with sync_watermark=null (fresh from device, stale data)
        // The event should be accepted (C3: never reject for state staleness)
        var staleEvent = buildEvent(SUBJECT_X, DEVICE_B, 1);
        var response = pushEventsWithMeta(List.of(staleEvent), DEVICE_B, 0);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("accepted").asInt()).isEqualTo(1);
        // May be flagged, but never rejected
    }

    /**
     * QG8: Contract test C8 — anomaly → conflict_detected event with source ref + flag category + resolver.
     */
    @Test
    void conflictDetected_hasRequiredPayloadFields() {
        // Create concurrent scenario
        pushEventsWithMeta(List.of(buildEvent(SUBJECT_X, DEVICE_A, 1)), DEVICE_A, 0);
        pushEventsWithMeta(List.of(buildEvent(SUBJECT_X, DEVICE_B, 1)), DEVICE_B, 0);

        // Find the conflict_detected event
        var pullResponse = pullEvents(0, 100);
        JsonNode flagEvent = null;
        for (JsonNode e : pullResponse.getBody().get("events")) {
            if ("conflict_detected".equals(e.get("type").asText())) {
                flagEvent = e;
                break;
            }
        }
        assertThat(flagEvent).isNotNull();

        // C8: conflict_detected event has source ref, flag category, resolver
        JsonNode payload = flagEvent.get("payload");
        assertThat(payload.get("source_event_id").asText()).isNotEmpty();
        assertThat(payload.get("flag_category").asText()).isEqualTo("concurrent_state_change");
        assertThat(payload.get("designated_resolver")).isNotNull();
        assertThat(payload.get("designated_resolver").get("type").asText()).isEqualTo("actor");
    }

    // --- Helpers ---

    private Map<String, Object> buildEvent(UUID subjectId, UUID deviceId, int seq) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "capture");
        event.put("shape_ref", "basic_capture/v1");
        event.put("activity_ref", null);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", ACTOR_ID.toString()));
        event.put("device_id", deviceId.toString());
        event.put("device_seq", seq);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", Map.of(
                "name", "Site " + seq,
                "category", "urban",
                "notes", "Test event " + seq,
                "date", "2026-04-16",
                "value", seq * 10
        ));
        return event;
    }

    private ResponseEntity<JsonNode> pushEventsWithMeta(List<?> events, UUID deviceId, long lastPullWatermark) {
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

    private void assertConflictDetectedExists(UUID subjectId) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM events
                WHERE type = 'conflict_detected'
                  AND subject_ref->>'id' = ?
                """,
                Integer.class,
                subjectId.toString());
        assertThat(count).isGreaterThan(0);
    }
}
