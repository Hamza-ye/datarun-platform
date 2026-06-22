package dev.datarun.server.authorization;

import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.event.EventRepository.OperationalAttentionItem;
import dev.datarun.server.event.EventRepository.OperationalScope;
import dev.datarun.server.event.EventRepository.OperationalWorkEvent;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

@Service
public class OperationalResponsibilityHandoffService {

    static final String SCOPED_CONTEXT_CAVEAT =
            "Current assigned work and prior context are limited to what this session can see now.";
    static final String EMPTY_CONTEXT =
            "No visible handoff context for the current actor.";
    static final String CONTEXT_INCOMPLETE_COPY =
            "Context incomplete; this handoff shows only currently visible accepted context.";
    static final String FRESHNESS_UNKNOWN_COPY =
            "Freshness unknown; this does not prove every device has synced.";
    static final String NEEDS_ATTENTION_COPY =
            "Visible work has unresolved attention.";
    static final String LATE_SYNCED_WORK_COPY =
            "Late synced work may include work captured offline before it became visible here.";
    static final String STALE_RESPONSIBILITY_COPY =
            "Stale responsibility; a synced item may have been created after central responsibility changed.";
    static final String NOT_CURRENTLY_RESOLVABLE_COPY =
            "Not currently resolvable by this session; use the designated reviewer standing.";

    private static final int CURRENT_WORK_LIMIT = 5;

    private final EventRepository eventRepository;
    private final ScopeResolver scopeResolver;

    public OperationalResponsibilityHandoffService(EventRepository eventRepository,
                                                   ScopeResolver scopeResolver) {
        this.eventRepository = eventRepository;
        this.scopeResolver = scopeResolver;
    }

    public OperationalResponsibilityHandoffContext context(UUID actorId) {
        OffsetDateTime preparedAt = OffsetDateTime.now(ZoneOffset.UTC);
        List<ActiveAssignment> assignments = scopeResolver.getActiveAssignments(actorId);
        List<OperationalScope> scopes = operationalScopes(assignments);
        List<HandoffWorkItem> workItems = eventRepository
                .findOperationalHandoffCurrentWork(scopes, CURRENT_WORK_LIMIT)
                .stream()
                .map(work -> handoffWorkItem(actorId, scopes, work))
                .toList();

        return new OperationalResponsibilityHandoffContext(
                preparedAt,
                SCOPED_CONTEXT_CAVEAT,
                assignedWork(assignments),
                latestVisibleInputAt(workItems).map(OffsetDateTime::toString).orElse(null),
                globalCaveats(workItems),
                workItems,
                traceContext(workItems));
    }

    private HandoffWorkItem handoffWorkItem(UUID actorId,
                                            List<OperationalScope> scopes,
                                            OperationalWorkEvent currentWork) {
        UUID subjectId = subjectId(currentWork.event());
        Optional<OperationalWorkEvent> priorContext = Optional.empty();
        if (currentWork.event().syncWatermark() != null) {
            priorContext = eventRepository.findOperationalHandoffPriorContext(
                    subjectId,
                    currentWork.event().activityRef(),
                    currentWork.event().syncWatermark(),
                    scopes);
        }
        Optional<OperationalAttentionItem> attention =
                eventRepository.findVisibleUnresolvedOperationalAttention(
                        currentWork.event().id(), actorId, scopes);
        List<Caveat> caveats = itemCaveats(attention);
        return new HandoffWorkItem(
                workSummary(currentWork),
                priorContext.map(this::workSummary).orElse(null),
                attention.map(this::attentionStanding).orElse(null),
                caveats);
    }

    private List<OperationalScope> operationalScopes(List<ActiveAssignment> assignments) {
        return assignments.stream()
                .map(assignment -> new OperationalScope(
                        assignment.geographicPath(),
                        assignment.subjectList(),
                        assignment.activityList()))
                .toList();
    }

    private List<String> assignedWork(List<ActiveAssignment> assignments) {
        TreeSet<String> labels = new TreeSet<>();
        for (ActiveAssignment assignment : assignments) {
            if (assignment.activityList() == null) {
                labels.add("Assigned work in current scope");
                continue;
            }
            assignment.activityList().stream()
                    .map(activity -> displayName(activity, "Assigned Work"))
                    .forEach(labels::add);
        }
        return List.copyOf(labels);
    }

    private Optional<OffsetDateTime> latestVisibleInputAt(List<HandoffWorkItem> items) {
        return items.stream()
                .flatMap(item -> {
                    List<OffsetDateTime> times = new ArrayList<>();
                    times.add(item.currentWork().receivedAt());
                    if (item.hasPriorContext()) {
                        times.add(item.priorContext().receivedAt());
                    }
                    if (item.hasAttention()) {
                        times.add(item.attention().flaggedAt());
                    }
                    return times.stream();
                })
                .max(Comparator.naturalOrder());
    }

