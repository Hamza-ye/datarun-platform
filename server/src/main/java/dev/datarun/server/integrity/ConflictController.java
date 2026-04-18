package dev.datarun.server.integrity;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.datarun.server.event.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Conflict management endpoints: list flags, resolve flags, create manual identity_conflict.
 */
@RestController
@RequestMapping("/api/conflicts")
public class ConflictController {

    private final ConflictResolutionService resolutionService;

    public ConflictController(ConflictResolutionService resolutionService) {
        this.resolutionService = resolutionService;
    }

    /**
     * List unresolved conflict flags, grouped by subject + category.
     */
    @GetMapping
    public ResponseEntity<?> listFlags() {
        List<ConflictResolutionService.FlagSummary> flags = resolutionService.listUnresolvedFlags();
        return ResponseEntity.ok(Map.of("flags", flags));
    }

    /**
     * Resolve a conflict flag.
     * POST /api/conflicts/{flag_id}/resolve
     */
    @PostMapping("/{flagId}/resolve")
    public ResponseEntity<?> resolve(@PathVariable UUID flagId,
                                      @RequestBody ResolveRequest request) {
        if (request.resolution() == null || request.actorId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "missing_required_fields",
                            "message", "resolution and actor_id are required"));
        }

        try {
            Event event = resolutionService.resolve(
                    flagId, request.resolution(), request.reclassifiedSubjectId(),
                    request.actorId(), request.reason());
            return ResponseEntity.ok(Map.of(
                    "event_id", event.id().toString(),
                    "flag_event_id", flagId.toString(),
                    "resolution", request.resolution()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "precondition_failed", "message", e.getMessage()));
        }
    }

    /**
     * Create a manual identity_conflict flag (DD-5: manual-only in Phase 1).
     * POST /api/conflicts/identity
     */
    @PostMapping("/identity")
    public ResponseEntity<?> createIdentityConflict(@RequestBody IdentityConflictRequest request) {
        if (request.sourceEventId() == null || request.actorId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "missing_required_fields",
                            "message", "source_event_id and actor_id are required"));
        }

        try {
            Event event = resolutionService.createManualIdentityConflict(
                    request.sourceEventId(), request.actorId(), request.reason());
            return ResponseEntity.ok(Map.of(
                    "event_id", event.id().toString(),
                    "source_event_id", request.sourceEventId().toString(),
                    "flag_category", "identity_conflict"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "precondition_failed", "message", e.getMessage()));
        }
    }

    public record ResolveRequest(
            String resolution,
            @JsonProperty("actor_id") UUID actorId,
            @JsonProperty("reclassified_subject_id") UUID reclassifiedSubjectId,
            String reason
    ) {}

    public record IdentityConflictRequest(
            @JsonProperty("source_event_id") UUID sourceEventId,
            @JsonProperty("actor_id") UUID actorId,
            String reason
    ) {}
}
