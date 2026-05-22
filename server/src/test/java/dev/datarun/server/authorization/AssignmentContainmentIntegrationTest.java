package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IDR-024 / FP-007 gates for assignment-administration containment.
 */
@AutoConfigureMockMvc
class AssignmentContainmentIntegrationTest extends AbstractIntegrationTest {

    @Autowired private AssignmentService assignmentService;
    @Autowired private LocationRepository locationRepository;
    @Autowired private ActorTokenRepository actorTokenRepository;
    @Autowired private TestRestTemplate rest;
    @Autowired private MockMvc mockMvc;

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

    @Test
    void assignmentApiRejectsUnauthenticatedCreateAndEnd() throws Exception {
        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/assignments/" + UUID.randomUUID() + "/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void assignmentApiIgnoresSpoofedCreatorActorIdOnCreate() {
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, CREATOR, "coordinator",
                districtX, null, null, past(), null);
        String creatorToken = actorTokenRepository.createToken(CREATOR);

        Map<String, Object> body = assignmentCreateBody(
                TARGET, "field_worker", districtY, null, null);
        body.put("creator_actor_id", ADMIN.toString());

        ResponseEntity<JsonNode> response = postJson("/api/assignments", body, creatorToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("error").asText()).contains("Scope containment violation");
        assertThat(assignmentCreatedCountForTarget(TARGET)).isZero();
    }

    @Test
    void assignmentApiIgnoresSpoofedActorIdOnEnd() {
        bootstrapAdmin();
        Event targetAssignment = assignmentService.createAssignment(ADMIN, TARGET, "field_worker",
                districtX, null, null, past(), null);
        UUID assignmentId = UUID.fromString(targetAssignment.subjectRef().path("id").asText());
        assignmentService.createAssignment(ADMIN, CREATOR, "coordinator",
                districtY, null, null, past(), null);
        String creatorToken = actorTokenRepository.createToken(CREATOR);

        ResponseEntity<JsonNode> response = postJson(
                "/api/assignments/" + assignmentId + "/end",
                Map.of("actor_id", ADMIN.toString(), "reason", "spoofed root actor"),
                creatorToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("error").asText())
                .contains("cannot end assignment outside their scope");
        assertThat(assignmentEndedCount(assignmentId)).isZero();
    }

    @Test
    void assignmentApiAuthenticatedCreatorWithInsufficientScopeRejected() {
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, CREATOR, "coordinator",
                districtX, null, null, past(), null);
        String creatorToken = actorTokenRepository.createToken(CREATOR);

        ResponseEntity<JsonNode> response = postJson(
                "/api/assignments",
                assignmentCreateBody(TARGET, "field_worker", districtY, null, null),
                creatorToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("error").asText()).contains("Scope containment violation");
        assertThat(assignmentCreatedCountForTarget(TARGET)).isZero();
    }

    @Test
    void assignmentApiAuthenticatedCreatorWithCoveringMultiAxisScopeSucceeds() {
        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, CREATOR, "coordinator",
                region, List.of(subjectA, subjectB), List.of("vaccination", "survey"),
                past(), null);
        String creatorToken = actorTokenRepository.createToken(CREATOR);

        ResponseEntity<JsonNode> response = postJson(
                "/api/assignments",
                assignmentCreateBody(TARGET, "field_worker", districtX,
                        List.of(subjectA), List.of("vaccination")),
                creatorToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UUID assignmentId = UUID.fromString(response.getBody().path("assignment_id").asText());
        assertThat(assignmentActorId(assignmentId)).isEqualTo(CREATOR.toString());
        assertThat(assignmentCreatedCountForTarget(TARGET)).isEqualTo(1);
    }

    @Test
    void assignmentApiCannotReachInitialBootstrapBySpoofingCreatorActorId() {
        String unassignedToken = actorTokenRepository.createToken(UNASSIGNED);
        Map<String, Object> body = assignmentCreateBody(TARGET, "admin", null, null, null);
        body.put("creator_actor_id", ADMIN.toString());

        ResponseEntity<JsonNode> response = postJson("/api/assignments", body, unassignedToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("error").asText()).contains("actor has no active assignments");
        assertThat(assignmentCreatedCount()).isZero();
    }

    private void bootstrapAdmin() {
        assignmentService.createInitialBootstrapAssignment(ADMIN, "admin",
                null, null, null, past(), null);
    }

    private OffsetDateTime past() {
        return OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
    }

    private ResponseEntity<JsonNode> postJson(String path, Map<String, Object> body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), JsonNode.class);
    }

    private Map<String, Object> assignmentCreateBody(UUID targetActorId, String role,
                                                     UUID geographicScope,
                                                     List<UUID> subjectList,
                                                     List<String> activityList) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("target_actor_id", targetActorId.toString());
        body.put("role", role);
        if (geographicScope != null) {
            body.put("geographic_scope", geographicScope.toString());
        }
        if (subjectList != null) {
            body.put("subject_list", subjectList.stream().map(UUID::toString).toList());
        }
        if (activityList != null) {
            body.put("activity_list", activityList);
        }
        body.put("valid_from", past().toString());
        return body;
    }

    private int assignmentCreatedCount() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_created/v1'
                """, Integer.class);
        return count == null ? 0 : count;
    }

    private int assignmentCreatedCountForTarget(UUID targetActorId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_created/v1'
                  AND payload->'target_actor'->>'id' = ?
                """, Integer.class, targetActorId.toString());
        return count == null ? 0 : count;
    }

    private int assignmentEndedCount(UUID assignmentId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_ended/v1'
                  AND subject_ref->>'id' = ?
                """, Integer.class, assignmentId.toString());
        return count == null ? 0 : count;
    }

    private String assignmentActorId(UUID assignmentId) {
        return jdbcTemplate.queryForObject("""
                SELECT actor_ref->>'id'
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_created/v1'
                  AND subject_ref->>'id' = ?
                ORDER BY sync_watermark
                LIMIT 1
                """, String.class, assignmentId.toString());
    }
}
