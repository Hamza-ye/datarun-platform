package dev.datarun.server.ops.provisioning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.config.ConfigPackager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class StockOperationsPilotPackageIntegrationTest extends AbstractIntegrationTest {

    private static final UUID OPERATOR =
            UUID.fromString("15000000-0000-4000-8000-000000000010");
    private static final UUID PILOT_ADMIN =
            UUID.fromString("15000000-0000-4000-8000-000000000001");
    private static final UUID STOCK_SUPERVISOR =
            UUID.fromString("15000000-0000-4000-8000-000000000003");

    @Autowired
    private OneShotProvisioningService provisioningService;

    @Autowired
    private ConfigPackager configPackager;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM auth_principal_binding_operations");
        jdbcTemplate.update("DELETE FROM auth_principal_bindings");
        jdbcTemplate.update("DELETE FROM config_packages");
        jdbcTemplate.update("DELETE FROM deployment_config");
        jdbcTemplate.update("DELETE FROM expression_rules");
        jdbcTemplate.update("DELETE FROM activities");
        jdbcTemplate.update("DELETE FROM shapes");
        jdbcTemplate.update("DELETE FROM events");
        jdbcTemplate.update("DELETE FROM device_sync_state");
    }

    @Test
    void stockOperationsPilotPackageLoadsThroughExistingProvisioningPaths()
            throws Exception {
        String configManifest = packageFile("reviewed-config.json");

        JsonNode firstPublish = provisioningService.execute(
                "config-publish", configManifest, OPERATOR, "NW-150-focused");
        JsonNode secondPublish = provisioningService.execute(
                "config-publish", configManifest, OPERATOR, "NW-150-focused");

        assertThat(firstPublish.path("config_version").asInt()).isEqualTo(1);
        assertThat(firstPublish.path("published").asBoolean()).isTrue();
        assertThat(firstPublish.path("changed_authoring_rows").asInt()).isGreaterThan(0);
        assertThat(secondPublish.path("config_version").asInt()).isEqualTo(1);
        assertThat(secondPublish.path("published").asBoolean()).isFalse();
        assertThat(secondPublish.path("changed_authoring_rows").asInt()).isZero();

        JsonNode packageJson = configPackager.getLatest().orElseThrow().packageJson();
        assertThat(packageJson.path("shapes").has("stocktake_line/v1")).isTrue();
        JsonNode fields = packageJson.at("/shapes/stocktake_line~1v1/fields");
        assertThat(fields.size()).isEqualTo(3);
        assertThat(fieldNames(fields))
                .containsExactly("stocktake_date", "stock_category", "quantity");
        assertThat(packageJson.at("/activities/stock_operations/shapes/0").asText())
                .isEqualTo("stocktake_line/v1");
        JsonNode stockOperationsRoles =
                packageJson.at("/activities/stock_operations/roles");
        assertThat(stockOperationsRoles.path("field_worker").path(0).asText())
                .isEqualTo("capture");
        assertThat(stockOperationsRoles.has("supervisor")).isFalse();
        assertThat(stockOperationsRoles.toString()).doesNotContain("review");

        JsonNode syntheticAssumptions =
                objectMapper.readTree(packageFile("synthetic-assumptions.json"));
        JsonNode subjectAnchor = syntheticAssumptions.path("subject_anchor");
        assertThat(subjectAnchor.path("event_subject_ref_type").asText())
                .isEqualTo("subject");
        assertThat(subjectAnchor.toString())
                .contains("stocktake_line/v1")
                .contains("stock-scope subject");
        assertThat(subjectAnchor.path("stamped_by").asText())
                .contains("subject_binding is null");
        assertThat(stringValues(subjectAnchor.path("non_goals")))
                .contains("stock ledger",
                        "process subject emission",
                        "new platform scope mechanism");

        JsonNode adminPolicy = deploymentConfig("admin_command_capabilities");
        assertThat(adminPolicy.at("/actors/" + PILOT_ADMIN + "/0").asText())
                .isEqualTo("web_admin.access");
        assertThat(adminPolicy.at("/actors/" + STOCK_SUPERVISOR).toString())
                .contains("web_admin.read_scoped");
        JsonNode assignmentAdminPolicy =
                deploymentConfig("assignment_admin_capabilities");
        assertThat(stringValues(assignmentAdminPolicy.at("/roles/pilot_admin")))
                .containsExactlyInAnyOrder(
                        "assignment_admin.create",
                        "assignment_admin.end");

        JsonNode principalBindings = provisioningService.execute(
                "principal-bindings",
                packageFile("principal-bindings.synthetic.json"),
                OPERATOR,
                "NW-150-bindings");
        assertThat(principalBindings.path("applied_operations").asInt()).isEqualTo(3);
        assertThat(principalBindings.path("changed_operations").asInt()).isEqualTo(3);
        assertThat(count("auth_principal_bindings")).isEqualTo(3);

        JsonNode bootstrap = provisioningService.execute(
                "assignment-bootstrap",
                packageFile("assignment-bootstrap.synthetic-admin.json"),
                OPERATOR,
                "NW-150-bootstrap");
        assertThat(bootstrap.path("created").asBoolean()).isTrue();
        assertThat(bootstrap.path("assignment_event_id").asText()).isNotBlank();
        assertThat(count("events")).isEqualTo(1);
        assertThat(bootstrapRole()).isEqualTo("pilot_admin");
    }

    private String packageFile(String fileName) throws Exception {
        return Files.readString(Path.of(
                "..",
                "deploy",
                "reference",
                "pilot-packages",
                "stock-operations",
                fileName));
    }

    private List<String> fieldNames(JsonNode fields) {
        return StreamSupport.stream(fields.spliterator(), false)
                .map(field -> field.path("name").asText())
                .toList();
    }

    private List<String> stringValues(JsonNode values) {
        return StreamSupport.stream(values.spliterator(), false)
                .map(JsonNode::asText)
                .toList();
    }

    private JsonNode deploymentConfig(String key) throws Exception {
        String json = jdbcTemplate.queryForObject("""
                SELECT config_json::text
                FROM deployment_config
                WHERE config_key = ?
                """, String.class, key);
        return objectMapper.readTree(json);
    }

    private String bootstrapRole() {
        return jdbcTemplate.queryForObject("""
                SELECT payload->>'role'
                FROM events
                WHERE type = 'assignment_changed'
                """, String.class);
    }

    private int count(String table) {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table, Integer.class);
        return value == null ? 0 : value;
    }

}
