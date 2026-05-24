package dev.datarun.server.integrity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.config.FlagSeverityConfigService;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.ServerIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Conflict resolution: creates conflict_resolved events.
 * Also handles manual identity_conflict flag creation (DD-5).
 * Resolution outcomes: accepted (include in state), rejected (permanent exclusion),
 * reclassified (change subject attribution).
 */
@Service
public class ConflictResolutionService {

    private static final Logger log = LoggerFactory.getLogger(ConflictResolutionService.class);

    private final EventRepository eventRepository;
    private final ServerIdentity serverIdentity;
    private final FlagSeverityConfigService flagSeverityConfigService;
    private final ObjectMapper objectMapper;
    private final ResolverRoutingService resolverRoutingService;

    public ConflictResolutionService(EventRepository eventRepository,
                                     ServerIdentity serverIdentity,
                                     FlagSeverityConfigService flagSeverityConfigService,
                                     ObjectMapper objectMapper,
                                     ResolverRoutingService resolverRoutingService) {
        this.eventRepository = eventRepository;
        this.serverIdentity = serverIdentity;
        this.flagSeverityConfigService = flagSeverityConfigService;
        this.objectMapper = objectMapper;
        this.resolverRoutingService = resolverRoutingService;
    }

    /**
     * Resolve a conflict flag.
     *
     * @param flagEventId  the conflict_detected event ID
     * @param resolution   accepted | rejected | reclassified
     * @param reclassifiedSubjectId only required if resolution=reclassified
     * @param actorId      the resolver (coordinator)
     * @param reason       explanation
     * @return the conflict_resolved event
     * @throws IllegalArgumentException if preconditions fail
     */
    @Transactional
    public Event resolve(UUID flagEventId, String resolution, UUID reclassifiedSubjectId,
                         UUID actorId, String reason) {
        // Validate resolution value
        if (!"accepted".equals(resolution) && !"rejected".equals(resolution)
                && !"reclassified".equals(resolution)) {
            throw new IllegalArgumentException(
                    "Resolution must be 'accepted', 'rejected', or 'reclassified'");
        }

        if ("reclassified".equals(resolution) && reclassifiedSubjectId == null) {
            throw new IllegalArgumentException(
                    "reclassified_subject_id required when resolution is 'reclassified'");
        }

        // Look up the flag event
        Event flagEvent = eventRepository.findById(flagEventId);
        if (flagEvent == null) {
            throw new IllegalArgumentException("Flag event not found: " + flagEventId);
        }
        if (flagEvent.shapeRef() == null || !flagEvent.shapeRef().startsWith("conflict_detected/")) {
            throw new IllegalArgumentException(
                    "Event " + flagEventId + " is not a conflict_detected flag");
        }

        // Check not already resolved
        if (isAlreadyResolved(flagEventId)) {
            throw new IllegalArgumentException("Flag " + flagEventId + " is already resolved");
        }

        // Extract source_event_id from the flag's payload
        String sourceEventIdStr = flagEvent.payload().get("source_event_id").asText();
        UUID sourceEventId = UUID.fromString(sourceEventIdStr);

        // Extract subject from the flag event
        String subjectId = flagEvent.subjectRef().get("id").asText();

        // Build conflict_resolved event
        Event resolvedEvent = buildResolvedEvent(
                flagEventId, sourceEventId, subjectId, resolution,
                reclassifiedSubjectId, actorId, reason);

        eventRepository.insert(resolvedEvent);
        boolean canonical = isCanonicalResolution(flagEvent, resolvedEvent);
        if (!canonical) {
            Event unauthorizedFlag = buildUnauthorizedResolutionFlag(
                    flagEvent, resolvedEvent, subjectId);
            eventRepository.insert(unauthorizedFlag);
            log.info("Unauthorized conflict resolution recorded: flag={}, resolver={}, resolution_event={}",
                    flagEventId, actorId, resolvedEvent.id());
        } else {
            log.info("Conflict resolved: flag={}, resolution={}, source_event={}",
                    flagEventId, resolution, sourceEventId);
        }

        return resolvedEvent;
    }

    /**
     * Create a manual identity_conflict flag (DD-5: manual-only in Phase 1).
     *
     * @param sourceEventId the event suspected of referencing the wrong subject
     * @param actorId       the coordinator flagging the issue
     * @param reason        explanation
     * @return the conflict_detected event
     */
    public Event createManualIdentityConflict(UUID sourceEventId, UUID actorId, String reason) {
        Event sourceEvent = eventRepository.findById(sourceEventId);
        if (sourceEvent == null) {
            throw new IllegalArgumentException("Source event not found: " + sourceEventId);
        }

        String subjectId = sourceEvent.subjectRef().get("id").asText();

        // Deterministic ID for idempotency
        UUID flagId = ConflictDetector.deterministicUuid(sourceEventId, "identity_conflict");
        if (eventRepository.existsById(flagId)) {
            throw new IllegalArgumentException(
                    "Identity conflict flag already exists for event " + sourceEventId);
        }

        ResolverRef resolver = resolverRoutingService.routeManualIdentityConflict(sourceEvent, actorId);
        Event flagEvent = buildIdentityConflictFlag(flagId, sourceEventId, subjectId, actorId, resolver, reason);
        eventRepository.insert(flagEvent);
        log.info("Manual identity_conflict created: source_event={}, subject={}",
                sourceEventId, subjectId);

        return flagEvent;
    }

