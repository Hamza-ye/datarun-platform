package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Deploy-Time Validator (DtV) for expression rules (IDR-018 §DtV).
 * Validates expression structure, field references, operator-type compatibility,
 * and structural constraints before rules can be published.
 */
@Service
public class DeployTimeValidator {

    private static final Set<String> COMPARISON_OPS = Set.of(
            "eq", "neq", "gt", "gte", "lt", "lte", "in", "not_null");
    private static final Set<String> ORDERING_OPS = Set.of("gt", "gte", "lt", "lte");
    private static final Set<String> ORDERING_TYPES = Set.of("integer", "decimal", "date");
    private static final Set<String> LOGICAL_OPS = Set.of("and", "or", "not");
    private static final Set<String> RULE_TYPES = Set.of("show_condition", "warning", "default");

    private final ShapeRepository shapeRepository;
    private final ExpressionRepository expressionRepository;

    public DeployTimeValidator(ShapeRepository shapeRepository, ExpressionRepository expressionRepository) {
        this.shapeRepository = shapeRepository;
        this.expressionRepository = expressionRepository;
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
        return violations;
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

    private Map<String, String> buildFieldTypeMap(Shape shape) {
        Map<String, String> map = new HashMap<>();
        JsonNode fieldsNode = shape.schemaJson().get("fields");
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
