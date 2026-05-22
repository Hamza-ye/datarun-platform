package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * IDR-024 / FP-007 gates for assignment-administration containment.
 */
class AssignmentContainmentIntegrationTest extends AbstractIntegrationTest {

    @Autowired private AssignmentService assignmentService;
    @Autowired private LocationRepository locationRepository;

    private static final UUID ADMIN = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID CREATOR = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CREATOR_TWO = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID TARGET = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID TARGET_TWO = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID UNASSIGNED = UUID.fromString("30000000-0000-0000-0000-000000000001");

    private UUID region;
    private UUID districtX;
    private UUID districtY;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("DELETE FROM actor_tokens");
        jdbcTemplate.execute("DELETE FROM subject_locations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM device_sync_state");
        jdbcTemplate.execute("DELETE FROM locations");

        region = UUID.randomUUID();
        districtX = UUID.randomUUID();
        districtY = UUID.randomUUID();
        locationRepository.insert(region, "Region", null, "region");
        locationRepository.insert(districtX, "District X", region, "district");
        locationRepository.insert(districtY, "District Y", region, "district");
    }

    @Test
    void restrictedGeographicCreatorCannotCreateUnrestrictedGeography() {
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, CREATOR, "coordinator",
                districtX, null, null, past(), null);

        assertThatThrownBy(() -> assignmentService.createAssignment(CREATOR, TARGET, "field_worker",
                null, null, null, past(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scope containment violation");
    }

    @Test
    void subjectRestrictedCreatorCannotCreateSubjectUnrestrictedAssignment() {
        UUID subjectA = UUID.randomUUID();
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, CREATOR, "case_manager",
                null, List.of(subjectA), null, past(), null);

        assertThatThrownBy(() -> assignmentService.createAssignment(CREATOR, TARGET, "field_worker",
                null, null, null, past(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scope containment violation");
    }

    @Test
    void subjectRestrictedCreatorCannotCreateOutsideSubjectAssignment() {
        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, CREATOR, "case_manager",
                null, List.of(subjectA), null, past(), null);

        assertThatThrownBy(() -> assignmentService.createAssignment(CREATOR, TARGET, "field_worker",
                null, List.of(subjectB), null, past(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scope containment violation");
    }

    @Test
    void activityRestrictedCreatorCannotCreateActivityUnrestrictedAssignment() {
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, CREATOR, "activity_manager",
                null, null, List.of("vaccination"), past(), null);

        assertThatThrownBy(() -> assignmentService.createAssignment(CREATOR, TARGET, "field_worker",
                null, null, null, past(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scope containment violation");
    }

    @Test
    void activityRestrictedCreatorCannotCreateOutsideActivityAssignment() {
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, CREATOR, "activity_manager",
                null, null, List.of("vaccination"), past(), null);

        assertThatThrownBy(() -> assignmentService.createAssignment(CREATOR, TARGET, "field_worker",
                null, null, List.of("survey"), past(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scope containment violation");
    }

    @Test
    void subjectListOnlyCreatorWithUnrestrictedGeographyIsNotRootAuthority() {
        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, CREATOR, "case_manager",
                null, List.of(subjectA), null, past(), null);

        assertThatThrownBy(() -> assignmentService.createAssignment(CREATOR, TARGET, "field_worker",
                null, null, null, past(), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> assignmentService.createAssignment(CREATOR, TARGET, "field_worker",
                null, List.of(subjectB), null, past(), null))
                .isInstanceOf(IllegalArgumentException.class);

        Event bounded = assignmentService.createAssignment(CREATOR, TARGET, "field_worker",
                null, List.of(subjectA), null, past(), null);
        assertThat(bounded.shapeRef()).isEqualTo("assignment_created/v1");
    }

    @Test
    void separateCreatorAssignmentsDoNotUnionAcrossAxes() {
        UUID requestedSubject = UUID.randomUUID();
        UUID otherSubject = UUID.randomUUID();
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, CREATOR, "coordinator",
                districtX, List.of(otherSubject), List.of("vaccination"), past(), null);
        assignmentService.createAssignment(ADMIN, CREATOR, "coordinator",
                districtY, List.of(requestedSubject), List.of("vaccination"), past(), null);

        assertThatThrownBy(() -> assignmentService.createAssignment(CREATOR, TARGET, "field_worker",
                districtX, List.of(requestedSubject), List.of("vaccination"), past(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("one active creator assignment");
    }

    @Test
    void actorWithNoActiveAssignmentsCannotCreateBroadProductionAuthority() {
        assertThatThrownBy(() -> assignmentService.createAssignment(UNASSIGNED, TARGET, "admin",
                null, null, null, past(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("actor has no active assignments");
    }

    @Test
    void explicitInitialBootstrapCreatesRootThenRootCreatesBroadAuthority() {
        Event root = assignmentService.createInitialBootstrapAssignment(ADMIN, "admin",
                null, null, null, past(), null);
        JsonNode scope = root.payload().path("scope");
        assertThat(root.actorRef().path("id").asText()).isEqualTo("system:assignment_bootstrap/initial");
        assertThat(scope.path("geographic").isNull()).isTrue();
        assertThat(scope.path("subject_list").isNull()).isTrue();
        assertThat(scope.path("activity").isNull()).isTrue();

        Event delegatedRoot = assignmentService.createAssignment(ADMIN, TARGET, "supervisor",
                null, null, null, past(), null);
        assertThat(delegatedRoot.shapeRef()).isEqualTo("assignment_created/v1");

        assertThatThrownBy(() -> assignmentService.createInitialBootstrapAssignment(TARGET_TWO, "admin",
                null, null, null, past(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bootstrap authority unavailable");
    }

    @Test
    void emptySubjectAndActivityArraysRejectedOnAssignmentCreation() {
        bootstrapAdmin();

        assertThatThrownBy(() -> assignmentService.createAssignment(ADMIN, TARGET, "field_worker",
                null, Collections.emptyList(), null, past(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("subject_list");
        assertThatThrownBy(() -> assignmentService.createAssignment(ADMIN, TARGET, "field_worker",
                null, null, Collections.emptyList(), past(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("activity");
    }

    @Test
    void endAssignmentRequiresTargetAssignmentAuthority() {
        bootstrapAdmin();
        Event targetAssignment = assignmentService.createAssignment(ADMIN, TARGET, "field_worker",
                districtX, null, null, past(), null);
        UUID targetAssignmentId = UUID.fromString(targetAssignment.subjectRef().path("id").asText());

        assignmentService.createAssignment(ADMIN, CREATOR, "coordinator",
                districtY, null, null, past(), null);
        assertThatThrownBy(() -> assignmentService.endAssignment(
                targetAssignmentId, CREATOR, "outside scope"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot end assignment outside their scope");

        assignmentService.createAssignment(ADMIN, CREATOR_TWO, "coordinator",
                region, null, null, past(), null);
        Event endedByCoveringActor = assignmentService.endAssignment(
                targetAssignmentId, CREATOR_TWO, "covered target scope");
        assertThat(endedByCoveringActor.shapeRef()).isEqualTo("assignment_ended/v1");

        Event secondTargetAssignment = assignmentService.createAssignment(ADMIN, TARGET_TWO, "field_worker",
                districtX, null, null, past(), null);
        UUID secondTargetAssignmentId = UUID.fromString(secondTargetAssignment.subjectRef().path("id").asText());
        Event endedByRoot = assignmentService.endAssignment(secondTargetAssignmentId, ADMIN, "root authority");
        assertThat(endedByRoot.shapeRef()).isEqualTo("assignment_ended/v1");
    }

    private void bootstrapAdmin() {
        assignmentService.createInitialBootstrapAssignment(ADMIN, "admin",
                null, null, null, past(), null);
    }

    private OffsetDateTime past() {
        return OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
    }
}