    /**
     * List unresolved conflict flags.
     */
    public List<FlagSummary> listUnresolvedFlags() {
        return eventRepository.getJdbcTemplate().query("""
                SELECT cd.id AS flag_id,
                       cd.payload->>'source_event_id' AS source_event_id,
                       cd.payload->>'flag_category' AS flag_category,
                       cd.subject_ref->>'id' AS subject_id,
                       cd.timestamp AS flagged_at
                FROM events cd
                WHERE cd.shape_ref LIKE 'conflict_detected/%'
                  AND NOT EXISTS (
                      SELECT 1 FROM events cr
                      WHERE cr.shape_ref LIKE 'conflict_resolved/%'
                        AND cr.payload->>'flag_event_id' = cd.id::text
                        AND cr.actor_ref->>'type' = cd.payload->'designated_resolver'->>'type'
                        AND cr.actor_ref->>'id' = cd.payload->'designated_resolver'->>'id'
                  )
                ORDER BY cd.timestamp DESC
                """,
                (rs, rowNum) -> new FlagSummary(
                        rs.getString("flag_id"),
                        rs.getString("source_event_id"),
                        rs.getString("flag_category"),
                        flagSeverityConfigService.effectiveSeverity(rs.getString("flag_category")),
                        rs.getString("subject_id"),
                        rs.getTimestamp("flagged_at").toInstant().atOffset(ZoneOffset.UTC)
                ));
    }

    /**
     * List unresolved flags designated to one authenticated actor.
     */
    public List<FlagSummary> listResolvableFlags(UUID actorId) {
        return eventRepository.getJdbcTemplate().query("""
                SELECT cd.id AS flag_id,
                       cd.payload->>'source_event_id' AS source_event_id,
                       cd.payload->>'flag_category' AS flag_category,
                       cd.subject_ref->>'id' AS subject_id,
                       cd.timestamp AS flagged_at
                FROM events cd
                WHERE cd.shape_ref LIKE 'conflict_detected/%'
                  AND cd.payload->'designated_resolver'->>'type' = 'actor'
                  AND cd.payload->'designated_resolver'->>'id' = ?
                  AND NOT EXISTS (
                      SELECT 1 FROM events cr
                      WHERE cr.shape_ref LIKE 'conflict_resolved/%'
                        AND cr.payload->>'flag_event_id' = cd.id::text
                        AND cr.actor_ref->>'type' = cd.payload->'designated_resolver'->>'type'
                        AND cr.actor_ref->>'id' = cd.payload->'designated_resolver'->>'id'
                  )
                ORDER BY cd.timestamp DESC
                """,
                (rs, rowNum) -> new FlagSummary(
                        rs.getString("flag_id"),
                        rs.getString("source_event_id"),
                        rs.getString("flag_category"),
                        flagSeverityConfigService.effectiveSeverity(rs.getString("flag_category")),
                        rs.getString("subject_id"),
                        rs.getTimestamp("flagged_at").toInstant().atOffset(ZoneOffset.UTC)
                ),
                actorId.toString());
    }

    /**
     * Get a single unresolved flag with source event context (for the resolution UI).
     * Returns null if not found or already resolved.
     */
    public FlagDetail getFlagDetail(UUID flagEventId) {
        Event flagEvent = eventRepository.findById(flagEventId);
        if (flagEvent == null || flagEvent.shapeRef() == null
                || !flagEvent.shapeRef().startsWith("conflict_detected/")) {
            return null;
        }
        if (isAlreadyResolved(flagEventId)) {
            return null;
        }

        String sourceEventIdStr = flagEvent.payload().get("source_event_id").asText();
        Event sourceEvent = eventRepository.findById(UUID.fromString(sourceEventIdStr));

        return new FlagDetail(
                flagEventId.toString(),
                flagEvent.payload().get("flag_category").asText(),
                flagSeverityConfigService.effectiveSeverity(flagEvent.payload().get("flag_category").asText()),
                flagEvent.subjectRef().get("id").asText(),
                flagEvent.payload().has("reason") ? flagEvent.payload().get("reason").asText() : "",
                flagEvent.timestamp(),
                sourceEventIdStr,
                sourceEvent != null ? sourceEvent.type() : "unknown",
                sourceEvent != null ? sourceEvent.timestamp() : null,
                sourceEvent != null ? sourceEvent.payload() : null,
                sourceEvent != null ? sourceEvent.deviceId().toString() : "unknown"
        );
    }

