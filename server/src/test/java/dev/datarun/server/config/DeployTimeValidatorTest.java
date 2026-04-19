package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DeployTimeValidator (IDR-018 §DtV).
 * Tests key DtV checks for expression rules.
 */
class DeployTimeValidatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Shape testShape;

    @BeforeEach
    void setUp() {
        // Build a test shape with various field types
        ObjectNode schema = objectMapper.createObjectNode();
        ArrayNode fields = schema.putArray("fields");

        addField(fields, "name", "text", true);
        addField(fields, "age", "integer", false);
        addField(fields, "temperature", "decimal", false);
        addField(fields, "is_active", "boolean", false);
        addField(fields, "visit_date", "date", false);
        addField(fields, "status", "select", false, List.of("active", "closed", "partial"));
        addField(fields, "stockout_items", "multi_select", false, List.of("vaccines", "antimalarials", "bandages"));
        addField(fields, "notes", "narrative", false);

        schema.putNull("subject_binding");
        schema.putNull("uniqueness");

        testShape = new Shape("test_shape", 1, "active", "standard", schema, null);
    }

    @Test
    void validShowCondition_passes() {
        JsonNode expr = parse("""
                {"when": {"eq": ["payload.status", "active"]}}
                """);
        ExpressionRule rule = makeRule("status", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void invalidFieldReference_violation() {
        JsonNode expr = parse("""
                {"when": {"eq": ["payload.nonexistent_field", "active"]}}
                """);
        ExpressionRule rule = makeRule("status", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("nonexistent_field") && v.contains("not found")));
    }

    @Test
    void orderingOperatorOnTextField_violation() {
        JsonNode expr = parse("""
                {"when": {"gt": ["payload.name", "abc"]}}
                """);
        ExpressionRule rule = makeRule("name", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Ordering operator") && v.contains("text")));
    }

    @Test
    void multiSelectWithEq_violation() {
        JsonNode expr = parse("""
                {"when": {"eq": ["payload.stockout_items", "vaccines"]}}
                """);
        ExpressionRule rule = makeRule("stockout_items", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("multi_select") && v.contains("use 'in'")));
    }

    @Test
    void predicateCountExceedsThree_violation() {
        JsonNode expr = parse("""
                {"when": {"and": [
                    {"eq": ["payload.status", "active"]},
                    {"gt": ["payload.age", 5]},
                    {"lt": ["payload.age", 100]},
                    {"not_null": ["payload.notes"]}
                ]}}
                """);
        ExpressionRule rule = makeRule("name", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("2-3 predicates")));
    }

    @Test
    void nestedLogicalOperator_violation() {
        JsonNode expr = parse("""
                {"when": {"and": [
                    {"or": [
                        {"eq": ["payload.status", "active"]},
                        {"eq": ["payload.status", "partial"]}
                    ]},
                    {"gt": ["payload.age", 5]}
                ]}}
                """);
        ExpressionRule rule = makeRule("name", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Nested logical")));
    }

    @Test
    void defaultExpressionTypeMismatch_violation() {
        // Comparison produces boolean but target field is 'text'
        JsonNode expr = parse("""
                {"value": {"gt": ["payload.age", 18]}}
                """);
        ExpressionRule rule = makeRule("name", "default", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.contains("produces boolean") && v.contains("text")));
    }

    @Test
    void defaultExpressionBooleanFieldMatch_passes() {
        // Comparison produces boolean, target field IS boolean → ok
        JsonNode expr = parse("""
                {"value": {"gt": ["payload.age", 18]}}
                """);
        ExpressionRule rule = makeRule("is_active", "default", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void defaultRefExpression_passes() {
        JsonNode expr = parse("""
                {"value": {"ref": "context.actor.scope_name"}}
                """);
        ExpressionRule rule = makeRule("name", "default", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void warningRule_validWithMessage_passes() {
        JsonNode expr = parse("""
                {"when": {"and": [
                    {"eq": ["payload.status", "active"]},
                    {"lt": ["payload.age", 5]}
                ]}}
                """);
        ExpressionRule rule = makeRule("name", "warning", expr, "Age seems too low");

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void unknownOperator_rejected() {
        JsonNode expr = parse("""
                {"when": {"contains": ["payload.name", "test"]}}
                """);
        ExpressionRule rule = makeRule("name", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Unknown operator")));
    }

    @Test
    void notWrapsLogical_violation() {
        JsonNode expr = parse("""
                {"when": {"not": {"and": [
                    {"eq": ["payload.status", "active"]},
                    {"gt": ["payload.age", 5]}
                ]}}}
                """);
        ExpressionRule rule = makeRule("name", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Nested logical")));
    }

    @Test
    void targetFieldNotInShape_violation() {
        JsonNode expr = parse("""
                {"when": {"eq": ["payload.status", "active"]}}
                """);
        ExpressionRule rule = makeRule("nonexistent", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("does not exist")));
    }

    @Test
    void orderingOnIntegerField_passes() {
        JsonNode expr = parse("""
                {"when": {"gt": ["payload.age", 18]}}
                """);
        ExpressionRule rule = makeRule("age", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void orderingOnDecimalField_passes() {
        JsonNode expr = parse("""
                {"when": {"lte": ["payload.temperature", 37.5]}}
                """);
        ExpressionRule rule = makeRule("temperature", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    // --- Helpers ---

    private void addField(ArrayNode fields, String name, String type, boolean required) {
        addField(fields, name, type, required, null);
    }

    private void addField(ArrayNode fields, String name, String type, boolean required, List<String> options) {
        ObjectNode field = objectMapper.createObjectNode();
        field.put("name", name);
        field.put("type", type);
        field.put("required", required);
        field.put("deprecated", false);
        field.put("display_order", fields.size() + 1);
        if (options != null) {
            ArrayNode optArray = field.putArray("options");
            options.forEach(optArray::add);
        }
        fields.add(field);
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Bad test JSON", e);
        }
    }

    private ExpressionRule makeRule(String fieldName, String ruleType, JsonNode expression, String message) {
        return new ExpressionRule(
                UUID.randomUUID(), "test_activity", "test_shape/v1",
                fieldName, ruleType, expression, message, null);
    }

    private DeployTimeValidator makeValidator() {
        // For unit tests, we use a validator with null repositories
        // Only validateRule is used (doesn't need repos)
        return new DeployTimeValidator(null, null);
    }
}
