package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.authorization.ActorTokenRepository;
import dev.datarun.server.config.ShapeRepository;
import dev.datarun.server.config.ShapeService;
import dev.datarun.server.projection.PatternStateProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransitionViolationIntegrationTest extends AbstractIntegrationTest {

    private static final UUID WORKER = UUID.fromString("aaaa2000-0000-0000-0000-000000000001");
    private static final UUID OTHER_ACTOR = UUID.fromString("bbbb2000-0000-0000-0000-000000000002");
    private static final UUID DEVICE = UUID.fromString("dddd2000-0000-0000-0000-000000000001");
    private static final String ACTIVITY = "case_activity";
    private static final String REVIEWABLE_ACTIVITY = "reviewable_case_activity";
    private static final String NO_PATTERN_ACTIVITY = "no_pattern_activity";
    private static final String UNIQUE_ACTIVITY = "unique_case_activity";

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatternStateProjection patternStateProjection;

    @Autowired
    private ActorTokenRepository actorTokenRepository;

    @Autowired
    private ShapeRepository shapeRepository;

    private int deviceSeq;
    private String otherToken;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("DELETE FROM actor_tokens");
        jdbcTemplate.execute("DELETE FROM deployment_config");
        jdbcTemplate.execute("DELETE FROM subject_locations");
        jdbcTemplate.execute("DELETE FROM locations");
        jdbcTemplate.execute("DELETE FROM subject_aliases");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM device_sync_state");
        jdbcTemplate.execute("DELETE FROM activities");
        jdbcTemplate.execute("""
                DELETE FROM shapes
                WHERE name NOT IN ('conflict_detected','conflict_resolved',
                                   'subjects_merged','subject_split')
                """);
        provisionTestToken();
        otherToken = actorTokenRepository.createToken(OTHER_ACTOR);
        insertActivity(ACTIVITY, ongoingActivityConfig("case_opening/v1", "case_follow_up/v1",
                "case_resolution/v1", "case_closure_review/v1", false));
        insertActivity(REVIEWABLE_ACTIVITY, ongoingActivityConfig("reviewable_case_opening/v1",
                "reviewable_case_follow_up/v1", "reviewable_case_resolution/v1",
                "reviewable_case_closure_review/v1", true));
        insertActivity(NO_PATTERN_ACTIVITY, """
                {"name":"no_pattern_activity","sensitivity":"standard","pattern":null}
                """);
        deviceSeq = 1;
    }

    @Test
    void closedInteractionAcceptedFlaggedAndNeverAdvancesWithoutLegalTransition() {
        UUID subject = UUID.randomUUID();
        seedClosedCase(subject, ACTIVITY, "case_opening/v1", "case_follow_up/v1",
                "case_resolution/v1", "case_closure_review/v1");

        PushedEvent invalidInteraction = pushEvent(subject, ACTIVITY, "capture",
                "case_follow_up/v1", "2026-05-24T11:00:01Z",
                Map.of("notes", "offline post-closure interaction"));

        assertThat(invalidInteraction.response().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(invalidInteraction.response().getBody().get("accepted").asInt()).isEqualTo(1);
        assertThat(invalidInteraction.response().getBody().get("flags_raised").asInt()).isEqualTo(1);

        JsonNode flag = findFlagFor(invalidInteraction.id(), "transition_violation");
        assertThat(flag.get("type").asText()).isEqualTo("alert");
        assertThat(flag.get("shape_ref").asText()).isEqualTo("conflict_detected/v1");
        JsonNode payload = flag.get("payload");
        assertThat(payload.get("flag_category").asText()).isEqualTo("transition_violation");
        assertThat(payload.get("resolvability").asText()).isEqualTo("auto_eligible");
        assertThat(payload.get("designated_resolver").isObject()).isTrue();
        assertThat(payload.get("designated_resolver").size()).isEqualTo(2);
        assertThat(payload.get("designated_resolver").get("type").asText()).isEqualTo("actor");
        assertThat(payload.get("designated_resolver").get("id").asText())
                .isEqualTo(TEST_ACTOR_ID.toString());

        JsonNode unresolvedState = stateFor(subject, ACTIVITY);
        assertThat(unresolvedState.get("current_state").asText()).isEqualTo("closed");
        assertThat(unresolvedState.at("/pattern_specific/interaction_count").asInt()).isEqualTo(1);

        ResponseEntity<JsonNode> resolution = resolveFlag(
                UUID.fromString(flag.get("id").asText()), TEST_TOKEN, "accepted");
        assertThat(resolution.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode acceptedState = stateFor(subject, ACTIVITY);
        assertThat(acceptedState.get("current_state").asText()).isEqualTo("closed");
        assertThat(acceptedState.at("/pattern_specific/interaction_count").asInt()).isEqualTo(1);
    }

    @Test
    void nonDesignatedAcceptedResolutionDoesNotReincludeTransitionViolation() {
        UUID subject = UUID.randomUUID();
        seedClosedCase(subject, ACTIVITY, "case_opening/v1", "case_follow_up/v1",
                "case_resolution/v1", "case_closure_review/v1");
        PushedEvent invalidInteraction = pushEvent(subject, ACTIVITY, "capture",
                "case_follow_up/v1", "2026-05-24T11:00:01Z",
                Map.of("notes", "non-designated resolver test"));
        JsonNode flag = findFlagFor(invalidInteraction.id(), "transition_violation");

        ResponseEntity<JsonNode> resolution = resolveFlag(
                UUID.fromString(flag.get("id").asText()), otherToken, "accepted");
        assertThat(resolution.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode state = stateFor(subject, ACTIVITY);
        assertThat(state.get("current_state").asText()).isEqualTo("closed");
        assertThat(state.at("/pattern_specific/interaction_count").asInt()).isEqualTo(1);
        assertThat(flagSources("scope_violation")).contains(resolution.getBody().get("event_id").asText());
    }

    @Test
    void noFlagForEventsOutsideAnyPatternBinding() {
        UUID subject = UUID.randomUUID();
        PushedEvent event = pushEvent(subject, NO_PATTERN_ACTIVITY, "capture",
                "case_follow_up/v1", "2026-05-24T08:00:01Z",
                Map.of("notes", "not pattern-bound"));

        assertThat(event.response().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(event.response().getBody().get("flags_raised").asInt()).isEqualTo(0);
        assertThat(flagSources("transition_violation")).isEmpty();
    }

    @Test
    void eventLevelReviewOverlayRemainsIndependentFromSubjectOngoingResolution() {
        UUID subject = UUID.randomUUID();
        pushEvent(subject, REVIEWABLE_ACTIVITY, "capture", "reviewable_case_opening/v1",
                "2026-05-24T08:00:01Z", Map.of("status", "opened"));
        PushedEvent followUp = pushEvent(subject, REVIEWABLE_ACTIVITY, "capture",
                "reviewable_case_follow_up/v1", "2026-05-24T09:00:01Z",
                Map.of("notes", "reviewable interaction"));
        pushEvent(subject, REVIEWABLE_ACTIVITY, "review", "case_follow_up_review/v1",
                "2026-05-24T10:00:01Z",
                Map.of("source_event_id", followUp.id().toString(), "decision", "accepted"));

        assertThat(flagSources("transition_violation")).isEmpty();
        ArrayNode states = patternStateProjection.projectCurrent(
                OffsetDateTime.parse("2026-05-24T12:00:01Z"));
        JsonNode subjectState = findState(states, "subject", subject, REVIEWABLE_ACTIVITY,
                "ongoing_resolution/v1");
        JsonNode eventState = findEventState(states, followUp.id(), "capture_with_review/v1");

        assertThat(subjectState.get("current_state").asText()).isEqualTo("active");
        assertThat(subjectState.at("/pattern_specific/interaction_count").asInt()).isEqualTo(1);
        assertThat(eventState.get("current_state").asText()).isEqualTo("accepted");
        assertThat(eventState.at("/pattern_specific/latest_review_outcome").asText()).isEqualTo("accepted");
    }

    @Test
    void priorDomainUniquenessFlagPreventsTransitionDetectionForSameEvent() {
        createUniqueFollowUpShape();
        insertActivity(UNIQUE_ACTIVITY, ongoingActivityConfig("unique_case_opening/v1",
                "unique_follow_up/v1", "unique_case_resolution/v1",
                "unique_case_closure_review/v1", false));
        UUID subject = UUID.randomUUID();
        pushEvent(subject, UNIQUE_ACTIVITY, "capture", "unique_case_opening/v1",
                "2026-05-24T08:00:01Z", Map.of("status", "opened"));
        pushEvent(subject, UNIQUE_ACTIVITY, "capture", "unique_follow_up/v1",
                "2026-05-24T09:00:01Z", Map.of("visit_code", "same"));
        pushEvent(subject, UNIQUE_ACTIVITY, "capture", "unique_case_resolution/v1",
                "2026-05-24T10:00:01Z", Map.of("outcome", "resolved"));
        pushEvent(subject, UNIQUE_ACTIVITY, "review", "unique_case_closure_review/v1",
                "2026-05-24T10:30:01Z", Map.of("decision", "closed"));

        PushedEvent duplicateAndInvalid = pushEvent(subject, UNIQUE_ACTIVITY, "capture",
                "unique_follow_up/v1", "2026-05-24T11:00:01Z",
                Map.of("visit_code", "same"));

        assertThat(duplicateAndInvalid.response().getBody().get("flags_raised").asInt()).isEqualTo(1);
        assertThat(findFlagFor(duplicateAndInvalid.id(), "domain_uniqueness_violation")).isNotNull();
        assertThat(flagSources("transition_violation")).doesNotContain(duplicateAndInvalid.id().toString());
    }

    @Test
    void priorSamePassDomainFlagIsExcludedFromLaterTransitionState() {
        createUniqueOpeningShape();
        createUniqueFollowUpShape();
        insertActivity(UNIQUE_ACTIVITY, ongoingActivityConfig("unique_case_opening/v1",
                "unique_follow_up/v1", "unique_case_resolution/v1",
                "unique_case_closure_review/v1", false));
        pushEvent(UUID.randomUUID(), UNIQUE_ACTIVITY, "capture", "unique_case_opening/v1",
                "2026-05-24T07:00:01Z", Map.of("visit_code", "shared"));

        UUID subject = UUID.randomUUID();
        UUID duplicateOpeningId = UUID.randomUUID();
        UUID followUpId = UUID.randomUUID();
        ResponseEntity<JsonNode> response = pushEvents(List.of(
                eventMap(duplicateOpeningId, subject, UNIQUE_ACTIVITY, "capture",
                        "unique_case_opening/v1", "2026-05-24T08:00:01Z",
                        Map.of("visit_code", "shared")),
                eventMap(followUpId, subject, UNIQUE_ACTIVITY, "capture",
                        "unique_follow_up/v1", "2026-05-24T09:00:01Z",
                        Map.of("visit_code", "later"))
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("accepted").asInt()).isEqualTo(2);
        assertThat(response.getBody().get("flags_raised").asInt()).isEqualTo(2);
        assertThat(findFlagFor(duplicateOpeningId, "domain_uniqueness_violation")).isNotNull();
        assertThat(findFlagFor(followUpId, "transition_violation")).isNotNull();
    }

    private void seedClosedCase(UUID subject, String activity, String openingShape,
                                String interactionShape, String resolutionShape,
                                String closureReviewShape) {
        pushEvent(subject, activity, "capture", openingShape,
                "2026-05-24T08:00:01Z", Map.of("status", "opened"));
        pushEvent(subject, activity, "capture", interactionShape,
                "2026-05-24T09:00:01Z", Map.of("notes", "follow up"));
        pushEvent(subject, activity, "capture", resolutionShape,
                "2026-05-24T10:00:01Z", Map.of("outcome", "resolved"));
        pushEvent(subject, activity, "review", closureReviewShape,
                "2026-05-24T10:30:01Z", Map.of("decision", "closed"));
    }

    private PushedEvent pushEvent(UUID subjectId, String activityRef, String type,
                                  String shapeRef, String timestamp, Map<String, Object> payload) {
        UUID eventId = UUID.randomUUID();
        ResponseEntity<JsonNode> response = pushEvents(List.of(
                eventMap(eventId, subjectId, activityRef, type, shapeRef, timestamp, payload)));
        return new PushedEvent(eventId, response);
    }

    private Map<String, Object> eventMap(UUID eventId, UUID subjectId, String activityRef, String type,
                                         String shapeRef, String timestamp, Map<String, Object> payload) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", eventId.toString());
        event.put("type", type);
        event.put("shape_ref", shapeRef);
        event.put("activity_ref", activityRef);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", WORKER.toString()));
        event.put("device_id", DEVICE.toString());
        event.put("device_seq", deviceSeq++);
        event.put("sync_watermark", null);
        event.put("timestamp", timestamp);
        event.put("payload", payload);
        return event;
    }

    private ResponseEntity<JsonNode> pushEvents(List<Map<String, Object>> events) {
        return rest.exchange("/api/sync/push",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("events", events, "last_pull_watermark", 0), jsonHeaders()),
                JsonNode.class);
    }

    private ResponseEntity<JsonNode> resolveFlag(UUID flagId, String token, String resolution) {
        return rest.exchange("/api/conflicts/" + flagId + "/resolve",
                HttpMethod.POST,
                new HttpEntity<>(Map.of(
                        "resolution", resolution,
                        "reason", "test transition resolution"
                ), authHeaders(token)),
                JsonNode.class);
    }

    private JsonNode stateFor(UUID subject, String activity) {
        ArrayNode states = patternStateProjection.projectCurrent(
                OffsetDateTime.parse("2026-05-24T12:00:01Z"));
        return findState(states, "subject", subject, activity, "ongoing_resolution/v1");
    }

    private JsonNode findState(ArrayNode states, String composition, UUID subject,
                               String activity, String bindingRef) {
        for (JsonNode state : states) {
            if (!composition.equals(state.get("composition").asText())) {
                continue;
            }
            JsonNode key = state.get("state_key");
            if (subject.toString().equals(key.at("/subject_ref/id").asText())
                    && activity.equals(key.get("activity_ref").asText())
                    && bindingRef.equals(key.get("binding_ref").asText())) {
                return state;
            }
        }
        throw new AssertionError("Missing state for " + subject + " " + activity + " " + bindingRef);
    }

    private JsonNode findEventState(ArrayNode states, UUID sourceEventId, String bindingRef) {
        for (JsonNode state : states) {
            if (!"event".equals(state.get("composition").asText())) {
                continue;
            }
            JsonNode key = state.get("state_key");
            if (sourceEventId.toString().equals(key.get("source_event_id").asText())
                    && bindingRef.equals(key.get("binding_ref").asText())) {
                return state;
            }
        }
        throw new AssertionError("Missing event state for " + sourceEventId + " " + bindingRef);
    }

    private JsonNode findFlagFor(UUID sourceEventId, String category) {
        String payload = jdbcTemplate.queryForObject("""
                SELECT row_to_json(e)::text
                FROM events e
                WHERE e.shape_ref = 'conflict_detected/v1'
                  AND e.payload->>'flag_category' = ?
                  AND e.payload->>'source_event_id' = ?
                ORDER BY e.sync_watermark ASC
                LIMIT 1
                """, String.class, category, sourceEventId.toString());
        try {
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private void insertActivity(String name, String json) {
        try {
            JsonNode config = objectMapper.readTree(json);
            jdbcTemplate.update("""
                    INSERT INTO activities (name, config_json, status, sensitivity)
                    VALUES (?, ?::jsonb, 'active', 'standard')
                    """, name, config.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String ongoingActivityConfig(String openingShape, String interactionShape,
                                         String resolutionShape, String closureReviewShape,
                                         boolean includeReviewOverlay) {
        String eventBindings = includeReviewOverlay ? """
                [{
                  "ref": "capture_with_review/v1",
                  "composition": "event",
                  "shape_roles": {
                    "review_decision": ["case_follow_up_review/v1"]
                  },
                  "activation_roles": {
                    "on_shapes": ["%s"]
                  },
                  "participant_roles": {
                    "capturer": ["field_worker"],
                    "reviewer": ["supervisor"]
                  },
                  "parameters": {}
                }]
                """.formatted(interactionShape) : "[]";
        return """
                {
                  "name": "case_activity",
                  "sensitivity": "standard",
                  "roles": {
                    "field_worker": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": {
                      "ref": "ongoing_resolution/v1",
                      "composition": "subject",
                      "shape_roles": {
                        "opening": ["%s"],
                        "interaction": ["%s"],
                        "resolution": ["%s"],
                        "closure_review": ["%s"]
                      },
                      "participant_roles": {
                        "assigned_worker": ["field_worker"],
                        "supervisor": ["supervisor"]
                      },
                      "parameters": {}
                    },
                    "event": %s
                  }
                }
                """.formatted(openingShape, interactionShape, resolutionShape,
                closureReviewShape, eventBindings);
    }

    private void createUniqueFollowUpShape() {
        createUniqueShape("unique_follow_up", """
                {
                  "scope": ["subject_ref", "activity_ref", "payload.visit_code"],
                  "device_action": "warn"
                }
                """);
    }

    private void createUniqueOpeningShape() {
        createUniqueShape("unique_case_opening", """
                {
                  "scope": ["activity_ref", "payload.visit_code"],
                  "device_action": "warn"
                }
                """);
    }

    private void createUniqueShape(String name, String uniquenessJson) {
        ObjectNode schema = objectMapper.createObjectNode();
        ArrayNode fields = schema.putArray("fields");
        ObjectNode visitCode = objectMapper.createObjectNode();
        visitCode.put("name", "visit_code");
        visitCode.put("type", "text");
        visitCode.put("required", true);
        visitCode.put("deprecated", false);
        fields.add(visitCode);
        schema.putNull("subject_binding");
        schema.set("uniqueness", parse(uniquenessJson));

        ShapeService service = new ShapeService(shapeRepository, objectMapper);
        List<String> violations = service.createShape(name, "standard", schema);
        assertThat(violations).isEmpty();
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = jsonHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    private record PushedEvent(UUID id, ResponseEntity<JsonNode> response) {}
}