    public record FlagDetail(
            String flagId,
            String flagCategory,
            String severity,
            String subjectId,
            String reason,
            OffsetDateTime flaggedAt,
            String sourceEventId,
            String sourceEventType,
            OffsetDateTime sourceEventTimestamp,
            com.fasterxml.jackson.databind.JsonNode sourceEventPayload,
            String sourceDeviceId
    ) {}

    private boolean isAlreadyResolved(UUID flagEventId) {
        Integer count = eventRepository.getJdbcTemplate().queryForObject("""
                SELECT COUNT(*)
                FROM events cr
                JOIN events cd ON cd.id::text = cr.payload->>'flag_event_id'
                WHERE cr.shape_ref LIKE 'conflict_resolved/%'
                  AND cd.shape_ref LIKE 'conflict_detected/%'
                  AND cr.payload->>'flag_event_id' = ?
                  AND cr.actor_ref->>'type' = cd.payload->'designated_resolver'->>'type'
                  AND cr.actor_ref->>'id' = cd.payload->'designated_resolver'->>'id'
                """,
                Integer.class, flagEventId.toString());
        return count != null && count > 0;
    }

    private boolean isCanonicalResolution(Event flagEvent, Event resolvedEvent) {
        ResolverRef resolver = ResolverRef.fromJson(flagEvent.payload().get("designated_resolver"));
        return resolver != null && resolver.matchesActorRef(resolvedEvent.actorRef());
    }

    private Event buildResolvedEvent(UUID flagEventId, UUID sourceEventId, String subjectId,
                                      String resolution, UUID reclassifiedSubjectId,
                                      UUID actorId, String reason) {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", subjectId);

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", actorId.toString());

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("flag_event_id", flagEventId.toString());
        payload.put("source_event_id", sourceEventId.toString());
        payload.put("resolution", resolution);
        if (reclassifiedSubjectId != null) {
            payload.put("reclassified_subject_id", reclassifiedSubjectId.toString());
        }
        if (reason != null) {
            payload.put("reason", reason);
        }

        return new Event(
                UUID.randomUUID(),
                "review",
                "conflict_resolved/v1",
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

    private Event buildUnauthorizedResolutionFlag(Event originalFlag,
                                                  Event unauthorizedResolution,
                                                  String subjectId) {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", subjectId);

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", "system:conflict_detector/scope_violation");

        ResolverRef resolver = resolverRoutingService.routeUnauthorizedResolution(originalFlag);
        ObjectNode resolverNode = objectMapper.createObjectNode();
        resolver.writeTo(resolverNode);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("source_event_id", unauthorizedResolution.id().toString());
        payload.put("flag_category", "scope_violation");
        payload.put("resolvability", FlagCatalog.resolvabilityFor("scope_violation"));
        payload.set("designated_resolver", resolverNode);
        payload.put("reason", "Conflict resolution event was not authored by the designated resolver");

        return new Event(
                ConflictDetector.deterministicUuid(unauthorizedResolution.id(), "scope_violation"),
                "alert",
                "conflict_detected/v1",
                null,
                subjectRef,
                actorRef,
                serverIdentity.getDeviceId(),
                (int) serverIdentity.nextDeviceSeq(),
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                payload
        );
    }

    private Event buildIdentityConflictFlag(UUID flagId, UUID sourceEventId, String subjectId,
                                             UUID actorId, ResolverRef designatedResolver,
                                             String reason) {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", subjectId);

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", actorId.toString());

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("source_event_id", sourceEventId.toString());
        payload.put("flag_category", "identity_conflict");
        payload.put("resolvability", FlagCatalog.resolvabilityFor("identity_conflict"));
        ObjectNode resolver = objectMapper.createObjectNode();
        designatedResolver.writeTo(resolver);
        payload.set("designated_resolver", resolver);
        payload.put("reason", reason != null ? reason : "Manual identity conflict flag");

        return new Event(
                flagId,
                "alert",
                "conflict_detected/v1",
                null,
                subjectRef,
                actorRef,
                serverIdentity.getDeviceId(),
                (int) serverIdentity.nextDeviceSeq(),
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                payload
        );
    }

    public record FlagSummary(
            @JsonProperty("flag_id") String flagId,
            @JsonProperty("source_event_id") String sourceEventId,
            @JsonProperty("flag_category") String flagCategory,
            @JsonProperty("severity") String severity,
            @JsonProperty("subject_id") String subjectId,
            @JsonProperty("flagged_at") OffsetDateTime flaggedAt
    ) {}
}
