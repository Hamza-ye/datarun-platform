package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Config Packager: assembles validated configuration into atomic JSON payload.
 * Package format uses the accepted full snapshot shape — empty sections
 * for artifacts not yet populated (expressions, triggers).
 */
@Service
public class ConfigPackager {

    private static final long PUBLICATION_LOCK_ID = 0x4441544152554E43L;

    private final ShapeRepository shapeRepository;
    private final ActivityRepository activityRepository;
    private final ExpressionRepository expressionRepository;
    private final DeployTimeValidator deployTimeValidator;
    private final FlagSeverityConfigService flagSeverityConfigService;
    private final PatternRegistry patternRegistry;
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ConfigPackager(ShapeRepository shapeRepository,
                          ActivityRepository activityRepository,
                          ExpressionRepository expressionRepository,
                          DeployTimeValidator deployTimeValidator,
                          FlagSeverityConfigService flagSeverityConfigService,
                          PatternRegistry patternRegistry,
                          JdbcTemplate jdbc,
                          ObjectMapper objectMapper) {
        this.shapeRepository = shapeRepository;
        this.activityRepository = activityRepository;
        this.expressionRepository = expressionRepository;
        this.deployTimeValidator = deployTimeValidator;
        this.flagSeverityConfigService = flagSeverityConfigService;
        this.patternRegistry = patternRegistry;
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    /**
     * Assemble and store a new config package. Returns the new version number.
     * Fetches all shapes (including deprecated) and active activities.
     */
    @Transactional
    public int publish(java.util.UUID publishedBy) {
        lockPublication();
        return storePackage(buildPackage(), publishedBy);
    }

    /**
     * Publish only when the validated package content differs from the latest
     * immutable package. Metadata fields are excluded from the comparison.
     */
    @Transactional
    public PublicationResult publishIfChanged(java.util.UUID publishedBy) {
        lockPublication();
        ObjectNode packageJson = buildPackage();
        Optional<ConfigPackage> latest = getLatest();
        if (latest.isPresent() && packageContent(latest.get().packageJson()).equals(packageJson)) {
            return new PublicationResult(latest.get().version(), false);
        }
        return new PublicationResult(storePackage(packageJson, publishedBy), true);
    }

    /**
     * Assemble a validated package without assigning publication metadata.
     */
    public ObjectNode buildPackage() {
        List<String> violations = deployTimeValidator.validateAll();
        if (!violations.isEmpty()) {
            throw new IllegalStateException("DtV violations: " + String.join("; ", violations));
        }

        ObjectNode packageJson = objectMapper.createObjectNode();

        // Shapes: all versions, keyed by shape_ref, including deprecated versions.
        ObjectNode shapesNode = objectMapper.createObjectNode();
        for (Shape shape : shapeRepository.findAll()) {
            if (ShapeService.isPlatformShapeName(shape.name())) {
                continue;
            }
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
        Set<String> referencedPatternRefs = new LinkedHashSet<>();
        for (Activity activity : activityRepository.findActive()) {
            ObjectNode activityEntry = objectMapper.createObjectNode();
            activityEntry.put("name", activity.name());
            activityEntry.put("status", activity.status());
            activityEntry.put("sensitivity", activity.sensitivity());
            JsonNode config = activity.configJson();
            if (config.has("shapes")) activityEntry.set("shapes", config.get("shapes"));
            if (config.has("roles")) activityEntry.set("roles", config.get("roles"));
            if (config.has("pattern")) {
                activityEntry.set("pattern", config.get("pattern"));
                collectPatternRefs(config.get("pattern"), referencedPatternRefs);
            } else {
                activityEntry.putNull("pattern");
            }
            activitiesNode.set(activity.name(), activityEntry);
        }
        packageJson.set("activities", activitiesNode);
        packageJson.set("pattern_definitions", patternRegistry.packageDefinitions(referencedPatternRefs));

        // Expressions: grouped by "{activity_ref}.{shape_ref}" key.
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

        // Flag severity overrides: deployment-wide L0 map (IDR-022).
        packageJson.set("flag_severity_overrides", flagSeverityConfigService.getValidatedOverrides());

        // Sensitivity classifications
        ObjectNode sensClassNode = objectMapper.createObjectNode();
        ObjectNode shapesSens = objectMapper.createObjectNode();
        for (Shape shape : shapeRepository.findAll()) {
            if (ShapeService.isPlatformShapeName(shape.name())) {
                continue;
            }
            shapesSens.put(shape.shapeRef(), shape.sensitivity());
        }
        sensClassNode.set("shapes", shapesSens);
        ObjectNode actSens = objectMapper.createObjectNode();
        for (Activity activity : activityRepository.findActive()) {
            actSens.put(activity.name(), activity.sensitivity());
        }
        sensClassNode.set("activities", actSens);
        packageJson.set("sensitivity_classifications", sensClassNode);

        return packageJson;
    }

    private int storePackage(ObjectNode packageJson, java.util.UUID publishedBy) {
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

    private ObjectNode packageContent(JsonNode packageJson) {
        ObjectNode content = (ObjectNode) packageJson.deepCopy();
        content.remove("version");
        content.remove("published_at");
        return content;
    }

    private void lockPublication() {
        jdbc.queryForList("SELECT pg_advisory_xact_lock(?)", PUBLICATION_LOCK_ID);
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
    public record PublicationResult(int version, boolean published) {}

    private void collectPatternRefs(JsonNode patternNode, Set<String> refs) {
        if (patternNode == null || !patternNode.isObject()) {
            return;
        }
        collectPatternRef(patternNode.get("subject"), refs);
        JsonNode eventBindings = patternNode.get("event");
        if (eventBindings != null && eventBindings.isArray()) {
            for (JsonNode binding : eventBindings) {
                collectPatternRef(binding, refs);
            }
        }
    }

    private void collectPatternRef(JsonNode binding, Set<String> refs) {
        if (binding == null || !binding.isObject()) {
            return;
        }
        JsonNode ref = binding.get("ref");
        if (ref != null && ref.isTextual()) {
            refs.add(ref.asText());
        }
    }
}