    private List<Caveat> globalCaveats(List<HandoffWorkItem> items) {
        List<Caveat> caveats = new ArrayList<>();
        caveats.add(new Caveat("Context incomplete", CONTEXT_INCOMPLETE_COPY));
        caveats.add(new Caveat("Freshness unknown", FRESHNESS_UNKNOWN_COPY));
        if (items.stream().anyMatch(HandoffWorkItem::hasAttention)) {
            caveats.add(new Caveat("Needs attention", NEEDS_ATTENTION_COPY));
        }
        if (items.stream().anyMatch(HandoffWorkItem::hasLateSyncedWorkCaveat)) {
            caveats.add(new Caveat("Late synced work", LATE_SYNCED_WORK_COPY));
        }
        if (items.stream().anyMatch(HandoffWorkItem::hasStaleResponsibilityCaveat)) {
            caveats.add(new Caveat("Stale responsibility", STALE_RESPONSIBILITY_COPY));
        }
        if (items.stream().anyMatch(HandoffWorkItem::hasNotCurrentlyResolvableCaveat)) {
            caveats.add(new Caveat("Not currently resolvable", NOT_CURRENTLY_RESOLVABLE_COPY));
        }
        return caveats;
    }

    private List<Caveat> itemCaveats(Optional<OperationalAttentionItem> attention) {
        if (attention.isEmpty()) {
            return List.of();
        }
        List<Caveat> caveats = new ArrayList<>();
        caveats.add(new Caveat("Needs attention", NEEDS_ATTENTION_COPY));
        if (isLateOrStaleCategory(attention.get().category())) {
            caveats.add(new Caveat("Late synced work", LATE_SYNCED_WORK_COPY));
            caveats.add(new Caveat("Stale responsibility", STALE_RESPONSIBILITY_COPY));
        }
        if (!attention.get().assignedToCurrentActor() || attention.get().resolverUnassigned()) {
            caveats.add(new Caveat("Not currently resolvable", NOT_CURRENTLY_RESOLVABLE_COPY));
        }
        return caveats;
    }

    private AttentionStanding attentionStanding(OperationalAttentionItem attention) {
        return new AttentionStanding(
                safeCategoryLabel(attention.category()),
                resolverStanding(attention),
                attention.flaggedAt(),
                isLateOrStaleCategory(attention.category()),
                !attention.assignedToCurrentActor() || attention.resolverUnassigned());
    }

    private String resolverStanding(OperationalAttentionItem attention) {
        if (attention.resolverUnassigned()) {
            return "Not currently resolvable; no designated reviewer is currently assigned.";
        }
        if (attention.assignedToCurrentActor()) {
            return "You are the designated reviewer for this attention item.";
        }
        return "Not currently resolvable for this session; another designated reviewer is assigned.";
    }

    private boolean isLateOrStaleCategory(String category) {
        return "temporal_authority_expired".equals(category)
                || "role_stale".equals(category);
    }

    private WorkSummary workSummary(OperationalWorkEvent work) {
        Event event = work.event();
        UUID subjectId = subjectId(event);
        return new WorkSummary(
                displayName(event.shapeRef(), "Work Item"),
                displayName(event.activityRef(), "Assigned Work"),
                subjectId == null ? "" : subjectId.toString(),
                work.receivedAt(),
                event.timestamp());
    }

    private TraceContext traceContext(List<HandoffWorkItem> items) {
        return items.stream()
                .findFirst()
                .map(item -> new TraceContext(
                        "Current scoped work",
                        item.currentWork().activity(),
                        item.currentWork().receivedAtText(),
                        "/web-admin/operational"))
                .orElse(null);
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

    public record OperationalResponsibilityHandoffContext(
            OffsetDateTime preparedAt,
            String scopedContextCaveat,
            List<String> currentAssignedWork,
            String latestVisibleInputAt,
            List<Caveat> caveats,
            List<HandoffWorkItem> currentWork,
            TraceContext traceContext
    ) {
        public String preparedAtText() {
            return preparedAt.toString();
        }

        public boolean hasAssignedWork() {
            return !currentAssignedWork.isEmpty();
        }

        public boolean hasLatestVisibleInput() {
            return latestVisibleInputAt != null;
        }

        public boolean hasCurrentWork() {
            return !currentWork.isEmpty();
        }

        public boolean hasTraceContext() {
            return traceContext != null;
        }
    }

    public record HandoffWorkItem(
            WorkSummary currentWork,
            WorkSummary priorContext,
            AttentionStanding attention,
            List<Caveat> caveats
    ) {
        public boolean hasPriorContext() {
            return priorContext != null;
        }

        public boolean hasAttention() {
            return attention != null;
        }

        public boolean hasLateSyncedWorkCaveat() {
            return attention != null && attention.lateOrStale();
        }

        public boolean hasStaleResponsibilityCaveat() {
            return attention != null && attention.lateOrStale();
        }

        public boolean hasNotCurrentlyResolvableCaveat() {
            return attention != null && attention.notCurrentlyResolvable();
        }
    }

    public record WorkSummary(
            String workType,
            String activity,
            String subjectId,
            OffsetDateTime receivedAt,
            OffsetDateTime workTime
    ) {
        public String receivedAtText() {
            return receivedAt.toString();
        }

        public String workTimeText() {
            return workTime.toString();
        }
    }

    public record AttentionStanding(
            String reason,
            String reviewerStanding,
            OffsetDateTime flaggedAt,
            boolean lateOrStale,
            boolean notCurrentlyResolvable
    ) {
        public String flaggedAtText() {
            return flaggedAt.toString();
        }
    }

    public record Caveat(String label, String copy) {}

    public record TraceContext(
            String label,
            String activity,
            String receivedAt,
            String path
    ) {}
}
