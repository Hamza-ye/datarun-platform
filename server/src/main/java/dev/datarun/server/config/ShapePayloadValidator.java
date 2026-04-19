package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validates event payloads against their shape definition.
 * Structural validation only — rejects malformed payloads at push time.
 * This is NOT accept-and-flag; structurally invalid payloads are rejected (400).
 */
@Component
public class ShapePayloadValidator {

    private final ShapeRepository shapeRepository;

    public ShapePayloadValidator(ShapeRepository shapeRepository) {
        this.shapeRepository = shapeRepository;
    }

    /**
     * Validate a payload against the shape referenced by shape_ref.
     * Returns empty list if valid, or list of violation messages.
     * Returns empty list if shape is not found (unknown shapes are not rejected —
     * they may be from future versions or pre-Phase-3 hardcoded shapes).
     */
    public List<String> validate(String shapeRef, JsonNode payload) {
        if (shapeRef == null || payload == null) return List.of();

        String[] parsed = ShapeService.parseShapeRef(shapeRef);
        if (parsed == null) return List.of(); // Can't parse → skip validation

        Optional<Shape> shapeOpt = shapeRepository.findByNameAndVersion(
                parsed[0], Integer.parseInt(parsed[1]));
        if (shapeOpt.isEmpty()) return List.of(); // Unknown shape → pass through

        Shape shape = shapeOpt.get();
        JsonNode fieldsNode = shape.schemaJson().get("fields");
        if (fieldsNode == null || !fieldsNode.isArray()) return List.of();

        List<String> violations = new ArrayList<>();

        // Build field definitions map
        Map<String, JsonNode> fieldDefs = new LinkedHashMap<>();
        for (int i = 0; i < fieldsNode.size(); i++) {
            JsonNode field = fieldsNode.get(i);
            fieldDefs.put(field.path("name").asText(""), field);
        }

        // Check required fields present
        for (var entry : fieldDefs.entrySet()) {
            String fieldName = entry.getKey();
            JsonNode fieldDef = entry.getValue();
            boolean required = fieldDef.path("required").asBoolean(false);
            boolean deprecated = fieldDef.path("deprecated").asBoolean(false);

            if (required && !deprecated) {
                if (!payload.has(fieldName) || payload.get(fieldName).isNull()) {
                    violations.add("Required field '" + fieldName + "' is missing");
                }
            }
        }

        // Check field types match
        Iterator<String> payloadFields = payload.fieldNames();
        while (payloadFields.hasNext()) {
            String fieldName = payloadFields.next();
            JsonNode fieldDef = fieldDefs.get(fieldName);
            if (fieldDef == null) {
                // Unknown fields are allowed (forward compatibility)
                continue;
            }

            JsonNode value = payload.get(fieldName);
            if (value.isNull()) continue; // Null is always acceptable for non-required

            String expectedType = fieldDef.path("type").asText("");
            String typeError = validateFieldType(fieldName, value, expectedType, fieldDef);
            if (typeError != null) {
                violations.add(typeError);
            }
        }

        return violations;
    }

    private String validateFieldType(String fieldName, JsonNode value, String expectedType, JsonNode fieldDef) {
        return switch (expectedType) {
            case "text", "narrative" -> {
                if (!value.isTextual())
                    yield "Field '" + fieldName + "': expected text, got " + value.getNodeType();
                yield null;
            }
            case "integer" -> {
                if (!value.isInt() && !value.isLong())
                    yield "Field '" + fieldName + "': expected integer, got " + value.getNodeType();
                yield null;
            }
            case "decimal" -> {
                if (!value.isNumber())
                    yield "Field '" + fieldName + "': expected number, got " + value.getNodeType();
                yield null;
            }
            case "boolean" -> {
                if (!value.isBoolean())
                    yield "Field '" + fieldName + "': expected boolean, got " + value.getNodeType();
                yield null;
            }
            case "date" -> {
                if (!value.isTextual())
                    yield "Field '" + fieldName + "': expected date string, got " + value.getNodeType();
                yield null;
            }
            case "select" -> {
                if (!value.isTextual())
                    yield "Field '" + fieldName + "': expected string for select, got " + value.getNodeType();
                // Validate against options
                JsonNode options = fieldDef.get("options");
                if (options != null && options.isArray()) {
                    boolean found = false;
                    for (JsonNode opt : options) {
                        if (opt.asText("").equals(value.asText())) { found = true; break; }
                    }
                    if (!found)
                        yield "Field '" + fieldName + "': value '" + value.asText() + "' not in options";
                }
                yield null;
            }
            case "multi_select" -> {
                if (!value.isArray())
                    yield "Field '" + fieldName + "': expected array for multi_select, got " + value.getNodeType();
                // Validate each element against options
                JsonNode options = fieldDef.get("options");
                if (options != null && options.isArray() && value.isArray()) {
                    Set<String> validOpts = new HashSet<>();
                    for (JsonNode opt : options) validOpts.add(opt.asText(""));
                    for (JsonNode elem : value) {
                        if (!validOpts.contains(elem.asText("")))
                            yield "Field '" + fieldName + "': value '" + elem.asText() + "' not in options";
                    }
                }
                yield null;
            }
            case "location", "subject_ref" -> {
                if (!value.isTextual())
                    yield "Field '" + fieldName + "': expected UUID string, got " + value.getNodeType();
                yield null;
            }
            default -> null; // Unknown type → skip
        };
    }
}
