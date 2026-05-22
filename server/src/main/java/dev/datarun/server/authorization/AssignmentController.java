package dev.datarun.server.authorization;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.datarun.server.event.Event;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> createAssignment(@RequestBody CreateAssignmentRequest request,
                                              HttpServletRequest httpRequest) {
        UUID authenticatedActorId = authenticatedActorId(httpRequest);
        if (authenticatedActorId == null) {
            return unauthorized();
        }
        if (request.targetActorId() == null || request.role() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "missing required fields: target_actor_id, role"));
        }

        try {
            OffsetDateTime validFrom = request.validFrom() != null
                    ? OffsetDateTime.parse(request.validFrom())
                    : OffsetDateTime.now();
            OffsetDateTime validTo = request.validTo() != null
                    ? OffsetDateTime.parse(request.validTo())
                    : null;

            Event event = assignmentService.createAssignment(
                    authenticatedActorId,
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
                                           @RequestBody(required = false) EndAssignmentRequest request,
                                           HttpServletRequest httpRequest) {
        UUID authenticatedActorId = authenticatedActorId(httpRequest);
        if (authenticatedActorId == null) {
            return unauthorized();
        }

        try {
            String reason = request != null ? request.reason() : null;
            Event event = assignmentService.endAssignment(id, authenticatedActorId, reason);
            return ResponseEntity.ok(Map.of("event_id", event.id().toString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/actor/{actorId}")
    public ResponseEntity<?> getActiveAssignments(@PathVariable UUID actorId,
                                                  HttpServletRequest httpRequest) {
        UUID authenticatedActorId = authenticatedActorId(httpRequest);
        if (authenticatedActorId == null) {
            return unauthorized();
        }
        if (!authenticatedActorId.equals(actorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "actor_mismatch"));
        }
        var assignments = scopeResolver.getActiveAssignments(actorId);
        return ResponseEntity.ok(Map.of("assignments", assignments));
    }

    private UUID authenticatedActorId(HttpServletRequest request) {
        Object actorId = request.getAttribute(ActorTokenInterceptor.ACTOR_ID_ATTR);
        if (actorId instanceof UUID uuid) {
            return uuid;
        }
        return null;
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "missing_authenticated_actor"));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateAssignmentRequest(
            @JsonProperty("target_actor_id") UUID targetActorId,
            String role,
            @JsonAlias("geographic_id")
            @JsonProperty("geographic_scope") UUID geographicScope,
            @JsonProperty("subject_list") List<UUID> subjectList,
            @JsonProperty("activity_list") List<String> activityList,
            @JsonProperty("valid_from") String validFrom,
            @JsonProperty("valid_to") String validTo
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EndAssignmentRequest(
            String reason
    ) {}
}
