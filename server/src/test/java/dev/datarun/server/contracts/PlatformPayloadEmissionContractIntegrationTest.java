package dev.datarun.server.contracts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.authorization.AssignmentService;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.IdentityService;
import dev.datarun.server.integrity.ConflictResolutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies that actual server emission paths produce payloads accepted by the
 * platform payload contracts bundled from contracts/shapes.
 */
class PlatformPayloadEmissionContractIntegrationTest extends AbstractIntegrationTest {

    private static final UUID ACTOR_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID DEVICE_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    @Autowired private AssignmentService assignmentService;
    @Autowired private IdentityService identityService;
    @Autowired private ConflictResolutionService conflictResolutionService;
    @Autowired private EventRepository eventRepository;
    @Autowired private PlatformPayloadContractValidator validator;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("DELETE FROM actor_tokens");
        jdbcTemplate.execute("DELETE FROM subject_locations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM device_sync_state");
        jdbcTemplate.execute("DELETE FROM subject_aliases");
        jdbcTemplate.execute("DELETE FROM deployment_config");
        configureDefaultAssignmentAdminCapabilities();
    }

    @Test
    void serverPlatformEmissionPaths_validateAgainstPayloadContracts() {
        Event assignmentCreated = assignmentService.createInitialBootstrapAssignment(
                ACTOR_ID, "admin", null, null, null, past(), null);
        assertValid(assignmentCreated);

        Event source = insertSourceEvent(UUID.randomUUID());
        Event conflictDetected = conflictResolutionService.createManualIdentityConflict(
                source.id(), ACTOR_ID, "possible duplicate identity");
        assertValid(conflictDetected);

        Event conflictResolved = conflictResolutionService.resolve(
                conflictDetected.id(), "accepted", null, ACTOR_ID, "reviewed");
        assertValid(conflictResolved);

        Event subjectsMerged = identityService.merge(
                UUID.randomUUID(), UUID.randomUUID(), ACTOR_ID, "duplicate subject");
        assertValid(subjectsMerged);

        Event subjectSplit = identityService.split(
                UUID.randomUUID(), ACTOR_ID, "incorrect subject attribution");
        assertValid(subjectSplit);

        UUID assignmentId = UUID.fromString(assignmentCreated.subjectRef().path("id").asText());
        Event assignmentEnded = assignmentService.endAssignment(assignmentId, ACTOR_ID, "handoff complete");
        assertValid(assignmentEnded);
    }

    @Test
    void eventRepository_rejectsInvalidPlatformPayloadBackstop() {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "assignment");
        subjectRef.put("id", UUID.randomUUID().toString());

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", ACTOR_ID.toString());

        Event invalid = new Event(
                UUID.randomUUID(),
                "assignment_changed",
                "assignment_ended/v1",
                null,
                subjectRef,
                actorRef,
                DEVICE_ID,
                99,
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                objectMapper.createObjectNode()
        );

        assertThatThrownBy(() -> eventRepository.insert(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("assignment_ended/v1")
                .hasMessageContaining("reason");
    }

    private void assertValid(Event event) {
        assertThat(validator.validate(event.shapeRef(), event.payload()))
                .as(event.shapeRef())
                .isEmpty();
    }

    private Event insertSourceEvent(UUID subjectId) {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", subjectId.toString());

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", ACTOR_ID.toString());

        Event event = new Event(
                UUID.randomUUID(),
                "capture",
                "basic_capture/v1",
                null,
                subjectRef,
                actorRef,
                DEVICE_ID,
                1,
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                objectMapper.createObjectNode()
        );
        eventRepository.insert(event);
        return event;
    }

    private OffsetDateTime past() {
        return OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }
}
