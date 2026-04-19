package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3a integration tests: shape/activity CRUD, config endpoint, payload validation.
 * Quality gates from phase-3.md §5 Phase 3a.
 */
class ConfigIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShapeRepository shapeRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ConfigPackager configPackager;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        // Clean config tables (reverse dependency order)
        jdbcTemplate.update("DELETE FROM config_packages");
        jdbcTemplate.update("DELETE FROM expression_rules");
        jdbcTemplate.update("DELETE FROM activities");
        jdbcTemplate.update("DELETE FROM shapes");
        // Clean event tables for sync tests
        jdbcTemplate.update("DELETE FROM events");
        jdbcTemplate.update("DELETE FROM device_sync_state");
        // Provision test token and root-scope assignment
        provisionTestToken();
    }

    // --- Shape CRUD ---

    @Test
    void createShape_validSchema_succeeds() {
        // QG: Deployer creates shape with 5 fields in admin UI → shape stored with correct schema
        ObjectNode schema = buildSchema(5, "household_visit");
        ShapeService service = new ShapeService(shapeRepository, objectMapper);

        List<String> violations = service.createShape("household_visit", "standard", schema);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
        var shape = shapeRepository.findByNameAndVersion("household_visit", 1);
        assertTrue(shape.isPresent());
        assertEquals(5, shape.get().schemaJson().get("fields").size());
    }

    @Test
    void createShape_invalidName_rejected() {
        // QG: Shape name "Household-Visit" → DtV rejects ("invalid name format")
        ObjectNode schema = buildSchema(1, null);
        ShapeService service = new ShapeService(shapeRepository, objectMapper);

        List<String> violations = service.createShape("Household-Visit", "standard", schema);

        assertFalse(violations.isEmpty());
        assertTrue(violations.get(0).contains("must match [a-z][a-z0-9_]*"));
    }

    @Test
    void createShape_exceedsFieldBudget_rejected() {
        // QG: Shape with 61 fields → DtV rejects with specific violation
        ObjectNode schema = buildSchema(61, null);
        ShapeService service = new ShapeService(shapeRepository, objectMapper);

        List<String> violations = service.createShape("big_shape", "standard", schema);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("exceeds 60-field budget")));
    }

    @Test
    void createVersion_addsOptionalField_succeeds() {
        // QG: Create household_visit/v2 adding optional field → v1 events still project correctly
        ObjectNode schemaV1 = buildSchema(3, "household_visit");
        ShapeService service = new ShapeService(shapeRepository, objectMapper);
        service.createShape("household_visit", "standard", schemaV1);

        // V2 adds an optional field
        ObjectNode schemaV2 = buildSchema(3, "household_visit");
        ArrayNode fields = (ArrayNode) schemaV2.get("fields");
        ObjectNode newField = objectMapper.createObjectNode();
        newField.put("name", "extra_notes");
        newField.put("type", "narrative");
        newField.put("required", false);
        newField.put("deprecated", false);
        fields.add(newField);

        List<String> violations = service.createVersion("household_visit", "standard", schemaV2);

        assertTrue(violations.isEmpty(), "Got violations: " + violations);
        assertEquals(2, shapeRepository.findByName("household_visit").size());
        var v2 = shapeRepository.findByNameAndVersion("household_visit", 2);
        assertTrue(v2.isPresent());
        assertEquals(4, v2.get().schemaJson().get("fields").size());
    }

    @Test
    void deprecateShape_hiddenButProjectable() {
        // QG: Shape deprecated → hidden from new form list, existing events still project
        ShapeService service = new ShapeService(shapeRepository, objectMapper);
        service.createShape("old_form", "standard", buildSchema(2, null));

        service.deprecate("old_form", 1);

        var shape = shapeRepository.findByNameAndVersion("old_form", 1);
        assertTrue(shape.isPresent());
        assertEquals("deprecated", shape.get().status());
        // Still in findAll (included in config package for projection)
        assertFalse(shapeRepository.findAll().isEmpty());
    }

    // --- Activity CRUD ---

    @Test
    void createActivity_validConfig_succeeds() {
        // QG: Activity created with shape + role-action mapping
        ShapeService ss = new ShapeService(shapeRepository, objectMapper);
        ss.createShape("household_visit", "standard", buildSchema(3, "household_visit"));

        ActivityService as = new ActivityService(activityRepository, shapeRepository, objectMapper);
        ObjectNode config = objectMapper.createObjectNode();
        ArrayNode shapes = config.putArray("shapes");
        shapes.add("household_visit/v1");
        ObjectNode roles = config.putObject("roles");
        ArrayNode fwActions = roles.putArray("field_worker");
        fwActions.add("capture");
        ArrayNode supActions = roles.putArray("supervisor");
        supActions.add("capture");
        supActions.add("review");

        List<String> violations = as.createActivity("hh_monitoring", "standard", config);

        assertTrue(violations.isEmpty(), "Got violations: " + violations);
        assertTrue(activityRepository.exists("hh_monitoring"));
    }

    @Test
    void createActivity_missingShape_rejected() {
        // QG: Activity referencing non-existent shape → rejected
        ActivityService as = new ActivityService(activityRepository, shapeRepository, objectMapper);
        ObjectNode config = objectMapper.createObjectNode();
        config.putArray("shapes").add("nonexistent/v1");
        config.putObject("roles").putArray("worker").add("capture");

        List<String> violations = as.createActivity("bad_activity", "standard", config);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("does not exist")));
    }

    // --- Config Endpoint (IDR-019) ---

    @Test
    void configEndpoint_returnsPackageFormat() {
        // QG: Shape version created → device receives it at next sync via config endpoint
        ShapeService ss = new ShapeService(shapeRepository, objectMapper);
        ss.createShape("facility_obs", "standard", buildSchema(3, "facility_obs"));

        ActivityService as = new ActivityService(activityRepository, shapeRepository, objectMapper);
        ObjectNode config = objectMapper.createObjectNode();
        config.putArray("shapes").add("facility_obs/v1");
        config.putObject("roles").putArray("field_worker").add("capture");
        as.createActivity("monitoring", "standard", config);

        configPackager.publish(null);

        // GET /api/sync/config
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/sync/config",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                JsonNode.class);

        assertEquals(200, response.getStatusCode().value());
        JsonNode body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.get("version").asInt());
        assertTrue(body.has("shapes"));
        assertTrue(body.has("activities"));
        assertTrue(body.has("expressions"));
        assertTrue(body.has("flag_severity_overrides"));
        assertTrue(body.has("sensitivity_classifications"));

        // Shape is present in package
        assertTrue(body.get("shapes").has("facility_obs/v1"));
        JsonNode shapeInPkg = body.get("shapes").get("facility_obs/v1");
        assertEquals("facility_obs", shapeInPkg.get("name").asText());
        assertEquals(1, shapeInPkg.get("version").asInt());
        assertEquals(3, shapeInPkg.get("fields").size());

        // Activity is present
        assertTrue(body.get("activities").has("monitoring"));
    }

    @Test
    void configEndpoint_304WhenUpToDate() {
        // QG: If-None-Match → 304 Not Modified
        ShapeService ss = new ShapeService(shapeRepository, objectMapper);
        ss.createShape("test_shape", "standard", buildSchema(1, null));
        configPackager.publish(null);

        HttpHeaders headers = authHeaders();
        headers.set("If-None-Match", "1");

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/sync/config",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Void.class);

        assertEquals(304, response.getStatusCode().value());
    }

    @Test
    void configEndpoint_allVersionsIncludingDeprecated() {
        // QG: Config package includes all shape versions including deprecated (C14)
        ShapeService ss = new ShapeService(shapeRepository, objectMapper);
        ss.createShape("obs", "standard", buildSchema(2, null));
        ss.createVersion("obs", "standard", buildSchema(3, null));
        ss.deprecate("obs", 1);

        configPackager.publish(null);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/sync/config",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                JsonNode.class);

        JsonNode shapes = response.getBody().get("shapes");
        assertTrue(shapes.has("obs/v1"), "Deprecated v1 must be in package");
        assertTrue(shapes.has("obs/v2"), "Active v2 must be in package");
        assertEquals("deprecated", shapes.get("obs/v1").get("status").asText());
        assertEquals("active", shapes.get("obs/v2").get("status").asText());
    }

    // --- Pull Response includes config_version ---

    @Test
    void pullResponse_includesConfigVersion() {
        // QG: sync pull response contains config_version
        ShapeService ss = new ShapeService(shapeRepository, objectMapper);
        ss.createShape("test", "standard", buildSchema(1, null));
        configPackager.publish(null);

        ObjectNode pullRequest = objectMapper.createObjectNode();
        pullRequest.put("since_watermark", 0);
        pullRequest.put("limit", 10);

        HttpEntity<JsonNode> entity = new HttpEntity<>(pullRequest, authHeaders());
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/sync/pull", HttpMethod.POST, entity, JsonNode.class);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().has("config_version"));
        assertEquals(1, response.getBody().get("config_version").asInt());
    }

    // --- Payload Validation Against Shape ---

    @Test
    void push_validPayload_accepted() {
        // QG: Mobile captures event with shape_ref = household_visit/v1 → server validates payload
        ShapeService ss = new ShapeService(shapeRepository, objectMapper);
        ObjectNode schema = buildSchema(0, null);
        ArrayNode fields = (ArrayNode) schema.get("fields");

        ObjectNode nameField = objectMapper.createObjectNode();
        nameField.put("name", "name");
        nameField.put("type", "text");
        nameField.put("required", true);
        nameField.put("deprecated", false);
        fields.add(nameField);

        ObjectNode ageField = objectMapper.createObjectNode();
        ageField.put("name", "age");
        ageField.put("type", "integer");
        ageField.put("required", false);
        ageField.put("deprecated", false);
        fields.add(ageField);

        ss.createShape("patient_reg", "standard", schema);

        // Push valid event
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("name", "John");
        payload.put("age", 30);

        ObjectNode event = buildEvent("patient_reg/v1", payload);
        ObjectNode pushRequest = buildPushRequest(event);

        ResponseEntity<JsonNode> response = pushEvents(pushRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().get("accepted").asInt());
    }

    @Test
    void push_missingRequiredField_rejected() {
        // QG: Mobile pushes event with payload that doesn't match shape → 400
        ShapeService ss = new ShapeService(shapeRepository, objectMapper);
        ObjectNode schema = buildSchema(0, null);
        ArrayNode fields = (ArrayNode) schema.get("fields");
        ObjectNode nameField = objectMapper.createObjectNode();
        nameField.put("name", "name");
        nameField.put("type", "text");
        nameField.put("required", true);
        nameField.put("deprecated", false);
        fields.add(nameField);
        ss.createShape("strict_form", "standard", schema);

        // Push event missing required "name" field
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("other", "stuff");

        ObjectNode event = buildEvent("strict_form/v1", payload);
        ObjectNode pushRequest = buildPushRequest(event);

        ResponseEntity<JsonNode> response = pushEvents(pushRequest);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("shape_validation_failed", response.getBody().get("error").asText());
    }

    @Test
    void push_wrongFieldType_rejected() {
        // Push event with integer where text expected → 400
        ShapeService ss = new ShapeService(shapeRepository, objectMapper);
        ObjectNode schema = buildSchema(0, null);
        ArrayNode fields = (ArrayNode) schema.get("fields");
        ObjectNode nameField = objectMapper.createObjectNode();
        nameField.put("name", "name");
        nameField.put("type", "text");
        nameField.put("required", true);
        nameField.put("deprecated", false);
        fields.add(nameField);
        ss.createShape("typed_form", "standard", schema);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("name", 12345); // int instead of text

        ObjectNode event = buildEvent("typed_form/v1", payload);
        ObjectNode pushRequest = buildPushRequest(event);

        ResponseEntity<JsonNode> response = pushEvents(pushRequest);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void push_unknownShape_passesThrough() {
        // Events with shape_ref not in shapes table should still be accepted
        // (pre-Phase-3 events, forward compatibility)
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("data", "value");

        ObjectNode event = buildEvent("unknown_shape/v99", payload);
        ObjectNode pushRequest = buildPushRequest(event);

        ResponseEntity<JsonNode> response = pushEvents(pushRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().get("accepted").asInt());
    }

    // --- Sensitivity ---

    @Test
    void sensitivityClassification_inConfigPackage() {
        // QG: Sensitivity classification on shape → config package carries classification
        ShapeService ss = new ShapeService(shapeRepository, objectMapper);
        ss.createShape("sensitive_form", "elevated", buildSchema(2, null));

        configPackager.publish(null);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/sync/config",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                JsonNode.class);

        JsonNode sensClass = response.getBody().get("sensitivity_classifications");
        assertEquals("elevated", sensClass.get("shapes").get("sensitive_form/v1").asText());
    }

    // --- Helpers ---

    private ObjectNode buildSchema(int fieldCount, String shapeName) {
        ObjectNode schema = objectMapper.createObjectNode();
        ArrayNode fields = schema.putArray("fields");
        for (int i = 0; i < fieldCount; i++) {
            ObjectNode field = objectMapper.createObjectNode();
            field.put("name", "field_" + (i + 1));
            field.put("type", "text");
            field.put("required", false);
            field.put("deprecated", false);
            field.put("display_order", i + 1);
            fields.add(field);
        }
        schema.putNull("subject_binding");
        schema.putNull("uniqueness");
        return schema;
    }

    private ObjectNode buildEvent(String shapeRef, JsonNode payload) {
        ObjectNode event = objectMapper.createObjectNode();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "capture");
        event.put("shape_ref", shapeRef);
        event.putNull("activity_ref");

        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", UUID.randomUUID().toString());
        event.set("subject_ref", subjectRef);

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", TEST_ACTOR_ID.toString());
        event.set("actor_ref", actorRef);

        event.put("device_id", UUID.randomUUID().toString());
        event.put("device_seq", 1);
        event.putNull("sync_watermark");
        event.put("timestamp", "2026-04-20T10:00:00Z");
        event.set("payload", payload);
        return event;
    }

    private ObjectNode buildPushRequest(ObjectNode... events) {
        ObjectNode request = objectMapper.createObjectNode();
        ArrayNode eventsArray = request.putArray("events");
        for (ObjectNode e : events) eventsArray.add(e);
        return request;
    }

    private ResponseEntity<JsonNode> pushEvents(ObjectNode request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ObjectNode> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange("/api/sync/push", HttpMethod.POST, entity, JsonNode.class);
    }
}
