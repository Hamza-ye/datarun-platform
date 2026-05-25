package dev.datarun.server.contracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runtime bridge for platform-owned payload contracts under contracts/shapes.
 *
 * <p>These JSON Schemas are not deployer-authored shape DSL definitions. They describe
 * platform-administered payloads such as assignments, identity lifecycle events, and
 * integrity flags.
 */
@Component
public class PlatformPayloadContractValidator {

    private final Map<String, JsonSchema> schemas;

    public PlatformPayloadContractValidator(ObjectMapper objectMapper) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        Map<String, JsonSchema> loaded = new LinkedHashMap<>();
        PlatformPayloadShapes.RESOURCE_BY_SHAPE_REF.forEach((shapeRef, resourcePath) -> {
            try (InputStream input = new ClassPathResource(resourcePath).getInputStream()) {
                JsonNode schemaJson = objectMapper.readTree(input);
                loaded.put(shapeRef, factory.getSchema(schemaJson));
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Missing or unreadable platform payload contract " + resourcePath
                                + " for " + shapeRef, e);
            }
        });
        this.schemas = Map.copyOf(loaded);
    }

    public boolean isPlatformPayloadShape(String shapeRef) {
        return schemas.containsKey(shapeRef);
    }

    public Set<String> shapeRefs() {
        return schemas.keySet();
    }

    public List<String> validate(String shapeRef, JsonNode payload) {
        JsonSchema schema = schemas.get(shapeRef);
        if (schema == null) {
            return List.of();
        }
        JsonNode effectivePayload = payload == null ? NullNode.getInstance() : payload;
        return schema.validate(effectivePayload).stream()
                .map(ValidationMessage::getMessage)
                .sorted()
                .toList();
    }

    public void requireValid(String shapeRef, JsonNode payload) {
        List<String> violations = validate(shapeRef, payload);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                    "Payload for " + shapeRef + " violates platform contract: " + violations);
        }
    }
}
