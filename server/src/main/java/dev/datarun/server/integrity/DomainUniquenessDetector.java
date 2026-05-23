package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.config.Shape;
import dev.datarun.server.config.ShapeRepository;
import dev.datarun.server.config.ShapeService;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.ServerIdentity;
import dev.datarun.server.identity.SubjectAliasProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Authoritative Phase 4.3 shape-declared domain uniqueness detector.
 *
 * <p>Uniqueness violations are state anomalies: the incoming event remains accepted,
 * and this detector emits the standard conflict_detected/v1 flag that targets only
 * the incoming source event.
 */
@Component
public class DomainUniquenessDetector {

    private static final Logger log = LoggerFactory.getLogger(DomainUniquenessDetector.class);
    private static final String FLAG_CATEGORY = "domain_uniqueness_violation";
    private static final String DETECTOR_VERSION = "domain_uniqueness_detector/v1";

    private final EventRepository eventRepository;
    private final ShapeRepository shapeRepository;
    private final SubjectAliasProjection subjectAliasProjection;
    private final ServerIdentity serverIdentity;
    private final ObjectMapper objectMapper;
    private final ZoneId deploymentZone;

    public DomainUniquenessDetector(EventRepository eventRepository,
                                    ShapeRepository shapeRepository,
                                    SubjectAliasProjection subjectAliasProjection,
                                    ServerIdentity serverIdentity,
                                    ObjectMapper objectMapper,
                                    @Value("${datarun.uniqueness.timezone:UTC}") String timezone) {
        this.eventRepository = eventRepository;
        this.shapeRepository = shapeRepository;
        this.subjectAliasProjection = subjectAliasProjection;
        this.serverIdentity = serverIdentity;
        this.objectMapper = objectMapper;
        this.deploymentZone = resolveZone(timezone);
    }

    public List<Event> evaluate(List<Event> acceptedEvents) {
        List<Event> flagEvents = new ArrayList<>();

        for (Event event : acceptedEvents) {
            if (isIntegrityOrIdentityEvent(event) || isAssignmentEvent(event)) {
                continue;
            }

            Optional<Constraint> constraint = findConstraint(event.shapeRef());
            if (constraint.isEmpty()) {
                continue;
            }

            Long incomingWatermark = eventRepository.getSyncWatermark(event.id());
            if (incomingWatermark == null) {
                continue;
            }

            NormalizedKey incomingKey = buildKey(event, constraint.get());
            if (incomingKey == null) {
                continue;
            }
            PeriodWindow incomingWindow = buildWindow(event, constraint.get().period());

            List<UUID> conflictingEventIds = new ArrayList<>();
            List<Event> candidates = eventRepository.findPriorAuthoritativeByShape(
                    event.shapeRef(), incomingWatermark);
            for (Event candidate : candidates) {
                NormalizedKey candidateKey = buildKey(candidate, constraint.get());
                if (candidateKey == null || !incomingKey.hash().equals(candidateKey.hash())) {
                    continue;
                }
                PeriodWindow candidateWindow = buildWindow(candidate, constraint.get().period());
                if (!sameWindow(incomingWindow, candidateWindow)) {
                    continue;
                }
                conflictingEventIds.add(candidate.id());
            }

            if (!conflictingEventIds.isEmpty()) {
                UUID subjectId = extractSubjectId(event);
                if (subjectId == null) {
                    continue;
                }
                Event flag = buildFlagEvent(
                        event, subjectId, constraint.get(), incomingKey,
                        incomingWindow, conflictingEventIds);
                flagEvents.add(flag);
                log.info("Domain uniqueness violation detected for event {} ({})",
                        event.id(), event.shapeRef());
            }
        }

        return flagEvents;
    }

    private Optional<Constraint> findConstraint(String shapeRef) {
        String[] parts = ShapeService.parseShapeRef(shapeRef);
        if (parts == null) {
            return Optional.empty();
        }
        Optional<Shape> shape = shapeRepository.findByNameAndVersion(
                parts[0], Integer.parseInt(parts[1]));
        if (shape.isEmpty()) {
            return Optional.empty();
        }

        JsonNode uniqueness = shape.get().schemaJson().get("uniqueness");
        if (uniqueness == null || uniqueness.isNull() || !uniqueness.isObject()) {
            return Optional.empty();
        }

        JsonNode scopeNode = uniqueness.get("scope");
        if (scopeNode == null || !scopeNode.isArray() || scopeNode.isEmpty()) {
            return Optional.empty();
        }

        List<String> scope = new ArrayList<>();
        for (JsonNode dimension : scopeNode) {
            if (!dimension.isTextual()) {
                return Optional.empty();
            }
            scope.add(dimension.asText());
        }

        Period period = null;
        JsonNode periodNode = uniqueness.get("period");
        if (periodNode != null && !periodNode.isNull()) {
            JsonNode type = periodNode.get("type");
            if (type == null || !type.isTextual()) {
                return Optional.empty();
            }
            period = new Period(type.asText());
        }

        return Optional.of(new Constraint(shapeRef + "#uniqueness", scope, period));
    }

    private NormalizedKey buildKey(Event event, Constraint constraint) {
        List<KeyPart> parts = new ArrayList<>();
        for (String dimension : constraint.scope()) {
            String value = switch (dimension) {
                case "subject_ref" -> normalizeSubject(event);
                case "activity_ref" -> normalizeNullable(event.activityRef());
                default -> normalizePayloadDimension(event, dimension);
            };
            if (value == null) {
                return null;
            }
            parts.add(new KeyPart(dimension, value));
        }
        return new NormalizedKey(parts, sha256(canonicalKey(parts)));
    }

