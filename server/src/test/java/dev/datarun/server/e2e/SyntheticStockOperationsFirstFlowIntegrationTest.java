package dev.datarun.server.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.authorization.ActorTokenRepository;
import dev.datarun.server.authorization.AssignmentService;
import dev.datarun.server.authorization.LocationRepository;
import dev.datarun.server.authorization.SubjectLocationRepository;
import dev.datarun.server.authorization.WebAdminSessionService;
import dev.datarun.server.config.ActivityService;
import dev.datarun.server.config.AdminCommandCapabilityPolicy;
import dev.datarun.server.config.ConfigPackager;
import dev.datarun.server.config.ShapeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class SyntheticStockOperationsFirstFlowIntegrationTest extends AbstractIntegrationTest {

    private static final String ISSUER = "https://issuer.test/datarun";
    private static final UUID ADMIN =
            UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID STOCK_WORKER =
            UUID.fromString("14814814-0000-4000-8000-000000000001");
    private static final UUID STOCK_SUPERVISOR =
            UUID.fromString("14814814-0000-4000-8000-000000000002");
    private static final UUID STOCK_DEVICE =
            UUID.fromString("14814814-0000-4000-8000-000000000003");
    private static final String STOCK_ACTIVITY = "stock_operations";
    private static final String STOCK_SHAPE = "stocktake_line/v1";

    @Autowired private MockMvc mvc;
    @Autowired private TestRestTemplate rest;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ShapeService shapeService;
    @Autowired private ActivityService activityService;
    @Autowired private ConfigPackager configPackager;
    @Autowired private AssignmentService assignmentService;
    @Autowired private LocationRepository locationRepository;
    @Autowired private SubjectLocationRepository subjectLocationRepository;
    @Autowired private ActorTokenRepository actorTokenRepository;

    private UUID region;
    private UUID warehouseDistrict;
    private UUID pilotStockScopeSubjectId;
    private String warehouseDistrictPath;
    private String workerToken;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("DELETE FROM actor_tokens");
        jdbcTemplate.execute("DELETE FROM subject_locations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM device_sync_state");
        jdbcTemplate.execute("DELETE FROM config_packages");
        jdbcTemplate.execute("DELETE FROM expression_rules");
        jdbcTemplate.execute("DELETE FROM activities");
        jdbcTemplate.execute("DELETE FROM shapes");
        jdbcTemplate.execute("DELETE FROM deployment_config");
        jdbcTemplate.execute("DELETE FROM locations");
        jdbcTemplate.execute("DELETE FROM auth_principal_binding_operations");
        jdbcTemplate.execute("DELETE FROM auth_principal_bindings");
        configureDefaultAssignmentAdminCapabilities();

        region = UUID.randomUUID();
        warehouseDistrict = UUID.randomUUID();
        locationRepository.insert(region, "Synthetic stock region", null, "region");
        locationRepository.insert(warehouseDistrict, "Synthetic warehouse lane",
                region, "district");
        warehouseDistrictPath = locationRepository.findPathById(warehouseDistrict);
        pilotStockScopeSubjectId = UUID.randomUUID();
        subjectLocationRepository.upsert(
                pilotStockScopeSubjectId, warehouseDistrict, warehouseDistrictPath);

        workerToken = actorTokenRepository.createToken(STOCK_WORKER);
    }

    @Test
    void stocktakeLineConfigPublishesSyncPushAcceptsAndScopedReportObserves() throws Exception {
        publishStocktakeLineConfig();
        assignSyntheticStockOperationsActors();

        ResponseEntity<JsonNode> configResponse = rest.exchange(
                "/api/sync/config",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(workerToken)),
                JsonNode.class);
        assertThat(configResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(configResponse.getBody().path("version").asInt()).isEqualTo(1);
        assertThat(configResponse.getBody().path("shapes").has(STOCK_SHAPE)).isTrue();
        assertThat(configResponse.getBody().path("activities").has(STOCK_ACTIVITY)).isTrue();
        assertThat(configResponse.getBody().at("/shapes/stocktake_line~1v1/fields").size())
                .isEqualTo(3);
        JsonNode stockOperationRoles =
                configResponse.getBody().at("/activities/stock_operations/roles");
        assertThat(stockOperationRoles.path("field_worker").path(0).asText())
                .isEqualTo("capture");
        assertThat(stockOperationRoles.has("supervisor")).isFalse();
        assertThat(stockOperationRoles.toString()).doesNotContain("review");

        long workerKnowledge = latestWatermark(pullEvents(workerToken, 0, 100));
        List<Map<String, Object>> stocktakeRows = List.of(
                stocktakeLineEvent(1, "mids_kit", 42),
                stocktakeLineEvent(2, "rapid_test_kit", 7));

        ResponseEntity<JsonNode> pushResponse =
                pushEvents(workerToken, workerKnowledge, stocktakeRows);

        assertThat(pushResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pushResponse.getBody().path("accepted").asInt()).isEqualTo(2);
        assertThat(pushResponse.getBody().path("duplicates").asInt()).isZero();
        assertThat(pushResponse.getBody().path("flags_raised").asInt()).isZero();
        assertThat(storedStocktakeLineCount()).isEqualTo(2);
        assertThat(storedStocktakeCategories())
                .containsExactlyInAnyOrder("mids_kit", "rapid_test_kit");
        assertThat(storedStocktakeSubjectRows())
                .hasSize(2)
                .allSatisfy(row -> {
                    assertThat(row.get("subject_type")).isEqualTo("subject");
                    assertThat(row.get("subject_id"))
                            .isEqualTo(pilotStockScopeSubjectId.toString());
                    assertThat(row.get("location_path")).isEqualTo(warehouseDistrictPath);
                });

        configureReportCommands(STOCK_SUPERVISOR);
        MockHttpSession session = webAdminSession(STOCK_SUPERVISOR);
        String report = mvc.perform(get("/web-admin/operational/report")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(report)
                .contains("Scoped Operational Report Snapshot")
                .contains("known_latest_input")
                .contains("Current scoped standing only. Coverage not measured.")
                .contains("Open configured work evidence")
                .doesNotContain("Configured work details")
                .doesNotContain("Field values")
                .doesNotContain("stocktake_date")
                .doesNotContain("stock_category")
                .doesNotContain("quantity")
                .doesNotContain("mids_kit")
                .doesNotContain("rapid_test_kit")
                .doesNotContain(">42<")
                .doesNotContain(">7<")
                .doesNotContain(pilotStockScopeSubjectId.toString())
                .doesNotContain("Stocktake Line Details")
                .doesNotContain("review workflow")
                .doesNotContain("stock ledger")
                .doesNotContain("complete")
                .doesNotContain("all devices current");
        assertActivityRow(report, "Stock Operations", 2, 0, 0);

        String evidence = mvc.perform(get(configuredWorkEvidencePath(report))
                        .session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(evidence)
                .contains("Configured Work Evidence")
                .contains("Visible Work Evidence")
                .contains("Activity")
                .contains("Record type")
                .contains("Latest synced/received")
                .contains("stock_operations")
                .contains("stocktake_line/v1")
                .contains("stocktake_date")
                .contains("stock_category")
                .contains("quantity")
                .contains("2026-06-23")
                .contains("rapid_test_kit")
                .contains(">7<")
                .contains("Visible through current assignment scope")
                .contains("Latest synced/received time is scoped and not a guarantee.")
                .doesNotContain("mids_kit")
                .doesNotContain(">42<")
                .doesNotContain(pilotStockScopeSubjectId.toString())
                .doesNotContain(STOCK_WORKER.toString())
                .doesNotContain(STOCK_DEVICE.toString())
                .doesNotContain("subject_ref")
                .doesNotContain("actor_ref")
                .doesNotContain("device_id")
                .doesNotContain("sync_watermark")
                .doesNotContain("Stocktake Line Details")
                .doesNotContain("review workflow")
                .doesNotContain("stock ledger")
                .doesNotContain("complete")
                .doesNotContain("all devices current");
    }

    private void publishStocktakeLineConfig() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.putNull("subject_binding");
        schema.putNull("uniqueness");
        ArrayNode fields = schema.putArray("fields");
        addField(fields, "stocktake_date", "date", true);
        addSelectField(fields, "stock_category", true,
                "mids_kit", "rapid_test_kit", "itn_bale");
        addIntegerField(fields, "quantity", true, 0);

        assertThat(shapeService.createShape("stocktake_line", "standard", schema)).isEmpty();

        ObjectNode activityConfig = objectMapper.createObjectNode();
        activityConfig.putArray("shapes").add(STOCK_SHAPE);
        ObjectNode roles = activityConfig.putObject("roles");
        roles.putArray("field_worker").add("capture");
        assertThat(activityService.createActivity(
                STOCK_ACTIVITY, "standard", activityConfig)).isEmpty();
        assertThat(configPackager.publish(null)).isEqualTo(1);
    }

    private void assignSyntheticStockOperationsActors() {
        assignmentService.createInitialBootstrapAssignment(
                ADMIN, "admin", null, null, null, past(), null);
        assignmentService.createAssignment(
                ADMIN, STOCK_WORKER, "field_worker", warehouseDistrict, null,
                List.of(STOCK_ACTIVITY), past(), null);
        assignmentService.createAssignment(
                ADMIN, STOCK_SUPERVISOR, "supervisor", warehouseDistrict, null,
                List.of(STOCK_ACTIVITY), past(), null);
    }

    private Map<String, Object> stocktakeLineEvent(int deviceSeq, String category, int quantity) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "capture");
        event.put("shape_ref", STOCK_SHAPE);
        event.put("activity_ref", STOCK_ACTIVITY);
        event.put("subject_ref", Map.of(
                "type", "subject",
                "id", pilotStockScopeSubjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", STOCK_WORKER.toString()));
        event.put("device_id", STOCK_DEVICE.toString());
        event.put("device_seq", deviceSeq);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", Map.of(
                "stocktake_date", "2026-06-23",
                "stock_category", category,
                "quantity", quantity));
        return event;
    }

    private void addField(ArrayNode fields, String name, String type, boolean required) {
        ObjectNode field = fields.addObject();
        field.put("name", name);
        field.put("type", type);
        field.put("required", required);
        field.put("deprecated", false);
        field.put("display_order", fields.size());
    }

    private void addSelectField(ArrayNode fields, String name, boolean required,
                                String... options) {
        ObjectNode field = fields.addObject();
        field.put("name", name);
        field.put("type", "select");
        field.put("required", required);
        field.put("deprecated", false);
        field.put("display_order", fields.size());
        ArrayNode optionArray = field.putArray("options");
        for (String option : options) {
            optionArray.add(option);
        }
    }

    private void addIntegerField(ArrayNode fields, String name, boolean required, int min) {
        ObjectNode field = fields.addObject();
        field.put("name", name);
        field.put("type", "integer");
        field.put("required", required);
        field.put("deprecated", false);
        field.put("display_order", fields.size());
        field.putObject("validation").put("min", min);
    }

    private ResponseEntity<JsonNode> pullEvents(String token, long sinceWatermark, int limit) {
        Map<String, Object> request = Map.of(
                "since_watermark", sinceWatermark,
                "limit", limit,
                "device_id", STOCK_DEVICE.toString(),
                "config_version", 1);
        return rest.exchange("/api/sync/pull", HttpMethod.POST,
                new HttpEntity<>(request, bearerHeaders(token)), JsonNode.class);
    }

    private ResponseEntity<JsonNode> pushEvents(String token, long lastPullWatermark,
                                                List<Map<String, Object>> events) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("events", events);
        request.put("device_id", STOCK_DEVICE.toString());
        request.put("last_pull_watermark", lastPullWatermark);
        return rest.exchange("/api/sync/push", HttpMethod.POST,
                new HttpEntity<>(request, bearerHeaders(token)), JsonNode.class);
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    private long latestWatermark(ResponseEntity<JsonNode> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().path("latest_watermark").asLong();
    }

    private int storedStocktakeLineCount() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE shape_ref = 'stocktake_line/v1'
                  AND activity_ref = 'stock_operations'
                """, Integer.class);
        return count == null ? 0 : count;
    }

    private List<String> storedStocktakeCategories() {
        return jdbcTemplate.queryForList("""
                SELECT payload->>'stock_category'
                FROM events
                WHERE shape_ref = 'stocktake_line/v1'
                ORDER BY device_seq
                """, String.class);
    }

    private List<Map<String, Object>> storedStocktakeSubjectRows() {
        return jdbcTemplate.queryForList("""
                SELECT subject_ref->>'type' AS subject_type,
                       subject_ref->>'id' AS subject_id,
                       location_path
                FROM events
                WHERE shape_ref = 'stocktake_line/v1'
                ORDER BY device_seq
                """);
    }

    private void configureReportCommands(UUID actorId) throws Exception {
        ObjectNode policy = objectMapper.createObjectNode();
        policy.put("schema_version", 1);
        ArrayNode grants = policy.putObject("actors").putArray(actorId.toString());
        grants.add(AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        grants.add(AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);
        jdbcTemplate.update("""
                INSERT INTO deployment_config (config_key, config_json, updated_by, updated_at)
                VALUES (?, ?::jsonb, ?::uuid, NOW())
                ON CONFLICT (config_key) DO UPDATE
                SET config_json = EXCLUDED.config_json,
                    updated_by = EXCLUDED.updated_by,
                    updated_at = NOW()
                """,
                AdminCommandCapabilityPolicy.CONFIG_KEY,
                objectMapper.writeValueAsString(policy),
                actorId.toString());
    }

    private MockHttpSession webAdminSession(UUID actorId) {
        bindPrincipal(actorId);
        MockHttpSession session = new MockHttpSession();
        Instant now = Instant.now();
        session.setAttribute(WebAdminSessionService.ACTOR_ID_ATTR, actorId.toString());
        session.setAttribute(WebAdminSessionService.ISSUER_ATTR, ISSUER);
        session.setAttribute(WebAdminSessionService.SUBJECT_ATTR, subjectFor(actorId));
        session.setAttribute(WebAdminSessionService.AUTH_SOURCE_ATTR, "oidc-jwks-principal");
        session.setAttribute(WebAdminSessionService.LOGIN_TIME_ATTR, now);
        session.setAttribute(WebAdminSessionService.LAST_SEEN_TIME_ATTR, now);
        session.setAttribute(WebAdminSessionService.EXPIRES_AT_ATTR, now.plusSeconds(1800));
        session.setAttribute(WebAdminSessionService.SESSION_CORRELATION_ID_ATTR,
                UUID.randomUUID().toString());
        return session;
    }

    private void bindPrincipal(UUID actorId) {
        jdbcTemplate.update("""
                INSERT INTO auth_principal_bindings (issuer, subject, actor_id)
                VALUES (?, ?, ?::uuid)
                ON CONFLICT DO NOTHING
                """, ISSUER, subjectFor(actorId), actorId.toString());
    }

    private String subjectFor(UUID actorId) {
        return "principal-" + actorId;
    }

    private OffsetDateTime past() {
        return OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
    }

    private void assertActivityRow(String html, String activity, long clean,
                                   long excluded, long unresolved) {
        Pattern row = Pattern.compile(
                "<tr>\\s*<td>" + Pattern.quote(activity) + "</td>\\s*"
                        + "<td>" + clean + "</td>\\s*"
                        + "<td>" + excluded + "</td>\\s*"
                        + "<td>" + unresolved + "</td>",
                Pattern.DOTALL);
        assertThat(row.matcher(html).find())
                .as("activity row %s clean=%s excluded=%s unresolved=%s",
                        activity, clean, excluded, unresolved)
                .isTrue();
    }

    private String configuredWorkEvidencePath(String html) {
        java.util.regex.Matcher matcher = Pattern.compile(
                        "href=\"([^\"]*/web-admin/operational/evidence\\?workToken=[^\"]+)\"")
                .matcher(html);
        assertThat(matcher.find())
                .as("configured work evidence link")
                .isTrue();
        return matcher.group(1);
    }
}
