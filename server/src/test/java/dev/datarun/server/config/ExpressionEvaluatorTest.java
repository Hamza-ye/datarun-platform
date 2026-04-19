package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cross-platform expression evaluation tests (E7 equivalence pattern).
 * Reads from contracts/fixtures/expression-evaluation.json — same fixtures used by Dart evaluator.
 */
class ExpressionEvaluatorTest {

    private static JsonNode fixtures;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void loadFixtures() throws Exception {
        // Walk up from server/ to find contracts/
        File fixtureFile = new File("../contracts/fixtures/expression-evaluation.json");
        if (!fixtureFile.exists()) {
            // Fallback: absolute path
            fixtureFile = new File("/home/hamza/datarun-platform/contracts/fixtures/expression-evaluation.json");
        }
        fixtures = objectMapper.readTree(fixtureFile);
        assertNotNull(fixtures.get("cases"), "Fixture file must contain 'cases' array");
    }

    @TestFactory
    Stream<DynamicTest> evaluateAllFixtures() {
        return StreamSupport.stream(fixtures.get("cases").spliterator(), false)
                .map(testCase -> {
                    String id = testCase.get("id").asText();
                    String description = testCase.get("description").asText();
                    return DynamicTest.dynamicTest(id + ": " + description, () -> runCase(testCase));
                });
    }

    private void runCase(JsonNode testCase) {
        String id = testCase.get("id").asText();
        JsonNode expression = testCase.get("expression");
        JsonNode valuesNode = testCase.get("values");
        Map<String, Object> values = parseValues(valuesNode);

        if (testCase.has("expected")) {
            // Condition evaluation → boolean
            boolean expected = testCase.get("expected").asBoolean();
            boolean actual = ExpressionEvaluator.evaluateCondition(expression, values);
            assertEquals(expected, actual, "Case '" + id + "' condition mismatch");
        } else if (testCase.has("expected_value")) {
            // Value evaluation
            JsonNode expectedNode = testCase.get("expected_value");
            Object actual = ExpressionEvaluator.evaluateValue(expression, values);

            if (expectedNode.isNull()) {
                assertNull(actual, "Case '" + id + "' expected null value");
            } else if (expectedNode.isTextual()) {
                assertEquals(expectedNode.asText(), actual, "Case '" + id + "' value mismatch");
            } else if (expectedNode.isNumber()) {
                assertNotNull(actual, "Case '" + id + "' expected non-null number");
                double expectedNum = expectedNode.asDouble();
                double actualNum = ((Number) actual).doubleValue();
                assertEquals(expectedNum, actualNum, 0.001, "Case '" + id + "' numeric value mismatch");
            } else if (expectedNode.isBoolean()) {
                assertEquals(expectedNode.asBoolean(), actual, "Case '" + id + "' boolean value mismatch");
            } else {
                fail("Case '" + id + "': unsupported expected_value type");
            }
        } else {
            fail("Case '" + id + "': must have 'expected' or 'expected_value'");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseValues(JsonNode valuesNode) {
        Map<String, Object> values = new HashMap<>();
        if (valuesNode == null || valuesNode.isNull()) return values;

        valuesNode.fields().forEachRemaining(entry -> {
            JsonNode val = entry.getValue();
            if (val.isNull()) {
                values.put(entry.getKey(), null);
            } else if (val.isTextual()) {
                values.put(entry.getKey(), val.asText());
            } else if (val.isBoolean()) {
                values.put(entry.getKey(), val.asBoolean());
            } else if (val.isIntegralNumber()) {
                values.put(entry.getKey(), val.asLong());
            } else if (val.isFloatingPointNumber()) {
                values.put(entry.getKey(), val.asDouble());
            } else if (val.isArray()) {
                List<Object> list = new ArrayList<>();
                for (JsonNode item : val) {
                    if (item.isTextual()) list.add(item.asText());
                    else if (item.isNumber()) list.add(item.numberValue());
                    else if (item.isBoolean()) list.add(item.asBoolean());
                }
                values.put(entry.getKey(), list);
            }
        });
        return values;
    }
}
