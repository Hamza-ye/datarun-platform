package dev.datarun.server.contracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import dev.datarun.server.config.ShapeService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract tests for the deployer-authored form shape DSL.
 */
class ShapeFormatSchemaContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchema schema = loadSchema("shape-format.schema.json");

    @Test
    void representativeDeployerShape_validatesAgainstSchemaAndCurrentServerValidator() {
        JsonNode shape = parse("""
                {
                  "fields": [
                    {
                      "name": "subject_id",
                      "type": "subject_ref",
                      "required": true,
                      "description": "Subject",
                      "display_order": 1,
                      "group": null,
                      "deprecated": false,
                      "options": null,
                      "validation": null
                    },
                    {
                      "name": "risk_level",
                      "type": "select",
                      "required": true,
                      "display_order": 2,
                      "deprecated": false,
                      "options": ["low", "medium", "high"],
                      "validation": null
                    },
                    {
                      "name": "notes",
                      "type": "narrative",
                      "required": false,
                      "display_order": 3,
                      "group": "review",
                      "deprecated": false,
                      "options": null,
                      "validation": {"max_length": 500}
                    }
                  ],
                  "uniqueness": {
                    "scope": ["subject_ref", "activity_ref", "payload.risk_level"],
                    "period": {"type": "calendar_day", "timezone": "deployment"},
                    "device_action": "warn"
                  },
                  "subject_binding": "subject_id"
                }
                """);

        assertThat(validate(shape)).isEmpty();
        assertThat(new ShapeService(null, objectMapper)
                .validateShape("facility_visit", shape)).isEmpty();
    }

    @Test
    void invalidShapeExamples_failSchemaAndCurrentServerValidatorWhereRuntimeIsStrict() {
        JsonNode missingFields = parse("""
                {"subject_binding": null, "uniqueness": null}
                """);
        assertThat(validate(missingFields)).isNotEmpty();
        assertThat(new ShapeService(null, objectMapper)
                .validateShape("missing_fields", missingFields)).isNotEmpty();

        JsonNode selectWithoutOptions = parse("""
                {
                  "fields": [
                    {"name": "status", "type": "select", "required": true}
                  ],
                  "subject_binding": null,
                  "uniqueness": null
                }
                """);
        assertThat(validate(selectWithoutOptions)).isNotEmpty();
        assertThat(new ShapeService(null, objectMapper)
                .validateShape("bad_select", selectWithoutOptions)).isNotEmpty();

        JsonNode oldUniquenessKey = parse("""
                {
                  "fields": [
                    {"name": "status", "type": "text", "required": false}
                  ],
                  "subject_binding": null,
                  "uniqueness": {"scope": ["payload.status"], "action": "warn"}
                }
                """);
        assertThat(validate(oldUniquenessKey)).isNotEmpty();
        assertThat(new ShapeService(null, objectMapper)
                .validateShape("old_uniqueness", oldUniquenessKey)).isNotEmpty();
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
