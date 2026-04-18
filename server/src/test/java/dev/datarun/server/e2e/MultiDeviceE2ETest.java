package dev.datarun.server.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.subject.SubjectProjection;
import dev.datarun.server.subject.SubjectSummary;
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
 * End-to-end multi-device scenario:
 * Two devices push concurrent captures for the same subject →
 * conflict auto-detected → admin resolves → projection shows correct state.
 */
class MultiDeviceE2ETest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate rest;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SubjectProjection subjectProjection;

    private static final UUID DEVICE_A = UUID.fromString("aaaa0000-0000-0000-0000-000000000001");
    private static final UUID DEVICE_B = UUID.fromString("bbbb0000-0000-0000-0000-000000000002");
    private static final UUID ACTOR_ID = UUID.fromString("cccc0000-0000-0000-0000-000000000001");
    private static final String SUBJECT_ID = "dddd0000-0000-0000-0000-000000000001";

    @BeforeEach
    void cleanDb() {
        jdbc.execute("DELETE FROM events");
        jdbc.execute("DELETE FROM subject_aliases");
        jdbc.execute("DELETE FROM subject_lifecycle");
        jdbc.execute("DELETE FROM device_sync_state");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
    }

    @Test
    void multiDevice_concurrentCapture_flagRaised_resolved_projectionCorrect() {
        // --- Step 1: Device A pushes 2 captures for subject ---
        var eventA1 = buildCapture(UUID.randomUUID().toString(), SUBJECT_ID, DEVICE_A, 1, "2026-04-18T09:00:00Z");
        var eventA2 = buildCapture(UUID.randomUUID().toString(), SUBJECT_ID, DEVICE_A, 2, "2026-04-18T09:05:00Z");
        var pushA = pushEvents(DEVICE_A, 0L, List.of(eventA1, eventA2));
        assertThat(pushA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pushA.getBody().get("accepted").asInt()).isEqualTo(2);

        // --- Step 2: Device B pushes 1 capture for same subject (last_pull_watermark=0) ---
        var eventB1 = buildCapture(UUID.randomUUID().toString(), SUBJECT_ID, DEVICE_B, 1, "2026-04-18T09:10:00Z");
        var pushB = pushEvents(DEVICE_B, 0L, List.of(eventB1));
        assertThat(pushB.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pushB.getBody().get("accepted").asInt()).isEqualTo(1);

        // --- Step 3: Verify conflict flag raised ---
        var flagsResponse = rest.getForEntity("/api/conflicts", JsonNode.class);
        assertThat(flagsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode flags = flagsResponse.getBody().get("flags");
        assertThat(flags.size()).as("Exactly one flag should be raised").isEqualTo(1);

        String flagId = flags.get(0).get("flag_id").asText();
        assertThat(flagId).isNotBlank();

        // --- Step 4: PE should show flag (1 subject, 1 flag, only 2 events counted) ---
        List<SubjectSummary> beforeResolve = subjectProjection.listSubjects();
        assertThat(beforeResolve).hasSize(1);
        assertThat(beforeResolve.get(0).flagCount()).isEqualTo(1);
        // Flagged event excluded from count: 2 unflagged captures
        assertThat(beforeResolve.get(0).eventCount()).isEqualTo(2);

        // --- Step 5: Admin resolves flag as accepted ---
        Map<String, Object> resolveBody = Map.of(
                "resolution", "accepted",
                "actor_id", ACTOR_ID.toString(),
                "reason", "Verified correct by supervisor"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var resolveResponse = rest.exchange(
                "/api/conflicts/" + flagId + "/resolve",
                HttpMethod.POST,
                new HttpEntity<>(resolveBody, headers),
                JsonNode.class);
        assertThat(resolveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resolveResponse.getBody().get("resolution").asText()).isEqualTo("accepted");

        // --- Step 6: PE should show resolved state (all 3 captures, 0 flags) ---
        List<SubjectSummary> afterResolve = subjectProjection.listSubjects();
        assertThat(afterResolve).hasSize(1);
        assertThat(afterResolve.get(0).id()).isEqualTo(SUBJECT_ID);
        assertThat(afterResolve.get(0).eventCount()).isEqualTo(3);
        assertThat(afterResolve.get(0).flagCount()).isEqualTo(0);

        // --- Step 7: No unresolved flags remain ---
        var flagsAfter = rest.getForEntity("/api/conflicts", JsonNode.class);
        assertThat(flagsAfter.getBody().get("flags").size()).isEqualTo(0);

        // --- Step 8: Pull from watermark 0 returns all events including system events ---
        var pullResponse = pullEvents(0, 100);
        assertThat(pullResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode pulledEvents = pullResponse.getBody().get("events");
        // 2 from A + 1 from B + 1 conflict_detected + 1 conflict_resolved = 5
        assertThat(pulledEvents.size()).isEqualTo(5);
    }

    // --- Helpers ---

    private Map<String, Object> buildCapture(String id, String subjectId,
                                              UUID deviceId, int seq, String timestamp) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", id);
        event.put("type", "capture");
        event.put("shape_ref", "basic_capture/v1");
        event.put("activity_ref", null);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId));
        event.put("actor_ref", Map.of("type", "actor", "id", ACTOR_ID.toString()));
        event.put("device_id", deviceId.toString());
        event.put("device_seq", seq);
        event.put("sync_watermark", null);
        event.put("timestamp", timestamp);
        event.put("payload", Map.of(
                "name", "Subject-" + id,
                "category", "test",
                "notes", "E2E test event " + id
        ));
        return event;
    }

    private ResponseEntity<JsonNode> pushEvents(UUID deviceId, long lastPullWatermark,
                                                 List<?> events) {
        Map<String, Object> request = Map.of(
                "events", events,
                "device_id", deviceId.toString(),
                "last_pull_watermark", lastPullWatermark
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange("/api/sync/push", HttpMethod.POST,
                new HttpEntity<>(request, headers), JsonNode.class);
    }

    private ResponseEntity<JsonNode> pullEvents(long sinceWatermark, int limit) {
        Map<String, Object> request = Map.of("since_watermark", sinceWatermark, "limit", limit);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange("/api/sync/pull", HttpMethod.POST,
                new HttpEntity<>(request, headers), JsonNode.class);
    }
}
