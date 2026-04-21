package dev.datarun.server.contracts;

import dev.datarun.server.config.Shape;
import dev.datarun.server.config.ShapeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 3e contract test: the four platform-bundled shapes
 * ({@code conflict_detected/v1}, {@code conflict_resolved/v1},
 * {@code subjects_merged/v1}, {@code subject_split/v1}) are registered by
 * the server at application-ready time (ADR-002 Addendum DD-1).
 *
 * <p>Without these shapes registered, the server cannot emit architecturally
 * valid integrity or identity events. Their presence is a contract, not a
 * deployer concern.
 */
@SpringBootTest
@ActiveProfiles("test")
class PlatformShapeBootstrapTest {

    @Autowired
    private ShapeRepository shapeRepository;

    @ParameterizedTest
    @ValueSource(strings = {
            "conflict_detected",
            "conflict_resolved",
            "subjects_merged",
            "subject_split"
    })
    void platformShape_registeredAtStartup(String shapeName) {
        Optional<Shape> shape = shapeRepository.findByNameAndVersion(shapeName, 1);
        assertThat(shape)
                .as("Platform-bundled shape %s/v1 must be registered by PlatformShapeBootstrap",
                        shapeName)
                .isPresent();
    }
}
