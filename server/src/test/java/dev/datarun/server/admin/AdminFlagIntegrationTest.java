package dev.datarun.server.admin;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datarun.server.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Admin UI integration tests — flag list, flag detail, resolution form submission.
 */
class AdminFlagIntegrationTest extends AbstractIntegrationTest {

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
        jdbc.execute("DELETE FROM events");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbc.execute("DELETE FROM device_sync_state");
        jdbc.execute("DELETE FROM subject_aliases");
        jdbc.execute("DELETE FROM subject_lifecycle");
    }

    @Test
    void subjectList_showsFlagBadge() {
        createConcurrentConflict();

        var response = rest.getForEntity("/admin", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        // Flag count badge should be present
        assertThat(body).contains("Flags");
        // Subject should be listed
        assertThat(body).contains(SUBJECT_X.toString().substring(0, 8));
    }

    @Test
    void flagList_showsUnresolvedFlags() {
        createConcurrentConflict();

        var response = rest.getForEntity("/admin/flags", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).contains("concurrent_state_change");
        assertThat(body).contains("Review");
    }

    @Test
    void flagDetail_showsFlagAndSourceEvent() {
        createConcurrentConflict();
        UUID flagId = findFlagEventId();

        var response = rest.getForEntity("/admin/flags/" + flagId, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).contains("concurrent_state_change");
        assertThat(body).contains("capture");
        assertThat(body).contains("Resolve Flag");
    }

    @Test
    void flagDetail_nonExistent_redirectsToList() {
        var response = rest.getForEntity("/admin/flags/" + UUID.randomUUID(), String.class);
        // TestRestTemplate follows redirects, so we end up at flag-list
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Unresolved Flags");
    }

    @Test
    void resolveViaForm_acceptedResolution_redirectsToList() {
        createConcurrentConflict();
        UUID flagId = findFlagEventId();

        // POST form data (like a browser form submission)
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("resolution", "accepted");
        formData.add("reason", "Verified correct by supervisor");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

        var response = rest.postForEntity(
                "/admin/flags/" + flagId + "/resolve", entity, String.class);
        // Should redirect to flag list
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation().getPath()).startsWith("/admin/flags");

        // Verify conflict_resolved event was created
        Integer resolvedCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE type = 'conflict_resolved'", Integer.class);
        assertThat(resolvedCount).isEqualTo(1);

        // Flag list should now be empty
        var listResponse = rest.getForEntity("/admin/flags", String.class);
        assertThat(listResponse.getBody()).contains("All clear");
    }

    @Test
    void subjectDetail_showsFlaggedIndicator() {
        createConcurrentConflict();

        var response = rest.getForEntity("/admin/subjects/" + SUBJECT_X, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("FLAGGED");
    }

    // --- Helpers ---

    private void createConcurrentConflict() {
        var eventA = buildEvent(SUBJECT_X, DEVICE_A, 1);
        pushEvents(List.of(eventA), DEVICE_A, 0);
        var eventB = buildEvent(SUBJECT_X, DEVICE_B, 1);
        pushEvents(List.of(eventB), DEVICE_B, 0);
    }

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

    private UUID findFlagEventId() {
        return jdbc.queryForObject(
                "SELECT id FROM events WHERE type = 'conflict_detected' ORDER BY sync_watermark LIMIT 1",
                UUID.class);
    }
}
