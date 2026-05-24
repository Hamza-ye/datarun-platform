package dev.datarun.server.sync;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.authorization.ActorTokenRepository;
import dev.datarun.server.authorization.AssignmentService;
import dev.datarun.server.event.Event;
import dev.datarun.server.identity.IdentityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SubjectHistoryBackfillIntegrationTest extends AbstractIntegrationTest {

    private static final UUID ADMIN = TEST_ACTOR_ID;
    private static final UUID ACTOR = UUID.fromString("aaaa1000-0000-0000-0000-000000000001");
    private static final UUID OTHER_ACTOR = UUID.fromString("bbbb1000-0000-0000-0000-000000000002");
    private static final UUID DEVICE = UUID.fromString("dddd1000-0000-0000-0000-000000000001");
    private static final UUID BACKFILL_DEVICE = UUID.fromString("eeee1000-0000-0000-0000-000000000001");
    private static final String ACTIVITY = "case_activity";
    private static final String OTHER_ACTIVITY = "other_activity";

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private ActorTokenRepository actorTokenRepository;

    @Autowired
    private IdentityService identityService;

    private String token;
    private int deviceSeq;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("DELETE FROM actor_tokens");
        jdbcTemplate.execute("DELETE FROM subject_locations");
        jdbcTemplate.execute("DELETE FROM subject_aliases");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM device_sync_state");
        provisionTestToken();
        token = actorTokenRepository.createToken(ACTOR);
        deviceSeq = 1;
    }

    @Test
    void subjectListAssignment_backfillReturnsPriorTimeline_normalPullDoesNot() {
        UUID subject = UUID.randomUUID();
        UUID oldEventId = pushCapture(subject, ACTIVITY, "historical opening");
        long oldWatermark = watermark(oldEventId);

        assignmentService.createAssignment(ADMIN, ACTOR, "field_worker",
                null, List.of(subject), List.of(ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        ResponseEntity<JsonNode> normalPull = pull(token, oldWatermark, 100);
        assertThat(normalPull.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureSubjectIds(normalPull.getBody().get("events")))
                .doesNotContain(subject.toString());

        ResponseEntity<JsonNode> backfill = subjectHistory(token, subject, ACTIVITY, 0, 100);
        assertThat(backfill.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureSubjectIds(backfill.getBody().get("events")))
                .containsExactly(subject.toString());
        assertThat(backfill.getBody().get("next_cursor").asLong()).isGreaterThanOrEqualTo(oldWatermark);
    }

    @Test
    void subjectHistoryBackfill_doesNotUpdateNormalDeviceWatermark() {
        UUID subject = UUID.randomUUID();
        UUID oldEventId = pushCapture(subject, ACTIVITY, "historical opening");
        long oldWatermark = watermark(oldEventId);

        assignmentService.createAssignment(ADMIN, ACTOR, "field_worker",
                null, List.of(subject), List.of(ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        ResponseEntity<JsonNode> normalPull = pull(token, oldWatermark, 100, BACKFILL_DEVICE);
        long deviceWatermarkBefore = deviceWatermark(BACKFILL_DEVICE);
        assertThat(deviceWatermarkBefore).isEqualTo(normalPull.getBody().get("latest_watermark").asLong());

        ResponseEntity<JsonNode> backfill = subjectHistory(token, subject, ACTIVITY, 0, 100);
        assertThat(backfill.getStatusCode()).isEqualTo(HttpStatus.OK);

        long deviceWatermarkAfter = deviceWatermark(BACKFILL_DEVICE);
        assertThat(deviceWatermarkAfter).isEqualTo(deviceWatermarkBefore);
    }

    @Test
    void subjectHistoryBackfill_cursorRetriesAreIdempotentAndAuthorizationIsPerPage() {
        UUID subject = UUID.randomUUID();
        UUID first = pushCapture(subject, ACTIVITY, "page 1");
        pushCapture(subject, ACTIVITY, "page 2");

        Event assignment = assignmentService.createAssignment(ADMIN, ACTOR, "field_worker",
                null, List.of(subject), List.of(ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        UUID assignmentId = UUID.fromString(assignment.subjectRef().get("id").asText());

        ResponseEntity<JsonNode> firstPage = subjectHistory(token, subject, ACTIVITY, 0, 1);
        ResponseEntity<JsonNode> retryFirstPage = subjectHistory(token, subject, ACTIVITY, 0, 1);
        assertThat(firstPage.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(retryFirstPage.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eventIds(retryFirstPage.getBody().get("events")))
                .isEqualTo(eventIds(firstPage.getBody().get("events")));
        assertThat(firstPage.getBody().get("events").get(0).get("id").asText())
                .isEqualTo(first.toString());

        assignmentService.endAssignment(assignmentId, ADMIN, "revoked before next backfill page");

        long nextCursor = firstPage.getBody().get("next_cursor").asLong();
        ResponseEntity<JsonNode> secondPage = subjectHistory(token, subject, ACTIVITY, nextCursor, 1);
        assertThat(secondPage.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(secondPage.getBody().get("error").asText())
                .isEqualTo("subject_history_not_authorized");
    }

    @Test
    void subjectHistoryBackfill_mergeAliasHistoryIsIncludedForSurvivingAndRetiredReads() {
        UUID retired = UUID.randomUUID();
        UUID surviving = UUID.randomUUID();
        pushCapture(retired, ACTIVITY, "retired history");
        pushCapture(surviving, ACTIVITY, "surviving history");

        identityService.merge(retired, surviving, ADMIN, "duplicate subject");
        assignmentService.createAssignment(ADMIN, ACTOR, "field_worker",
                null, List.of(surviving), List.of(ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        ResponseEntity<JsonNode> survivingRead = subjectHistory(token, surviving, ACTIVITY, 0, 100);
        assertThat(survivingRead.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureSubjectIds(survivingRead.getBody().get("events")))
                .containsExactlyInAnyOrder(retired.toString(), surviving.toString());

        ResponseEntity<JsonNode> retiredRead = subjectHistory(token, retired, ACTIVITY, 0, 100);
        assertThat(retiredRead.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(retiredRead.getBody().get("subject_id").asText()).isEqualTo(surviving.toString());
        assertThat(captureSubjectIds(retiredRead.getBody().get("events")))
                .containsExactlyInAnyOrder(retired.toString(), surviving.toString());
    }

    @Test
    void subjectHistoryBackfill_splitSuccessorDoesNotInheritSourceHistory_sourceKeepsOwnHistory() {
        UUID source = UUID.randomUUID();
        pushCapture(source, ACTIVITY, "source history");

        Event split = identityService.split(source, ADMIN, "split subject");
        UUID successor = UUID.fromString(split.payload().get("successor_id").asText());
        pushCapture(successor, ACTIVITY, "successor history");

        assignmentService.createAssignment(ADMIN, ACTOR, "field_worker",
                null, List.of(source), List.of(ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, ACTOR, "field_worker",
                null, List.of(successor), List.of(ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        ResponseEntity<JsonNode> sourceRead = subjectHistory(token, source, ACTIVITY, 0, 100);
        assertThat(sourceRead.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureSubjectIds(sourceRead.getBody().get("events")))
                .containsExactly(source.toString());

        ResponseEntity<JsonNode> successorRead = subjectHistory(token, successor, ACTIVITY, 0, 100);
        assertThat(successorRead.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureSubjectIds(successorRead.getBody().get("events")))
                .containsExactly(successor.toString());
    }

    @Test
    void subjectHistoryBackfill_doesNotExposeUnrelatedSubjectsActivitiesOrAuditWideAssignments() {
        UUID subject = UUID.randomUUID();
        UUID otherSubject = UUID.randomUUID();
        pushCapture(subject, ACTIVITY, "in scope");
        pushCapture(subject, OTHER_ACTIVITY, "wrong activity");
        pushCapture(otherSubject, ACTIVITY, "wrong subject");

        assignmentService.createAssignment(ADMIN, ACTOR, "field_worker",
                null, List.of(subject), List.of(ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, OTHER_ACTOR, "field_worker",
                null, List.of(otherSubject), List.of(ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        ResponseEntity<JsonNode> backfill = subjectHistory(token, subject, ACTIVITY, 0, 100);
        assertThat(backfill.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode events = backfill.getBody().get("events");

        assertThat(captureSubjectIds(events)).containsExactly(subject.toString());
        for (JsonNode event : events) {
            if ("basic_capture/v1".equals(event.get("shape_ref").asText())) {
                assertThat(event.get("activity_ref").asText()).isEqualTo(ACTIVITY);
                assertThat(event.get("subject_ref").get("id").asText()).isEqualTo(subject.toString());
            }
            if ("assignment_created/v1".equals(event.get("shape_ref").asText())) {
                assertThat(event.get("payload").get("target_actor").get("id").asText())
                        .isNotEqualTo(OTHER_ACTOR.toString());
            }
        }

        ResponseEntity<JsonNode> unrelatedSubjectRead = subjectHistory(token, otherSubject, ACTIVITY, 0, 100);
        assertThat(unrelatedSubjectRead.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<JsonNode> unrelatedActivityRead = subjectHistory(token, subject, OTHER_ACTIVITY, 0, 100);
        assertThat(unrelatedActivityRead.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private UUID pushCapture(UUID subjectId, String activityRef, String notes) {
        UUID eventId = UUID.randomUUID();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", eventId.toString());
        event.put("type", "capture");
        event.put("shape_ref", "basic_capture/v1");
        event.put("activity_ref", activityRef);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", ADMIN.toString()));
        event.put("device_id", DEVICE.toString());
        event.put("device_seq", deviceSeq++);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", Map.of(
                "name", "Subject " + subjectId,
                "category", "test",
                "notes", notes,
                "date", "2026-05-24",
                "value", deviceSeq
        ));

        ResponseEntity<JsonNode> response = rest.exchange("/api/sync/push",
                HttpMethod.POST, new HttpEntity<>(Map.of("events", List.of(event)), jsonHeaders()),
                JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return eventId;
    }

    private ResponseEntity<JsonNode> pull(String actorToken, long sinceWatermark, int limit) {
        return pull(actorToken, sinceWatermark, limit, null);
    }

    private ResponseEntity<JsonNode> pull(String actorToken, long sinceWatermark, int limit, UUID deviceId) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("since_watermark", sinceWatermark);
        request.put("limit", limit);
        if (deviceId != null) {
            request.put("device_id", deviceId.toString());
        }
        return rest.exchange("/api/sync/pull", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(actorToken)), JsonNode.class);
    }

    private ResponseEntity<JsonNode> subjectHistory(String actorToken, UUID subjectId,
                                                    String activityRef, long cursor, int limit) {
        Map<String, Object> request = Map.of(
                "subject_id", subjectId.toString(),
                "activity_ref", activityRef,
                "cursor", cursor,
                "limit", limit
        );
        return rest.exchange("/api/sync/subject-history", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(actorToken)), JsonNode.class);
    }

    private long watermark(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT sync_watermark FROM events WHERE id = ?::uuid",
                Long.class,
                eventId.toString());
    }

    private long deviceWatermark(UUID deviceId) {
        return jdbcTemplate.queryForObject("""
                SELECT last_pull_watermark
                FROM device_sync_state
                WHERE device_id = ?::uuid
                """, Long.class, deviceId.toString());
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders authHeaders(String actorToken) {
        HttpHeaders headers = jsonHeaders();
        headers.set("Authorization", "Bearer " + actorToken);
        return headers;
    }

    private List<String> captureSubjectIds(JsonNode events) {
        List<String> subjects = new ArrayList<>();
        for (JsonNode event : events) {
            if ("basic_capture/v1".equals(event.get("shape_ref").asText())) {
                subjects.add(event.get("subject_ref").get("id").asText());
            }
        }
        return subjects;
    }

    private List<String> eventIds(JsonNode events) {
        List<String> ids = new ArrayList<>();
        for (JsonNode event : events) {
            ids.add(event.get("id").asText());
        }
        return ids;
    }
}
