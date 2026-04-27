package dev.datarun.ship1.event;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.Test;

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
}
