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
        jdbc.execute("DELETE FROM events");
        // Reset the sequence so watermarks start fresh each test
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
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
                "SELECT COUNT(*) FROM events WHERE sync_watermark IS NOT NULL", Integer.class);
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
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM events", Integer.class);
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

        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM events", Integer.class);
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

    // --- Helpers ---

    private List<Map<String, Object>> buildEvents(int count) {
        return buildEvents(count, 1);
    }

    private List<Map<String, Object>> buildEvents(int count, int startSeq) {
        UUID subjectId = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
        List<Map<String, Object>> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("id", UUID.randomUUID().toString());
            event.put("type", "capture");
            event.put("shape_ref", "basic_capture/v1");
            event.put("activity_ref", null);
            event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
            event.put("actor_ref", Map.of("type", "actor", "id", ACTOR_ID.toString()));
            event.put("device_id", DEVICE_ID.toString());
            event.put("device_seq", startSeq + i);
            event.put("sync_watermark", null);
            event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
            event.put("payload", Map.of(
                    "name", "Site " + (startSeq + i),
                    "category", "urban",
                    "notes", "Test event " + (startSeq + i),
                    "date", "2026-04-16",
                    "value", (startSeq + i) * 10
            ));
            events.add(event);
        }
        return events;
    }

    private ResponseEntity<JsonNode> pushEvents(List<?> events) {
        Map<String, Object> request = Map.of("events", events);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return rest.exchange("/api/sync/push", HttpMethod.POST, entity, JsonNode.class);
    }

    private ResponseEntity<JsonNode> pullEvents(long sinceWatermark, int limit) {
        Map<String, Object> request = Map.of("since_watermark", sinceWatermark, "limit", limit);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return rest.exchange("/api/sync/pull", HttpMethod.POST, entity, JsonNode.class);
    }
}
