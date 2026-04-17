package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
 * Evaluates incoming events for concurrency conflicts.
 * Detection: per-event W_effective = min(event.sync_watermark, request.last_pull_watermark).
 * If subject has events from other devices with sync_watermark > W_effective → flag raised.
 * Produces conflict_detected events with deterministic UUIDs (idempotent).
 */
@Component
public class ConflictDetector {

    private static final Logger log = LoggerFactory.getLogger(ConflictDetector.class);
    private static final String FLAG_CATEGORY = "concurrent_state_change";
    private static final String STALE_REFERENCE_CATEGORY = "stale_reference";

    private final EventRepository eventRepository;
    private final ServerIdentity serverIdentity;
    private final AliasCache aliasCache;
    private final ObjectMapper objectMapper;

    public ConflictDetector(EventRepository eventRepository,
                            ServerIdentity serverIdentity,
                            AliasCache aliasCache,
                            ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.serverIdentity = serverIdentity;
        this.aliasCache = aliasCache;
        this.objectMapper = objectMapper;
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
                "Event created with knowledge horizon before concurrent events from another device");
    }

    private Event createFlagEventForSweep(UUID sourceEventId, UUID subjectId, UUID flagId) {
        return buildFlagEvent(flagId, sourceEventId, subjectId, FLAG_CATEGORY,
                "Event created with knowledge horizon before concurrent events from another device");
    }

    private Event createStaleReferenceFlag(Event sourceEvent, UUID subjectId) {
        UUID flagId = deterministicUuid(sourceEvent.id(), STALE_REFERENCE_CATEGORY);
        UUID canonicalId = aliasCache.resolve(subjectId);
        return buildFlagEvent(flagId, sourceEvent.id(), subjectId, STALE_REFERENCE_CATEGORY,
                "Event references retired subject " + subjectId + ", canonical ID: " + canonicalId);
    }

    private Event buildFlagEvent(UUID flagId, UUID sourceEventId, UUID subjectId,
                                  String flagCategory, String reason) {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", subjectId.toString());

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", "system");

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("source_event_id", sourceEventId.toString());
        payload.put("flag_category", flagCategory);
        payload.put("resolvability", "manual_only");
        ObjectNode resolver = objectMapper.createObjectNode();
        resolver.put("type", "actor");
        resolver.put("id", "system");
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
