package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datarun.server.config.ActivityService;
import dev.datarun.server.config.Shape;
import dev.datarun.server.config.ShapeService;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.event.EventRepository.OperationalReportActivityStanding;
import dev.datarun.server.event.EventRepository.OperationalScope;
import dev.datarun.server.event.EventRepository.OperationalWorkEvent;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
    static final String CONFIGURED_WORK_EVIDENCE_CAVEAT =
            "Visible through current assignment scope. Latest synced/received "
                    + "time is scoped and not a guarantee.";
    static final String NO_VISIBLE_CONFIGURED_WORK_EVIDENCE =
            "No visible configured work evidence is available for this session.";
    static final String SENSITIVE_FIELD_VALUES_SUPPRESSED =
            "Configured field values are suppressed for this evidence.";

    private final EventRepository eventRepository;
    private final ScopeResolver scopeResolver;
    private final ShapeService shapeService;
    private final ActivityService activityService;

    public ScopedOperationalReportSnapshotService(EventRepository eventRepository,
                                                  ScopeResolver scopeResolver,
                                                  ShapeService shapeService,
                                                  ActivityService activityService) {
        this.eventRepository = eventRepository;
        this.scopeResolver = scopeResolver;
        this.shapeService = shapeService;
        this.activityService = activityService;
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

    public ConfiguredWorkEvidence configuredWorkEvidence(UUID actorId, UUID eventId) {
        List<OperationalScope> scopes = operationalScopes(
                scopeResolver.getActiveAssignments(actorId));
        if (scopes.isEmpty() || eventId == null) {
            return ConfiguredWorkEvidence.notVisible(NO_VISIBLE_CONFIGURED_WORK_EVIDENCE);
        }
        return eventRepository.findScopedVisibleWorkEvent(eventId, scopes)
                .map(this::configuredWorkEvidence)
                .orElseGet(() -> ConfiguredWorkEvidence.notVisible(
                        NO_VISIBLE_CONFIGURED_WORK_EVIDENCE));
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

    private ConfiguredWorkEvidence configuredWorkEvidence(
            OperationalWorkEvent work) {
        Optional<Shape> shape = configuredShape(work.event().shapeRef());
        boolean canRenderFieldValues = canRenderFieldValues(
                work.event().activityRef(), shape);
        return ConfiguredWorkEvidence.visible(
                activityLabel(work.event().activityRef()),
                work.event().activityRef(),
                recordTypeLabel(work.event().shapeRef(), shape),
                work.event().shapeRef(),
                canRenderFieldValues
                        ? configuredFieldValues(shape, work.event().payload())
                        : List.of(),
                canRenderFieldValues,
                canRenderFieldValues ? null : SENSITIVE_FIELD_VALUES_SUPPRESSED,
                safeText(work.receivedAt() == null
                        ? null
                        : work.receivedAt().toString()),
                CONFIGURED_WORK_EVIDENCE_CAVEAT);
    }

    private String activityLabel(String activityRef) {
        if (activityRef == null || activityRef.isBlank()) {
            return "Assigned Work";
        }
        return activityService.getActivity(activityRef)
                .map(activity -> configuredLabel(activity.configJson()))
                .filter(label -> !label.isBlank())
                .orElseGet(() -> displayName(activityRef, "Assigned Work"));
    }

    private String recordTypeLabel(String shapeRef, Optional<Shape> shape) {
        return shape
                .map(Shape::schemaJson)
                .map(this::configuredLabel)
                .filter(label -> !label.isBlank())
                .orElseGet(() -> displayName(shapeRef, "Configured Record"));
    }

    private List<ConfiguredWorkFieldValue> configuredFieldValues(
            Optional<Shape> shape, JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            return List.of();
        }
        if (shape.isEmpty()) {
            return List.of();
        }

        JsonNode fields = shape.get().schemaJson().path("fields");
        if (!fields.isArray()) {
            return List.of();
        }

        List<JsonNode> configuredFields = new ArrayList<>();
        fields.forEach(configuredFields::add);
        configuredFields.sort(Comparator
                .comparingInt((JsonNode field) ->
                        field.path("display_order").asInt(Integer.MAX_VALUE))
                .thenComparing(field -> field.path("name").asText("")));

        return configuredFields.stream()
                .filter(field -> {
                    String name = field.path("name").asText(null);
                    return name != null && !name.isBlank();
                })
                .map(field -> {
                    String name = field.path("name").asText();
                    return new ConfiguredWorkFieldValue(
                            fieldLabel(field),
                            payloadValueText(payload.get(name), field));
                })
                .toList();
    }

    private Optional<Shape> configuredShape(String shapeRef) {
        if (ShapeService.isPlatformShapeRef(shapeRef)) {
            return Optional.empty();
        }
        String[] parsed = ShapeService.parseShapeRef(shapeRef);
        if (parsed == null) {
            return Optional.empty();
        }
        return shapeService.getShape(parsed[0], Integer.parseInt(parsed[1]));
    }

    private boolean hasConfiguredShape(String shapeRef) {
        return configuredShape(shapeRef).isPresent();
    }

    private boolean canRenderFieldValues(String activityRef, Optional<Shape> shape) {
        if (shape.isEmpty() || !isStandardSensitivity(shape.get().sensitivity())) {
            return false;
        }
        return activityService.getActivity(activityRef)
                .map(activity -> isStandardSensitivity(activity.sensitivity()))
                .orElse(false);
    }

    private boolean isStandardSensitivity(String sensitivity) {
        return sensitivity == null || "standard".equalsIgnoreCase(sensitivity);
    }

    private String configuredLabel(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        for (String key : List.of("label", "display_label", "displayName", "title")) {
            String label = node.path(key).asText("");
            if (!label.isBlank()) {
                return label;
            }
        }
        return "";
    }

    private String fieldLabel(JsonNode field) {
        String configured = configuredLabel(field);
        if (!configured.isBlank()) {
            return configured;
        }
        String name = field.path("name").asText("");
        return name.isBlank() ? "Configured field" : name;
    }

    private String payloadValueText(JsonNode value, JsonNode field) {
        if (value == null || value.isMissingNode() || value.isNull()) {
            return "Not recorded";
        }
        if (isScalarValue(value)) {
            return safeText(value.asText());
        }
        if (value.isArray()) {
            return isMultiSelectField(field)
                    ? multiSelectValueText(value)
                    : "Unsupported value";
        }
        return "Unsupported value";
    }

    private boolean isScalarValue(JsonNode value) {
        return value.isTextual() || value.isNumber() || value.isBoolean();
    }

    private boolean isMultiSelectField(JsonNode field) {
        return "multi_select".equals(field.path("type").asText(""));
    }

    private String multiSelectValueText(JsonNode value) {
        List<String> values = new ArrayList<>();
        for (JsonNode item : value) {
            if (item == null || item.isMissingNode() || item.isNull()) {
                continue;
            }
            if (!isScalarValue(item)) {
                return "Unsupported value";
            }
            values.add(safeText(item.asText()));
        }
        return values.isEmpty() ? "Not recorded" : String.join(", ", values);
    }

    private TraceContext traceContext(List<OperationalScope> scopes) {
        Optional<OperationalWorkEvent> latest =
                eventRepository.findLatestVisibleSubjectWorkEvent(scopes);
        return latest
                .map(work -> new TraceContext(
                        work.event().id(),
                        "Latest visible input",
                        displayName(work.event().activityRef(), "Assigned Work"),
                        work.receivedAt().toString(),
                        hasConfiguredShape(work.event().shapeRef())))
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

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "Not recorded";
        }
        return value;
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

        public boolean hasConfiguredWorkEvidenceTarget() {
            return traceContext != null && traceContext.hasConfiguredWorkEvidenceTarget();
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

    public record ConfiguredWorkEvidence(
            boolean visible,
            String emptyText,
            String activity,
            String activityRef,
            String recordType,
            String shapeRef,
            List<ConfiguredWorkFieldValue> fieldValues,
            boolean fieldValuesVisible,
            String fieldValuesSuppressedText,
            String latestSyncedReceived,
            String visibilityText
    ) {
        static ConfiguredWorkEvidence notVisible(String emptyText) {
            return new ConfiguredWorkEvidence(
                    false, emptyText, null, null, null, null, List.of(),
                    false, null, null, null);
        }

        static ConfiguredWorkEvidence visible(
                String activity,
                String activityRef,
                String recordType,
                String shapeRef,
                List<ConfiguredWorkFieldValue> fieldValues,
                boolean fieldValuesVisible,
                String fieldValuesSuppressedText,
                String latestSyncedReceived,
                String visibilityText) {
            return new ConfiguredWorkEvidence(
                    true, null, activity, activityRef, recordType, shapeRef,
                    fieldValues, fieldValuesVisible, fieldValuesSuppressedText,
                    latestSyncedReceived, visibilityText);
        }

        public boolean hasFieldValues() {
            return !fieldValues.isEmpty();
        }

        public boolean hasSuppressedFieldValues() {
            return fieldValuesSuppressedText != null;
        }
    }

    public record ConfiguredWorkFieldValue(
            String label,
            String value
    ) {}

    public record TraceContext(
            UUID eventId,
            String label,
            String activity,
            String receivedAt,
            boolean hasConfiguredWorkEvidenceTarget
    ) {}
}
