package dev.datarun.server.subject;

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

class SubjectControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    private static final UUID DEVICE_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID ACTOR_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID SUBJECT_1 = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
    private static final UUID SUBJECT_2 = UUID.fromString("8d0f7780-8536-51ef-a55c-f18fd2e4a1f8");

    @BeforeEach
    void cleanDb() {
        jdbc.execute("DELETE FROM events");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
    }

    @Test
    void listSubjects_returnsAllSubjectsWithSummary() {
        pushEvent(SUBJECT_1, 1, "capture");
        pushEvent(SUBJECT_1, 2, "capture");
        pushEvent(SUBJECT_2, 3, "capture");

        ResponseEntity<JsonNode> response = rest.getForEntity("/api/subjects", JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode subjects = response.getBody().get("subjects");
        assertThat(subjects.size()).isEqualTo(2);

        // Find subject 1
        boolean foundSubject1 = false;
        for (JsonNode s : subjects) {
            if (s.get("id").asText().equals(SUBJECT_1.toString())) {
                assertThat(s.get("event_count").asInt()).isEqualTo(2);
                assertThat(s.get("latest_event_type").asText()).isEqualTo("capture");
                foundSubject1 = true;
            }
        }
        assertThat(foundSubject1).isTrue();
    }

    @Test
    void getSubjectEvents_returnsTimelineOrdered() {
        pushEvent(SUBJECT_1, 1, "capture");
        pushEvent(SUBJECT_1, 2, "capture");

        ResponseEntity<JsonNode> response = rest.getForEntity(
                "/api/subjects/" + SUBJECT_1 + "/events", JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode events = response.getBody().get("events");
        assertThat(events.size()).isEqualTo(2);

        // Ordered by watermark
        assertThat(events.get(0).get("sync_watermark").asLong())
                .isLessThan(events.get(1).get("sync_watermark").asLong());
    }

    @Test
    void getSubjectEvents_unknownSubject_returns404() {
        ResponseEntity<JsonNode> response = rest.getForEntity(
                "/api/subjects/" + UUID.randomUUID() + "/events", JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("error").asText()).isEqualTo("subject_not_found");
    }

    private void pushEvent(UUID subjectId, int seq, String type) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", type);
        event.put("shape_ref", "basic_capture/v1");
        event.put("activity_ref", null);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", ACTOR_ID.toString()));
        event.put("device_id", DEVICE_ID.toString());
        event.put("device_seq", seq);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", Map.of("name", "Test", "category", "urban", "notes", "n", "date", "2026-04-16", "value", 1));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of("events", List.of(event));
        rest.exchange("/api/sync/push", HttpMethod.POST,
                new HttpEntity<>(body, headers), JsonNode.class);
    }
}