    private String normalizeSubject(Event event) {
        UUID subjectId = extractSubjectId(event);
        if (subjectId == null) {
            return null;
        }
        return subjectAliasProjection.resolve(subjectId).toString();
    }

    private String normalizePayloadDimension(Event event, String dimension) {
        if (!dimension.startsWith("payload.")) {
            return null;
        }
        String fieldName = dimension.substring("payload.".length());
        JsonNode value = event.payload() != null ? event.payload().get(fieldName) : null;
        if (value == null || value.isMissingNode() || value.isNull()) {
            return "<null>";
        }
        if (value.isTextual()) {
            return value.asText();
        }
        if (value.isNumber()) {
            return value.decimalValue().stripTrailingZeros().toPlainString();
        }
        if (value.isBoolean()) {
            return Boolean.toString(value.asBoolean());
        }
        return null;
    }

    private String normalizeNullable(String value) {
        return value == null ? "<null>" : value;
    }

    private String canonicalKey(List<KeyPart> parts) {
        StringBuilder builder = new StringBuilder();
        for (KeyPart part : parts) {
            builder.append(part.dimension())
                    .append('=')
                    .append(part.value())
                    .append('\n');
        }
        return builder.toString();
    }

    private PeriodWindow buildWindow(Event event, Period period) {
        if (period == null) {
            return null;
        }

        ZonedDateTime zoned = event.timestamp().atZoneSameInstant(deploymentZone);
        LocalDate date = zoned.toLocalDate();
        ZonedDateTime start = switch (period.type()) {
            case "calendar_day" -> date.atStartOfDay(deploymentZone);
            case "calendar_week" -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay(deploymentZone);
            case "calendar_month" -> date.withDayOfMonth(1).atStartOfDay(deploymentZone);
            default -> null;
        };
        if (start == null) {
            return null;
        }

        ZonedDateTime end = switch (period.type()) {
            case "calendar_day" -> start.plusDays(1);
            case "calendar_week" -> start.plusWeeks(1);
            case "calendar_month" -> start.plusMonths(1);
            default -> null;
        };
        if (end == null) {
            return null;
        }

        return new PeriodWindow(
                period.type(),
                deploymentZone.getId(),
                start.toOffsetDateTime(),
                end.toOffsetDateTime());
    }

    private boolean sameWindow(PeriodWindow a, PeriodWindow b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.type().equals(b.type())
                && a.windowStart().toInstant().equals(b.windowStart().toInstant())
                && a.windowEnd().toInstant().equals(b.windowEnd().toInstant());
    }

    private Event buildFlagEvent(Event sourceEvent,
                                 UUID subjectId,
                                 Constraint constraint,
                                 NormalizedKey key,
                                 PeriodWindow window,
                                 List<UUID> conflictingEventIds) {
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
        payload.put("reason", "Incoming event duplicates a shape-declared domain uniqueness constraint");
        payload.put("constraint_ref", constraint.constraintRef());
        payload.put("shape_ref", sourceEvent.shapeRef());
        if (sourceEvent.activityRef() != null) {
            payload.put("activity_ref", sourceEvent.activityRef());
        } else {
            payload.putNull("activity_ref");
        }

        ObjectNode keyNode = objectMapper.createObjectNode();
        keyNode.put("hash", key.hash());
        ArrayNode dimensions = keyNode.putArray("dimensions");
        key.parts().forEach(part -> dimensions.add(part.dimension()));
        payload.set("normalized_key", keyNode);

        if (window != null) {
            ObjectNode periodNode = objectMapper.createObjectNode();
            periodNode.put("type", window.type());
            periodNode.put("timezone", window.timezone());
            periodNode.put("window_start", window.windowStart().toString());
            periodNode.put("window_end", window.windowEnd().toString());
            payload.set("period", periodNode);
        }

        ArrayNode conflicts = payload.putArray("conflicting_event_ids");
        conflictingEventIds.forEach(id -> conflicts.add(id.toString()));
        payload.put("detector_version", DETECTOR_VERSION);

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

    private boolean isAssignmentEvent(Event event) {
        return "assignment_changed".equals(event.type());
    }

    private boolean isIntegrityOrIdentityEvent(Event event) {
        String shapeRef = event.shapeRef();
        if (shapeRef == null) {
            return false;
        }
        return shapeRef.startsWith("conflict_detected/")
                || shapeRef.startsWith("conflict_resolved/")
                || shapeRef.startsWith("subjects_merged/")
                || shapeRef.startsWith("subject_split/");
    }

    private ZoneId resolveZone(String timezone) {
        try {
            if (timezone == null || timezone.isBlank()) {
                return ZoneOffset.UTC;
            }
            return ZoneId.of(timezone);
        } catch (Exception e) {
            log.warn("Invalid datarun.uniqueness.timezone '{}'; using UTC", timezone);
            return ZoneOffset.UTC;
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private record Constraint(String constraintRef, List<String> scope, Period period) {}
    private record Period(String type) {}
    private record PeriodWindow(String type, String timezone,
                                OffsetDateTime windowStart, OffsetDateTime windowEnd) {}
    private record KeyPart(String dimension, String value) {}
    private record NormalizedKey(List<KeyPart> parts, String hash) {}
}
