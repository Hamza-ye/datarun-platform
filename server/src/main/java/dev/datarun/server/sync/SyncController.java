package dev.datarun.server.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.authorization.ActiveAssignment;
import dev.datarun.server.authorization.ActorTokenInterceptor;
import dev.datarun.server.authorization.ScopeResolver;
import dev.datarun.server.event.EnvelopeValidator;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.integrity.ConflictDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private static final Logger log = LoggerFactory.getLogger(SyncController.class);

    private final EventRepository eventRepository;
    private final EnvelopeValidator envelopeValidator;
    private final ObjectMapper objectMapper;
    private final ConflictDetector conflictDetector;
    private final TransactionTemplate transactionTemplate;
    private final JdbcTemplate jdbc;
    private final ScopeResolver scopeResolver;

    public SyncController(EventRepository eventRepository,
                          EnvelopeValidator envelopeValidator,
                          ObjectMapper objectMapper,
                          ConflictDetector conflictDetector,
                          TransactionTemplate transactionTemplate,
                          JdbcTemplate jdbc,
                          ScopeResolver scopeResolver) {
        this.eventRepository = eventRepository;
        this.envelopeValidator = envelopeValidator;
        this.objectMapper = objectMapper;
        this.conflictDetector = conflictDetector;
        this.transactionTemplate = transactionTemplate;
        this.jdbc = jdbc;
        this.scopeResolver = scopeResolver;
    }

    @PostMapping("/push")
    public ResponseEntity<?> push(@RequestBody PushRequest request) {
        if (request.events() == null || request.events().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "empty_batch"));
        }

        // Validate all events against envelope schema first
        List<Map<String, Object>> validationErrors = new ArrayList<>();
        for (int i = 0; i < request.events().size(); i++) {
            JsonNode eventNode = objectMapper.valueToTree(request.events().get(i));
            List<String> errors = envelopeValidator.validate(eventNode);
            if (!errors.isEmpty()) {
                validationErrors.add(Map.of(
                        "index", i,
                        "errors", errors
                ));
            }
        }

        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "validation_failed", "details", validationErrors));
        }

        // --- Tx1: Persist events ---
        List<Event> acceptedEvents = new ArrayList<>();
        int[] counts = {0, 0}; // [accepted, duplicates]
        transactionTemplate.executeWithoutResult(status -> {
            for (Event event : request.events()) {
                if (eventRepository.insert(event)) {
                    acceptedEvents.add(event);
                    counts[0]++;
                } else {
                    counts[1]++;
                }
            }
        });

        // --- Tx2: Conflict detection (separate transaction) ---
        // CD failure does not affect event persistence (C3 satisfied)
        int flagsRaised = 0;
        if (!acceptedEvents.isEmpty()) {
            long lastPullWatermark = request.lastPullWatermark() != null
                    ? request.lastPullWatermark() : 0L;
            try {
                List<Event> flagEvents = conflictDetector.evaluate(acceptedEvents, lastPullWatermark);
                if (!flagEvents.isEmpty()) {
                    flagsRaised = persistFlagEvents(flagEvents);
                }
            } catch (Exception e) {
                log.warn("Conflict detection failed (events already persisted, flags missing): {}",
                        e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
                "accepted", counts[0],
                "duplicates", counts[1],
                "flags_raised", flagsRaised));
    }

    @PostMapping("/pull")
    public ResponseEntity<?> pull(@RequestBody PullRequest request,
                                  HttpServletRequest httpRequest) {
        if (request.sinceWatermark() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "invalid_watermark"));
        }

        int limit = request.limit() != null ? request.limit() : 100;
        if (limit < 1) limit = 1;
        if (limit > 1000) limit = 1000;

        // Resolve actor from token (set by ActorTokenInterceptor)
        UUID actorId = (UUID) httpRequest.getAttribute(ActorTokenInterceptor.ACTOR_ID_ATTR);

        // Compute actor's scope from active assignments
        List<ActiveAssignment> assignments = scopeResolver.getActiveAssignments(actorId);

        // If any assignment has unrestricted geographic scope (null), actor sees all events
        boolean hasUnrestrictedGeo = assignments.stream()
                .anyMatch(a -> a.geographicPath() == null);

        List<String> scopePaths = assignments.stream()
                .map(ActiveAssignment::geographicPath)
                .filter(p -> p != null)
                .toList();

        List<Event> events;
        if (assignments.isEmpty()) {
            // No active assignments → empty result (no events authorized)
            events = List.of();
        } else if (hasUnrestrictedGeo) {
            // Unrestricted geographic scope → return all events (same as pre-Phase 2)
            events = eventRepository.findSince(request.sinceWatermark(), limit);
        } else {
            events = eventRepository.findSinceScoped(
                    request.sinceWatermark(), limit, actorId, scopePaths);
        }

        long latestWatermark = events.isEmpty()
                ? request.sinceWatermark()
                : events.get(events.size() - 1).syncWatermark();
        boolean hasMore = events.size() == limit;

        // Update device_sync_state on each pull (bookkeeping)
        if (request.deviceId() != null) {
            updateDeviceSyncState(request.deviceId(), latestWatermark);
        }

        return ResponseEntity.ok(Map.of(
                "events", events,
                "latest_watermark", latestWatermark,
                "has_more", hasMore
        ));
    }

    private int persistFlagEvents(List<Event> flagEvents) {
        Integer result = transactionTemplate.execute(status -> {
            int persisted = 0;
            for (Event flag : flagEvents) {
                if (eventRepository.insert(flag)) {
                    persisted++;
                }
                // Duplicate flag (deterministic ID) → ON CONFLICT DO NOTHING equivalent
            }
            return persisted;
        });
        return result != null ? result : 0;
    }

    private void updateDeviceSyncState(UUID deviceId, long latestWatermark) {
        try {
            jdbc.update("""
                INSERT INTO device_sync_state (device_id, last_pull_watermark, last_pull_at)
                VALUES (?::uuid, ?, NOW())
                ON CONFLICT (device_id) DO UPDATE
                SET last_pull_watermark = GREATEST(device_sync_state.last_pull_watermark, EXCLUDED.last_pull_watermark),
                    last_pull_at = NOW()
                """,
                    deviceId.toString(), latestWatermark);
        } catch (Exception e) {
            log.warn("Failed to update device_sync_state for {}: {}", deviceId, e.getMessage());
        }
    }

    public record PushRequest(
            List<Event> events,
            @JsonProperty("device_id") UUID deviceId,
            @JsonProperty("last_pull_watermark") Long lastPullWatermark
    ) {}

    public record PullRequest(
            @JsonProperty("since_watermark") Long sinceWatermark,
            Integer limit,
            @JsonProperty("device_id") UUID deviceId
    ) {}
}
