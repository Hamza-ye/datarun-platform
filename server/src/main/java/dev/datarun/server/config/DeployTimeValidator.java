package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datarun.server.integrity.FlagCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Deploy-Time Validator (DtV) for expression rules (IDR-018 §DtV).
 * Validates expression structure, field references, operator-type compatibility,
 * and structural constraints before rules can be published.
 */
@Service
public class DeployTimeValidator {

    public static final Set<String> ACTIVITY_WORK_ACTION_TYPES = Set.of(
            "capture", "review", "alert", "task_created", "task_completed");

    private static final Set<String> COMPARISON_OPS = Set.of(
            "eq", "neq", "gt", "gte", "lt", "lte", "in", "not_null");
    private static final Set<String> ORDERING_OPS = Set.of("gt", "gte", "lt", "lte");
    private static final Set<String> ORDERING_TYPES = Set.of("integer", "decimal", "date");
    private static final Set<String> LOGICAL_OPS = Set.of("and", "or", "not");
    private static final Set<String> RULE_TYPES = Set.of("show_condition", "warning", "default");
    private static final Set<String> UNIQUENESS_KEYS = Set.of("scope", "period", "device_action");
    private static final Set<String> UNIQUENESS_PERIOD_KEYS = Set.of("type", "timezone");
    private static final Set<String> UNIQUENESS_PERIOD_TYPES = Set.of(
            "calendar_day", "calendar_week", "calendar_month");
    private static final Set<String> UNIQUENESS_DEVICE_ACTIONS = Set.of(
            "warn", "confirm", "require_confirmation");
    private static final Set<String> SCALAR_PAYLOAD_TYPES = Set.of(
            "text", "integer", "decimal", "boolean", "date", "select",
            "location", "subject_ref", "narrative");
    private static final Set<String> PATTERN_SET_KEYS = Set.of("subject", "event");
    private static final Set<String> PATTERN_BINDING_KEYS = Set.of(
            "ref", "composition", "shape_roles", "activation_roles", "participant_roles", "parameters");

    private final ShapeRepository shapeRepository;
    private final ExpressionRepository expressionRepository;
    private final ActivityRepository activityRepository;
    private final PatternRegistry patternRegistry;

    @Autowired
    public DeployTimeValidator(ShapeRepository shapeRepository,
                               ExpressionRepository expressionRepository,
                               ActivityRepository activityRepository,
                               PatternRegistry patternRegistry) {
        this.shapeRepository = shapeRepository;
        this.expressionRepository = expressionRepository;
        this.activityRepository = activityRepository;
        this.patternRegistry = patternRegistry;
    }

    public DeployTimeValidator(ShapeRepository shapeRepository, ExpressionRepository expressionRepository) {
        this(shapeRepository, expressionRepository, null, new PatternRegistry());
    }

    /**
     * Validate a single expression rule against its target shape.
     * Returns empty list if valid.
     */
    public List<String> validateRule(ExpressionRule rule, Shape shape) {
        List<String> violations = new ArrayList<>();

        // Rule type must be valid
        if (!RULE_TYPES.contains(rule.ruleType())) {
            violations.add("Unknown rule_type '" + rule.ruleType() + "'");
            return violations;
        }

        // Build field map: name → type
        Map<String, String> fieldTypes = buildFieldTypeMap(shape);

        // Target field must exist in shape
        if (!fieldTypes.containsKey(rule.fieldName())) {
            violations.add("Field '" + rule.fieldName() + "' does not exist in shape '" + rule.shapeRef() + "'");
        }

        JsonNode expression = rule.expression();
        if (expression == null || expression.isNull()) {
            violations.add("Expression is null");
            return violations;
        }

        // Validate based on rule type
        switch (rule.ruleType()) {
            case "show_condition", "warning" -> {
                JsonNode when = expression.get("when");
                if (when == null) {
                    violations.add("Rule type '" + rule.ruleType() + "' requires a 'when' node");
                } else {
                    validateConditionNode(when, fieldTypes, violations, false);
                }
            }
            case "default" -> {
                JsonNode value = expression.get("value");
                if (value == null) {
                    violations.add("Rule type 'default' requires a 'value' node");
                } else {
                    validateValueNode(value, rule.fieldName(), fieldTypes, violations);
                }
            }
        }

        return violations;
    }

