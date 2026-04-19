package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.authorization.ActiveAssignment;
import dev.datarun.server.authorization.ScopeResolver;
import dev.datarun.server.authorization.SubjectLocationRepository;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.AliasCache;
import dev.datarun.server.identity.ServerIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Evaluates incoming events for concurrency conflicts and authorization violations.
 * 
 * Identity detection (Phase 1):
 *   - concurrent_state_change: W_effective comparison across devices
 *   - stale_reference: event references a retired subject ID
 * 
 * Authorization detection (Phase 2c):
 *   - temporal_authority_expired: assignment ended before event pushed (auto_eligible)
 *   - scope_violation: subject outside actor's active scope (manual_only)
 *   - role_stale: actor's role changed between event creation and push (manual_only)
 * 
 * Detection ordering: temporal_authority_expired → scope_violation → role_stale
 * (prevents mis-classifying expired actors as scope violators)
 */
@Component
public class ConflictDetector {

    private static final Logger log = LoggerFactory.getLogger(ConflictDetector.class);
    private static final String FLAG_CATEGORY = "concurrent_state_change";
    private static final String STALE_REFERENCE_CATEGORY = "stale_reference";
    private static final String TEMPORAL_AUTHORITY_EXPIRED = "temporal_authority_expired";
    private static final String SCOPE_VIOLATION = "scope_violation";
    private static final String ROLE_STALE = "role_stale";

    private final EventRepository eventRepository;
    private final ServerIdentity serverIdentity;
    private final AliasCache aliasCache;
    private final ObjectMapper objectMapper;
    private final ScopeResolver scopeResolver;
    private final SubjectLocationRepository subjectLocationRepository;

    public ConflictDetector(EventRepository eventRepository,
                            ServerIdentity serverIdentity,
                            AliasCache aliasCache,
                            ObjectMapper objectMapper,
                            ScopeResolver scopeResolver,
                            SubjectLocationRepository subjectLocationRepository) {
        this.eventRepository = eventRepository;
        this.serverIdentity = serverIdentity;
        this.aliasCache = aliasCache;
        this.objectMapper = objectMapper;
        this.scopeResolver = scopeResolver;
        this.subjectLocationRepository = subjectLocationRepository;
    }

    /**
     * Evaluate a batch of newly persisted events for concurrency conflicts.
     * Returns list of conflict_detected events to persist.
     *
     * @param acceptedEvents events that were just persisted (with sync_watermarks now assigned)
     * @param lastPullWatermark the pushing device's reported last pull watermark
     */
    public List<Event> evaluate(List<Event> acceptedEvents, long lastPullWatermark) {
        List<Event> flagEvents = new ArrayList<>();

        for (Event event : acceptedEvents) {
            // Only evaluate domain events (not system events)
            if (isSystemEventType(event.type())) {
                continue;
            }

            UUID subjectId = extractSubjectId(event);
            if (subjectId == null) {
                continue;
            }

            // stale_reference check: event references a retired subject ID
            if (aliasCache.isRetired(subjectId)) {
                Event staleFlag = createStaleReferenceFlag(event, subjectId);
                flagEvents.add(staleFlag);
                log.info("Stale reference detected for subject {} (event {}), canonical: {}",
                        subjectId, event.id(), aliasCache.resolve(subjectId));
            }

            // Retrieve the server-assigned watermark for this event
            Long eventWatermark = eventRepository.getSyncWatermark(event.id());
            if (eventWatermark == null) {
                continue;
            }

            // W_effective = min(event.sync_watermark, request.last_pull_watermark)
            long wEffective = Math.min(eventWatermark, lastPullWatermark);

            // Check: does subject have events from OTHER devices with sync_watermark > W_effective?
            if (eventRepository.hasNewerEventsFromOtherDevices(subjectId, event.deviceId(), wEffective)) {
                Event flagEvent = createFlagEvent(event, subjectId);
                flagEvents.add(flagEvent);
                log.info("Concurrent state change detected for subject {} (event {})",
                        subjectId, event.id());
            }
        }

        return flagEvents;
    }

