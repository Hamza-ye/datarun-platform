package dev.datarun.server.contracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.config.ActivityService;
import dev.datarun.server.config.ConfigPackager;
import dev.datarun.server.config.FlagSeverityConfigService;
import dev.datarun.server.config.ShapeService;
import dev.datarun.server.config.ActivityRepository;
import dev.datarun.server.config.ShapeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract tests for the server-emitted, mobile-consumed config package shape.
 */
class ConfigPackageSchemaContractTest extends AbstractIntegrationTest {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private ShapeRepository shapeRepository;
    @Autowired private ActivityRepository activityRepository;
    @Autowired private ConfigPackager configPackager;
    @Autowired private FlagSeverityConfigService flagSeverityConfigService;

    private JsonSchema schema;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM config_packages");
        jdbcTemplate.update("DELETE FROM deployment_config");
        jdbcTemplate.update("DELETE FROM expression_rules");
        jdbcTemplate.update("DELETE FROM activities");
        jdbcTemplate.update("DELETE FROM shapes");
        schema = loadSchema("config-package.schema.json");
    }

    @Test
    void publishedCurrentConfigPackage_validatesAgainstRootSchema() {
        JsonNode packageJson = publishRepresentativePackage();

        assertThat(validate(packageJson)).isEmpty();
        assertThat(packageJson.at("/activities/reporting_monitoring/status").asText())
                .isEqualTo("active");
        assertThat(packageJson.at("/pattern_definitions/definitions/capture_with_review~1v1/ref").asText())
                .isEqualTo("capture_with_review/v1");
        assertThat(packageJson.at("/shapes/conflict_detected~1v1").isMissingNode())
                .as("platform payload schemas are not deployer form shapes")
                .isTrue();
    }

    @Test
    void packageSchema_allowsUnknownTopLevelKeysForMobileForwardCompatibility() {
        ObjectNode packageJson = publishRepresentativePackage().deepCopy();
        packageJson.putObject("future_package_section").put("ignored_by_old_mobile", true);

        assertThat(validate(packageJson)).isEmpty();
    }

    @Test
    void packageSchema_rejectsInvalidKnownSections() {
        JsonNode validPackage = publishRepresentativePackage();

        ObjectNode missingActivityStatus = validPackage.deepCopy();
        ((ObjectNode) missingActivityStatus.at("/activities/reporting_monitoring")).remove("status");
        assertThat(validate(missingActivityStatus)).isNotEmpty();

        ObjectNode platformShapeLeak = validPackage.deepCopy();
        JsonNode deployerShape = platformShapeLeak.at("/shapes/report_visit~1v1").deepCopy();
        ((ObjectNode) platformShapeLeak.get("shapes")).set("conflict_detected/v1", deployerShape);
        assertThat(validate(platformShapeLeak)).isNotEmpty();

        ObjectNode nestedSeverity = validPackage.deepCopy();
        ObjectNode nested = objectMapper.createObjectNode();
        nested.put("reporting_monitoring", "blocking");
        ((ObjectNode) nestedSeverity.get("flag_severity_overrides")).set("role_stale", nested);
        assertThat(validate(nestedSeverity)).isNotEmpty();
    }

    private JsonNode publishRepresentativePackage() {
        ShapeService shapeService = new ShapeService(shapeRepository, objectMapper);
        ActivityService activityService =
                new ActivityService(activityRepository, shapeRepository, objectMapper);

        assertThat(shapeService.createShape("report_visit", "elevated", visitShape()))
                .isEmpty();
        assertThat(shapeService.createShape("report_visit_review", "standard", reviewShape()))
                .isEmpty();

        assertThat(activityService.createActivity("reporting_monitoring", "elevated",
                reportingActivity())).isEmpty();

        assertThat(flagSeverityConfigService.updateOverrides(parse("""
                {"role_stale": "informational"}
                """), null)).isEmpty();

        jdbcTemplate.update("""
                INSERT INTO expression_rules
                    (id, activity_ref, shape_ref, field_name, rule_type, expression, message)
                VALUES (?::uuid, ?, ?, ?, ?, ?::jsonb, ?)
                """,
                UUID.randomUUID().toString(),
                "reporting_monitoring",
                "report_visit/v1",
                "risk_level",
                "warning",
                "{\"when\":{\"eq\":[\"payload.risk_level\",\"high\"]}}",
                "High risk visit should be reviewed");

        configPackager.publish(null);
        return configPackager.getLatest().orElseThrow().packageJson();
    }

    private ObjectNode visitShape() {
        ObjectNode schema = objectMapper.createObjectNode();
        ArrayNode fields = schema.putArray("fields");
        addField(fields, "subject_id", "subject_ref", true);
        addField(fields, "risk_level", "select", true, "low", "medium", "high");
        addField(fields, "notes", "narrative", false);
        schema.put("subject_binding", "subject_id");
        schema.set("uniqueness", parse("""
                {
                  "scope": ["subject_ref", "activity_ref", "payload.risk_level"],
                  "period": {"type": "calendar_day", "timezone": "deployment"},
                  "device_action": "warn"
                }
                """));
        return schema;
    }

    private ObjectNode reviewShape() {
        ObjectNode schema = objectMapper.createObjectNode();
        ArrayNode fields = schema.putArray("fields");
        addField(fields, "decision", "select", true, "accepted", "returned");
        addField(fields, "review_note", "narrative", false);
        schema.putNull("subject_binding");
        schema.putNull("uniqueness");
        return schema;
    }

    private ObjectNode reportingActivity() {
        ObjectNode config = objectMapper.createObjectNode();
        config.putArray("shapes").add("report_visit/v1").add("report_visit_review/v1");
        ObjectNode roles = config.putObject("roles");
        roles.putArray("field_worker").add("capture");
        roles.putArray("supervisor").add("review");
        config.set("pattern", parse("""
                {
                  "subject": null,
                  "event": [
                    {
                      "ref": "capture_with_review/v1",
                      "composition": "event",
                      "shape_roles": {
                        "review_decision": ["report_visit_review/v1"]
                      },
                      "activation_roles": {
                        "on_shapes": ["report_visit/v1"]
                      },
                      "participant_roles": {
                        "capturer": ["field_worker"],
                        "reviewer": ["supervisor"]
                      },
                      "parameters": {}
                    }
                  ]
                }
                """));
        return config;
    }

    private void addField(ArrayNode fields, String name, String type,
                          boolean required, String... options) {
        ObjectNode field = objectMapper.createObjectNode();
        field.put("name", name);
        field.put("type", type);
        field.put("required", required);
        field.put("deprecated", false);
        field.put("display_order", fields.size() + 1);
        if (options.length > 0) {
            ArrayNode optionArray = field.putArray("options");
            for (String option : options) {
                optionArray.add(option);
            }
        } else {
            field.putNull("options");
        }
        field.putNull("validation");
        fields.add(field);
    }

    private Set<ValidationMessage> validate(JsonNode node) {
        return schema.validate(node);
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Bad test JSON", e);
        }
    }

    private JsonSchema loadSchema(String filename) {
        Path schemaPath = Paths.get("..", "contracts", filename)
                .toAbsolutePath().normalize();
        assertThat(Files.exists(schemaPath)).as("Missing " + schemaPath).isTrue();
        try (var input = Files.newInputStream(schemaPath)) {
            return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
                    .getSchema(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + filename, e);
        }
    }
}
