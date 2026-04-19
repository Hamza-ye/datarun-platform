package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure-function evaluator for IDR-018 JSON AST expression nodes.
 * No DB access, no side effects. AST + values → result.
 *
 * Supports comparison operators (eq, neq, gt, gte, lt, lte, in, not_null),
 * logical operators (and, or, not), and value nodes (ref).
 */
public class ExpressionEvaluator {

    private static final Set<String> COMPARISON_OPS = Set.of(
            "eq", "neq", "gt", "gte", "lt", "lte", "in", "not_null");
    private static final Set<String> LOGICAL_OPS = Set.of("and", "or", "not");
    private static final Set<String> REFERENCE_PREFIXES = Set.of(
            "payload.", "entity.", "context.", "event.");

    private ExpressionEvaluator() {}

    /**
     * Evaluate a condition expression → boolean.
     * Handles comparison, logical, and bare comparison nodes.
     */
    public static boolean evaluateCondition(JsonNode expression, Map<String, Object> values) {
        if (expression == null || expression.isNull() || expression.isMissingNode()) {
            return false;
        }

        var fields = expression.fields();
        if (!fields.hasNext()) return false;
        var entry = fields.next();
        String operator = entry.getKey();
        JsonNode operand = entry.getValue();

        if (COMPARISON_OPS.contains(operator)) {
            return evaluateComparison(operator, operand, values);
        } else if (LOGICAL_OPS.contains(operator)) {
            return evaluateLogical(operator, operand, values);
        }

        return false;
    }

    /**
     * Evaluate a value expression → Object (for ref nodes and comparison-as-value).
     */
    public static Object evaluateValue(JsonNode expression, Map<String, Object> values) {
        if (expression == null || expression.isNull() || expression.isMissingNode()) {
            return null;
        }

        var fields = expression.fields();
        if (!fields.hasNext()) return null;
        var entry = fields.next();
        String operator = entry.getKey();
        JsonNode operand = entry.getValue();

        if ("ref".equals(operator)) {
            String ref = operand.asText(null);
            if (ref == null) return null;
            return values.get(ref);
        }

        // Comparison-as-value → boolean
        if (COMPARISON_OPS.contains(operator)) {
            return evaluateComparison(operator, operand, values);
        }
        if (LOGICAL_OPS.contains(operator)) {
            return evaluateLogical(operator, operand, values);
        }

        return null;
    }

    private static boolean evaluateComparison(String op, JsonNode operands, Map<String, Object> values) {
        if ("not_null".equals(op)) {
            if (!operands.isArray() || operands.isEmpty()) return false;
            Object val = resolveOperand(operands.get(0), values);
            return val != null;
        }

        if (!operands.isArray() || operands.size() < 2) return false;
        Object left = resolveOperand(operands.get(0), values);
        Object right = resolveOperand(operands.get(1), values);

        if ("in".equals(op)) {
            return evaluateIn(left, operands.get(1), right, values);
        }

        // Null safety: any null in comparison → false
        if (left == null || right == null) return false;

        try {
            return switch (op) {
                case "eq" -> isEqual(left, right);
                case "neq" -> !isEqual(left, right);
                case "gt" -> compareNumeric(left, right) > 0;
                case "gte" -> compareNumeric(left, right) >= 0;
                case "lt" -> compareNumeric(left, right) < 0;
                case "lte" -> compareNumeric(left, right) <= 0;
                default -> false;
            };
        } catch (ArithmeticException e) {
            // Type coercion failure → false
            return false;
        }
    }

    private static boolean evaluateLogical(String op, JsonNode operand, Map<String, Object> values) {
        return switch (op) {
            case "and" -> {
                if (!operand.isArray()) yield false;
                for (JsonNode child : operand) {
                    if (!evaluateCondition(child, values)) yield false;
                }
                yield operand.size() > 0;
            }
            case "or" -> {
                if (!operand.isArray()) yield false;
                for (JsonNode child : operand) {
                    if (evaluateCondition(child, values)) yield true;
                }
                yield false;
            }
            case "not" -> !evaluateCondition(operand, values);
            default -> false;
        };
    }

    @SuppressWarnings("unchecked")
    private static boolean evaluateIn(Object needle, JsonNode rightNode, Object right, Map<String, Object> values) {
        if (needle == null) return false;

        // If right resolved to a List (multi_select field), check membership
        if (right instanceof List<?> list) {
            return list.stream().anyMatch(item -> isEqual(needle, item));
        }

        // If the right operand is a literal array in the AST
        if (rightNode.isArray()) {
            // Check if it's a reference that resolved to something
            // The rightNode in the AST is the literal array
            for (JsonNode element : rightNode) {
                Object elementVal = jsonNodeToValue(element);
                if (elementVal != null && isEqual(needle, elementVal)) return true;
            }
            return false;
        }

        // Right is a reference to a field — already resolved above as right
        return false;
    }

    private static Object resolveOperand(JsonNode node, Map<String, Object> values) {
        if (node == null || node.isNull()) return null;

        if (node.isTextual()) {
            String text = node.asText();
            if (isReference(text)) {
                return values.get(text);
            }
            return text;
        }

        if (node.isArray()) {
            // Literal array (for 'in' haystack) — don't resolve, handled by evaluateIn
            return null;
        }

        return jsonNodeToValue(node);
    }

    private static boolean isReference(String text) {
        for (String prefix : REFERENCE_PREFIXES) {
            if (text.startsWith(prefix)) return true;
        }
        return false;
    }

    private static Object jsonNodeToValue(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isTextual()) return node.asText();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isIntegralNumber()) return node.asLong();
        if (node.isFloatingPointNumber()) return node.asDouble();
        return node.asText();
    }

    private static boolean isEqual(Object a, Object b) {
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;

        // Type coercion: try numeric comparison
        Double na = toNumber(a);
        Double nb = toNumber(b);
        if (na != null && nb != null) {
            return Double.compare(na, nb) == 0;
        }

        // String comparison as fallback
        return a.toString().equals(b.toString());
    }

    private static int compareNumeric(Object a, Object b) {
        Double na = toNumber(a);
        Double nb = toNumber(b);
        if (na == null || nb == null) {
            // Coercion failure → treat as "not comparable" → false for all ordering ops
            // Return 0 won't satisfy gt/lt but will satisfy gte/lte — use a sentinel instead
            throw new ArithmeticException("coercion_failure");
        }
        return Double.compare(na, nb);
    }

    private static Double toNumber(Object val) {
        if (val instanceof Number num) return num.doubleValue();
        if (val instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
