package dev.datarun.server.ops.provisioning;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.authorization.AssignmentAdminCapabilityService;
import dev.datarun.server.config.Activity;
import dev.datarun.server.config.ActivityService;
import dev.datarun.server.config.ConfigPackager;
import dev.datarun.server.config.DeployTimeValidator;
import dev.datarun.server.config.ExpressionRepository;
import dev.datarun.server.config.ExpressionRule;
import dev.datarun.server.config.FlagSeverityConfigService;
import dev.datarun.server.config.Shape;
import dev.datarun.server.config.ShapeService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class ReviewedConfigProvisioner {

    private static final long CONFIG_APPLY_LOCK_ID = 0x4441544152554E50L;
    private static final Set<String> STATUSES = Set.of("active", "deprecated");
    private static final Set<String> SENSITIVITIES =
            Set.of("standard", "elevated", "restricted");

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbc;
    private final ShapeService shapeService;
    private final ActivityService activityService;
    private final ExpressionRepository expressionRepository;
    private final DeployTimeValidator deployTimeValidator;
    private final FlagSeverityConfigService flagSeverityConfigService;
    private final AssignmentAdminCapabilityService assignmentAdminCapabilityService;
    private final ConfigPackager configPackager;

    public ReviewedConfigProvisioner(
            ObjectMapper objectMapper,
            JdbcTemplate jdbc,
            ShapeService shapeService,
            ActivityService activityService,
            ExpressionRepository expressionRepository,
            DeployTimeValidator deployTimeValidator,
            FlagSeverityConfigService flagSeverityConfigService,
            AssignmentAdminCapabilityService assignmentAdminCapabilityService,
            ConfigPackager configPackager) {
        this.objectMapper = objectMapper;
        this.jdbc = jdbc;
        this.shapeService = shapeService;
        this.activityService = activityService;
        this.expressionRepository = expressionRepository;
        this.deployTimeValidator = deployTimeValidator;
        this.flagSeverityConfigService = flagSeverityConfigService;
        this.assignmentAdminCapabilityService = assignmentAdminCapabilityService;
        this.configPackager = configPackager;
    }

    @Transactional
    public ConfigProvisioningResult applyReviewedConfig(String inputJson, UUID operatorId) {
        ReviewedConfigManifest manifest = parse(inputJson);
        validateManifest(manifest);
        jdbc.queryForList("SELECT pg_advisory_xact_lock(?)", CONFIG_APPLY_LOCK_ID);

        int changed = applyShapes(manifest.shapes());
        changed += applyActivities(manifest.activities());
        changed += applyExpressions(manifest.expressions());
        changed += applyDeploymentConfig(manifest, operatorId);

        List<String> violations = deployTimeValidator.validateAll();
        if (!violations.isEmpty()) {
            throw new ProvisioningCommandException(
                    "reviewed config failed deploy-time validation: "
                            + String.join("; ", violations));
        }

        ConfigPackager.PublicationResult publication =
                configPackager.publishIfChanged(operatorId);
        return new ConfigProvisioningResult(
                publication.version(), publication.published(), changed);
    }

    private ReviewedConfigManifest parse(String inputJson) {
        if (inputJson == null || inputJson.isBlank()) {
            throw new ProvisioningCommandException("reviewed config input is empty");
        }
        try {
            return objectMapper.copy()
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(inputJson, ReviewedConfigManifest.class);
        } catch (Exception exception) {
            throw new ProvisioningCommandException("invalid reviewed config JSON", exception);
        }
    }

    private void validateManifest(ReviewedConfigManifest manifest) {
        if (manifest == null || manifest.schemaVersion() != 1) {
            throw new ProvisioningCommandException("schema_version must be 1");
        }
        requireText(manifest.source(), "source");
        requirePresent(manifest.shapes(), "shapes");
        requirePresent(manifest.activities(), "activities");
        requirePresent(manifest.expressions(), "expressions");
        requirePresent(manifest.flagSeverityOverrides(), "flag_severity_overrides");
        requirePresent(manifest.assignmentAdminCapabilities(), "assignment_admin_capabilities");

        Set<String> shapeRefs = new HashSet<>();
        Map<String, Set<Integer>> versionsByName = new HashMap<>();
        for (ReviewedShape shape : manifest.shapes()) {
            if (shape == null) {
                throw new ProvisioningCommandException("shapes cannot contain null");
            }
            requireText(shape.name(), "shape.name");
            if (shape.version() < 1) {
                throw new ProvisioningCommandException("shape.version must be positive");
            }
            validateStatusAndSensitivity(shape.status(), shape.sensitivity(), "shape");
            requirePresent(shape.schemaJson(), "shape.schema_json");
            if (ShapeService.isPlatformShapeName(shape.name())) {
                throw new ProvisioningCommandException(
                        "platform payload shapes are not reviewed config rows");
            }
            String shapeRef = shape.name() + "/v" + shape.version();
            if (!shapeRefs.add(shapeRef)) {
                throw new ProvisioningCommandException("duplicate shape_ref: " + shapeRef);
            }
            versionsByName.computeIfAbsent(shape.name(), ignored -> new HashSet<>())
                    .add(shape.version());
        }
        versionsByName.forEach((name, versions) -> {
            for (int version = 1; version <= versions.size(); version++) {
                if (!versions.contains(version)) {
                    throw new ProvisioningCommandException(
                            "shape versions must be contiguous for " + name);
                }
            }
        });

        Set<String> activityNames = new HashSet<>();
        for (ReviewedActivity activity : manifest.activities()) {
            if (activity == null) {
                throw new ProvisioningCommandException("activities cannot contain null");
            }
            requireText(activity.name(), "activity.name");
            validateStatusAndSensitivity(activity.status(), activity.sensitivity(), "activity");
            requirePresent(activity.configJson(), "activity.config_json");
            if (!activityNames.add(activity.name())) {
                throw new ProvisioningCommandException(
                        "duplicate activity name: " + activity.name());
            }
        }

        Set<String> expressionKeys = new HashSet<>();
        Set<UUID> expressionIds = new HashSet<>();
        for (ReviewedExpression expression : manifest.expressions()) {
            if (expression == null || expression.id() == null) {
                throw new ProvisioningCommandException("expression.id is required");
            }
            requireText(expression.activityRef(), "expression.activity_ref");
            requireText(expression.shapeRef(), "expression.shape_ref");
            requireText(expression.fieldName(), "expression.field_name");
            requireText(expression.ruleType(), "expression.rule_type");
            requirePresent(expression.expression(), "expression.expression");
            String key = expressionKey(expression);
            if (!expressionKeys.add(key)) {
                throw new ProvisioningCommandException("duplicate expression rule: " + key);
            }
            if (!expressionIds.add(expression.id())) {
                throw new ProvisioningCommandException(
                        "duplicate expression id: " + expression.id());
            }
        }
    }

    private int applyShapes(List<ReviewedShape> desiredShapes) {
        Map<String, Shape> existing = new LinkedHashMap<>();
        for (Shape shape : shapeService.getDeployerShapes()) {
            existing.put(shape.shapeRef(), shape);
        }
        Set<String> desiredRefs = new HashSet<>();
        desiredShapes.forEach(shape ->
                desiredRefs.add(shape.name() + "/v" + shape.version()));
        Set<String> unmanaged = new HashSet<>(existing.keySet());
        unmanaged.removeAll(desiredRefs);
        if (!unmanaged.isEmpty()) {
            throw new ProvisioningCommandException(
                    "reviewed config omits existing shapes: " + sorted(unmanaged));
        }

        int changed = 0;
        List<ReviewedShape> ordered = desiredShapes.stream()
                .sorted(Comparator.comparing(ReviewedShape::name)
                        .thenComparingInt(ReviewedShape::version))
                .toList();
        for (ReviewedShape desired : ordered) {
            String ref = desired.name() + "/v" + desired.version();
            Shape current = existing.get(ref);
            if (current == null) {
                List<String> violations = desired.version() == 1
                        ? shapeService.createShape(
                        desired.name(), desired.sensitivity(), desired.schemaJson())
                        : shapeService.createVersion(
                        desired.name(), desired.sensitivity(), desired.schemaJson());
                requireNoViolations("shape " + ref, violations);
                changed++;
                if ("deprecated".equals(desired.status())) {
                    shapeService.deprecate(desired.name(), desired.version());
                    changed++;
                }
                continue;
            }
            if (!Objects.equals(current.sensitivity(), desired.sensitivity())
                    || !Objects.equals(current.schemaJson(), desired.schemaJson())) {
                throw new ProvisioningCommandException(
                        "immutable shape differs from reviewed input: " + ref);
            }
            if ("deprecated".equals(current.status())
                    && "active".equals(desired.status())) {
                throw new ProvisioningCommandException(
                        "deprecated shape cannot be reactivated: " + ref);
            }
            if (!current.status().equals(desired.status())) {
                shapeService.deprecate(desired.name(), desired.version());
                changed++;
            }
        }
        return changed;
    }

    private int applyActivities(List<ReviewedActivity> desiredActivities) {
        Map<String, Activity> existing = new LinkedHashMap<>();
        activityService.getAllActivities()
                .forEach(activity -> existing.put(activity.name(), activity));
        Set<String> desiredNames = new HashSet<>();
        desiredActivities.forEach(activity -> desiredNames.add(activity.name()));
        Set<String> unmanaged = new HashSet<>(existing.keySet());
        unmanaged.removeAll(desiredNames);
        if (!unmanaged.isEmpty()) {
            throw new ProvisioningCommandException(
                    "reviewed config omits existing activities: " + sorted(unmanaged));
        }

        int changed = 0;
        for (ReviewedActivity desired : desiredActivities.stream()
                .sorted(Comparator.comparing(ReviewedActivity::name)).toList()) {
            Activity current = existing.get(desired.name());
            if (current == null) {
                requireNoViolations("activity " + desired.name(),
                        activityService.createActivity(
                                desired.name(), desired.sensitivity(), desired.configJson()));
                changed++;
            } else if (!Objects.equals(current.configJson(), desired.configJson())
                    || !Objects.equals(current.sensitivity(), desired.sensitivity())
                    || ("active".equals(desired.status())
                    && !"active".equals(current.status()))) {
                requireNoViolations("activity " + desired.name(),
                        activityService.updateActivity(
                                desired.name(), desired.sensitivity(), desired.configJson()));
                changed++;
            }
            if ("deprecated".equals(desired.status())) {
                Activity effective = activityService.getActivity(desired.name()).orElseThrow();
                if (!"deprecated".equals(effective.status())) {
                    activityService.deprecate(desired.name());
                    changed++;
                }
            }
        }
        return changed;
    }

    private int applyExpressions(List<ReviewedExpression> desiredExpressions) {
        Map<String, ExpressionRule> existing = new LinkedHashMap<>();
        expressionRepository.findAll()
                .forEach(rule -> existing.put(expressionKey(rule), rule));
        Map<String, ReviewedExpression> desired = new LinkedHashMap<>();
        desiredExpressions.forEach(rule -> desired.put(expressionKey(rule), rule));

        int changed = 0;
        for (Map.Entry<String, ExpressionRule> entry : existing.entrySet()) {
            ReviewedExpression wanted = desired.get(entry.getKey());
            if (wanted == null || !sameExpression(entry.getValue(), wanted)) {
                expressionRepository.delete(entry.getValue().id());
                changed++;
            }
        }
        for (Map.Entry<String, ReviewedExpression> entry : desired.entrySet()) {
            ExpressionRule current = existing.get(entry.getKey());
            if (current == null || !sameExpression(current, entry.getValue())) {
                ReviewedExpression rule = entry.getValue();
                expressionRepository.insert(new ExpressionRule(
                        rule.id(), rule.activityRef(), rule.shapeRef(), rule.fieldName(),
                        rule.ruleType(), rule.expression(), rule.message(), (Instant) null));
                changed++;
            }
        }
        return changed;
    }

    private int applyDeploymentConfig(ReviewedConfigManifest manifest, UUID operatorId) {
        int changed = 0;
        if (!flagSeverityConfigService.getValidatedOverrides()
                .equals(manifest.flagSeverityOverrides())) {
            requireNoViolations("flag_severity_overrides",
                    flagSeverityConfigService.updateOverrides(
                            manifest.flagSeverityOverrides(), operatorId));
            changed++;
        }
        if (!assignmentAdminCapabilityService.getValidatedPolicy()
                .equals(manifest.assignmentAdminCapabilities())) {
            requireNoViolations("assignment_admin_capabilities",
                    assignmentAdminCapabilityService.updatePolicy(
                            manifest.assignmentAdminCapabilities(), operatorId));
            changed++;
        }
        return changed;
    }

    private boolean sameExpression(ExpressionRule current, ReviewedExpression desired) {
        return current.id().equals(desired.id())
                && current.activityRef().equals(desired.activityRef())
                && current.shapeRef().equals(desired.shapeRef())
                && current.fieldName().equals(desired.fieldName())
                && current.ruleType().equals(desired.ruleType())
                && current.expression().equals(desired.expression())
                && Objects.equals(current.message(), desired.message());
    }

    private String expressionKey(ReviewedExpression expression) {
        return String.join("\n", expression.activityRef(), expression.shapeRef(),
                expression.fieldName(), expression.ruleType());
    }

    private String expressionKey(ExpressionRule expression) {
        return String.join("\n", expression.activityRef(), expression.shapeRef(),
                expression.fieldName(), expression.ruleType());
    }

    private void validateStatusAndSensitivity(
            String status, String sensitivity, String prefix) {
        if (!STATUSES.contains(status)) {
            throw new ProvisioningCommandException(
                    prefix + ".status must be active or deprecated");
        }
        if (!SENSITIVITIES.contains(sensitivity)) {
            throw new ProvisioningCommandException(
                    prefix + ".sensitivity is invalid");
        }
    }

    private void requireNoViolations(String subject, List<String> violations) {
        if (!violations.isEmpty()) {
            throw new ProvisioningCommandException(
                    subject + " is invalid: " + String.join("; ", violations));
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ProvisioningCommandException(field + " is required");
        }
    }

    private void requirePresent(Object value, String field) {
        if (value == null) {
            throw new ProvisioningCommandException(field + " is required");
        }
    }

    private List<String> sorted(Set<String> values) {
        return values.stream().sorted().toList();
    }

    public record ReviewedConfigManifest(
            @JsonProperty("schema_version") int schemaVersion,
            String source,
            List<ReviewedShape> shapes,
            List<ReviewedActivity> activities,
            List<ReviewedExpression> expressions,
            @JsonProperty("flag_severity_overrides") JsonNode flagSeverityOverrides,
            @JsonProperty("assignment_admin_capabilities") JsonNode assignmentAdminCapabilities) {}

    public record ReviewedShape(
            String name,
            int version,
            String status,
            String sensitivity,
            @JsonProperty("schema_json") JsonNode schemaJson) {}

    public record ReviewedActivity(
            String name,
            String status,
            String sensitivity,
            @JsonProperty("config_json") JsonNode configJson) {}

    public record ReviewedExpression(
            UUID id,
            @JsonProperty("activity_ref") String activityRef,
            @JsonProperty("shape_ref") String shapeRef,
            @JsonProperty("field_name") String fieldName,
            @JsonProperty("rule_type") String ruleType,
            JsonNode expression,
            String message) {}

    public record ConfigProvisioningResult(
            int configVersion, boolean published, int changedAuthoringRows) {}
}
