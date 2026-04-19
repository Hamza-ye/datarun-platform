package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Shape lifecycle management: create, version, deprecate, validate.
 * DtV L1 checks are applied on save — field count, type vocabulary, name format, duplicates.
 */
@Service
public class ShapeService {

    private static final int MAX_FIELDS = 60;
    private static final Set<String> VALID_TYPES = Set.of(
            "text", "integer", "decimal", "boolean", "date",
            "select", "multi_select", "location", "subject_ref", "narrative"
    );
    public static final List<String> VALID_TYPES_LIST = List.of(
            "text", "integer", "decimal", "boolean", "date",
            "select", "multi_select", "location", "subject_ref", "narrative"
    );
    private static final java.util.regex.Pattern FIELD_NAME_PATTERN =
            java.util.regex.Pattern.compile("^[a-z][a-z0-9_]*$");
    private static final java.util.regex.Pattern SHAPE_NAME_PATTERN =
            java.util.regex.Pattern.compile("^[a-z][a-z0-9_]*$");

    private final ShapeRepository shapeRepository;
    private final ObjectMapper objectMapper;

    public ShapeService(ShapeRepository shapeRepository, ObjectMapper objectMapper) {
        this.shapeRepository = shapeRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Validate a shape's schema_json and return any violations.
     * Empty list = valid.
     */
    public List<String> validateShape(String name, JsonNode schemaJson) {
        List<String> violations = new ArrayList<>();

        // Shape name format
        if (!SHAPE_NAME_PATTERN.matcher(name).matches()) {
            violations.add("Invalid shape name '" + name + "': must match [a-z][a-z0-9_]*");
        }

        // Fields array required
        JsonNode fieldsNode = schemaJson.get("fields");
        if (fieldsNode == null || !fieldsNode.isArray()) {
            violations.add("Shape must have a 'fields' array");
            return violations;
        }

        // Field count budget
        if (fieldsNode.size() > MAX_FIELDS) {
            violations.add("Shape has " + fieldsNode.size() + " fields — exceeds " + MAX_FIELDS + "-field budget");
        }

        // Per-field validation
        Set<String> fieldNames = new HashSet<>();
        for (int i = 0; i < fieldsNode.size(); i++) {
            JsonNode field = fieldsNode.get(i);
            String fieldName = field.path("name").asText("");
            String fieldType = field.path("type").asText("");
            String prefix = "Field[" + i + "] '" + fieldName + "': ";

            // Field name format
            if (!FIELD_NAME_PATTERN.matcher(fieldName).matches()) {
                violations.add(prefix + "name must match [a-z][a-z0-9_]*");
            }

            // Duplicate field names
            if (!fieldNames.add(fieldName)) {
                violations.add(prefix + "duplicate field name");
            }

            // Type vocabulary
            if (!VALID_TYPES.contains(fieldType)) {
                violations.add(prefix + "unknown type '" + fieldType + "'");
            }

            // Options required for select/multi_select
            if ("select".equals(fieldType) || "multi_select".equals(fieldType)) {
                JsonNode options = field.get("options");
                if (options == null || !options.isArray() || options.isEmpty()) {
                    violations.add(prefix + "select/multi_select requires non-empty 'options' array");
                }
            }

            // required must be boolean
            if (field.has("required") && !field.get("required").isBoolean()) {
                violations.add(prefix + "'required' must be boolean");
            }
        }

        // subject_binding validation
        JsonNode bindingNode = schemaJson.get("subject_binding");
        if (bindingNode != null && !bindingNode.isNull()) {
            String binding = bindingNode.asText("");
            if (!binding.isEmpty()) {
                boolean found = false;
                for (int i = 0; i < fieldsNode.size(); i++) {
                    JsonNode field = fieldsNode.get(i);
                    if (binding.equals(field.path("name").asText(""))) {
                        if (!"subject_ref".equals(field.path("type").asText(""))) {
                            violations.add("subject_binding '" + binding + "' must reference a 'subject_ref' field");
                        }
                        if (!field.path("required").asBoolean(false)) {
                            violations.add("subject_binding '" + binding + "' must reference a required field");
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    violations.add("subject_binding '" + binding + "' references non-existent field");
                }
            }
        }

        return violations;
    }

    /**
     * Create a new shape (version 1).
     * Returns violations if invalid, empty list on success.
     */
    public List<String> createShape(String name, String sensitivity, JsonNode schemaJson) {
        List<String> violations = validateShape(name, schemaJson);
        if (!violations.isEmpty()) return violations;

        if (shapeRepository.exists(name, 1)) {
            return List.of("Shape '" + name + "' version 1 already exists");
        }

        Shape shape = new Shape(name, 1, "active", sensitivity, schemaJson, null);
        shapeRepository.insert(shape);
        return List.of();
    }

    /**
     * Create a new version of an existing shape.
     * Version number = latest + 1.
     */
    public List<String> createVersion(String name, String sensitivity, JsonNode schemaJson) {
        List<String> violations = validateShape(name, schemaJson);
        if (!violations.isEmpty()) return violations;

        Optional<Shape> latest = shapeRepository.findLatestVersion(name);
        if (latest.isEmpty()) {
            return List.of("Shape '" + name + "' does not exist — use create instead");
        }

        int nextVersion = latest.get().version() + 1;
        Shape shape = new Shape(name, nextVersion, "active", sensitivity, schemaJson, null);
        shapeRepository.insert(shape);
        return List.of();
    }

    /**
     * Deprecate a shape version. Hidden from new form creation but events still project.
     */
    public void deprecate(String name, int version) {
        if (!shapeRepository.exists(name, version)) {
            throw new IllegalArgumentException("Shape '" + name + "/v" + version + "' not found");
        }
        shapeRepository.updateStatus(name, version, "deprecated");
    }

    public Optional<Shape> getShape(String name, int version) {
        return shapeRepository.findByNameAndVersion(name, version);
    }

    /**
     * Parse a shape_ref string "{name}/v{version}" into name and version.
     * Returns null if format is invalid.
     */
    public static String[] parseShapeRef(String shapeRef) {
        if (shapeRef == null) return null;
        int slashIdx = shapeRef.lastIndexOf("/v");
        if (slashIdx < 1) return null;
        String name = shapeRef.substring(0, slashIdx);
        try {
            int version = Integer.parseInt(shapeRef.substring(slashIdx + 2));
            return new String[]{name, String.valueOf(version)};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<Shape> getAllShapes() {
        return shapeRepository.findAll();
    }

    public List<Shape> getShapeVersions(String name) {
        return shapeRepository.findByName(name);
    }

    /**
     * Get distinct shape names.
     */
    public List<String> getShapeNames() {
        return shapeRepository.findAll().stream()
                .map(Shape::name)
                .distinct()
                .toList();
    }
}
