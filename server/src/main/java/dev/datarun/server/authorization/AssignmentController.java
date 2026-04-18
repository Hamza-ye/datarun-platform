package dev.datarun.server.authorization;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.datarun.server.event.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Assignment management API. Online-only (ADR-3 S5).
 * Creates/ends assignments through the event store — no direct table writes.
 */
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final ScopeResolver scopeResolver;

    public AssignmentController(AssignmentService assignmentService,
                                ScopeResolver scopeResolver) {
        this.assignmentService = assignmentService;
        this.scopeResolver = scopeResolver;
    }

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody CreateAssignmentRequest request) {
        if (request.creatorActorId() == null || request.targetActorId() == null || request.role() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "missing required fields: creator_actor_id, target_actor_id, role"));
        }

        try {
            OffsetDateTime validFrom = request.validFrom() != null
                    ? OffsetDateTime.parse(request.validFrom())
                    : OffsetDateTime.now();
            OffsetDateTime validTo = request.validTo() != null
                    ? OffsetDateTime.parse(request.validTo())
                    : null;

            Event event = assignmentService.createAssignment(
                    request.creatorActorId(),
                    request.targetActorId(),
                    request.role(),
                    request.geographicScope(),
                    request.subjectList(),
                    request.activityList(),
                    validFrom,
                    validTo
            );

            return ResponseEntity.ok(Map.of(
                    "assignment_id", event.subjectRef().get("id").asText(),
                    "event_id", event.id().toString()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<?> endAssignment(@PathVariable UUID id,
                                           @RequestBody EndAssignmentRequest request) {
        if (request.actorId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "missing required field: actor_id"));
        }

        try {
            Event event = assignmentService.endAssignment(id, request.actorId(), request.reason());
            return ResponseEntity.ok(Map.of("event_id", event.id().toString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/actor/{actorId}")
    public ResponseEntity<?> getActiveAssignments(@PathVariable UUID actorId) {
        var assignments = scopeResolver.getActiveAssignments(actorId);
        return ResponseEntity.ok(Map.of("assignments", assignments));
    }

    public record CreateAssignmentRequest(
            @JsonProperty("creator_actor_id") UUID creatorActorId,
            @JsonProperty("target_actor_id") UUID targetActorId,
            String role,
            @JsonProperty("geographic_scope") UUID geographicScope,
            @JsonProperty("subject_list") List<UUID> subjectList,
            @JsonProperty("activity_list") List<String> activityList,
            @JsonProperty("valid_from") String validFrom,
            @JsonProperty("valid_to") String validTo
    ) {}

    public record EndAssignmentRequest(
            @JsonProperty("actor_id") UUID actorId,
            String reason
    ) {}
}
