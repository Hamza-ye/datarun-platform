package dev.datarun.server.authorization;

import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.event.EventRepository.OperationalAttentionItem;
import dev.datarun.server.event.EventRepository.OperationalScope;
import dev.datarun.server.event.EventRepository.OperationalWorkEvent;
import dev.datarun.server.config.FlagSeverityConfigService;
import dev.datarun.server.integrity.ConflictResolutionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class WebAdminOperationalViewService {

    static final String NO_SCOPED_WORK_FRESHNESS =
            "No scoped synced work is visible yet.";
    static final String NEEDS_REVIEW_LABEL = "Needs review";
    static final String NEEDS_REVIEW_COPY =
            "One unresolved attention item is attached to this work.";
    static final String NO_ATTENTION_ITEM =
            "No unresolved attention item is attached to the current scoped work.";
    static final String RESOLVER_CURRENT_ACTOR =
            "You are the assigned reviewer for this item.";
    static final String RESOLVER_OTHER_ACTOR =
            "This item is assigned to another reviewer.";
    static final String RESOLVER_UNASSIGNED =
            "This item is blocked because no reviewer is currently assigned.";

    private final EventRepository eventRepository;
    private final ScopeResolver scopeResolver;
    private final FlagSeverityConfigService flagSeverityConfigService;
    private final ConflictResolutionService conflictResolutionService;

    public WebAdminOperationalViewService(EventRepository eventRepository,
                                          ScopeResolver scopeResolver,
                                          FlagSeverityConfigService flagSeverityConfigService,
                                          ConflictResolutionService conflictResolutionService) {
        this.eventRepository = eventRepository;
        this.scopeResolver = scopeResolver;
        this.flagSeverityConfigService = flagSeverityConfigService;
        this.conflictResolutionService = conflictResolutionService;
    }

    public OperationalObservation observe(UUID actorId) {
        List<OperationalScope> scopes = operationalScopes(actorId);
        if (scopes.isEmpty()) {
            return OperationalObservation.empty(NO_SCOPED_WORK_FRESHNESS);
        }

        return eventRepository.findLatestVisibleSubjectWorkEvent(scopes)
                .map(work -> new OperationalObservation(
                        freshnessText(work),
                        latestWork(work),
                        attentionCue(work.event(), actorId, scopes)))
                .orElseGet(() -> OperationalObservation.empty(NO_SCOPED_WORK_FRESHNESS));
    }

    public AttentionReview review(UUID actorId) {
        List<OperationalScope> scopes = operationalScopes(actorId);
        if (scopes.isEmpty()) {
            return AttentionReview.empty(NO_SCOPED_WORK_FRESHNESS);
        }
        Optional<OperationalWorkEvent> work =
                eventRepository.findLatestVisibleSubjectWorkEvent(scopes);
        if (work.isEmpty()) {
            return AttentionReview.empty(NO_SCOPED_WORK_FRESHNESS);
        }
        return eventRepository.findVisibleUnresolvedOperationalAttention(
                        work.get().event().id(), actorId, scopes)
                .map(item -> new AttentionReview(null, attentionDetail(item)))
                .orElseGet(() -> AttentionReview.empty(NO_ATTENTION_ITEM));
    }

    public void resolveCurrentAttention(UUID actorId, String resolution, String reason) {
        AttentionReview review = review(actorId);
        if (!review.hasItem()) {
            throw new IllegalArgumentException("No unresolved attention item is available.");
        }
        AttentionDetail item = review.item();
        if (!item.canResolve()) {
            throw new IllegalArgumentException("This attention item is not resolvable by the current reviewer.");
        }
        conflictResolutionService.resolve(
                item.flagId(), resolution, null, actorId, blankToNull(reason));
    }

    private List<OperationalScope> operationalScopes(UUID actorId) {
        return scopeResolver.getActiveAssignments(actorId).stream()
                .map(assignment -> new OperationalScope(
                        assignment.geographicPath(),
                        assignment.subjectList(),
                        assignment.activityList()))
                .toList();
    }

    private LatestWork latestWork(OperationalWorkEvent work) {
        Event event = work.event();
        UUID subjectId = subjectId(event);
        return new LatestWork(
                displayName(event.shapeRef(), "Work Item"),
                displayName(event.activityRef(), "Assigned Work"),
                subjectId == null ? "" : subjectId.toString(),
                work.receivedAt().toString(),
                event.timestamp().toString());
    }

    private String freshnessText(OperationalWorkEvent work) {
        return "Latest visible synced work was received by Datarun at "
                + work.receivedAt()
                + ". This does not prove all devices are current.";
    }

    private AttentionCue attentionCue(Event event, UUID actorId, List<OperationalScope> scopes) {
        return eventRepository.findVisibleUnresolvedOperationalAttention(
                        event.id(), actorId, scopes)
                .map(item -> new AttentionCue(
                        NEEDS_REVIEW_LABEL,
                        NEEDS_REVIEW_COPY,
                        "/web-admin/operational/attention"))
                .orElse(null);
    }

    private AttentionDetail attentionDetail(OperationalAttentionItem item) {
        String category = safeCategoryLabel(item.category());
        String severity = displayName(
                flagSeverityConfigService.effectiveSeverity(item.category()),
                "Attention");
        return new AttentionDetail(
                item.flagId(),
                latestWork(item.sourceWork()),
                category,
                severity,
                item.reason() == null || item.reason().isBlank()
                        ? "Review requested for this work item."
                        : item.reason(),
                item.flaggedAt().toString(),
                resolverStanding(item),
                item.assignedToCurrentActor() && !item.resolverUnassigned());
    }

    private String resolverStanding(OperationalAttentionItem item) {
        if (item.resolverUnassigned()) {
            return RESOLVER_UNASSIGNED;
        }
        if (item.assignedToCurrentActor()) {
            return RESOLVER_CURRENT_ACTOR;
        }
        return RESOLVER_OTHER_ACTOR;
    }

    private String safeCategoryLabel(String category) {
        if (category == null || category.isBlank()) {
            return "Attention Item";
        }
        return switch (category) {
            case "scope_violation" -> "Scope Review";
            case "temporal_authority_expired" -> "Timing Review";
            case "role_stale" -> "Role Review";
            case "concurrent_state_change" -> "Concurrent Work Review";
            case "identity_conflict" -> "Identity Review";
            case "domain_uniqueness_violation" -> "Duplicate Work Review";
            case "transition_violation" -> "Workflow Transition Review";
            default -> "Attention Item";
        };
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

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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
            String receivedAt,
            String workTime
    ) {}

    public record AttentionCue(String label, String copy, String reviewPath) {}

    public record AttentionReview(String emptyText, AttentionDetail item) {
        static AttentionReview empty(String emptyText) {
            return new AttentionReview(emptyText, null);
        }

        public boolean hasItem() {
            return item != null;
        }
    }

    public record AttentionDetail(
            UUID flagId,
            LatestWork sourceWork,
            String category,
            String severity,
            String reason,
            String flaggedAt,
            String resolverStanding,
            boolean canResolve
    ) {}
}
