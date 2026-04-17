package dev.datarun.server.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.datarun.server.event.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Identity endpoints: merge and split subjects.
 * Online-only, server-validated (F9).
 */
@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final IdentityService identityService;

    public IdentityController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @PostMapping("/merge")
    public ResponseEntity<?> merge(@RequestBody MergeRequest request) {
        if (request.retiredId() == null || request.survivingId() == null || request.actorId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "missing_required_fields",
                            "message", "retired_id, surviving_id, and actor_id are required"));
        }

        try {
            Event event = identityService.merge(
                    request.retiredId(), request.survivingId(), request.actorId(), request.reason());
            return ResponseEntity.ok(Map.of(
                    "event_id", event.id().toString(),
                    "retired_id", request.retiredId().toString(),
                    "surviving_id", request.survivingId().toString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "precondition_failed", "message", e.getMessage()));
        }
    }

    @PostMapping("/split")
    public ResponseEntity<?> split(@RequestBody SplitRequest request) {
        if (request.sourceId() == null || request.actorId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "missing_required_fields",
                            "message", "source_id and actor_id are required"));
        }

        try {
            Event event = identityService.split(
                    request.sourceId(), request.actorId(), request.reason());
            String successorId = event.payload().get("successor_id").asText();
            return ResponseEntity.ok(Map.of(
                    "event_id", event.id().toString(),
                    "source_id", request.sourceId().toString(),
                    "successor_id", successorId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "precondition_failed", "message", e.getMessage()));
        }
    }

    public record MergeRequest(
            @JsonProperty("retired_id") UUID retiredId,
            @JsonProperty("surviving_id") UUID survivingId,
            @JsonProperty("actor_id") UUID actorId,
            String reason
    ) {}

    public record SplitRequest(
            @JsonProperty("source_id") UUID sourceId,
            @JsonProperty("actor_id") UUID actorId,
            String reason
    ) {}
}