    /**
     * Validate all expression rules in the system.
     * Returns empty list if all valid.
     */
    public List<String> validateAll() {
        List<String> violations = new ArrayList<>();
        List<ExpressionRule> rules = expressionRepository.findAll();

        for (Shape shape : shapeRepository.findAll()) {
            if (ShapeService.isPlatformShapeName(shape.name())) {
                continue;
            }
            List<String> shapeViolations = validateShapeUniqueness(shape.shapeRef(), shape.schemaJson());
            for (String v : shapeViolations) {
                violations.add("Shape " + shape.shapeRef() + ": " + v);
            }
        }

        for (ExpressionRule rule : rules) {
            String[] parts = ShapeService.parseShapeRef(rule.shapeRef());
            if (parts == null) {
                violations.add("Rule " + rule.id() + ": invalid shape_ref '" + rule.shapeRef() + "'");
                continue;
            }
            Optional<Shape> shape = shapeRepository.findByNameAndVersion(parts[0], Integer.parseInt(parts[1]));
            if (shape.isEmpty()) {
                violations.add("Rule " + rule.id() + ": shape '" + rule.shapeRef() + "' not found");
                continue;
            }
            List<String> ruleViolations = validateRule(rule, shape.get());
            for (String v : ruleViolations) {
                violations.add("Rule " + rule.id() + " (" + rule.fieldName() + "/" + rule.ruleType() + "): " + v);
            }
        }

        if (activityRepository != null) {
            for (Activity activity : activityRepository.findActive()) {
                List<String> roleViolations = validateActivityRoles(activity.configJson().get("roles"));
                for (String v : roleViolations) {
                    violations.add("Activity " + activity.name() + ": " + v);
                }
                List<String> patternViolations = validateActivityPatternBinding(
                        activity.name(), activity.configJson(), this::shapeRefExists, patternRegistry);
                for (String v : patternViolations) {
                    violations.add("Activity " + activity.name() + ": " + v);
                }
            }
        }
        return violations;
    }

    @FunctionalInterface
    public interface ShapeRefLookup {
        boolean exists(String shapeRef);
    }

    public static List<String> validateActivityRoles(JsonNode rolesNode) {
        List<String> violations = new ArrayList<>();
        if (rolesNode == null || !rolesNode.isObject()) {
            violations.add("Activity must have a 'roles' object");
            return violations;
        }

        rolesNode.fields().forEachRemaining(entry -> {
            String role = entry.getKey();
            JsonNode actions = entry.getValue();
            if (role == null || role.isBlank()) {
                violations.add("Activity role name must be non-empty");
            }
            if (actions == null || !actions.isArray()) {
                violations.add("Activity role '" + role + "' must map to an action array");
                return;
            }
            if (actions.isEmpty()) {
                violations.add("Activity role '" + role + "' must have a non-empty action list");
            }
            for (JsonNode actionNode : actions) {
                if (!actionNode.isTextual()) {
                    violations.add("Activity role '" + role + "' action must be a string");
                    continue;
                }
                String action = actionNode.asText();
                if (!ACTIVITY_WORK_ACTION_TYPES.contains(action)) {
                    violations.add("Unknown action '" + action + "' for activity role '" + role + "'");
                }
            }
        });

        return violations;
    }

    public static List<String> validateActivityPatternBinding(String activityName,
                                                              JsonNode activityConfig,
                                                              ShapeRefLookup shapeRefLookup,
                                                              PatternRegistry registry) {
        List<String> violations = new ArrayList<>();
        if (activityConfig == null || activityConfig.isNull()) {
            return violations;
        }
        JsonNode patternNode = activityConfig.get("pattern");
        if (patternNode == null || patternNode.isNull()) {
            return violations;
        }
        if (!patternNode.isObject()) {
            violations.add("Activity '" + activityName + "' pattern must be an object or null");
            return violations;
        }

        patternNode.fields().forEachRemaining(entry -> {
            if (!PATTERN_SET_KEYS.contains(entry.getKey())) {
                violations.add("Activity '" + activityName + "' pattern key '" + entry.getKey()
                        + "' is not supported; deployers bind platform patterns only");
            }
        });

        Map<String, String> transitionBoundOwners = new LinkedHashMap<>();
        int[] subjectBindingCount = new int[]{0};

        JsonNode subjectNode = patternNode.get("subject");
        if (subjectNode != null && !subjectNode.isNull()) {
            if (subjectNode.isArray()) {
                if (subjectNode.size() > 1) {
                    violations.add("Activity '" + activityName
                            + "' may have at most one subject-level pattern binding");
                }
                violations.add("Activity '" + activityName + "' pattern.subject must be an object or null");
            } else if (!subjectNode.isObject()) {
                violations.add("Activity '" + activityName + "' pattern.subject must be an object or null");
            } else {
                subjectBindingCount[0]++;
                validatePatternBinding(activityName, "subject", subjectNode, "subject",
                        activityConfig.get("roles"), shapeRefLookup, registry, transitionBoundOwners, violations);
            }
        }

        JsonNode eventNode = patternNode.get("event");
        if (eventNode != null && !eventNode.isNull()) {
            if (!eventNode.isArray()) {
                violations.add("Activity '" + activityName + "' pattern.event must be an array");
            } else {
                for (int i = 0; i < eventNode.size(); i++) {
                    JsonNode binding = eventNode.get(i);
                    if (binding == null || !binding.isObject()) {
                        violations.add("Activity '" + activityName + "' pattern.event[" + i
                                + "] must be an object");
                        continue;
                    }
                    if ("subject".equals(binding.path("composition").asText(null))) {
                        subjectBindingCount[0]++;
                    }
                    validatePatternBinding(activityName, "event[" + i + "]", binding, "event",
                            activityConfig.get("roles"), shapeRefLookup, registry, transitionBoundOwners, violations);
                }
            }
        }

        if (subjectBindingCount[0] > 1) {
            violations.add("Activity '" + activityName
                    + "' may have at most one subject-level pattern binding");
        }

        return violations;
    }

