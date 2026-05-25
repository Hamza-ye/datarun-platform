package dev.datarun.server.contracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.config.ShapePayloadValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FP-010 contract tests for the six platform-bundled event payload shapes.
 */
class PlatformPayloadShapeContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PlatformPayloadContractValidator validator =
            new PlatformPayloadContractValidator(objectMapper);

    @ParameterizedTest
    @ValueSource(strings = {
            "assignment_created/v1",
            "assignment_ended/v1",
            "conflict_detected/v1",
            "conflict_resolved/v1",
            "subjects_merged/v1",
            "subject_split/v1"
    })
    void platformPayloadContract_isBundledForRuntimeValidation(String shapeRef) {
        assertThat(validator.isPlatformPayloadShape(shapeRef)).isTrue();
    }

    @Test
    void representativePlatformPayloads_validateAgainstContracts() {
        representativePayloads().forEach((shapeRef, payload) ->
                assertThat(validator.validate(shapeRef, payload))
                        .as(shapeRef)
                        .isEmpty());
    }

    @Test
    void requiredFields_areEnforcedByBundledContracts() {
        ObjectNode missingAssignmentTarget = objectMapper.createObjectNode();
        missingAssignmentTarget.put("role", "admin");
        missingAssignmentTarget.set("scope", scope(null, null, null));
        missingAssignmentTarget.put("valid_from", "2026-01-01T00:00:00Z");
        missingAssignmentTarget.putNull("valid_to");

        assertThat(validator.validate("assignment_created/v1", missingAssignmentTarget))
                .anySatisfy(error -> assertThat(error).contains("target_actor"));

        ObjectNode missingFlagCategory = objectMapper.createObjectNode();
        missingFlagCategory.put("source_event_id", UUID.randomUUID().toString());
        missingFlagCategory.put("resolvability", "manual_only");
        missingFlagCategory.set("designated_resolver", actor(UUID.randomUUID()));

        assertThat(validator.validate("conflict_detected/v1", missingFlagCategory))
                .anySatisfy(error -> assertThat(error).contains("flag_category"));

        ObjectNode missingResolver = objectMapper.createObjectNode();
        missingResolver.put("source_event_id", UUID.randomUUID().toString());
        missingResolver.put("flag_category", "scope_violation");
        missingResolver.put("resolvability", "manual_only");

        assertThat(validator.validate("conflict_detected/v1", missingResolver))
                .anySatisfy(error -> assertThat(error).contains("designated_resolver"));
    }

    @Test
    void syncPayloadValidator_usesPlatformContractsForPlatformShapes() {
        ShapePayloadValidator shapePayloadValidator = new ShapePayloadValidator(null, validator);

        ObjectNode missingReason = objectMapper.createObjectNode();

        assertThat(shapePayloadValidator.validate("assignment_ended/v1", missingReason))
                .anySatisfy(error -> assertThat(error).contains("reason"));
    }

    private Map<String, JsonNode> representativePayloads() {
        Map<String, JsonNode> payloads = new LinkedHashMap<>();

        ObjectNode assignmentCreated = objectMapper.createObjectNode();
        assignmentCreated.set("target_actor", actor(UUID.randomUUID()));
        assignmentCreated.put("role", "case_manager");
        assignmentCreated.set("scope", scope(UUID.randomUUID(), UUID.randomUUID(), "case_followup"));
        assignmentCreated.put("valid_from", "2026-01-01T00:00:00Z");
        assignmentCreated.putNull("valid_to");
        payloads.put("assignment_created/v1", assignmentCreated);

        ObjectNode assignmentEnded = objectMapper.createObjectNode();
        assignmentEnded.put("reason", "rotation ended");
        payloads.put("assignment_ended/v1", assignmentEnded);

        ObjectNode conflictDetected = objectMapper.createObjectNode();
        conflictDetected.put("source_event_id", UUID.randomUUID().toString());
        conflictDetected.put("flag_category", "scope_violation");
        conflictDetected.put("resolvability", "manual_only");
        conflictDetected.set("designated_resolver", actor(UUID.randomUUID()));
        conflictDetected.put("reason", "Subject outside actor scope");
        payloads.put("conflict_detected/v1", conflictDetected);

        ObjectNode conflictResolved = objectMapper.createObjectNode();
        conflictResolved.put("flag_event_id", UUID.randomUUID().toString());
        conflictResolved.put("source_event_id", UUID.randomUUID().toString());
        conflictResolved.put("resolution", "accepted");
        conflictResolved.putNull("reclassified_subject_id");
        conflictResolved.put("reason", "Reviewed");
        payloads.put("conflict_resolved/v1", conflictResolved);

        ObjectNode subjectsMerged = objectMapper.createObjectNode();
        subjectsMerged.put("surviving_id", UUID.randomUUID().toString());
        subjectsMerged.put("retired_id", UUID.randomUUID().toString());
        subjectsMerged.put("reason", "duplicate record");
        payloads.put("subjects_merged/v1", subjectsMerged);

        ObjectNode subjectSplit = objectMapper.createObjectNode();
        subjectSplit.put("source_id", UUID.randomUUID().toString());
        subjectSplit.put("successor_id", UUID.randomUUID().toString());
        subjectSplit.put("reason", "incorrect identity");
        payloads.put("subject_split/v1", subjectSplit);

        return payloads;
    }

    private ObjectNode actor(UUID actorId) {
        ObjectNode actor = objectMapper.createObjectNode();
        actor.put("type", "actor");
        actor.put("id", actorId.toString());
        return actor;
    }

    private ObjectNode scope(UUID geographic, UUID subject, String activity) {
        ObjectNode scope = objectMapper.createObjectNode();
        if (geographic == null) {
            scope.putNull("geographic");
        } else {
            scope.put("geographic", geographic.toString());
        }
        if (subject == null) {
            scope.putNull("subject_list");
        } else {
            ArrayNode subjects = scope.putArray("subject_list");
            subjects.add(subject.toString());
        }
        if (activity == null) {
            scope.putNull("activity");
        } else {
            ArrayNode activities = scope.putArray("activity");
            activities.add(activity);
        }
        return scope;
    }
}
