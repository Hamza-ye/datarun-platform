package dev.datarun.server.ops.provisioning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OneShotProvisioningIntegrationTest extends AbstractIntegrationTest {

    private static final UUID OPERATOR =
            UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID ACTOR =
            UUID.fromString("40000000-0000-0000-0000-000000000002");
    private static final UUID OTHER_ACTOR =
            UUID.fromString("40000000-0000-0000-0000-000000000003");
    private static final UUID EXPRESSION_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final OffsetDateTime VALID_FROM =
            OffsetDateTime.parse("2026-06-14T00:00:00Z");

    @Autowired
    private OneShotProvisioningService provisioningService;

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
    void principalBindingCommandUsesAcceptedProvisionerAndReappliesIdempotently()
            throws Exception {
        String manifest = objectMapper.writeValueAsString(Map.of(
                "manifest_version", "reviewed-bindings/v1",
                "source", "change:binding-001",
                "operations", List.of(Map.of(
                        "operation_id", "bind-reviewed-actor",
                        "issuer", "https://idp.example.test",
                        "subject", "reviewed-user",
                        "actor_id", ACTOR.toString(),
                        "state", "active",
                        "reason", "approved initial binding"))));

        byte[] firstInput = manifest.getBytes(StandardCharsets.UTF_8);
        byte[] secondInput = (manifest + "\n").getBytes(StandardCharsets.UTF_8);
        JsonNode first = provisioningService.execute(
                "principal-bindings", firstInput, OPERATOR, "CHG-001");
        JsonNode second = provisioningService.execute(
                "principal-bindings", secondInput, OPERATOR, "CHG-001");

        assertThat(first.path("applied_operations").asInt()).isEqualTo(1);
        assertThat(first.path("changed_operations").asInt()).isEqualTo(1);
        assertThat(second.path("applied_operations").asInt()).isZero();
        assertThat(second.path("skipped_operations").asInt()).isEqualTo(1);
        assertThat(first.path("input_sha256").asText()).isEqualTo(sha256(firstInput));
        assertThat(second.path("input_sha256").asText()).isEqualTo(sha256(secondInput));
        assertThat(second.path("input_sha256").asText())
                .isNotEqualTo(first.path("input_sha256").asText());
        assertThat(count("auth_principal_bindings")).isEqualTo(1);
        assertThat(count("auth_principal_binding_operations")).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("""
                SELECT applied_by
                FROM auth_principal_binding_operations
                WHERE operation_id = 'bind-reviewed-actor'
                """, String.class))
                .isEqualTo("operator:" + OPERATOR + ";evidence:CHG-001");
    }

    @Test
    void reviewedConfigPublishesOnceAndExactReapplySkipsNewVersion()
            throws Exception {
        String manifest = reviewedConfigManifest("capture");

        JsonNode first = provisioningService.execute(
                "config-publish", manifest, OPERATOR, "CHG-002");
        JsonNode second = provisioningService.execute(
                "config-publish", manifest, OPERATOR, "CHG-002");

        assertThat(first.path("config_version").asInt()).isEqualTo(1);
        assertThat(first.path("published").asBoolean()).isTrue();
        assertThat(first.path("changed_authoring_rows").asInt()).isGreaterThan(0);
        assertThat(second.path("config_version").asInt()).isEqualTo(1);
        assertThat(second.path("published").asBoolean()).isFalse();
        assertThat(second.path("changed_authoring_rows").asInt()).isZero();
        assertThat(first.path("input_sha256").asText()).isEqualTo(sha256(manifest));
        assertThat(second.path("input_sha256").asText()).isEqualTo(sha256(manifest));
        assertThat(count("shapes")).isEqualTo(1);
        assertThat(count("activities")).isEqualTo(1);
        assertThat(count("expression_rules")).isEqualTo(1);
        assertThat(count("config_packages")).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("""
                SELECT published_by
                FROM config_packages
                WHERE version = 1
                """, UUID.class)).isEqualTo(OPERATOR);
    }

    @Test
    void invalidReviewedConfigRollsBackAuthoringAndPublication() throws Exception {
        String invalid = reviewedConfigManifest("not_an_envelope_action");

        assertThatThrownBy(() -> provisioningService.execute(
                "config-publish", invalid, OPERATOR, "CHG-003"))
                .isInstanceOf(ProvisioningCommandException.class)
                .hasMessageContaining("activity field_visit is invalid");

        assertThat(count("shapes")).isZero();
        assertThat(count("activities")).isZero();
        assertThat(count("expression_rules")).isZero();
        assertThat(count("config_packages")).isZero();
    }

    @Test
    void bootstrapCreatesOnceSkipsExactReapplyAndRejectsDrift()
            throws Exception {
        String exact = bootstrapManifest(ACTOR, "admin");

        JsonNode first = provisioningService.execute(
                "assignment-bootstrap", exact, OPERATOR, "CHG-004");
        JsonNode second = provisioningService.execute(
                "assignment-bootstrap", exact, OPERATOR, "CHG-004");

        assertThat(first.path("created").asBoolean()).isTrue();
        assertThat(second.path("created").asBoolean()).isFalse();
        assertThat(second.path("assignment_event_id").asText())
                .isEqualTo(first.path("assignment_event_id").asText());
        assertThat(first.path("input_sha256").asText()).isEqualTo(sha256(exact));
        assertThat(second.path("input_sha256").asText()).isEqualTo(sha256(exact));
        assertThat(countAssignmentEvents()).isEqualTo(1);

        String drift = bootstrapManifest(OTHER_ACTOR, "admin");
        assertThatThrownBy(() -> provisioningService.execute(
                "assignment-bootstrap", drift, OPERATOR, "CHG-005"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existing assignment state differs");
        assertThat(countAssignmentEvents()).isEqualTo(1);
    }

    @Test
    void commandRequiresExplicitEvidenceAndRejectsInvalidBootstrapInput()
            throws Exception {
        assertThatThrownBy(() -> provisioningService.execute(
                "assignment-bootstrap", bootstrapManifest(ACTOR, "admin"),
                OPERATOR, " "))
                .isInstanceOf(ProvisioningCommandException.class)
                .hasMessageContaining("evidence_id");

        Map<String, Object> invalid = new LinkedHashMap<>();
        invalid.put("schema_version", 1);
        invalid.put("source", "change:bootstrap-invalid");
        invalid.put("target_actor_id", ACTOR);
        invalid.put("role", "admin");
        invalid.put("valid_from", VALID_FROM);
        invalid.put("valid_to", VALID_FROM.minusMinutes(1));

        assertThatThrownBy(() -> provisioningService.execute(
                "assignment-bootstrap", objectMapper.writeValueAsString(invalid),
                OPERATOR, "CHG-006"))
                .isInstanceOf(ProvisioningCommandException.class)
                .hasMessageContaining("valid_to must be after valid_from");
        assertThat(countAssignmentEvents()).isZero();

        ObjectNode unknownField = (ObjectNode) objectMapper.readTree(
                bootstrapManifest(ACTOR, "admin"));
        unknownField.put("creator_actor_id", OPERATOR.toString());
        assertThatThrownBy(() -> provisioningService.execute(
                "assignment-bootstrap", objectMapper.writeValueAsString(unknownField),
                OPERATOR, "CHG-007"))
                .isInstanceOf(ProvisioningCommandException.class)
                .hasMessageContaining("invalid bootstrap JSON");
    }

    private String reviewedConfigManifest(String action) throws Exception {
        ObjectNode schema = objectMapper.createObjectNode();
        ArrayNode fields = schema.putArray("fields");
        fields.addObject()
                .put("name", "notes")
                .put("type", "text")
                .put("required", false);
        schema.putNull("subject_binding");
        schema.putNull("uniqueness");

        ObjectNode activityConfig = objectMapper.createObjectNode();
        activityConfig.putArray("shapes").add("visit/v1");
        activityConfig.putObject("roles").putArray("worker").add(action);

        ObjectNode expression = objectMapper.createObjectNode();
        expression.putObject("value").put("ref", "context.actor.scope_name");

        ObjectNode capabilities = objectMapper.createObjectNode();
        capabilities.put("schema_version", 1);
        capabilities.putObject("roles").putArray("admin")
                .add("assignment_admin.create")
                .add("assignment_admin.end");

        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("schema_version", 1);
        manifest.put("source", "change:config-001");
        manifest.put("shapes", List.of(Map.of(
                "name", "visit",
                "version", 1,
                "status", "active",
                "sensitivity", "standard",
                "schema_json", schema)));
        manifest.put("activities", List.of(Map.of(
                "name", "field_visit",
                "status", "active",
                "sensitivity", "standard",
                "config_json", activityConfig)));
        manifest.put("expressions", List.of(Map.of(
                "id", EXPRESSION_ID,
                "activity_ref", "field_visit",
                "shape_ref", "visit/v1",
                "field_name", "notes",
                "rule_type", "default",
                "expression", expression)));
        manifest.put("flag_severity_overrides", objectMapper.createObjectNode());
        manifest.put("assignment_admin_capabilities", capabilities);
        return objectMapper.writeValueAsString(manifest);
    }

    private String bootstrapManifest(UUID actorId, String role) throws Exception {
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("schema_version", 1);
        manifest.put("source", "change:bootstrap-001");
        manifest.put("target_actor_id", actorId);
        manifest.put("role", role);
        manifest.put("geographic_id", null);
        manifest.put("subject_list", null);
        manifest.put("activity_list", null);
        manifest.put("valid_from", VALID_FROM);
        manifest.put("valid_to", null);
        return objectMapper.writeValueAsString(manifest);
    }

    private int count(String table) {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table, Integer.class);
        return value == null ? 0 : value;
    }

    private int countAssignmentEvents() {
        Integer value = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE type = 'assignment_changed'
                """, Integer.class);
        return value == null ? 0 : value;
    }

    private String sha256(String input) throws Exception {
        return sha256(input.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256(byte[] input) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(input));
    }
}