    private static void validatePatternBinding(String activityName,
                                               String bindingPath,
                                               JsonNode binding,
                                               String expectedSlotComposition,
                                               JsonNode activityRolesNode,
                                               ShapeRefLookup shapeRefLookup,
                                               PatternRegistry registry,
                                               Map<String, String> transitionBoundOwners,
                                               List<String> violations) {
        binding.fields().forEachRemaining(entry -> {
            if (!PATTERN_BINDING_KEYS.contains(entry.getKey())) {
                violations.add("Activity '" + activityName + "' pattern." + bindingPath
                        + " key '" + entry.getKey()
                        + "' is not supported; deployers bind platform patterns only");
            }
        });

        JsonNode refNode = binding.get("ref");
        if (refNode == null || !refNode.isTextual() || refNode.asText().isBlank()) {
            violations.add("Activity '" + activityName + "' pattern." + bindingPath
                    + ".ref must be a non-empty string");
            return;
        }
        String ref = refNode.asText();
        Optional<PatternRegistry.PatternDefinition> definitionOpt = registry.find(ref);
        if (definitionOpt.isEmpty()) {
            violations.add("Unknown pattern ref '" + ref + "' in activity '" + activityName + "'");
            return;
        }
        PatternRegistry.PatternDefinition definition = definitionOpt.get();
        if (!definition.bindingEnabled()) {
            violations.add("Pattern ref '" + ref
                    + "' is registered but not enabled for binding");
        }

        JsonNode compositionNode = binding.get("composition");
        if (compositionNode == null || !compositionNode.isTextual()) {
            violations.add("Pattern binding '" + ref + "' composition must be a string");
        } else {
            String composition = compositionNode.asText();
            if (!expectedSlotComposition.equals(composition)) {
                violations.add("Pattern binding '" + ref + "' in " + bindingPath
                        + " must use composition '" + expectedSlotComposition + "'");
            }
            if (!definition.allowedCompositions().contains(composition)) {
                violations.add("Pattern binding '" + ref + "' composition '" + composition
                        + "' does not match platform definition");
            }
        }

        JsonNode parametersNode = binding.get("parameters");
        int approvalLevels = validatePatternParameters(ref, definition, parametersNode, violations);

        JsonNode shapeRolesNode = binding.get("shape_roles");
        validateShapeRoles(activityName, bindingPath, ref, definition, shapeRolesNode,
                shapeRefLookup, transitionBoundOwners, violations);

        JsonNode activationRolesNode = binding.get("activation_roles");
        validateActivationRoles(ref, definition, activationRolesNode, shapeRefLookup, violations);

        JsonNode participantRolesNode = binding.get("participant_roles");
        validateParticipantRoles(ref, definition, participantRolesNode,
                activityRolesNode, shapeRolesNode, approvalLevels, violations);
    }

