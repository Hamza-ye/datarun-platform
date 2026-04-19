package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Config Packager: assembles validated configuration into atomic JSON payload (IDR-019).
 * Package format uses the full IDR-019 envelope from day one — empty sections
 * for artifacts not yet populated (expressions, triggers).
 */
@Service
public class ConfigPackager {

    private final ShapeRepository shapeRepository;
    private final ActivityRepository activityRepository;
    private final ExpressionRepository expressionRepository;
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ConfigPackager(ShapeRepository shapeRepository,
                          ActivityRepository activityRepository,
                          ExpressionRepository expressionRepository,
                          JdbcTemplate jdbc,
                          ObjectMapper objectMapper) {
        this.shapeRepository = shapeRepository;
        this.activityRepository = activityRepository;
        this.expressionRepository = expressionRepository;
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    /**
     * Assemble and store a new config package. Returns the new version number.
     * Fetches all shapes (including deprecated) and active activities.
     */
    public int publish(java.util.UUID publishedBy) {
        ObjectNode packageJson = objectMapper.createObjectNode();

        // Shapes: all versions, keyed by shape_ref (IDR-019: "all shape versions including deprecated")
        ObjectNode shapesNode = objectMapper.createObjectNode();
        for (Shape shape : shapeRepository.findAll()) {
            ObjectNode shapeEntry = objectMapper.createObjectNode();
            shapeEntry.put("name", shape.name());
            shapeEntry.put("version", shape.version());
            shapeEntry.put("status", shape.status());
            shapeEntry.put("sensitivity", shape.sensitivity());
            // Flatten schema_json fields into shape entry
            JsonNode schemaJson = shape.schemaJson();
            if (schemaJson.has("fields")) {
                shapeEntry.set("fields", schemaJson.get("fields"));
            }
            shapeEntry.set("subject_binding", schemaJson.get("subject_binding"));
            shapeEntry.set("uniqueness", schemaJson.get("uniqueness"));
            shapesNode.set(shape.shapeRef(), shapeEntry);
        }
        packageJson.set("shapes", shapesNode);

        // Activities: all active, keyed by name
        ObjectNode activitiesNode = objectMapper.createObjectNode();
        for (Activity activity : activityRepository.findActive()) {
            ObjectNode activityEntry = objectMapper.createObjectNode();
            activityEntry.put("name", activity.name());
            activityEntry.put("sensitivity", activity.sensitivity());
            JsonNode config = activity.configJson();
            if (config.has("shapes")) activityEntry.set("shapes", config.get("shapes"));
            if (config.has("roles")) activityEntry.set("roles", config.get("roles"));
            activityEntry.putNull("pattern"); // Phase 4
            activitiesNode.set(activity.name(), activityEntry);
        }
        packageJson.set("activities", activitiesNode);

        // Expressions: grouped by "{activity_ref}.{shape_ref}" key (IDR-018/IDR-019)
        ObjectNode expressionsNode = objectMapper.createObjectNode();
        List<ExpressionRule> allRules = expressionRepository.findAll();
        Map<String, ArrayNode> groupedRules = new LinkedHashMap<>();
        for (ExpressionRule rule : allRules) {
            String key = rule.activityRef() + "." + rule.shapeRef();
            groupedRules.computeIfAbsent(key, k -> objectMapper.createArrayNode());
            ObjectNode ruleNode = objectMapper.createObjectNode();
            ruleNode.put("field_name", rule.fieldName());
            ruleNode.put("rule_type", rule.ruleType());
            ruleNode.set("expression", rule.expression());
            if (rule.message() != null) {
                ruleNode.put("message", rule.message());
            }
            groupedRules.get(key).add(ruleNode);
        }
        for (var entry : groupedRules.entrySet()) {
            expressionsNode.set(entry.getKey(), entry.getValue());
        }
        packageJson.set("expressions", expressionsNode);

        // Flag severity overrides: empty stub for Phase 3 (IDR-019)
        packageJson.set("flag_severity_overrides", objectMapper.createObjectNode());

        // Sensitivity classifications
        ObjectNode sensClassNode = objectMapper.createObjectNode();
        ObjectNode shapesSens = objectMapper.createObjectNode();
        for (Shape shape : shapeRepository.findAll()) {
            shapesSens.put(shape.shapeRef(), shape.sensitivity());
        }
        sensClassNode.set("shapes", shapesSens);
        ObjectNode actSens = objectMapper.createObjectNode();
        for (Activity activity : activityRepository.findActive()) {
            actSens.put(activity.name(), activity.sensitivity());
        }
        sensClassNode.set("activities", actSens);
        packageJson.set("sensitivity_classifications", sensClassNode);

        // Determine next version
        Integer maxVersion = jdbc.queryForObject(
                "SELECT COALESCE(MAX(version), 0) FROM config_packages", Integer.class);
        int nextVersion = (maxVersion != null ? maxVersion : 0) + 1;

        // Add metadata to package
        packageJson.put("version", nextVersion);
        packageJson.put("published_at", OffsetDateTime.now().toString());

        // Store
        jdbc.update("""
                INSERT INTO config_packages (version, package_json, published_by)
                VALUES (?, ?::jsonb, ?::uuid)
                """,
                nextVersion,
                packageJson.toString(),
                publishedBy != null ? publishedBy.toString() : null);

        return nextVersion;
    }

    /**
     * Get the latest published config package.
     */
    public Optional<ConfigPackage> getLatest() {
        List<ConfigPackage> results = jdbc.query("""
                SELECT version, package_json, published_at, published_by
                FROM config_packages ORDER BY version DESC LIMIT 1
                """,
                (rs, rowNum) -> {
                    try {
                        return new ConfigPackage(
                                rs.getInt("version"),
                                objectMapper.readTree(rs.getString("package_json")),
                                rs.getTimestamp("published_at").toInstant()
                                        .atOffset(java.time.ZoneOffset.UTC));
                    } catch (Exception e) {
                        throw new java.sql.SQLException("Failed to parse package_json", e);
                    }
                });
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Get the latest config version number (0 if none published).
     */
    public int getLatestVersion() {
        Integer v = jdbc.queryForObject(
                "SELECT COALESCE(MAX(version), 0) FROM config_packages", Integer.class);
        return v != null ? v : 0;
    }

    public record ConfigPackage(int version, JsonNode packageJson, OffsetDateTime publishedAt) {}
}
