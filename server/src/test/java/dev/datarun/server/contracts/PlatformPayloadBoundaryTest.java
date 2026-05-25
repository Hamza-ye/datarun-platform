package dev.datarun.server.contracts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.config.ActivityService;
import dev.datarun.server.config.ConfigPackager;
import dev.datarun.server.config.Shape;
import dev.datarun.server.config.ShapeRepository;
import dev.datarun.server.config.ShapeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FP-010 boundary tests: platform payload JSON Schemas are runtime contracts,
 * not deployer-owned shape registry rows.
 */
class PlatformPayloadBoundaryTest extends AbstractIntegrationTest {

    @Autowired private ShapeRepository shapeRepository;
    @Autowired private ShapeService shapeService;
    @Autowired private ActivityService activityService;
    @Autowired private ConfigPackager configPackager;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void cleanConfig() {
        jdbcTemplate.update("DELETE FROM config_packages");
        jdbcTemplate.update("DELETE FROM expression_rules");
        jdbcTemplate.update("DELETE FROM activities");
        jdbcTemplate.update("DELETE FROM shapes");
    }

    @Test
    void platformPayloadContracts_areNotSeededAsDeployerShapeRows() {
        for (String shapeName : PlatformPayloadShapes.SHAPE_NAMES) {
            assertThat(shapeRepository.findByNameAndVersion(shapeName, 1))
                    .as(shapeName + "/v1 must not be a deployer shape row")
                    .isEmpty();
        }
    }

    @Test
    void platformPayloadNames_areNotDeployerEditable() {
        ObjectNode deployerSchema = objectMapper.createObjectNode();
        deployerSchema.putArray("fields");

        assertThat(shapeService.createShape("assignment_created", "standard", deployerSchema))
                .anySatisfy(error -> assertThat(error).contains("Platform-bundled shape"));

        assertThat(shapeService.createVersion("conflict_detected", "standard", deployerSchema))
                .anySatisfy(error -> assertThat(error).contains("Platform-bundled shape"));

        assertThatThrownBy(() -> shapeService.deprecate("subject_split", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Platform-bundled shape");
    }

    @Test
    void platformPayloadRefs_cannotBeBoundAsDeployerActivityShapes() {
        ObjectNode config = objectMapper.createObjectNode();
        config.putArray("shapes").add("conflict_detected/v1");
        config.putObject("roles").putArray("field_worker").add("capture");

        assertThat(activityService.createActivity("platform_shape_activity", "standard", config))
                .anySatisfy(error -> assertThat(error)
                        .contains("platform-bundled")
                        .contains("cannot be bound"));
    }

    @Test
    void legacyPlatformMirrorRows_areFilteredFromConfigPackages() {
        ObjectNode legacyMirror = objectMapper.createObjectNode();
        legacyMirror.putArray("fields")
                .addObject()
                .put("name", "source_event_id")
                .put("type", "text")
                .put("required", true);
        shapeRepository.insert(new Shape("conflict_detected", 1, "active", "standard", legacyMirror, null));

        int version = configPackager.publish(null);
        var latest = configPackager.getLatest().orElseThrow();

        assertThat(version).isEqualTo(latest.version());
        assertThat(latest.packageJson().path("shapes").has("conflict_detected/v1")).isFalse();
        assertThat(latest.packageJson().at("/sensitivity_classifications/shapes")
                .has("conflict_detected/v1")).isFalse();
    }
}