    private static int validatePatternParameters(String ref,
                                                 PatternRegistry.PatternDefinition definition,
                                                 JsonNode parametersNode,
                                                 List<String> violations) {
        if (parametersNode == null || parametersNode.isNull()) {
            if (!definition.requiredParameters().isEmpty()) {
                violations.add("Pattern binding '" + ref + "' parameters must be an object");
            }
            for (String required : definition.requiredParameters()) {
                violations.add("Pattern binding '" + ref + "' missing required parameters." + required);
            }
            return -1;
        }
        if (!parametersNode.isObject()) {
            violations.add("Pattern binding '" + ref + "' parameters must be an object");
            return -1;
        }

        parametersNode.fields().forEachRemaining(entry -> {
            if (!definition.allParameters().contains(entry.getKey())) {
                violations.add("Pattern binding '" + ref + "' parameter '" + entry.getKey()
                        + "' is not supported");
            }
        });
        for (String required : definition.requiredParameters()) {
            if (!parametersNode.has(required) || parametersNode.get(required).isNull()) {
                violations.add("Pattern binding '" + ref + "' missing required parameters." + required);
            }
        }

        if (!definition.levelBasedApproval()) {
            return -1;
        }

        JsonNode levelsNode = parametersNode.get("levels");
        if (levelsNode == null || !levelsNode.isIntegralNumber() || !levelsNode.canConvertToInt()) {
            violations.add("Pattern binding '" + ref + "' parameters.levels must be an integer >= 2");
            return -1;
        }
        int levels = levelsNode.asInt();
        if (levels < 2) {
            violations.add("Pattern binding '" + ref + "' parameters.levels must be >= 2");
            return -1;
        }
        return levels;
    }

    private static void validateShapeRoles(String activityName,
                                           String bindingPath,
                                           String ref,
                                           PatternRegistry.PatternDefinition definition,
                                           JsonNode shapeRolesNode,
                                           ShapeRefLookup shapeRefLookup,
                                           Map<String, String> transitionBoundOwners,
                                           List<String> violations) {
        if (shapeRolesNode == null || shapeRolesNode.isNull()) {
            if (!definition.requiredShapeRoles().isEmpty()) {
                violations.add("Pattern binding '" + ref + "' shape_roles must be an object");
            }
            for (String required : definition.requiredShapeRoles()) {
                violations.add("Pattern binding '" + ref + "' missing required shape_roles." + required);
            }
            return;
        }
        if (!shapeRolesNode.isObject()) {
            violations.add("Pattern binding '" + ref + "' shape_roles must be an object");
            return;
        }

        shapeRolesNode.fields().forEachRemaining(entry -> {
            String role = entry.getKey();
            if (definition.platformShapeRoles().containsKey(role)) {
                violations.add("Pattern binding '" + ref + "' shape_roles." + role
                        + " is platform-owned by the pattern definition");
                return;
            }
            if (!definition.allShapeRoles().contains(role)) {
                violations.add("Pattern binding '" + ref + "' shape_roles." + role + " is not supported");
            }
            List<String> refs = validateShapeRefArray("Pattern binding '" + ref + "' shape_roles." + role,
                    entry.getValue(), shapeRefLookup, violations);
            if (definition.transitionBoundShapeRoles().contains(role)) {
                for (String shapeRef : refs) {
                    String owner = "pattern." + bindingPath + ".shape_roles." + role;
                    String previous = transitionBoundOwners.putIfAbsent(shapeRef, owner);
                    if (previous != null) {
                        violations.add("Duplicate transition-bound shape ownership in activity '"
                                + activityName + "': shape '" + shapeRef + "' is bound by both '"
                                + previous + "' and '" + owner + "'");
                    }
                }
            }
        });

        for (String required : definition.requiredShapeRoles()) {
            JsonNode requiredNode = shapeRolesNode.get(required);
            if (requiredNode == null || !requiredNode.isArray() || requiredNode.isEmpty()) {
                violations.add("Pattern binding '" + ref + "' missing required shape_roles." + required);
            }
        }
    }

    private static void validateActivationRoles(String ref,
                                                PatternRegistry.PatternDefinition definition,
                                                JsonNode activationRolesNode,
                                                ShapeRefLookup shapeRefLookup,
                                                List<String> violations) {
        if (activationRolesNode == null || activationRolesNode.isNull()) {
            for (String required : definition.requiredActivationRoleLists()) {
                violations.add("Pattern binding '" + ref + "' missing required activation_roles." + required);
            }
            return;
        }
        if (!activationRolesNode.isObject()) {
            violations.add("Pattern binding '" + ref + "' activation_roles must be an object");
            return;
        }
        if (definition.allActivationRoleLists().isEmpty() && activationRolesNode.size() > 0) {
            violations.add("Pattern binding '" + ref + "' does not support activation_roles");
        }

        activationRolesNode.fields().forEachRemaining(entry -> {
            String role = entry.getKey();
            if (!definition.allActivationRoleLists().contains(role)) {
                violations.add("Pattern binding '" + ref + "' activation_roles." + role + " is not supported");
            }
            validateShapeRefArray("Pattern binding '" + ref + "' activation_roles." + role,
                    entry.getValue(), shapeRefLookup, violations);
        });

        for (String required : definition.requiredActivationRoleLists()) {
            JsonNode requiredNode = activationRolesNode.get(required);
            if (requiredNode == null || !requiredNode.isArray() || requiredNode.isEmpty()) {
                violations.add("Pattern binding '" + ref + "' missing required activation_roles." + required);
            }
        }
    }

