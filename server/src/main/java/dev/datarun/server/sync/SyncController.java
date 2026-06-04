package dev.datarun.server.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.authorization.ActiveAssignment;
import dev.datarun.server.authorization.ActorTokenInterceptor;
import dev.datarun.server.authorization.ScopeResolver;
import dev.datarun.server.config.ConfigPackager;
import dev.datarun.server.config.ShapePayloadValidator;
import dev.datarun.server.event.EnvelopeValidator;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.integrity.ConflictDetector;
import dev.datarun.server.integrity.DomainUniquenessDetector;
import dev.datarun.server.integrity.TransitionViolationDetector;
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
    private final ConfigPackager configPackager;
    private final ShapePayloadValidator shapePayloadValidator;
    private final DomainUniquenessDetector domainUniquenessDetector;
    private final TransitionViolationDetector transitionViolationDetector;
    private final SubjectHistoryBackfillService subjectHistoryBackfillService;

    public SyncController(EventRepository eventRepository,
                          EnvelopeValidator envelopeValidator,
                          ObjectMapper objectMapper,
                          ConflictDetector conflictDetector,
                          DomainUniquenessDetector domainUniquenessDetector,
                          TransitionViolationDetector transitionViolationDetector,
                          TransactionTemplate transactionTemplate,
                          JdbcTemplate jdbc,
                          ScopeResolver scopeResolver,
                          ConfigPackager configPackager,
                          ShapePayloadValidator shapePayloadValidator,
                          SubjectHistoryBackfillService subjectHistoryBackfillService) {
        this.eventRepository = eventRepository;
        this.envelopeValidator = envelopeValidator;
        this.objectMapper = objectMapper;
        this.conflictDetector = conflictDetector;
        this.domainUniquenessDetector = domainUniquenessDetector;
        this.transitionViolationDetector = transitionViolationDetector;
        this.transactionTemplate = transactionTemplate;
        this.jdbc = jdbc;
        this.scopeResolver = scopeResolver;
        this.configPackager = configPackager;
        this.shapePayloadValidator = shapePayloadValidator;
        this.subjectHistoryBackfillService = subjectHistoryBackfillService;
    }

    @PostMapping("/push")
    public ResponseEntity<?> push(@RequestBody PushRequest request,
                                  HttpServletRequest httpRequest) {
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

        UUID authenticatedActorId =
                (UUID) httpRequest.getAttribute(ActorTokenInterceptor.ACTOR_ID_ATTR);
        if (authenticatedActorId != null) {
            List<Map<String, Object>> actorErrors =
                    validateAuthenticatedActorBinding(request.events(), authenticatedActorId);
            if (!actorErrors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "actor_binding_failed", "details", actorErrors));
            }
        }

        // Validate payloads against shape definitions (Phase 3a)
        // Structural invalidity → 400 (not accept-and-flag)
        List<Map<String, Object>> shapeErrors = new ArrayList<>();
        for (int i = 0; i < request.events().size(); i++) {
            Event event = request.events().get(i);
            List<String> errors = shapePayloadValidator.validate(
                    event.shapeRef(), event.payload());
            if (!errors.isEmpty()) {
                shapeErrors.add(Map.of("index", i, "errors", errors));
            }
        }
        if (!shapeErrors.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "shape_validation_failed", "details", shapeErrors));
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
                // Identity detection: concurrent_state_change, stale_reference
                List<Event> flagEvents = conflictDetector.evaluate(acceptedEvents, lastPullWatermark);
                if (!flagEvents.isEmpty()) {
                    flagsRaised += persistFlagEvents(flagEvents);
                }
            } catch (Exception e) {
                log.warn("Identity conflict detection failed (events already persisted, flags missing): {}",
                        e.getMessage());
            }

            // Authorization detection: temporal_authority_expired, scope_violation, role_stale
            // Runs after identity CD in the same pipeline (phase-2.md §8)
            UUID actorId = authenticatedActorId != null
                    ? authenticatedActorId
                    : extractActorId(acceptedEvents);
            if (actorId != null) {
                try {
                    List<Event> authFlags = conflictDetector.evaluateAuth(
                            acceptedEvents, actorId, lastPullWatermark);
                    if (!authFlags.isEmpty()) {
                        flagsRaised += persistFlagEvents(authFlags);
                    }
                } catch (Exception e) {
                    log.warn("Authorization conflict detection failed (events persisted, auth flags missing): {}",
                            e.getMessage());
                }
            }

            try {
                List<Event> uniquenessFlags = domainUniquenessDetector.evaluate(acceptedEvents);
                if (!uniquenessFlags.isEmpty()) {
                    flagsRaised += persistFlagEvents(uniquenessFlags);
                }
            } catch (Exception e) {
                log.warn("Domain uniqueness detection failed (events persisted, uniqueness flags missing): {}",
                        e.getMessage());
            }

            try {
                List<Event> transitionFlags = transitionViolationDetector.evaluate(acceptedEvents);
                if (!transitionFlags.isEmpty()) {
                    flagsRaised += persistFlagEvents(transitionFlags);
                }
            } catch (Exception e) {
                log.warn("Pattern transition detection failed (events persisted, transition flags missing): {}",
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

        List<Event> events = findAuthorizedPullEvents(
                request.sinceWatermark(), limit, actorId, assignments);

        long latestWatermark = events.isEmpty()
                ? request.sinceWatermark()
                : events.get(events.size() - 1).syncWatermark();
        boolean hasMore = events.size() == limit;

        // Update device_sync_state on each pull (bookkeeping)
        if (request.deviceId() != null) {
            updateDeviceSyncState(request.deviceId(), latestWatermark,
                    request.configVersion());
        }

        // IDR-019: config_version discovery field in pull response
        int configVersion = configPackager.getLatestVersion();

        return ResponseEntity.ok(Map.of(
                "events", events,
                "latest_watermark", latestWatermark,
                "has_more", hasMore,
                "config_version", configVersion
        ));
    }

    @PostMapping("/subject-history")
    public ResponseEntity<?> subjectHistory(@RequestBody SubjectHistoryRequest request,
                                            HttpServletRequest httpRequest) {
        if (request.subjectId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "invalid_subject"));
        }
        if (request.activityRef() == null || request.activityRef().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "invalid_activity"));
        }

        long cursor = request.cursor() != null ? request.cursor() : 0L;
        if (cursor < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "invalid_cursor"));
        }

        int limit = request.limit() != null ? request.limit() : 100;
        if (limit < 1) limit = 1;
        if (limit > 1000) limit = 1000;

        UUID actorId = (UUID) httpRequest.getAttribute(ActorTokenInterceptor.ACTOR_ID_ATTR);
        try {
            SubjectHistoryBackfillService.SubjectHistoryPage page =
                    subjectHistoryBackfillService.page(
                            actorId, request.subjectId(), request.activityRef(), cursor, limit);
            return ResponseEntity.ok(Map.of(
                    "requested_subject_id", page.requestedSubjectId().toString(),
                    "subject_id", page.subjectId().toString(),
                    "activity_ref", page.activityRef(),
                    "cursor", page.cursor(),
                    "next_cursor", page.nextCursor(),
                    "has_more", page.hasMore(),
                    "events", page.events()
            ));
        } catch (SubjectHistoryBackfillService.UnauthorizedSubjectHistoryException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "subject_history_not_authorized"));
        }
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

    private List<Map<String, Object>> validateAuthenticatedActorBinding(
            List<Event> events, UUID authenticatedActorId) {
        List<Map<String, Object>> errors = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            String actorId = event.actorRef() == null
                    ? null
                    : event.actorRef().path("id").asText(null);
            if (actorId == null || actorId.isBlank()) {
                errors.add(Map.of("index", i, "error", "missing_actor_ref"));
                continue;
            }
            if (actorId.startsWith("system:")) {
                errors.add(Map.of("index", i, "error", "client_system_actor"));
                continue;
            }
            UUID eventActorId;
            try {
                eventActorId = UUID.fromString(actorId);
            } catch (IllegalArgumentException e) {
                errors.add(Map.of("index", i, "error", "invalid_actor_ref"));
                continue;
            }
            if (!authenticatedActorId.equals(eventActorId)) {
                errors.add(Map.of(
                        "index", i,
                        "error", "actor_mismatch",
                        "actor_id", actorId,
                        "authenticated_actor_id", authenticatedActorId.toString()));
            }
        }
        return errors;
    }

    private List<Event> findAuthorizedPullEvents(long sinceWatermark, int limit,
                                                 UUID actorId,
                                                 List<ActiveAssignment> assignments) {
        if (assignments.isEmpty()) {
            return List.of();
        }

        List<Event> authorized = new ArrayList<>();
        long scanWatermark = sinceWatermark;
        int candidateLimit = Math.max(limit, 100);

        while (authorized.size() < limit) {
            List<Event> candidates = findPullCandidates(
                    scanWatermark, candidateLimit, actorId, assignments);
            if (candidates.isEmpty()) {
                break;
            }

            boolean exhausted = candidates.size() < candidateLimit;
            for (Event event : candidates) {
                scanWatermark = event.syncWatermark();
                if (isAuthorizedPullEvent(event, actorId, assignments)) {
                    authorized.add(event);
                    if (authorized.size() == limit) {
                        break;
                    }
                }
            }

            if (authorized.size() == limit || exhausted) {
                break;
            }
        }

        return authorized;
    }

    private List<Event> findPullCandidates(long sinceWatermark, int limit,
                                           UUID actorId,
                                           List<ActiveAssignment> assignments) {
        boolean hasUnrestrictedGeo = assignments.stream()
                .anyMatch(a -> a.geographicPath() == null);
        if (hasUnrestrictedGeo) {
            return eventRepository.findSince(sinceWatermark, limit);
        }

        List<String> scopePaths = assignments.stream()
                .map(ActiveAssignment::geographicPath)
                .filter(p -> p != null)
                .toList();
        return eventRepository.findSinceScoped(sinceWatermark, limit, actorId, scopePaths);
    }

    private boolean isAuthorizedPullEvent(Event event, UUID actorId,
                                          List<ActiveAssignment> assignments) {
        if (assignments.stream().anyMatch(this::isUnrestrictedAssignment)) {
            return true;
        }

        if ("assignment_changed".equals(event.type())) {
            return isOwnAssignmentEvent(event, actorId);
        }

        PullAuthorizationContext context = authorizationContext(event);
        if (context.subjectId() == null) {
            return false;
        }

        return assignments.stream().anyMatch(assignment ->
                assignment.containsGeographically(context.locationPath())
                        && assignment.containsSubject(context.subjectId())
                        && (context.ignoreActivity()
                                || assignment.containsActivity(context.activityRef())));
    }

    private boolean isUnrestrictedAssignment(ActiveAssignment assignment) {
        return assignment.geographicPath() == null
                && assignment.subjectList() == null
                && assignment.activityList() == null;
    }

    private boolean isOwnAssignmentEvent(Event event, UUID actorId) {
        String targetActor = event.payload() == null
                ? null
                : event.payload().path("target_actor").path("id").asText(null);
        return actorId.toString().equals(targetActor);
    }

    private PullAuthorizationContext authorizationContext(Event event) {
        if (isConflictEvent(event)) {
            Event source = sourceEvent(event);
            if (source != null) {
                return authorizationContextFor(source, false);
            }
        }
        boolean identityLifecycle = event.shapeRef() != null
                && (event.shapeRef().startsWith("subjects_merged/")
                || event.shapeRef().startsWith("subject_split/"));
        return authorizationContextFor(event, identityLifecycle);
    }

    private PullAuthorizationContext authorizationContextFor(Event event, boolean ignoreActivity) {
        UUID subjectId = extractSubjectId(event);
        String locationPath = eventRepository.getLocationPath(event.id());
        return new PullAuthorizationContext(
                subjectId,
                locationPath,
                event.activityRef(),
                ignoreActivity);
    }

    private Event sourceEvent(Event event) {
        String sourceEventId = event.payload() == null
                ? null
                : event.payload().path("source_event_id").asText(null);
        if (sourceEventId == null || sourceEventId.isBlank()) {
            return null;
        }
        try {
            return eventRepository.findById(UUID.fromString(sourceEventId));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isConflictEvent(Event event) {
        String shapeRef = event.shapeRef();
        return shapeRef != null
                && (shapeRef.startsWith("conflict_detected/")
                || shapeRef.startsWith("conflict_resolved/"));
    }

    private UUID extractSubjectId(Event event) {
        if (event.subjectRef() == null
                || !"subject".equals(event.subjectRef().path("type").asText(null))) {
            return null;
        }
        try {
            return UUID.fromString(event.subjectRef().path("id").asText());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private record PullAuthorizationContext(
            UUID subjectId,
            String locationPath,
            String activityRef,
            boolean ignoreActivity
    ) {}

    /**
     * Extract actor_id from the first event in a batch.
     * All events in a push batch share the same actor.
     */
    private UUID extractActorId(List<Event> events) {
        for (Event e : events) {
            if (e.actorRef() != null && e.actorRef().has("id")) {
                String id = e.actorRef().get("id").asText(null);
                // System actors use the 'system:{source_type}/{source_id}' convention
                // (ADR-008 S2) and never carry a human UUID.
                if (id != null && !id.startsWith("system:")) {
                    try {
                        return UUID.fromString(id);
                    } catch (IllegalArgumentException ex) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private void updateDeviceSyncState(UUID deviceId, long latestWatermark,
                                       Integer configVersion) {
        try {
            if (configVersion != null && configVersion > 0) {
                jdbc.update("""
                    INSERT INTO device_sync_state (device_id, last_pull_watermark, last_pull_at, config_version)
                    VALUES (?::uuid, ?, NOW(), ?)
                    ON CONFLICT (device_id) DO UPDATE
                    SET last_pull_watermark = GREATEST(device_sync_state.last_pull_watermark, EXCLUDED.last_pull_watermark),
                        last_pull_at = NOW(),
                        config_version = GREATEST(device_sync_state.config_version, EXCLUDED.config_version)
                    """,
                        deviceId.toString(), latestWatermark, configVersion);
            } else {
                jdbc.update("""
                    INSERT INTO device_sync_state (device_id, last_pull_watermark, last_pull_at)
                    VALUES (?::uuid, ?, NOW())
                    ON CONFLICT (device_id) DO UPDATE
                    SET last_pull_watermark = GREATEST(device_sync_state.last_pull_watermark, EXCLUDED.last_pull_watermark),
                        last_pull_at = NOW()
                    """,
                        deviceId.toString(), latestWatermark);
            }
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
            @JsonProperty("device_id") UUID deviceId,
            @JsonProperty("config_version") Integer configVersion
    ) {}

    public record SubjectHistoryRequest(
            @JsonProperty("subject_id") UUID subjectId,
            @JsonProperty("activity_ref") String activityRef,
            Long cursor,
            Integer limit
    ) {}
}
