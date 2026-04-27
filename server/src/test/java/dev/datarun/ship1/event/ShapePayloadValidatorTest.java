package dev.datarun.ship1.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ship-3 unit coverage for {@link ShapePayloadValidator}. Loads the real Spring context so the
 * classpath-scan registry-load path runs. Covers:
 *
 * <ul>
 *   <li>multi-version routing on {@code shape_ref} (v1 + v2 known simultaneously);</li>
 *   <li>v1 still validates after v2 lands (ADR-004 §S10 "all versions valid forever");</li>
 *   <li>v2 additive field accepted, v1 schema rejects v2-only fields (additionalProperties:false);</li>
 *   <li>unknown {@code shape_ref} surfaces the {@code shape_unknown} marker;</li>
 *   <li>filename-to-shape_ref parsing handles both v-suffixed and plain conventions.</li>
 * </ul>
 */
@SpringBootTest(classes = dev.datarun.ship1.DatarunApplication.class)
@ActiveProfiles("test")
class ShapePayloadValidatorTest {

    @Autowired
    private ShapePayloadValidator validator;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void v1_and_v2_household_observation_both_known() {
        assertThat(validator.isKnown("household_observation/v1")).isTrue();
        assertThat(validator.isKnown("household_observation/v2")).isTrue();
        assertThat(validator.knownShapeRefs())
                .contains("household_observation/v1", "household_observation/v2",
                        "assignment_created/v1", "conflict_detected/v1");
    }

    @Test
    void v1_payload_validates_against_v1() throws Exception {
        JsonNode payload = mapper.readTree("""
                {
                  "household_name": "Khan",
                  "head_of_household_name": "HoH",
                  "household_size": 5,
                  "village_ref": "00000000-0000-0000-0000-000000000aaa"
                }""");
        assertThat(validator.validate("household_observation/v1", payload)).isEmpty();
    }

    @Test
    void v2_payload_with_new_optional_field_validates_against_v2() throws Exception {
        JsonNode payload = mapper.readTree("""
                {
                  "household_name": "Khan",
                  "head_of_household_name": "HoH",
                  "household_size": 5,
                  "village_ref": "00000000-0000-0000-0000-000000000aaa",
                  "head_of_household_phone": "+1-555-0100"
                }""");
        assertThat(validator.validate("household_observation/v2", payload)).isEmpty();
    }

    @Test
    void v2_payload_rejected_against_v1_because_additionalProperties_false() throws Exception {
        // The v2-only field head_of_household_phone is not in v1's properties; v1 has
        // additionalProperties: false, so v1 must reject it. This proves the version
        // boundary is enforced by the schema content, not by accident.
        JsonNode payload = mapper.readTree("""
                {
                  "household_name": "Khan",
                  "head_of_household_name": "HoH",
                  "household_size": 5,
                  "village_ref": "00000000-0000-0000-0000-000000000aaa",
                  "head_of_household_phone": "+1-555-0100"
                }""");
        List<String> errors = validator.validate("household_observation/v1", payload);
        assertThat(errors).isNotEmpty();
    }

    @Test
    void unknown_shape_ref_surfaces_shape_unknown_marker() throws Exception {
        JsonNode payload = mapper.readTree("{}");
        List<String> errors = validator.validate("household_observation/v9", payload);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("shape_unknown");
    }

    @Test
    void parseShapeRef_handles_both_naming_conventions() {
        assertThat(ShapePayloadValidator.parseShapeRef("household_observation.v2.schema.json"))
                .isEqualTo("household_observation/v2");
        assertThat(ShapePayloadValidator.parseShapeRef("household_observation.v1.schema.json"))
                .isEqualTo("household_observation/v1");
        assertThat(ShapePayloadValidator.parseShapeRef("assignment_created.schema.json"))
                .isEqualTo("assignment_created/v1");
    }
}