    private static void validateParticipantRoles(String ref,
                                                 PatternRegistry.PatternDefinition definition,
                                                 JsonNode participantRolesNode,
                                                 JsonNode activityRolesNode,
                                                 JsonNode shapeRolesNode,
                                                 int approvalLevels,
                                                 List<String> violations) {
        Map<String, Set<String>> actionRequirements =
                participantActionRequirements(definition, shapeRolesNode, approvalLevels);
        Set<String> requiredParticipants =
                requiredParticipantRoles(definition, shapeRolesNode, approvalLevels);
        Set<String> allowedParticipants = new LinkedHashSet<>(definition.allFixedParticipantRoles());
        allowedParticipants.addAll(actionRequirements.keySet());

        if (participantRolesNode == null || participantRolesNode.isNull()) {
            if (!requiredParticipants.isEmpty()) {
                violations.add("Pattern binding '" + ref + "' participant_roles must be an object");
            }
            for (String required : requiredParticipants) {
                violations.add("Pattern binding '" + ref + "' missing required participant_roles." + required);
            }
            return;
        }
        if (!participantRolesNode.isObject()) {
            violations.add("Pattern binding '" + ref + "' participant_roles must be an object");
            return;
        }

        participantRolesNode.fields().forEachRemaining(entry -> {
            String participantRole = entry.getKey();
            if (!allowedParticipants.contains(participantRole)) {
                violations.add("Pattern binding '" + ref + "' participant_roles."
                        + participantRole + " is not supported");
            }
            List<String> activityRoles = validateStringArray("Pattern binding '" + ref
                    + "' participant_roles." + participantRole, entry.getValue(), violations);
            Set<String> requiredActions = actionRequirements.getOrDefault(participantRole, Set.of());
            for (String activityRole : activityRoles) {
                JsonNode actions = activityRolesNode == null ? null : activityRolesNode.get(activityRole);
                if (actions == null || !actions.isArray()) {
                    violations.add("Pattern binding '" + ref + "' participant_roles." + participantRole
                            + " references unknown activity role '" + activityRole + "'");
                    continue;
                }
                for (String action : requiredActions) {
                    if (!arrayContains(actions, action)) {
                        violations.add("Pattern binding '" + ref + "' participant_roles." + participantRole
                                + " maps activity role '" + activityRole
                                + "' but that role does not allow action '" + action + "'");
                    }
                }
            }
        });

        for (String required : requiredParticipants) {
            JsonNode requiredNode = participantRolesNode.get(required);
            if (requiredNode == null || !requiredNode.isArray() || requiredNode.isEmpty()) {
                violations.add("Pattern binding '" + ref + "' missing required participant_roles." + required);
            }
        }
    }

    private static Map<String, Set<String>> participantActionRequirements(
            PatternRegistry.PatternDefinition definition,
            JsonNode shapeRolesNode,
            int approvalLevels) {
        Map<String, Set<String>> requirements = new LinkedHashMap<>(definition.participantActionRequirements());
        if (definition.levelBasedApproval() && approvalLevels >= 2) {
            for (int i = 1; i <= approvalLevels; i++) {
                requirements.put("level_" + i + "_reviewer", Set.of("review"));
            }
        }
        if (definition.transferSupervisorConditional() && hasAnyShapeRole(shapeRolesNode,
                Set.of("discrepancy_report", "discrepancy_resolution"))) {
            requirements.put("supervisor", Set.of("review"));
        }
        return requirements;
    }

    private static Set<String> requiredParticipantRoles(PatternRegistry.PatternDefinition definition,
                                                        JsonNode shapeRolesNode,
                                                        int approvalLevels) {
        Set<String> required = new LinkedHashSet<>(definition.requiredParticipantRoles());
        if (definition.levelBasedApproval() && approvalLevels >= 2) {
            for (int i = 1; i <= approvalLevels; i++) {
                required.add("level_" + i + "_reviewer");
            }
        }
        if (definition.transferSupervisorConditional() && hasAnyShapeRole(shapeRolesNode,
                Set.of("discrepancy_report", "discrepancy_resolution"))) {
            required.add("supervisor");
        }
        return required;
    }

