package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.config.Activity;
import dev.datarun.server.config.ActivityRepository;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.ServerIdentity;
import dev.datarun.server.projection.PatternStateProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Authoritative Phase 4.6 pattern transition detector.
 *
 * <p>Transition violations are state anomalies: the incoming event remains
 * accepted, and this detector emits a standard conflict_detected/v1 flag.
 */
@Component
public class TransitionViolationDetector {

    private static final Logger log = LoggerFactory.getLogger(TransitionViolationDetector.class);
    private static final String FLAG_CATEGORY = "transition_violation";

    private final EventRepository eventRepository;
    private final ActivityRepository activityRepository;
    private final PatternStateProjection patternStateProjection;
    private final ResolverRoutingService resolverRoutingService;
    private final ServerIdentity serverIdentity;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbc;

    public TransitionViolationDetector(EventRepository eventRepository,
                                       ActivityRepository activityRepository,
                                       PatternStateProjection patternStateProjection,
                                       ResolverRoutingService resolverRoutingService,
                                       ServerIdentity serverIdentity,
                                       ObjectMapper objectMapper,
                                       JdbcTemplate jdbc) {
        this.eventRepository = eventRepository;
        this.activityRepository = activityRepository;
        this.patternStateProjection = patternStateProjection;
        this.resolverRoutingService = resolverRoutingService;
        this.serverIdentity = serverIdentity;
        this.objectMapper = objectMapper;
        this.jdbc = jdbc;
    }

    public List<Event> evaluate(List<Event> acceptedEvents) {
        if (acceptedEvents == null || acceptedEvents.isEmpty()) {
            return List.of();
        }

        Map<String, JsonNode> activityConfigs = activeActivityConfigs();
        if (activityConfigs.isEmpty()) {
            return List.of();
        }

        Set<String> alreadyUnresolved = unresolvedFlaggedSourceIds(acceptedEvents);
        List<Event> allEvents = eventRepository.findAllOrdered();
        List<Event> flagEvents = new ArrayList<>();

        for (Event event : acceptedEvents) {
            if (alreadyUnresolved.contains(event.id().toString()) || isProjectionMetadata(event)) {
                continue;
            }
            Long incomingWatermark = eventRepository.getSyncWatermark(event.id());
            if (incomingWatermark == null) {
                continue;
            }

            List<Event> projectionBasis = projectionBasisBefore(allEvents, incomingWatermark);
            List<PatternStateProjection.TransitionCheck> checks =
                    patternStateProjection.checkTransition(event, projectionBasis, activityConfigs);
            PatternStateProjection.TransitionCheck violation = checks.stream()
                    .filter(check -> !check.allowed())
                    .findFirst()
                    .orElse(null);
            if (violation == null) {
                continue;
            }

            UUID subjectId = extractSubjectId(event);
            if (subjectId == null) {
                continue;
            }
            ResolverRef resolver = resolverRoutingService.route(event, FLAG_CATEGORY);
            flagEvents.add(buildFlagEvent(event, subjectId, violation, resolver));
            log.info("Pattern transition violation detected for event {} ({}, binding {})",
                    event.id(), event.shapeRef(), violation.bindingRef());
        }

        return flagEvents;
    }

    private Map<String, JsonNode> activeActivityConfigs() {
        Map<String, JsonNode> configs = new LinkedHashMap<>();
        for (Activity activity : activityRepository.findActive()) {
            configs.put(activity.name(), activity.configJson());
        }
        return configs;
    }

    private Set<String> unresolvedFlaggedSourceIds(List<Event> acceptedEvents) {
        List<String> sourceIds = acceptedEvents.stream()
                .map(event -> event.id().toString())
                .toList();
        if (sourceIds.isEmpty()) {
            return Set.of();
        }
        String placeholders = String.join(", ", java.util.Collections.nCopies(sourceIds.size(), "?"));
        String sql = """
                SELECT DISTINCT cd.payload->>'source_event_id'
                FROM events cd
                WHERE cd.shape_ref LIKE 'conflict_detected/%%'
                  AND cd.payload->>'source_event_id' IN (%s)
                  AND NOT EXISTS (
                      SELECT 1
                      FROM events cr
                      WHERE cr.shape_ref LIKE 'conflict_resolved/%%'
                        AND cr.payload->>'flag_event_id' = cd.id::text
                        AND cr.payload->>'resolution' = 'accepted'
                        AND cr.actor_ref->>'type' = cd.payload->'designated_resolver'->>'type'
                        AND cr.actor_ref->>'id' = cd.payload->'designated_resolver'->>'id'
                  )
                """.formatted(placeholders);
        return new LinkedHashSet<>(jdbc.queryForList(sql, String.class, sourceIds.toArray()));
    }

    private List<Event> projectionBasisBefore(List<Event> allEvents, long incomingWatermark) {
        Set<String> priorEventIds = new LinkedHashSet<>();
        for (Event event : allEvents) {
            if (event.syncWatermark() != null && event.syncWatermark() < incomingWatermark) {
                priorEventIds.add(event.id().toString());
            }
        }

        return allEvents.stream()
                .filter(event -> {
                    if (event.syncWatermark() != null && event.syncWatermark() < incomingWatermark) {
                        return true;
                    }
                    return isIntegrityFlag(event)
                            && priorEventIds.contains(sourceEventId(event));
                })
                .toList();
    }

    private Event buildFlagEvent(Event sourceEvent,
                                 UUID subjectId,
                                 PatternStateProjection.TransitionCheck violation,
                                 ResolverRef resolver) {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", subjectId.toString());

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", "system:conflict_detector/" + FLAG_CATEGORY);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("source_event_id", sourceEvent.id().toString());
        payload.put("flag_category", FLAG_CATEGORY);
        payload.put("resolvability", FlagCatalog.resolvabilityFor(FLAG_CATEGORY));
        ObjectNode resolverNode = objectMapper.createObjectNode();
        resolver.writeTo(resolverNode);
        payload.set("designated_resolver", resolverNode);
        payload.put("reason", reason(sourceEvent, violation));

        return new Event(
                ConflictDetector.deterministicUuid(sourceEvent.id(), FLAG_CATEGORY),
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

    private String reason(Event sourceEvent, PatternStateProjection.TransitionCheck violation) {
        String state = violation.currentState() == null ? "<none>" : violation.currentState();
        return "Event " + sourceEvent.type() + "/" + sourceEvent.shapeRef()
                + " is not allowed for pattern " + violation.bindingRef()
                + " role " + violation.shapeRole()
                + " from current state " + state;
    }

    private UUID extractSubjectId(Event event) {
        JsonNode subjectRef = event.subjectRef();
        if (subjectRef == null || !"subject".equals(subjectRef.path("type").asText())) {
            return null;
        }
        try {
            return UUID.fromString(subjectRef.path("id").asText());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isProjectionMetadata(Event event) {
        return isConflictMetadata(event)
                || event.shapeRef().startsWith("subjects_merged/")
                || event.shapeRef().startsWith("subject_split/");
    }

    private boolean isConflictMetadata(Event event) {
        return event.shapeRef().startsWith("conflict_detected/")
                || event.shapeRef().startsWith("conflict_resolved/");
    }

    private boolean isIntegrityFlag(Event event) {
        return event.shapeRef().startsWith("conflict_detected/");
    }

    private String sourceEventId(Event event) {
        JsonNode source = event.payload() == null ? null : event.payload().get("source_event_id");
        return source != null && source.isTextual() ? source.asText() : null;
    }
}
