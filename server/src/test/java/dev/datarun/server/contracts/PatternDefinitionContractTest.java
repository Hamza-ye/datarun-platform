package dev.datarun.server.contracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import dev.datarun.server.config.PatternRegistry;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract test for platform-owned pattern definitions.
 *
 * <p>{@code contracts/patterns/*.json} is the language-neutral source of truth
 * used by server packaging and mobile runtime config. Pattern definitions are
 * workflow specs, not deployer-authored activity config and not event payload
 * shapes.
 */
class PatternDefinitionContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void canonicalPatternDefinitionsValidateAgainstSchema() throws Exception {
        Path schemaPath = Paths.get("..", "contracts", "pattern-definition.schema.json")
                .toAbsolutePath().normalize();
        Path patternsDir = Paths.get("..", "contracts", "patterns")
                .toAbsolutePath().normalize();

        assertTrue(Files.exists(schemaPath), "Missing: " + schemaPath);
        assertTrue(Files.isDirectory(patternsDir), "Missing: " + patternsDir);

        JsonSchema schema;
        try (var schemaInput = Files.newInputStream(schemaPath)) {
            schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
                    .getSchema(schemaInput);
        }

        try (var files = Files.list(patternsDir)) {
            var patternFiles = files
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
            assertFalse(patternFiles.isEmpty(), "No pattern definition files found");

            for (Path patternFile : patternFiles) {
                JsonNode definition = objectMapper.readTree(patternFile.toFile());
                Set<ValidationMessage> errors = schema.validate(definition);
                assertTrue(errors.isEmpty(),
                        patternFile.getFileName() + " must validate against pattern-definition.schema.json: " + errors);
            }
        }
    }

    @Test
    void serverPatternRegistryLoadsCanonicalDefinitions() {
        PatternRegistry registry = new PatternRegistry(objectMapper);

        assertTrue(registry.find("capture_with_review/v1").isPresent());
        assertTrue(registry.find("ongoing_resolution/v1").isPresent());
        assertTrue(registry.find("multi_step_approval/v1").isPresent());
        assertTrue(registry.find("transfer_with_acknowledgment/v1").isPresent());
        assertTrue(registry.find("ongoing_resolution/v1").orElseThrow().bindingEnabled());
        assertEquals(Set.of("assignment_created/v1", "assignment_ended/v1"),
                registry.find("ongoing_resolution/v1").orElseThrow()
                        .platformShapeRoles().get("transfer"));
        assertTrue(registry.find("capture_with_review/v1").orElseThrow()
                .definitionJson().has("transitions"));
    }
}
