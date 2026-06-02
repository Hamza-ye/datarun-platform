package dev.datarun.server.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

class SyncControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID DEVICE_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID ACTOR_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

    @BeforeEach
    void cleanDb() {
        jdbc.execute("DELETE FROM actor_tokens");
        jdbc.execute("DELETE FROM subject_locations");
        jdbc.execute("DELETE FROM events");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        provisionTestToken();
    }

    /**
     * Quality gate: Push 10 events via curl → all persisted with watermarks assigned
     */
    @Test
    void push_10events_allPersistedWithWatermarks() {
        List<Map<String, Object>> events = buildEvents(10);

        ResponseEntity<JsonNode> response = pushEvents(events);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("accepted").asInt()).isEqualTo(10);
        assertThat(response.getBody().get("duplicates").asInt()).isEqualTo(0);

        // Verify watermarks assigned
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE sync_watermark > 0", Integer.class);
        assertThat(count).isEqualTo(10);
    }

    /**
     * Quality gate: Push same 10 events again → zero duplicates, same response
     */
    @Test
    void push_duplicateEvents_zeroDuplicates() {
        List<Map<String, Object>> events = buildEvents(10);

        pushEvents(events);
        ResponseEntity<JsonNode> response = pushEvents(events);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("accepted").asInt()).isEqualTo(0);
        assertThat(response.getBody().get("duplicates").asInt()).isEqualTo(10);

        // Still only 10 events in DB
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM events WHERE sync_watermark > 0", Integer.class);
        assertThat(count).isEqualTo(10);
    }

    /**
     * Quality gate: Pull with watermark 0 → all events returned, ordered
     */
    @Test
    void pull_fromZero_allEventsReturned() {
        pushEvents(buildEvents(5));

        ResponseEntity<JsonNode> response = pullEvents(0, 100);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();
        assertThat(body.get("events").size()).isEqualTo(5);
        assertThat(body.get("latest_watermark").asLong()).isGreaterThan(0);

        // Verify ordering by watermark
        long prevWatermark = 0;
        for (JsonNode event : body.get("events")) {
            long wm = event.get("sync_watermark").asLong();
            assertThat(wm).isGreaterThan(prevWatermark);
            prevWatermark = wm;
        }
    }

    /**
     * Quality gate: Pull with watermark N → only events after N returned
     */
    @Test
    void pull_fromWatermarkN_onlyNewerEvents() {
        pushEvents(buildEvents(5));

        // Pull all to get watermarks
        ResponseEntity<JsonNode> firstPull = pullEvents(0, 100);
        JsonNode allEvents = firstPull.getBody().get("events");
        long midWatermark = allEvents.get(2).get("sync_watermark").asLong();

        // Push 3 more
        pushEvents(buildEvents(3, 6));

        // Pull from midpoint
        ResponseEntity<JsonNode> response = pullEvents(midWatermark, 100);

        JsonNode events = response.getBody().get("events");
        // Should get events 4,5 from first batch + 3 from second = 5
        assertThat(events.size()).isEqualTo(5);
        for (JsonNode event : events) {
            assertThat(event.get("sync_watermark").asLong()).isGreaterThan(midWatermark);
        }
    }

    /**
     * Quality gate: Push with malformed envelope → 400 error, nothing persisted
     */
    @Test
    void push_malformedEnvelope_400Error() {
        Map<String, Object> badEvent = new HashMap<>();
        badEvent.put("id", UUID.randomUUID().toString());
        // Missing required fields: type, shape_ref, subject_ref, etc.

        ResponseEntity<JsonNode> response = pushEvents(List.of(badEvent));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error").asText()).isEqualTo("validation_failed");

        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM events WHERE sync_watermark > 0", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    /**
     * Quality gate: Push with empty batch → 400 error
     */
    @Test
    void push_emptyBatch_400Error() {
        Map<String, Object> request = Map.of("events", List.of());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<JsonNode> response = rest.exchange(
                "/api/sync/push", HttpMethod.POST, entity, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error").asText()).isEqualTo("empty_batch");
    }

    /**
     * Quality gate: JSON Schema validation — every accepted event validates against envelope.schema.json
     */
    @Test
    void push_validEvent_conformsToEnvelopeSchema() {
        List<Map<String, Object>> events = buildEvents(1);

        ResponseEntity<JsonNode> pushResponse = pushEvents(events);
        assertThat(pushResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pushResponse.getBody().get("accepted").asInt()).isEqualTo(1);

        // Pull it back and verify all 11 fields present
        ResponseEntity<JsonNode> pullResponse = pullEvents(0, 1);
        JsonNode pulledEvent = pullResponse.getBody().get("events").get(0);

        assertThat(pulledEvent.has("id")).isTrue();
        assertThat(pulledEvent.has("type")).isTrue();
        assertThat(pulledEvent.has("shape_ref")).isTrue();
        assertThat(pulledEvent.has("subject_ref")).isTrue();
        assertThat(pulledEvent.get("subject_ref").has("type")).isTrue();
        assertThat(pulledEvent.get("subject_ref").has("id")).isTrue();
        assertThat(pulledEvent.has("actor_ref")).isTrue();
        assertThat(pulledEvent.get("actor_ref").has("type")).isTrue();
        assertThat(pulledEvent.get("actor_ref").has("id")).isTrue();
        assertThat(pulledEvent.has("device_id")).isTrue();
        assertThat(pulledEvent.has("device_seq")).isTrue();
        assertThat(pulledEvent.has("sync_watermark")).isTrue();
        assertThat(pulledEvent.has("timestamp")).isTrue();
        assertThat(pulledEvent.has("payload")).isTrue();
    }

    /**
     * Pull pagination works correctly.
     */
    @Test
    void pull_pagination_respectsLimit() {
        pushEvents(buildEvents(10));

        ResponseEntity<JsonNode> response = pullEvents(0, 3);
        assertThat(response.getBody().get("events").size()).isEqualTo(3);

        long latest = response.getBody().get("latest_watermark").asLong();
        ResponseEntity<JsonNode> page2 = pullEvents(latest, 3);
        assertThat(page2.getBody().get("events").size()).isEqualTo(3);
    }

    @Test
    void s00StructuredCaptureCorrectionIsAppendOnlyIdempotentAndFlagsConcurrentAnomaly() {
        UUID subjectId = UUID.randomUUID();
        Map<String, Object> original = buildEvent(subjectId, DEVICE_ID, 1,
                Map.of(
                        "name", "Site Alpha",
                        "category", "urban",
                        "notes", "Initial structured capture",
                        "date", "2026-06-03",
                        "value", 10
                ));

        ResponseEntity<JsonNode> originalResponse =
                pushEventsWithMeta(List.of(original), DEVICE_ID, 0);
        assertThat(originalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(originalResponse.getBody().get("accepted").asInt()).isEqualTo(1);
        assertThat(originalResponse.getBody().get("duplicates").asInt()).isZero();
        assertThat(originalResponse.getBody().get("flags_raised").asInt()).isZero();

        String originalPayloadBefore = storedPayload(original);
        long originalWatermark = syncWatermark(original);

        Map<String, Object> correction = buildEvent(subjectId, DEVICE_ID, 2,
                Map.of(
                        "name", "Site Alpha",
                        "category", "urban",
                        "notes", "Corrected value from offline amendment",
                        "date", "2026-06-03",
                        "value", 11
                ));

        ResponseEntity<JsonNode> correctionResponse =
                pushEventsWithMeta(List.of(correction), DEVICE_ID, originalWatermark);
        assertThat(correctionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(correctionResponse.getBody().get("accepted").asInt()).isEqualTo(1);
        assertThat(eventCount(original)).isEqualTo(1);
        assertThat(eventCount(correction)).isEqualTo(1);
        assertThat(storedPayload(original)).isEqualTo(originalPayloadBefore);

        ResponseEntity<JsonNode> duplicateCorrectionResponse =
                pushEventsWithMeta(List.of(correction), DEVICE_ID, originalWatermark);
        assertThat(duplicateCorrectionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(duplicateCorrectionResponse.getBody().get("accepted").asInt()).isZero();
        assertThat(duplicateCorrectionResponse.getBody().get("duplicates").asInt()).isEqualTo(1);
        assertThat(duplicateCorrectionResponse.getBody().get("flags_raised").asInt()).isZero();
        assertThat(eventCount(correction)).isEqualTo(1);

        UUID otherDevice = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
        Map<String, Object> staleConcurrent = buildEvent(subjectId, otherDevice, 1,
                Map.of(
                        "name", "Site Alpha",
                        "category", "urban",
                        "notes", "Concurrent offline duplicate observation",
                        "date", "2026-06-03",
                        "value", 12
                ));

        ResponseEntity<JsonNode> concurrentResponse =
                pushEventsWithMeta(List.of(staleConcurrent), otherDevice, originalWatermark);
        assertThat(concurrentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(concurrentResponse.getBody().get("accepted").asInt()).isEqualTo(1);
        assertThat(concurrentResponse.getBody().get("flags_raised").asInt()).isEqualTo(1);
        assertThat(flagSources("concurrent_state_change"))
                .contains(staleConcurrent.get("id").toString());

        ResponseEntity<JsonNode> pullResponse = pullEvents(0, 100);
        assertThat(pullResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode pulledEvents = pullResponse.getBody().get("events");
        assertThat(eventIds(pulledEvents))
                .contains(original.get("id").toString(), correction.get("id").toString());
        assertThat(syncWatermarks(pulledEvents)).isSorted();

        JsonNode pulledOriginal = eventById(pulledEvents, original.get("id").toString());
        JsonNode pulledCorrection = eventById(pulledEvents, correction.get("id").toString());
        assertThat(pulledOriginal.get("type").asText()).isEqualTo("capture");
        assertThat(pulledOriginal.get("shape_ref").asText()).isEqualTo("basic_capture/v1");
        assertThat(pulledOriginal.get("activity_ref").asText()).isEqualTo("site_survey");
        assertThat(pulledOriginal.get("payload").get("notes").asText())
                .isEqualTo("Initial structured capture");
        assertThat(pulledCorrection.get("payload").get("notes").asText())
                .isEqualTo("Corrected value from offline amendment");
        assertThat(pulledCorrection.get("sync_watermark").asLong())
                .isGreaterThan(pulledOriginal.get("sync_watermark").asLong());
        assertEnvelopeFieldsPresent(pulledOriginal);
        assertEnvelopeFieldsPresent(pulledCorrection);
    }

    // --- Helpers ---

    private List<Map<String, Object>> buildEvents(int count) {
        return buildEvents(count, 1);
    }

    private List<Map<String, Object>> buildEvents(int count, int startSeq) {
        UUID subjectId = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
        List<Map<String, Object>> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> payload = Map.of(
                    "name", "Site " + (startSeq + i),
                    "category", "urban",
                    "notes", "Test event " + (startSeq + i),
                    "date", "2026-04-16",
                    "value", (startSeq + i) * 10
            );
            events.add(buildEvent(subjectId, DEVICE_ID, startSeq + i, null, payload));
        }
        return events;
    }

    private Map<String, Object> buildEvent(UUID subjectId, UUID deviceId, int seq,
                                           Map<String, Object> payload) {
        return buildEvent(subjectId, deviceId, seq, "site_survey", payload);
    }

    private Map<String, Object> buildEvent(UUID subjectId, UUID deviceId, int seq,
                                           String activityRef, Map<String, Object> payload) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "capture");
        event.put("shape_ref", "basic_capture/v1");
        event.put("activity_ref", activityRef);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", ACTOR_ID.toString()));
        event.put("device_id", deviceId.toString());
        event.put("device_seq", seq);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", payload);
        return event;
    }

    private ResponseEntity<JsonNode> pushEvents(List<?> events) {
        Map<String, Object> request = Map.of("events", events);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return rest.exchange("/api/sync/push", HttpMethod.POST, entity, JsonNode.class);
    }

    private ResponseEntity<JsonNode> pushEventsWithMeta(List<?> events, UUID deviceId,
                                                        long lastPullWatermark) {
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
        HttpHeaders headers = authHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return rest.exchange("/api/sync/pull", HttpMethod.POST, entity, JsonNode.class);
    }

    private String storedPayload(Map<String, Object> event) {
        return jdbc.queryForObject("""
                SELECT payload::text
                FROM events
                WHERE id = ?::uuid
                """, String.class, event.get("id").toString());
    }

    private long syncWatermark(Map<String, Object> event) {
        return jdbc.queryForObject("""
                SELECT sync_watermark
                FROM events
                WHERE id = ?::uuid
                """, Long.class, event.get("id").toString());
    }

    private int eventCount(Map<String, Object> event) {
        return jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE id = ?::uuid
                """, Integer.class, event.get("id").toString());
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

    private List<String> eventIds(JsonNode events) {
        List<String> ids = new ArrayList<>();
        for (JsonNode event : events) {
            ids.add(event.get("id").asText());
        }
        return ids;
    }

    private List<Long> syncWatermarks(JsonNode events) {
        List<Long> watermarks = new ArrayList<>();
        for (JsonNode event : events) {
            watermarks.add(event.get("sync_watermark").asLong());
        }
        return watermarks;
    }

    private JsonNode eventById(JsonNode events, String id) {
        for (JsonNode event : events) {
            if (id.equals(event.get("id").asText())) {
                return event;
            }
        }
        throw new AssertionError("event not found: " + id);
    }

    private void assertEnvelopeFieldsPresent(JsonNode event) {
        assertThat(event.has("id")).isTrue();
        assertThat(event.has("type")).isTrue();
        assertThat(event.has("shape_ref")).isTrue();
        assertThat(event.has("activity_ref")).isTrue();
        assertThat(event.has("subject_ref")).isTrue();
        assertThat(event.has("actor_ref")).isTrue();
        assertThat(event.has("device_id")).isTrue();
        assertThat(event.has("device_seq")).isTrue();
        assertThat(event.has("sync_watermark")).isTrue();
        assertThat(event.has("timestamp")).isTrue();
        assertThat(event.has("payload")).isTrue();
    }
}