    /**
     * Evaluate a batch of newly persisted events for authorization violations.
     * Runs AFTER identity detection in the push pipeline.
     *
     * Detection ordering (phase-2.md §8): temporal_authority_expired → scope_violation → role_stale.
     * If an assignment expired and the actor pushed afterward, the flag is temporal_authority_expired
     * (auto_eligible), not scope_violation (manual_only).
     *
     * @param acceptedEvents events that were just persisted
     * @param actorId the pushing actor
     */
    public List<Event> evaluateAuth(List<Event> acceptedEvents, UUID actorId) {
        List<Event> flagEvents = new ArrayList<>();
        if (actorId == null) return flagEvents;

        // Reconstruct ALL assignments (including ended) for temporal detection
        List<ActiveAssignment> allAssignments = scopeResolver.getAllAssignments(actorId);

        // If actor has no assignment history at all, skip auth CD entirely.
        // This handles pre-assignment actors and test scenarios without assignments.
        if (allAssignments.isEmpty()) return flagEvents;

        // Reconstruct actor's active assignments at push time
        List<ActiveAssignment> activeAssignments = scopeResolver.getActiveAssignments(actorId);

        // Determine actor's current role(s) from active assignments
        List<String> currentRoles = activeAssignments.stream()
                .map(ActiveAssignment::role)
                .distinct()
                .toList();

        for (Event event : acceptedEvents) {
            if (isSystemEventType(event.type()) || isAssignmentEventType(event.type())) {
                continue;
            }

            UUID subjectId = extractSubjectId(event);
            if (subjectId == null) continue;

            // --- 1. temporal_authority_expired ---
            // Check if any assignment covering this subject ended AFTER the event was created
            // (actor didn't know about the assignment ending)
            boolean temporalFlagged = false;
            for (ActiveAssignment ended : allAssignments) {
                if (!ended.ended()) continue;
                // Get the ended assignment's watermark
                Long endedWatermark = getAssignmentEndedWatermark(ended.assignmentId());
                if (endedWatermark == null) continue;

                // Event's sync_watermark represents when the server saw it;
                // we need to check if the assignment ended between event creation and push.
                // If the ended watermark is > the event's envelope sync_watermark
                // (from the pushing device's perspective), actor didn't know.
                Long eventWatermark = eventRepository.getSyncWatermark(event.id());
                if (eventWatermark == null) continue;

                // Assignment ended, and the subject was in that assignment's scope
                String subjectPath = subjectLocationRepository.findPathBySubjectId(subjectId);
                if (ended.containsGeographically(subjectPath)
                        && ended.containsSubject(subjectId)
                        && ended.containsActivity(event.activityRef())) {
                    // Assignment covered this event's subject and has since ended
                    Event flag = buildFlagEvent(
                            deterministicUuid(event.id(), TEMPORAL_AUTHORITY_EXPIRED),
                            event.id(), subjectId, TEMPORAL_AUTHORITY_EXPIRED,
                            "auto_eligible",
                            findBroadestScopeActor(subjectId),
                            "Assignment " + ended.assignmentId() + " expired; actor was unaware");
                    flagEvents.add(flag);
                    temporalFlagged = true;
                    log.info("Temporal authority expired for subject {} (event {}, assignment {})",
                            subjectId, event.id(), ended.assignmentId());
                    break; // One flag per event per category
                }
            }

            // --- 2. scope_violation (skip if temporal_authority_expired already flagged) ---
            if (!temporalFlagged && !activeAssignments.isEmpty()) {
                String subjectPath = subjectLocationRepository.findPathBySubjectId(subjectId);
                boolean inScope = scopeResolver.isInScope(
                        activeAssignments, subjectPath, subjectId, event.activityRef());
                if (!inScope) {
                    Event flag = buildFlagEvent(
                            deterministicUuid(event.id(), SCOPE_VIOLATION),
                            event.id(), subjectId, SCOPE_VIOLATION,
                            "manual_only",
                            findBroadestScopeActor(subjectId),
                            "Subject outside actor's active scope at push time");
                    flagEvents.add(flag);
                    log.info("Scope violation detected for subject {} (event {})",
                            subjectId, event.id());
                }
            }
            // If actor has zero active assignments → flag all events
            if (!temporalFlagged && activeAssignments.isEmpty()) {
                Event flag = buildFlagEvent(
                        deterministicUuid(event.id(), SCOPE_VIOLATION),
                        event.id(), subjectId, SCOPE_VIOLATION,
                        "manual_only",
                        findBroadestScopeActor(subjectId),
                        "Actor has no active assignments at push time");
                flagEvents.add(flag);
                log.info("Scope violation (no assignments) for subject {} (event {})",
                        subjectId, event.id());
            }

            // --- 3. role_stale ---
            // Check if actor's role at event creation differs from current role.
            // Phase 2: flag ALL role changes (Phase 3 refines to capability-restricted only).
            String roleAtCreation = findRoleAtWatermark(actorId, event);
            if (roleAtCreation != null && !currentRoles.isEmpty()
                    && !currentRoles.contains(roleAtCreation)) {
                Event flag = buildFlagEvent(
                        deterministicUuid(event.id(), ROLE_STALE),
                        event.id(), subjectId, ROLE_STALE,
                        "manual_only",
                        findSupervisorActor(actorId),
                        "Actor role was '" + roleAtCreation + "' at creation, now: " + currentRoles);
                flagEvents.add(flag);
                log.info("Role stale detected for subject {} (event {}): was={}, now={}",
                        subjectId, event.id(), roleAtCreation, currentRoles);
            }
        }

        return flagEvents;
    }