    private static boolean hasAnyShapeRole(JsonNode shapeRolesNode, Set<String> roles) {
        if (shapeRolesNode == null || !shapeRolesNode.isObject()) {
            return false;
        }
        for (String role : roles) {
            JsonNode value = shapeRolesNode.get(role);
            if (value != null && value.isArray() && !value.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static List<String> validateShapeRefArray(String path,
                                                      JsonNode node,
                                                      ShapeRefLookup shapeRefLookup,
                                                      List<String> violations) {
        List<String> refs = validateStringArray(path, node, violations);
        Set<String> seen = new LinkedHashSet<>();
        for (String shapeRef : refs) {
            if (!seen.add(shapeRef)) {
                violations.add(path + " contains duplicate shape_ref '" + shapeRef + "'");
            }
            String[] parsed = ShapeService.parseShapeRef(shapeRef);
            if (parsed == null) {
                violations.add(path + " contains invalid shape_ref '" + shapeRef + "'");
            } else if (shapeRefLookup == null || !shapeRefLookup.exists(shapeRef)) {
                violations.add(path + " references unknown shape_ref '" + shapeRef + "'");
            }
        }
        return refs;
    }

    private static List<String> validateStringArray(String path, JsonNode node, List<String> violations) {
        List<String> values = new ArrayList<>();
        if (node == null || !node.isArray()) {
            violations.add(path + " must be an array");
            return values;
        }
        if (node.isEmpty()) {
            violations.add(path + " must be non-empty");
            return values;
        }
        for (JsonNode value : node) {
            if (!value.isTextual() || value.asText().isBlank()) {
                violations.add(path + " values must be non-empty strings");
                continue;
            }
            values.add(value.asText());
        }
        return values;
    }

    private static boolean arrayContains(JsonNode arrayNode, String expected) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return false;
        }
        for (JsonNode item : arrayNode) {
            if (item.isTextual() && expected.equals(item.asText())) {
                return true;
            }
        }
        return false;
    }

    public static List<String> validateShapeUniqueness(String shapeRef, JsonNode schemaJson) {
        List<String> violations = new ArrayList<>();
        if (schemaJson == null || schemaJson.isNull()) {
            return violations;
        }

        JsonNode uniqueness = schemaJson.get("uniqueness");
        if (uniqueness == null || uniqueness.isNull()) {
            return violations;
        }
        if (!uniqueness.isObject()) {
            violations.add("uniqueness must be a single object");
            return violations;
        }

        Map<String, String> fieldTypes = buildFieldTypeMap(schemaJson);

        uniqueness.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            if ("action".equals(key)) {
                violations.add("uniqueness uses old key 'action'; use 'device_action'");
            } else if (!UNIQUENESS_KEYS.contains(key)) {
                violations.add("uniqueness key '" + key + "' is not supported");
            }
        });

        JsonNode scope = uniqueness.get("scope");
        if (scope == null || !scope.isArray() || scope.isEmpty()) {
            violations.add("uniqueness.scope must be a non-empty array");
        } else {
            for (JsonNode dimensionNode : scope) {
                if (!dimensionNode.isTextual()) {
                    violations.add("uniqueness.scope dimensions must be strings");
                    continue;
                }
                validateUniquenessScopeDimension(dimensionNode.asText(), fieldTypes, violations);
            }
        }

        JsonNode period = uniqueness.get("period");
        if (period != null && !period.isNull()) {
            validateUniquenessPeriod(period, violations);
        }

        JsonNode deviceAction = uniqueness.get("device_action");
        if (deviceAction != null && !deviceAction.isNull()) {
            if (!deviceAction.isTextual()) {
                violations.add("uniqueness.device_action must be a string");
            } else if (!UNIQUENESS_DEVICE_ACTIONS.contains(deviceAction.asText())) {
                violations.add("uniqueness.device_action '" + deviceAction.asText()
                        + "' is not supported");
            }
        }

