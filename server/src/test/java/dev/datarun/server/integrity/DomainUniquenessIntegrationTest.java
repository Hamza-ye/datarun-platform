package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.authorization.AssignmentService;
import dev.datarun.server.authorization.LocationRepository;
import dev.datarun.server.config.FlagSeverityConfigService;
import dev.datarun.server.config.ShapeRepository;
import dev.datarun.server.config.ShapeService;
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

class DomainUniquenessIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShapeRepository shapeRepository;

    @Autowired
    private FlagSeverityConfigService flagSeverityConfigService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private LocationRepository locationRepository;

    private static final UUID DEVICE_A = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID SUBJECT_X = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
    private static final UUID WORKER_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000101");
    private static final UUID SUPERVISOR_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000202");

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("DELETE FROM actor_tokens");
        jdbcTemplate.execute("DELETE FROM deployment_config");
        jdbcTemplate.execute("DELETE FROM subject_locations");
        jdbcTemplate.execute("DELETE FROM locations");
        jdbcTemplate.execute("DELETE FROM subject_aliases");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM device_sync_state");
        jdbcTemplate.execute("""
                DELETE FROM shapes
                WHERE name NOT IN ('conflict_detected','conflict_resolved',
                                   'subjects_merged','subject_split')
                """);
        provisionTestToken();
        createUniqueVisitShape();
    }

    @Test
    void duplicateAcceptedAndFlagged_targetsOnlyIncomingEvent() {
        Map<String, Object> first = buildVisitEvent(1, "household-42", "2026-05-20T10:00:00Z");
        Map<String, Object> duplicate = buildVisitEvent(2, "household-42", "2026-05-20T12:00:00Z");

        assertThat(pushEvents(List.of(first)).getBody().get("flags_raised").asInt()).isEqualTo(0);
        ResponseEntity<JsonNode> duplicateResponse = pushEvents(List.of(duplicate));

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(duplicateResponse.getBody().get("accepted").asInt()).isEqualTo(1);
        assertThat(duplicateResponse.getBody().get("flags_raised").asInt()).isEqualTo(1);

        JsonNode flag = findDomainUniquenessFlagFor(duplicate.get("id").toString());
        assertThat(flag.get("type").asText()).isEqualTo("alert");
        assertThat(flag.get("shape_ref").asText()).isEqualTo("conflict_detected/v1");
        JsonNode payload = flag.get("payload");
        assertThat(payload.get("flag_category").asText()).isEqualTo("domain_uniqueness_violation");
        assertThat(payload.get("resolvability").asText()).isEqualTo("manual_only");
        assertThat(payload.get("designated_resolver").get("id").asText())
                .isEqualTo(TEST_ACTOR_ID.toString());
        assertThat(payload.get("source_event_id").asText()).isEqualTo(duplicate.get("id").toString());
        assertThat(payload.get("constraint_ref").asText()).isEqualTo("unique_visit/v1#uniqueness");
        assertThat(payload.get("shape_ref").asText()).isEqualTo("unique_visit/v1");
        assertThat(payload.get("activity_ref").asText()).isEqualTo("monitoring");
        assertThat(payload.get("normalized_key").get("hash").asText()).isNotBlank();
        assertThat(textValues(payload.get("normalized_key").get("dimensions")))
                .containsExactly("subject_ref", "activity_ref", "payload.visit_code");
        assertThat(payload.get("period").get("type").asText()).isEqualTo("calendar_day");
        assertThat(textValues(payload.get("conflicting_event_ids")))
                .containsExactly(first.get("id").toString());
        assertThat(payload.get("detector_version").asText()).isEqualTo("domain_uniqueness_detector/v1");

        Integer firstFlags = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM events
                WHERE shape_ref = 'conflict_detected/v1'
                  AND payload->>'flag_category' = 'domain_uniqueness_violation'
                  AND payload->>'source_event_id' = ?
                """, Integer.class, first.get("id").toString());
        assertThat(firstFlags).isZero();
    }

    @Test
    void flaggedDuplicatesExcludedFromBasisUntilAcceptedResolution() {
        Map<String, Object> first = buildVisitEvent(1, "household-42", "2026-05-20T10:00:00Z");
        Map<String, Object> second = buildVisitEvent(2, "household-42", "2026-05-20T11:00:00Z");
        Map<String, Object> third = buildVisitEvent(3, "household-42", "2026-05-20T12:00:00Z");
        Map<String, Object> fourth = buildVisitEvent(4, "household-42", "2026-05-20T13:00:00Z");

        pushEvents(List.of(first));
        pushEvents(List.of(second));
        pushEvents(List.of(third));

        JsonNode thirdFlag = findDomainUniquenessFlagFor(third.get("id").toString());
        assertThat(textValues(thirdFlag.get("payload").get("conflicting_event_ids")))
                .containsExactly(first.get("id").toString());

        UUID secondFlagId = UUID.fromString(
                findDomainUniquenessFlagFor(second.get("id").toString()).get("id").asText());
        resolveFlag(secondFlagId, "accepted");

        pushEvents(List.of(fourth));
        JsonNode fourthFlag = findDomainUniquenessFlagFor(fourth.get("id").toString());

        assertThat(textValues(fourthFlag.get("payload").get("conflicting_event_ids")))
                .containsExactly(first.get("id").toString(), second.get("id").toString());
    }

    @Test
    void acceptedResolutionReincludesDuplicateInProjection() {
        Map<String, Object> first = buildVisitEvent(1, "household-42", "2026-05-20T10:00:00Z");
        Map<String, Object> duplicate = buildVisitEvent(2, "household-42", "2026-05-20T12:00:00Z");

        pushEvents(List.of(first));
        pushEvents(List.of(duplicate));

        assertThat(projectedEventCount(SUBJECT_X)).isEqualTo(1);

        UUID flagId = UUID.fromString(
                findDomainUniquenessFlagFor(duplicate.get("id").toString()).get("id").asText());
        ResponseEntity<JsonNode> resolveResponse = resolveFlag(flagId, "accepted");
        assertThat(resolveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(projectedEventCount(SUBJECT_X)).isEqualTo(2);
    }

    @Test
    void domainUniquenessFlag_routesToNearestActivitySteward() {
        UUID region = UUID.randomUUID();
        UUID village = UUID.randomUUID();
        locationRepository.insert(region, "Region", null, "region");
        locationRepository.insert(village, "Village", region, "village");
        registerSubjectLocation(SUBJECT_X, village);
        assignmentService.createAssignment(TEST_ACTOR_ID, SUPERVISOR_ID, "supervisor",
                village, null, List.of("monitoring"),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        Map<String, Object> first = buildVisitEvent(1, "household-42", "2026-05-20T10:00:00Z");
        Map<String, Object> duplicate = buildVisitEvent(2, "household-42", "2026-05-20T12:00:00Z");
        pushEvents(List.of(first));
        pushEvents(List.of(duplicate));

        JsonNode flag = findDomainUniquenessFlagFor(duplicate.get("id").toString());
        assertThat(flag.get("payload").get("designated_resolver").get("id").asText())
                .isEqualTo(SUPERVISOR_ID.toString());
    }

    @Test
    void subjectAliasesAreNormalizedForUniquenessKey() {
        UUID retiredSubject = UUID.fromString("11111111-2222-3333-4444-555555555555");
        jdbcTemplate.update("""
                INSERT INTO subject_aliases (retired_id, surviving_id, merged_at)
                VALUES (?::uuid, ?::uuid, '2026-05-20T09:00:00Z'::timestamptz)
                """, retiredSubject.toString(), SUBJECT_X.toString());

        Map<String, Object> retiredSubjectEvent = buildVisitEvent(
                retiredSubject, 1, "household-42", "2026-05-20T10:00:00Z");
        Map<String, Object> canonicalSubjectEvent = buildVisitEvent(
                SUBJECT_X, 2, "household-42", "2026-05-20T12:00:00Z");

        pushEvents(List.of(retiredSubjectEvent));
        ResponseEntity<JsonNode> duplicateResponse = pushEvents(List.of(canonicalSubjectEvent));

        assertThat(duplicateResponse.getBody().get("accepted").asInt()).isEqualTo(1);
        assertThat(duplicateResponse.getBody().get("flags_raised").asInt()).isEqualTo(1);

        JsonNode flag = findDomainUniquenessFlagFor(canonicalSubjectEvent.get("id").toString());
        assertThat(textValues(flag.get("payload").get("conflicting_event_ids")))
                .containsExactly(retiredSubjectEvent.get("id").toString());
    }

    @Test
    void flagSeverityOverrideDoesNotChangeResolvability() throws Exception {
        JsonNode overrides = objectMapper.readTree("""
                {"domain_uniqueness_violation": "informational"}
                """);
        assertThat(flagSeverityConfigService.updateOverrides(overrides, null)).isEmpty();

        Map<String, Object> first = buildVisitEvent(1, "household-42", "2026-05-20T10:00:00Z");
        Map<String, Object> duplicate = buildVisitEvent(2, "household-42", "2026-05-20T12:00:00Z");
        pushEvents(List.of(first));
        pushEvents(List.of(duplicate));

        ResponseEntity<JsonNode> listResponse = rest.exchange(
                "/api/conflicts", HttpMethod.GET, new HttpEntity<>(authHeaders()), JsonNode.class);
        JsonNode listed = listResponse.getBody().get("flags").get(0);
        assertThat(listed.get("flag_category").asText()).isEqualTo("domain_uniqueness_violation");
        assertThat(listed.get("severity").asText()).isEqualTo("informational");

        String resolvability = jdbcTemplate.queryForObject("""
                SELECT payload->>'resolvability'
                FROM events
                WHERE shape_ref = 'conflict_detected/v1'
                  AND payload->>'flag_category' = 'domain_uniqueness_violation'
                LIMIT 1
                """, String.class);
        assertThat(resolvability).isEqualTo("manual_only");
    }

    @Test
    void pullStillReturnsDuplicateAndFlagEvents() {
        Map<String, Object> first = buildVisitEvent(1, "household-42", "2026-05-20T10:00:00Z");
        Map<String, Object> duplicate = buildVisitEvent(2, "household-42", "2026-05-20T12:00:00Z");
        pushEvents(List.of(first));
        pushEvents(List.of(duplicate));

        Map<String, Object> request = Map.of("since_watermark", 0, "limit", 100);
        ResponseEntity<JsonNode> response = rest.exchange(
                "/api/sync/pull",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode events = response.getBody().get("events");
        assertThat(textValues(events, "id"))
                .contains(first.get("id").toString(), duplicate.get("id").toString());
        assertThat(textValues(events, "shape_ref"))
                .contains("conflict_detected/v1");
    }

    private void createUniqueVisitShape() {
        ObjectNode schema = objectMapper.createObjectNode();
        ArrayNode fields = schema.putArray("fields");
        ObjectNode visitCode = objectMapper.createObjectNode();
        visitCode.put("name", "visit_code");
        visitCode.put("type", "text");
        visitCode.put("required", true);
        visitCode.put("deprecated", false);
        fields.add(visitCode);
        schema.putNull("subject_binding");
        schema.set("uniqueness", parse("""
                {
                  "scope": ["subject_ref", "activity_ref", "payload.visit_code"],
                  "period": {"type": "calendar_day", "timezone": "deployment"},
                  "device_action": "warn"
                }
                """));

        ShapeService service = new ShapeService(shapeRepository, objectMapper);
        List<String> violations = service.createShape("unique_visit", "standard", schema);
        assertThat(violations).isEmpty();
    }

    private Map<String, Object> buildVisitEvent(int seq, String visitCode, String timestamp) {
        return buildVisitEvent(SUBJECT_X, seq, visitCode, timestamp);
    }

    private Map<String, Object> buildVisitEvent(UUID subjectId, int seq,
                                                String visitCode, String timestamp) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "capture");
        event.put("shape_ref", "unique_visit/v1");
        event.put("activity_ref", "monitoring");
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", WORKER_ID.toString()));
        event.put("device_id", DEVICE_A.toString());
        event.put("device_seq", seq);
        event.put("sync_watermark", null);
        event.put("timestamp", timestamp);
        event.put("payload", Map.of("visit_code", visitCode));
        return event;
    }

    private ResponseEntity<JsonNode> pushEvents(List<?> events) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("events", events);
        request.put("device_id", DEVICE_A.toString());
        request.put("last_pull_watermark", 0);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange(
                "/api/sync/push",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                JsonNode.class);
    }

    private JsonNode findDomainUniquenessFlagFor(String sourceEventId) {
        String payload = jdbcTemplate.queryForObject("""
                SELECT row_to_json(e)::text
                FROM events e
                WHERE e.shape_ref = 'conflict_detected/v1'
                  AND e.payload->>'flag_category' = 'domain_uniqueness_violation'
                  AND e.payload->>'source_event_id' = ?
                """, String.class, sourceEventId);
        try {
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ResponseEntity<JsonNode> resolveFlag(UUID flagId, String resolution) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("resolution", resolution);
        request.put("actor_id", TEST_ACTOR_ID.toString());
        request.put("reason", "Test domain uniqueness resolution");
        return rest.exchange(
                "/api/conflicts/" + flagId + "/resolve",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                JsonNode.class);
    }

    private void registerSubjectLocation(UUID subjectId, UUID locationId) {
        jdbcTemplate.update("""
                INSERT INTO subject_locations (subject_id, location_id, path)
                VALUES (?::uuid, ?::uuid, (SELECT path FROM locations WHERE id = ?::uuid))
                ON CONFLICT (subject_id) DO UPDATE SET location_id = EXCLUDED.location_id, path = EXCLUDED.path
                """, subjectId.toString(), locationId.toString(), locationId.toString());
    }

    private int projectedEventCount(UUID subjectId) {
        ResponseEntity<JsonNode> response = rest.getForEntity("/api/subjects", JsonNode.class);
        JsonNode subjects = response.getBody().get("subjects");
        for (JsonNode subject : subjects) {
            if (subjectId.toString().equals(subject.get("id").asText())) {
                return subject.get("event_count").asInt();
            }
        }
        return 0;
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Bad test JSON", e);
        }
    }

    private List<String> textValues(JsonNode array) {
        List<String> values = new ArrayList<>();
        for (JsonNode value : array) {
            values.add(value.asText());
        }
        return values;
    }

    private List<String> textValues(JsonNode array, String fieldName) {
        List<String> values = new ArrayList<>();
        for (JsonNode value : array) {
            values.add(value.get(fieldName).asText());
        }
        return values;
    }
}
