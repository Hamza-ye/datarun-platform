package dev.datarun.server.authorization;

import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class WebAdminOperationalViewService {

    static final String NO_SCOPED_WORK_FRESHNESS =
            "No scoped synced work is visible yet.";
    static final String NEEDS_REVIEW_LABEL = "Needs review";
    static final String NEEDS_REVIEW_COPY =
            "One unresolved attention item is attached to this work.";

    private static final int CANDIDATE_SCAN_LIMIT = 200;

    private final EventRepository eventRepository;
    private final ScopeResolver scopeResolver;

    public WebAdminOperationalViewService(EventRepository eventRepository,
                                          ScopeResolver scopeResolver) {
        this.eventRepository = eventRepository;
        this.scopeResolver = scopeResolver;
    }

    public OperationalObservation observe(UUID actorId) {
        List<ActiveAssignment> assignments = scopeResolver.getActiveAssignments(actorId);
        if (assignments.isEmpty()) {
            return OperationalObservation.empty(NO_SCOPED_WORK_FRESHNESS);
        }

        return eventRepository.findRecentSubjectWorkEvents(CANDIDATE_SCAN_LIMIT)
                .stream()
                .filter(event -> isVisible(event, assignments))
                .findFirst()
                .map(event -> new OperationalObservation(
                        freshnessText(event),
                        latestWork(event),
                        attentionCue(event)))
                .orElseGet(() -> OperationalObservation.empty(NO_SCOPED_WORK_FRESHNESS));
    }

    private boolean isVisible(Event event, List<ActiveAssignment> assignments) {
        UUID subjectId = subjectId(event);
        if (subjectId == null) {
            return false;
        }
        String locationPath = eventRepository.getLocationPath(event.id());
        return assignments.stream().anyMatch(assignment ->
                assignment.containsGeographically(locationPath)
                        && assignment.containsSubject(subjectId)
                        && assignment.containsActivity(event.activityRef()));
    }

    private LatestWork latestWork(Event event) {
        UUID subjectId = subjectId(event);
        return new LatestWork(
                displayName(event.shapeRef(), "Work Item"),
                displayName(event.activityRef(), "Assigned Work"),
                subjectId == null ? "" : subjectId.toString(),
                event.syncWatermark(),
                event.timestamp().toString());
    }

    private String freshnessText(Event event) {
        return "Latest visible synced work in Datarun. Freshness marker: "
                + event.syncWatermark()
                + ". This does not prove all devices are current.";
    }

    private AttentionCue attentionCue(Event event) {
        List<AttentionCue> cues = eventRepository.getJdbcTemplate().query("""
                SELECT cd.id
                FROM events cd
                WHERE cd.shape_ref LIKE 'conflict_detected/%'
                  AND cd.payload->>'source_event_id' = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM events cr
                      WHERE cr.shape_ref LIKE 'conflict_resolved/%'
                        AND cr.payload->>'flag_event_id' = cd.id::text
                        AND cr.actor_ref->>'type' = cd.payload->'designated_resolver'->>'type'
                        AND cr.actor_ref->>'id' = cd.payload->'designated_resolver'->>'id'
                  )
                ORDER BY cd.sync_watermark DESC
                LIMIT 1
                """,
                (rs, rowNum) -> new AttentionCue(NEEDS_REVIEW_LABEL, NEEDS_REVIEW_COPY),
                event.id().toString());
        return cues.isEmpty() ? null : cues.get(0);
    }

    private UUID subjectId(Event event) {
        if (event.subjectRef() == null
                || !"subject".equals(event.subjectRef().path("type").asText(null))) {
            return null;
        }
        try {
            return UUID.fromString(event.subjectRef().path("id").asText());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String displayName(String ref, String fallback) {
        if (ref == null || ref.isBlank()) {
            return fallback;
        }
        String base = ref.contains("/") ? ref.substring(0, ref.indexOf('/')) : ref;
        String normalized = base.replaceAll("[^A-Za-z0-9]+", " ").trim();
        if (normalized.isBlank()) {
            return fallback;
        }
        StringBuilder label = new StringBuilder();
        for (String part : normalized.split("\\s+")) {
            if (part.isBlank()) {
                continue;
            }
            if (label.length() > 0) {
                label.append(' ');
            }
            String lower = part.toLowerCase(Locale.ROOT);
            label.append(Character.toUpperCase(lower.charAt(0)));
            if (lower.length() > 1) {
                label.append(lower.substring(1));
            }
        }
        return label.length() == 0 ? fallback : label.toString();
    }

    public record OperationalObservation(
            String freshnessText,
            LatestWork latestWork,
            AttentionCue attentionCue
    ) {
        static OperationalObservation empty(String freshnessText) {
            return new OperationalObservation(freshnessText, null, null);
        }

        public boolean hasLatestWork() {
            return latestWork != null;
        }

        public boolean hasAttentionCue() {
            return attentionCue != null;
        }
    }

    public record LatestWork(
            String workType,
            String activity,
            String subjectId,
            Long syncWatermark,
            String workTime
    ) {}

    public record AttentionCue(String label, String copy) {}
}