        return violations;
    }

    public static List<String> validateFlagSeverityOverrides(JsonNode overridesNode) {
        List<String> violations = new ArrayList<>();
        if (overridesNode == null || !overridesNode.isObject()) {
            violations.add("flag_severity_overrides must be an object");
            return violations;
        }

        overridesNode.fields().forEachRemaining(entry -> {
            String category = entry.getKey();
            JsonNode severityNode = entry.getValue();
            if (FlagCatalog.RESERVED_CATEGORY.equals(category)) {
                violations.add("Reserved flag category 'reserved' cannot be overridden");
            } else if (!FlagCatalog.isKnownCategory(category)) {
                violations.add("Unknown flag category '" + category + "'");
            }

            if (severityNode == null || severityNode.isNull()) {
                violations.add("Severity for '" + category + "' must be 'blocking' or 'informational'");
                return;
            }
            if (severityNode.isObject() || severityNode.isArray()) {
                violations.add("flag_severity_overrides must be flat; nested/per-activity severity for '"
                        + category + "' is not supported");
                return;
            }
            if (!severityNode.isTextual()) {
                violations.add("Severity for '" + category + "' must be a string");
                return;
            }

            String severity = severityNode.asText();
            if (!FlagCatalog.isValidSeverity(severity)) {
                violations.add("Invalid severity '" + severity + "' for '" + category
                        + "'; expected 'blocking' or 'informational'");
            }
        });

        return violations;
    }

    private static void validateUniquenessScopeDimension(String dimension,
                                                         Map<String, String> fieldTypes,
                                                         List<String> violations) {
        if (dimension == null || dimension.isBlank()) {
            violations.add("uniqueness.scope dimensions must be non-empty");
            return;
        }
        if ("subject_ref".equals(dimension) || "activity_ref".equals(dimension)) {
            return;
        }
        if (!dimension.startsWith("payload.")) {
            violations.add("uniqueness.scope dimension '" + dimension
                    + "' is not supported; use subject_ref, activity_ref, or payload.<field_name>");
            return;
        }

        String fieldName = dimension.substring("payload.".length());
        if (fieldName.isBlank()) {
            violations.add("uniqueness.scope payload dimension must name a field");
            return;
        }
        String fieldType = fieldTypes.get(fieldName);
        if (fieldType == null) {
            violations.add("uniqueness.scope references unknown payload field '" + fieldName + "'");
            return;
        }
        if (!SCALAR_PAYLOAD_TYPES.contains(fieldType)) {
            violations.add("uniqueness.scope payload field '" + fieldName
                    + "' has non-scalar type '" + fieldType + "'");
        }
    }

    private static void validateUniquenessPeriod(JsonNode period, List<String> violations) {
        if (!period.isObject()) {
            violations.add("uniqueness.period must be an object");
            return;
        }
        period.fields().forEachRemaining(entry -> {
            if (!UNIQUENESS_PERIOD_KEYS.contains(entry.getKey())) {
                violations.add("uniqueness.period key '" + entry.getKey() + "' is not supported");
            }
        });

        JsonNode type = period.get("type");
        if (type == null || !type.isTextual()) {
            violations.add("uniqueness.period.type must be a string");
        } else if (!UNIQUENESS_PERIOD_TYPES.contains(type.asText())) {
            violations.add("uniqueness.period.type '" + type.asText() + "' is not supported");
        }

        JsonNode timezone = period.get("timezone");
        if (timezone != null && !timezone.isNull()) {
            if (!timezone.isTextual()) {
                violations.add("uniqueness.period.timezone must be a string");
            } else if (!"deployment".equals(timezone.asText())) {
                violations.add("uniqueness.period.timezone must be 'deployment' when present");
            }
        }
    }

    private void validateConditionNode(JsonNode node, Map<String, String> fieldTypes,
                                       List<String> violations, boolean insideLogical) {
        if (node == null || node.isNull()) {
            violations.add("Condition node is null");
            return;
        }

        var fields = node.fields();
        if (!fields.hasNext()) {
            violations.add("Empty condition node");
            return;
        }
        var entry = fields.next();
        String op = entry.getKey();
        JsonNode operand = entry.getValue();

        if (COMPARISON_OPS.contains(op)) {
            validateComparisonNode(op, operand, fieldTypes, violations);
        } else if (LOGICAL_OPS.contains(op)) {
            if (insideLogical) {
                violations.add("Nested logical operator '" + op + "' not allowed");
                return;
            }
            validateLogicalNode(op, operand, fieldTypes, violations);
        } else {
            violations.add("Unknown operator '" + op + "'");
        }
    }

    private void validateComparisonNode(String op, JsonNode operands,
                                        Map<String, String> fieldTypes, List<String> violations) {
        if ("not_null".equals(op)) {
            if (!operands.isArray() || operands.size() != 1) {
                violations.add("'not_null' requires exactly 1 operand");
                return;
            }
            validateOperandReference(operands.get(0), fieldTypes, violations);
            return;
        }

        if (!operands.isArray() || operands.size() != 2) {
            violations.add("Operator '" + op + "' requires exactly 2 operands");
            return;
        }

        JsonNode left = operands.get(0);
        JsonNode right = operands.get(1);

        validateOperandReference(left, fieldTypes, violations);
        if (!"in".equals(op)) {
            validateOperandReference(right, fieldTypes, violations);
        }

        // Ordering operator type compatibility
        if (ORDERING_OPS.contains(op)) {
            String leftFieldType = resolveFieldType(left, fieldTypes);
            if (leftFieldType != null && !ORDERING_TYPES.contains(leftFieldType)) {
                violations.add("Ordering operator '" + op + "' not valid for field type '" + leftFieldType + "'");
            }
        }

        // multi_select + eq/neq → rejected
        if ("eq".equals(op) || "neq".equals(op)) {
            String leftFieldType = resolveFieldType(left, fieldTypes);
            if ("multi_select".equals(leftFieldType)) {
                violations.add("Cannot use '" + op + "' on multi_select field — use 'in' instead");
            }
        }
    }

    private void validateLogicalNode(String op, JsonNode operand,
                                     Map<String, String> fieldTypes, List<String> violations) {
        if ("not".equals(op)) {
            if (operand.isArray()) {
                violations.add("'not' takes a single comparison, not an array");
                return;
            }
            validateConditionNode(operand, fieldTypes, violations, true);
            return;
        }

        // and / or
        if (!operand.isArray()) {
            violations.add("'" + op + "' requires an array of comparisons");
            return;
        }

        if (operand.size() < 2 || operand.size() > 3) {
            violations.add("'" + op + "' requires 2-3 predicates, got " + operand.size());
            return;
        }

        for (JsonNode child : operand) {
            validateConditionNode(child, fieldTypes, violations, true);
        }
    }

    private void validateValueNode(JsonNode node, String targetField,
                                   Map<String, String> fieldTypes, List<String> violations) {
        if (node == null || node.isNull()) {
            violations.add("Value node is null");
            return;
        }

        var fields = node.fields();
        if (!fields.hasNext()) {
            violations.add("Empty value node");
            return;
        }
        var entry = fields.next();
        String op = entry.getKey();

        if ("ref".equals(op)) {
            JsonNode refNode = entry.getValue();
            if (!refNode.isTextual()) {
                violations.add("'ref' value must be a string");
            }
            // ref → matching type check deferred (would need runtime context type info)
            return;
        }

        // Comparison-as-value → boolean. Target field must be boolean.
        if (COMPARISON_OPS.contains(op) || LOGICAL_OPS.contains(op)) {
            String targetType = fieldTypes.get(targetField);
            if (targetType != null && !"boolean".equals(targetType)) {
                violations.add("Default expression produces boolean but target field '"
                        + targetField + "' is type '" + targetType + "'");
            }
            // Also validate the condition structure
            validateConditionNode(node, fieldTypes, violations, false);
            return;
        }

        violations.add("Unknown value expression operator '" + op + "'");
    }

    private void validateOperandReference(JsonNode operand, Map<String, String> fieldTypes,
                                          List<String> violations) {
        if (operand == null || operand.isNull()) return;
        if (!operand.isTextual()) return; // literal number/boolean/array — ok

        String text = operand.asText();
        if (text.startsWith("payload.")) {
            String fieldName = text.substring("payload.".length());
            if (!fieldTypes.containsKey(fieldName)) {
                violations.add("Reference '" + text + "' — field '" + fieldName + "' not found in shape");
            }
        }
        // entity.*, context.*, event.* — valid namespace references, can't validate further at DtV
    }

    private String resolveFieldType(JsonNode operand, Map<String, String> fieldTypes) {
        if (operand == null || !operand.isTextual()) return null;
        String text = operand.asText();
        if (text.startsWith("payload.")) {
            return fieldTypes.get(text.substring("payload.".length()));
        }
        return null;
    }

    private boolean shapeRefExists(String shapeRef) {
        if (shapeRepository == null) return false;
        if (ShapeService.isPlatformShapeRef(shapeRef)) return false;
        String[] parts = ShapeService.parseShapeRef(shapeRef);
        if (parts == null) return false;
        return shapeRepository.exists(parts[0], Integer.parseInt(parts[1]));
    }

    private Map<String, String> buildFieldTypeMap(Shape shape) {
        return buildFieldTypeMap(shape.schemaJson());
    }

    private static Map<String, String> buildFieldTypeMap(JsonNode schemaJson) {
        Map<String, String> map = new HashMap<>();
        JsonNode fieldsNode = schemaJson.get("fields");
        if (fieldsNode != null && fieldsNode.isArray()) {
            for (JsonNode field : fieldsNode) {
                String name = field.path("name").asText("");
                String type = field.path("type").asText("");
                if (!name.isEmpty()) {
                    map.put(name, type);
                }
            }
        }
        return map;
    }
}
