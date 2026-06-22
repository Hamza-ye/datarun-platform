package dev.datarun.server.authorization;

import dev.datarun.server.event.EventRepository;
import dev.datarun.server.event.EventRepository.OperationalReportActivityStanding;
import dev.datarun.server.event.EventRepository.OperationalScope;
import dev.datarun.server.event.EventRepository.OperationalWorkEvent;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class ScopedOperationalReportSnapshotService {

    static final String SCOPED_VIEW_CAVEAT =
            "Current scoped standing only. Coverage not measured.";

    private final EventRepository eventRepository;
    private final ScopeResolver scopeResolver;

    public ScopedOperationalReportSnapshotService(EventRepository eventRepository,
                                                  ScopeResolver scopeResolver) {
        this.eventRepository = eventRepository;
        this.scopeResolver = scopeResolver;
    }

    public ScopedOperationalReportSnapshot snapshot(UUID actorId) {
        OffsetDateTime snapshotAsOf = OffsetDateTime.now(ZoneOffset.UTC);
        List<ActiveAssignment> assignments = scopeResolver.getActiveAssignments(actorId);
        List<OperationalScope> scopes = operationalScopes(assignments);
        List<ActivityStandingRow> rows = activityStandingRows(actorId, assignments, scopes);
        Optional<OffsetDateTime> latestVisibleInput = rows.stream()
                .map(ActivityStandingRow::latestVisibleInputAt)
                .filter(time -> time != null)
                .max(Comparator.naturalOrder());
        Optional<OffsetDateTime> latestCleanSourceWork = rows.stream()
                .map(ActivityStandingRow::latestCleanSourceWorkAt)
                .filter(time -> time != null)
                .max(Comparator.naturalOrder());
        FreshnessState freshnessState;
        if (latestVisibleInput.isPresent()) {
            freshnessState = FreshnessState.known_latest_input;
        } else if (rows.stream().noneMatch(ActivityStandingRow::hasVisibleSourceWork)) {
            freshnessState = FreshnessState.no_visible_input;
        } else {
            freshnessState = FreshnessState.unknown_latest_input;
        }

        return new ScopedOperationalReportSnapshot(
                snapshotAsOf,
                SCOPED_VIEW_CAVEAT,
                freshnessState,
                latestVisibleInput.map(OffsetDateTime::toString).orElse(null),
                latestCleanSourceWork.map(OffsetDateTime::toString).orElse(null),
                rows,
                total(rows, ActivityStandingRow::cleanSourceCount),
                total(rows, ActivityStandingRow::excludedUnresolvedSourceCount),
                total(rows, ActivityStandingRow::unresolvedIssueCount),
                traceContext(scopes));
    }

    private List<OperationalScope> operationalScopes(List<ActiveAssignment> assignments) {
        return assignments.stream()
                .map(assignment -> new OperationalScope(
                        assignment.geographicPath(),
                        assignment.subjectList(),
                        assignment.activityList()))
                .toList();
    }

    private List<ActivityStandingRow> activityStandingRows(
            UUID actorId,
            List<ActiveAssignment> assignments,
            List<OperationalScope> scopes) {
        Map<String, OperationalReportActivityStanding> standings = new TreeMap<>();
        assignments.stream()
                .map(ActiveAssignment::activityList)
                .filter(activityRefs -> activityRefs != null)
                .flatMap(List::stream)
                .map(this::canonicalActivityRef)
                .filter(activityRef -> activityRef != null)
                .forEach(activityRef -> standings.putIfAbsent(
                        activityRef, zeroActivityStanding(activityRef)));

        eventRepository.findScopedOperationalReportActivityStandings(actorId, scopes)
                .forEach(standing -> {
                    String activityRef = canonicalActivityRef(standing.activityRef());
                    if (activityRef != null) {
                        standings.put(activityRef, standing);
                    }
                });

        return standings.values().stream()
                .map(this::activityStandingRow)
                .toList();
    }

    private OperationalReportActivityStanding zeroActivityStanding(String activityRef) {
        return new OperationalReportActivityStanding(
                activityRef, 0, 0, 0, null, null);
    }

    private ActivityStandingRow activityStandingRow(
            OperationalReportActivityStanding standing) {
        return new ActivityStandingRow(
                displayName(standing.activityRef(), "Assigned Work"),
                standing.cleanSourceCount(),
                standing.excludedUnresolvedSourceCount(),
                standing.unresolvedIssueCount(),
                standing.latestVisibleInputAt(),
                standing.latestVisibleInputAt() == null
                        ? null
                        : standing.latestVisibleInputAt().toString(),
                standing.latestCleanSourceWorkAt(),
                standing.latestCleanSourceWorkAt() == null
                        ? null
                        : standing.latestCleanSourceWorkAt().toString(),
                "Coverage not measured");
    }

    private TraceContext traceContext(List<OperationalScope> scopes) {
        Optional<OperationalWorkEvent> latest =
                eventRepository.findLatestVisibleSubjectWorkEvent(scopes);
        return latest
                .map(work -> new TraceContext(
                        "Latest visible input",
                        displayName(work.event().activityRef(), "Assigned Work"),
                        work.receivedAt().toString(),
                        "/web-admin/operational"))
                .orElse(null);
    }

    private long total(List<ActivityStandingRow> rows, RowCount count) {
        return rows.stream().mapToLong(count::value).sum();
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

    private String canonicalActivityRef(String activityRef) {
        if (activityRef == null) {
            return null;
        }
        String trimmed = activityRef.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        return trimmed;
    }

    private interface RowCount {
        long value(ActivityStandingRow row);
    }

    public enum FreshnessState {
        known_latest_input,
        no_visible_input,
        unknown_latest_input
    }

    public record ScopedOperationalReportSnapshot(
            OffsetDateTime snapshotAsOf,
            String scopedViewCaveat,
            FreshnessState freshnessState,
            String latestVisibleInputAt,
            String latestCleanSourceWorkAt,
            List<ActivityStandingRow> activityRows,
            long visibleCleanSourceCount,
            long excludedUnresolvedSourceCount,
            long unresolvedIssueCount,
            TraceContext traceContext
    ) {
        public String snapshotAsOfText() {
            return snapshotAsOf.toString();
        }

        public boolean hasLatestVisibleInput() {
            return latestVisibleInputAt != null;
        }

        public boolean hasLatestCleanSourceWork() {
            return latestCleanSourceWorkAt != null;
        }

        public boolean hasActivityRows() {
            return !activityRows.isEmpty();
        }

        public boolean hasTraceContext() {
            return traceContext != null;
        }
    }

    public record ActivityStandingRow(
            String activity,
            long cleanSourceCount,
            long excludedUnresolvedSourceCount,
            long unresolvedIssueCount,
            OffsetDateTime latestVisibleInputAt,
            String latestVisibleInputAtText,
            OffsetDateTime latestCleanSourceWorkAt,
            String latestCleanSourceWorkAtText,
            String coverageText
    ) {
        public boolean hasLatestVisibleInput() {
            return latestVisibleInputAt != null;
        }

        public boolean hasLatestCleanSourceWork() {
            return latestCleanSourceWorkAt != null;
        }

        public boolean hasVisibleSourceWork() {
            return cleanSourceCount > 0 || excludedUnresolvedSourceCount > 0;
        }
    }

    public record TraceContext(
            String label,
            String activity,
            String receivedAt,
            String path
    ) {}
}