    /**
     * Sweep: re-evaluate subjects with multi-device events in a trailing window.
     * Stateless — deterministic IDs prevent duplicate flags.
     * Returns list of conflict_detected events to persist.
     */
    public List<Event> sweep(long trailingWindowWatermark) {
        // Find subjects that have events from multiple devices in the trailing window
        List<SweepCandidate> candidates = findSweepCandidates(trailingWindowWatermark);
        List<Event> flagEvents = new ArrayList<>();

        for (SweepCandidate candidate : candidates) {
            // For each event in the window from this device, check if concurrent
            // with events from other devices
            if (eventRepository.hasNewerEventsFromOtherDevices(
                    candidate.subjectId, candidate.deviceId, candidate.eventWatermark - 1)) {
                // Check if flag already exists (deterministic ID)
                UUID flagId = deterministicUuid(candidate.eventId, FLAG_CATEGORY);
                if (!eventRepository.existsById(flagId)) {
                    Event flagEvent = createFlagEventForSweep(
                            candidate.eventId, candidate.subjectId, flagId);
                    flagEvents.add(flagEvent);
                    log.info("Sweep: concurrent state change detected for subject {} (event {})",
                            candidate.subjectId, candidate.eventId);
                }
            }
        }

        return flagEvents;
    }

    private List<SweepCandidate> findSweepCandidates(long trailingWindowWatermark) {
        // Find events in the trailing window where their subject has events from multiple devices
        return eventRepository.getJdbcTemplate().query("""
                SELECT e.id, e.device_id, e.sync_watermark,
                       e.subject_ref->>'id' AS subject_id
                FROM events e
                WHERE e.sync_watermark > ?
                  AND e.type NOT IN ('conflict_detected', 'conflict_resolved', 'subjects_merged', 'subject_split')
                  AND EXISTS (
                      SELECT 1 FROM events e2
                      WHERE e2.subject_ref->>'id' = e.subject_ref->>'id'
                        AND e2.device_id != e.device_id
                        AND e2.type NOT IN ('conflict_detected', 'conflict_resolved', 'subjects_merged', 'subject_split')
                  )
                ORDER BY e.sync_watermark ASC
                """,
                (rs, rowNum) -> new SweepCandidate(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("device_id")),
                        rs.getLong("sync_watermark"),
                        UUID.fromString(rs.getString("subject_id"))
                ),
                trailingWindowWatermark);
    }

    private Event createFlagEvent(Event sourceEvent, UUID subjectId) {
        UUID flagId = deterministicUuid(sourceEvent.id(), FLAG_CATEGORY);
        return buildFlagEvent(flagId, sourceEvent.id(), subjectId, FLAG_CATEGORY,
                "manual_only", null,
                "Event created with knowledge horizon before concurrent events from another device");
    }

    private Event createFlagEventForSweep(UUID sourceEventId, UUID subjectId, UUID flagId) {
        return buildFlagEvent(flagId, sourceEventId, subjectId, FLAG_CATEGORY,
                "manual_only", null,
                "Event created with knowledge horizon before concurrent events from another device");
    }

    private Event createStaleReferenceFlag(Event sourceEvent, UUID subjectId) {
        UUID flagId = deterministicUuid(sourceEvent.id(), STALE_REFERENCE_CATEGORY);
        UUID canonicalId = aliasCache.resolve(subjectId);
        return buildFlagEvent(flagId, sourceEvent.id(), subjectId, STALE_REFERENCE_CATEGORY,
                "auto_eligible", null,
                "Event references retired subject " + subjectId + ", canonical ID: " + canonicalId);
    }

    private Event buildFlagEvent(UUID flagId, UUID sourceEventId, UUID subjectId,
                                  String flagCategory, String resolvability,
                                  UUID designatedResolver, String reason) {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", subjectId.toString());

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", "system");

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("source_event_id", sourceEventId.toString());
        payload.put("flag_category", flagCategory);
        payload.put("resolvability", resolvability);
        ObjectNode resolver = objectMapper.createObjectNode();
        resolver.put("type", "actor");
        resolver.put("id", designatedResolver != null ? designatedResolver.toString() : "system");
        payload.set("designated_resolver", resolver);
        payload.put("reason", reason);

        return new Event(
                flagId,
                "conflict_detected",
                "system/integrity/v1",
                null,
                subjectRef,
                actorRef,
                serverIdentity.getDeviceId(),
                (int) serverIdentity.nextDeviceSeq(),
                null, // sync_watermark assigned on insert
                OffsetDateTime.now(ZoneOffset.UTC),
                payload
        );
    }

    // --- Auth detection helpers ---

    /**
     * Find the watermark of the assignment_ended event for a given assignment.
     */
    private Long getAssignmentEndedWatermark(UUID assignmentId) {
        List<Long> watermarks = eventRepository.getJdbcTemplate().queryForList(
                "SELECT sync_watermark FROM events WHERE type = 'assignment_changed' " +
                "AND shape_ref = 'assignment_ended/v1' AND subject_ref->>'id' = ?",
                Long.class, assignmentId.toString());
        return watermarks.isEmpty() ? null : watermarks.get(0);
    }

    /**
     * Find the role the actor had at the time of event creation.
     * Looks at the assignment timeline relative to the event's sync_watermark.
     */
    private String findRoleAtWatermark(UUID actorId, Event event) {
        Long eventWatermark = eventRepository.getSyncWatermark(event.id());
        if (eventWatermark == null) return null;

        // Find the most recent assignment_created for this actor before or at the event's watermark
        List<String> roles = eventRepository.getJdbcTemplate().queryForList("""
                SELECT e.payload->>'role'
                FROM events e
                WHERE e.type = 'assignment_changed'
                  AND e.shape_ref = 'assignment_created/v1'
                  AND e.payload->'target_actor'->>'id' = ?
                  AND e.sync_watermark <= ?
                ORDER BY e.sync_watermark DESC
                LIMIT 1
                """, String.class, actorId.toString(), eventWatermark);
        return roles.isEmpty() ? null : roles.get(0);
    }

    /**
     * Find the broadest-scope actor whose scope contains the given subject.
     * Designated resolver for scope_violation and temporal_authority_expired flags.
     * Falls back to system if no suitable actor found.
     */
    private UUID findBroadestScopeActor(UUID subjectId) {
        // For Phase 2c: use the coordinator who created the subject's location assignment.
        // Simple heuristic: find actors with assignments containing this subject's location,
        // ordered by path length (shortest path = broadest scope).
        String subjectPath = subjectLocationRepository.findPathBySubjectId(subjectId);
        if (subjectPath == null) return null;

        List<String> actors = eventRepository.getJdbcTemplate().queryForList("""
                SELECT actor_id FROM (
                    SELECT DISTINCT e.payload->'target_actor'->>'id' AS actor_id, LENGTH(l.path) AS path_len
                    FROM events e
                    JOIN locations l ON l.id::text = e.payload->'scope'->>'geographic'
                    WHERE e.type = 'assignment_changed'
                      AND e.shape_ref = 'assignment_created/v1'
                      AND ? LIKE l.path || '%'
                      AND NOT EXISTS (
                          SELECT 1 FROM events e2
                          WHERE e2.type = 'assignment_changed'
                            AND e2.shape_ref = 'assignment_ended/v1'
                            AND e2.subject_ref->>'id' = e.subject_ref->>'id'
                      )
                ) sub ORDER BY path_len ASC LIMIT 1
                """, String.class, subjectPath);
        return actors.isEmpty() ? null : UUID.fromString(actors.get(0));
    }

    /**
     * Find the supervisor actor for a given actor (broadest scope that contains the actor's scope).
     * Falls back to null (system resolver) if no supervisor found.
     */
    private UUID findSupervisorActor(UUID actorId) {
        // Simple approach: find actors with broader geographic scope containing this actor's area
        List<ActiveAssignment> actorAssignments = scopeResolver.getActiveAssignments(actorId);
        if (actorAssignments.isEmpty()) return null;

        String actorPath = actorAssignments.stream()
                .map(ActiveAssignment::geographicPath)
                .filter(p -> p != null)
                .findFirst()
                .orElse(null);
        if (actorPath == null) return null;

        List<String> supervisors = eventRepository.getJdbcTemplate().queryForList("""
                SELECT actor_id FROM (
                    SELECT DISTINCT e.payload->'target_actor'->>'id' AS actor_id, LENGTH(l.path) AS path_len
                    FROM events e
                    JOIN locations l ON l.id::text = e.payload->'scope'->>'geographic'
                    WHERE e.type = 'assignment_changed'
                      AND e.shape_ref = 'assignment_created/v1'
                      AND ? LIKE l.path || '%'
                      AND LENGTH(l.path) < LENGTH(?)
                      AND e.payload->'target_actor'->>'id' != ?
                      AND NOT EXISTS (
                          SELECT 1 FROM events e2
                          WHERE e2.type = 'assignment_changed'
                            AND e2.shape_ref = 'assignment_ended/v1'
                            AND e2.subject_ref->>'id' = e.subject_ref->>'id'
                      )
                ) sub ORDER BY path_len ASC LIMIT 1
                """, String.class, actorPath, actorPath, actorId.toString());
        return supervisors.isEmpty() ? null : UUID.fromString(supervisors.get(0));
    }

    private boolean isAssignmentEventType(String type) {
        return "assignment_changed".equals(type);
    }

    /**
     * Deterministic UUID from (source_event_id + flag_category).
     * Enables idempotent sweep — same input always produces same flag ID.
     */
    static UUID deterministicUuid(UUID sourceEventId, String flagCategory) {
        String input = sourceEventId.toString() + ":" + flagCategory;
        return UUID.nameUUIDFromBytes(input.getBytes(StandardCharsets.UTF_8));
    }

    private UUID extractSubjectId(Event event) {
        JsonNode subjectRef = event.subjectRef();
        if (subjectRef != null && subjectRef.has("id")) {
            try {
                return UUID.fromString(subjectRef.get("id").asText());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    private boolean isSystemEventType(String type) {
        return "conflict_detected".equals(type) ||
               "conflict_resolved".equals(type) ||
               "subjects_merged".equals(type) ||
               "subject_split".equals(type);
    }

    record SweepCandidate(UUID eventId, UUID deviceId, long eventWatermark, UUID subjectId) {}
}
