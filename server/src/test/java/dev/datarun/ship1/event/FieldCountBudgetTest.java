package dev.datarun.ship1.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ADR-004 §S13 60-field budget enforcement at shape-registry load.
 *
 * <p>Ship-3 §6.1 sub-decision 5 — this is a unit test, not an HTTP walkthrough. No
 * coordinator-runtime authoring path exists in Ship-3, so an HTTP surface to assert against
 * does not exist (DR-2 / FP-012 trigger evidence). The enforcement runs inside
 * {@link ShapePayloadValidator#init()} on every shape resource loaded from the classpath; an
 * over-budget shape is rejected before it can be registered.
 *
 * <p>Field-count interpretation chosen: top-level {@code properties} keys. Nested object
 * properties are not flattened. This is the interpretation enforced by
 * {@link ShapePayloadValidator#enforceFieldCountBudget}; it is recorded here for retro
 * confirmation. If a deployer-authoring surface later forces a richer interpretation
 * (e.g. counting keys recursively across nested objects, or counting array element schemas),
 * this test must be updated in lock-step with the §S13 enforcement code.
 *
 * <p>Real platform-bundled and deployer shapes (assignment_*, conflict_*, household_observation
 * v1+v2, subjects_merged, subject_split) are far below the limit; the
 * {@link ShapePayloadValidatorTest#v1_and_v2_household_observation_both_known} integration test
 * proves they all load successfully through the same gate.
 */
class FieldCountBudgetTest {

    private static final JsonSchemaFactory FACTORY =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    @Test
    void rejects_shape_with_61_top_level_properties() {
        JsonSchema overlimit = synthesizeShapeWith(ShapePayloadValidator.MAX_FIELDS_PER_SHAPE + 1);

        assertThatThrownBy(() ->
                ShapePayloadValidator.enforceFieldCountBudget("synthetic_overlimit/v1", overlimit))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("synthetic_overlimit/v1")
                .hasMessageContaining(String.valueOf(ShapePayloadValidator.MAX_FIELDS_PER_SHAPE + 1))
                .hasMessageContaining(String.valueOf(ShapePayloadValidator.MAX_FIELDS_PER_SHAPE));
    }

    @Test
    void accepts_shape_with_exactly_60_top_level_properties() {
        JsonSchema atLimit = synthesizeShapeWith(ShapePayloadValidator.MAX_FIELDS_PER_SHAPE);
        // No throw — the limit is inclusive (≤ 60 OK; > 60 rejected).
        ShapePayloadValidator.enforceFieldCountBudget("synthetic_atlimit/v1", atLimit);
    }

    @Test
    void accepts_zero_property_shape() {
        // Edge case: an empty properties object is below the limit by definition.
        JsonSchema empty = FACTORY.getSchema("""
                { "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "type": "object",
                  "properties": {} }""");
        ShapePayloadValidator.enforceFieldCountBudget("synthetic_empty/v1", empty);
    }

    private static JsonSchema synthesizeShapeWith(int propertyCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "type": "object",
                  "properties": {
                """);
        for (int i = 0; i < propertyCount; i++) {
            if (i > 0) sb.append(",\n");
            sb.append("    \"f").append(i).append("\": {\"type\": \"string\"}");
        }
        sb.append("\n  }\n}");
        assertThat(propertyCount).isGreaterThanOrEqualTo(0);
        return FACTORY.getSchema(sb.toString());
    }

    // ------------------------------------------------------------------------------------------
    // G-9 closeout — public callable API: validateShapeBudget(JsonNode)
    //
    // Spec: ADR-004 §S13 row 1 ("Fields per shape" = 60) enforced at the candidate-schema level,
    // not just at JAR-bundle time. The public entry point accepts a JsonNode (the schema root)
    // so any future code path — including the FP-012b deployer-authoring HTTP surface — can call
    // it without first instantiating a JsonSchema. Until that surface exists, the guard is
    // exercised at startup and from these tests.
    // ------------------------------------------------------------------------------------------

    @Test
    void validateShapeBudget_jsonNode_rejects_61_top_level_properties() throws Exception {
        JsonNode overlimit =
                synthesizeJsonNodeWith(ShapePayloadValidator.MAX_FIELDS_PER_SHAPE + 1);

        assertThatThrownBy(() -> ShapePayloadValidator.validateShapeBudget(overlimit))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.valueOf(ShapePayloadValidator.MAX_FIELDS_PER_SHAPE + 1))
                .hasMessageContaining(String.valueOf(ShapePayloadValidator.MAX_FIELDS_PER_SHAPE));
    }

    @Test
    void validateShapeBudget_jsonNode_accepts_exactly_60_top_level_properties() throws Exception {
        JsonNode atLimit = synthesizeJsonNodeWith(ShapePayloadValidator.MAX_FIELDS_PER_SHAPE);
        // No throw — limit is inclusive (≤ 60 OK; > 60 rejected).
        ShapePayloadValidator.validateShapeBudget(atLimit);
    }

    /**
     * Simulates the {@code @PostConstruct} init-loop body: iterate a deliberately injected map
     * of {shape_ref → schema-root JsonNode}, call the budget guard on each, expect failure on
     * the over-budget entry with the offender's shape_ref in the message. This is the
     * "deliberately injected over-budget map" path called out in the G-9 spec — it proves the
     * boot-time gate fires <em>and</em> identifies the offender, without requiring a heavy
     * Spring context-failure test.
     */
    @Test
    void init_loop_gate_throws_with_offender_shape_ref_when_any_bundle_exceeds_budget()
            throws Exception {
        Map<String, JsonNode> bundle = new LinkedHashMap<>();
        bundle.put("good_shape/v1",
                synthesizeJsonNodeWith(ShapePayloadValidator.MAX_FIELDS_PER_SHAPE));
        bundle.put("synthetic_overlimit/v1",
                synthesizeJsonNodeWith(ShapePayloadValidator.MAX_FIELDS_PER_SHAPE + 1));

        assertThatThrownBy(() -> {
            for (Map.Entry<String, JsonNode> e : bundle.entrySet()) {
                ShapePayloadValidator.validateShapeBudget(e.getKey(), e.getValue());
            }
        })
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("synthetic_overlimit/v1")
                .hasMessageContaining(String.valueOf(ShapePayloadValidator.MAX_FIELDS_PER_SHAPE + 1))
                .hasMessageContaining("ADR-004 §S13");
    }

    private static JsonNode synthesizeJsonNodeWith(int propertyCount) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"$schema\":\"https://json-schema.org/draft/2020-12/schema\",")
                .append("\"type\":\"object\",\"properties\":{");
        for (int i = 0; i < propertyCount; i++) {
            if (i > 0) sb.append(',');
            sb.append("\"f").append(i).append("\":{\"type\":\"string\"}");
        }
        sb.append("}}");
        return mapper.readTree(sb.toString());
    }
}
