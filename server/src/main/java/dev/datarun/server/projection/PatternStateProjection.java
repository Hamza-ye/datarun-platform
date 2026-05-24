package dev.datarun.server.projection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.config.Activity;
import dev.datarun.server.config.ActivityRepository;
import dev.datarun.server.config.PatternRegistry;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rebuildable workflow-pattern state projection.
 *
 * State is derived from events, active activity pattern bindings, and platform
 * pattern definitions. It is not persisted and does not alter sync timelines.
 */
@Component
public class PatternStateProjection {

    private static final Set<String> NON_DOMAIN_SHAPE_PREFIXES = Set.of(
            "conflict_detected/",
            "conflict_resolved/",
            "subjects_merged/",
            "subject_split/"
    );
    // Canonical projection timestamp format: UTC with fixed six-digit microseconds.
    private static final DateTimeFormatter PROJECTION_TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.MICRO_OF_SECOND, 6, 6, true)
            .appendLiteral('Z')
            .toFormatter()
            .withZone(ZoneOffset.UTC);

    private final ActivityRepository activityRepository;
    private final EventRepository eventRepository;
    private final PatternRegistry patternRegistry;
    private final ObjectMapper objectMapper;

    public PatternStateProjection(ActivityRepository activityRepository,
                                  EventRepository eventRepository,
                                  PatternRegistry patternRegistry,
                                  ObjectMapper objectMapper) {
        this.activityRepository = activityRepository;
        this.eventRepository = eventRepository;
        this.patternRegistry = patternRegistry;
        this.objectMapper = objectMapper;
    }

    public ArrayNode projectCurrent(OffsetDateTime asOf) {
        Map<String, JsonNode> activeActivities = new LinkedHashMap<>();
        for (Activity activity : activityRepository.findActive()) {
            activeActivities.put(activity.name(), activity.configJson());
        }
        return project(eventRepository.findAllOrdered(), activeActivities, asOf);
    }

    public ArrayNode project(List<Event> events, Map<String, JsonNode> activityConfigs, OffsetDateTime asOf) {
        Map<String, List<Binding>> bindingsByActivity = parseBindings(activityConfigs);
        Set<String> excludedEventIds = nonAcceptedFlaggedEventIds(events);
        Map<String, StateInstance> states = new LinkedHashMap<>();

        events.stream()
                .sorted(Comparator
                        .comparing((Event e) -> e.syncWatermark() == null ? Long.MAX_VALUE : e.syncWatermark())
                        .thenComparing(Event::timestamp))
                .forEach(event -> {
                    if (excludedEventIds.contains(event.id().toString()) || isProjectionMetadata(event)) {
                        return;
                    }
                    if (event.activityRef() == null) {
                        return;
                    }
                    List<Binding> bindings = bindingsByActivity.getOrDefault(event.activityRef(), List.of());
                    for (Binding binding : bindings) {
                        if ("subject".equals(binding.composition())) {
                            applySubjectEvent(event, binding, states);
                        } else if ("event".equals(binding.composition())) {
                            applyEventEvent(event, binding, states);
                        }
                    }
                });

        ArrayNode output = objectMapper.createArrayNode();
        states.values().stream()
                .sorted(Comparator.comparing(StateInstance::sortKey))
                .map(state -> toJson(state, asOf))
                .forEach(output::add);
        return output;
    }

    private Map<String, List<Binding>> parseBindings(Map<String, JsonNode> activityConfigs) {
        Map<String, List<Binding>> result = new LinkedHashMap<>();
        activityConfigs.forEach((activityRef, config) -> {
            JsonNode pattern = config == null ? null : config.get("pattern");
            if (pattern == null || !pattern.isObject()) {
                return;
            }
            List<Binding> bindings = new ArrayList<>();
            JsonNode subject = pattern.get("subject");
            if (subject != null && subject.isObject()) {
                parseBinding(activityRef, subject, "subject").ifPresent(bindings::add);
            }
            JsonNode eventBindings = pattern.get("event");
            if (eventBindings != null && eventBindings.isArray()) {
                for (JsonNode binding : eventBindings) {
                    if (binding.isObject()) {
                        parseBinding(activityRef, binding, "event").ifPresent(bindings::add);
                    }
                }
            }
            if (!bindings.isEmpty()) {
                result.put(activityRef, bindings);
            }
        });
        return result;
    }

    private java.util.Optional<Binding> parseBinding(String activityRef, JsonNode binding, String expectedComposition) {
        String ref = binding.path("ref").asText(null);
        if (ref == null) {
            return java.util.Optional.empty();
        }
        var definitionOpt = patternRegistry.find(ref);
        if (definitionOpt.isEmpty() || !definitionOpt.get().bindingEnabled()) {
            return java.util.Optional.empty();
        }
        String composition = binding.path("composition").asText(null);
        if (!expectedComposition.equals(composition)) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(new Binding(
                activityRef,
                ref,
                composition,
                definitionOpt.get().definitionJson(),
                reverseRoleMap(binding.get("shape_roles")),
                reverseRoleMap(binding.get("activation_roles")),
                binding.path("parameters")
        ));
    }

    private Map<String, String> reverseRoleMap(JsonNode roles) {
        Map<String, String> result = new LinkedHashMap<>();
        if (roles == null || !roles.isObject()) {
            return result;
        }
        roles.fields().forEachRemaining(entry -> {
            if (entry.getValue().isArray()) {
                for (JsonNode shapeRef : entry.getValue()) {
                    if (shapeRef.isTextual()) {
                        result.put(shapeRef.asText(), entry.getKey());
                    }
                }
            }
        });
        return result;
    }

    private Set<String> nonAcceptedFlaggedEventIds(List<Event> events) {
        Map<String, String> flagToSource = new HashMap<>();
        Map<String, Set<String>> flagsBySource = new HashMap<>();
        Set<String> acceptedFlagIds = new LinkedHashSet<>();
        Set<String> acceptedSourcesWithoutFlagIds = new LinkedHashSet<>();
        for (Event event : events) {
            if (isIntegrityFlag(event)) {
                String sourceId = text(event.payload(), "source_event_id");
                if (sourceId != null) {
                    flagToSource.put(event.id().toString(), sourceId);
                    flagsBySource.computeIfAbsent(sourceId, ignored -> new LinkedHashSet<>())
                            .add(event.id().toString());
                }
            }
        }
        for (Event event : events) {
            if (isIntegrityResolution(event) && "accepted".equals(text(event.payload(), "resolution"))) {
                String sourceId = text(event.payload(), "source_event_id");
                String flagEventId = text(event.payload(), "flag_event_id");
                if (flagEventId != null) {
                    acceptedFlagIds.add(flagEventId);
                    if (sourceId == null) {
                        sourceId = flagToSource.get(flagEventId);
                    }
                }
                if (sourceId != null && flagEventId == null) {
                    acceptedSourcesWithoutFlagIds.add(sourceId);
                }
            }
        }
        Set<String> excluded = new LinkedHashSet<>();
        flagsBySource.forEach((sourceId, flagIds) -> {
            if (acceptedSourcesWithoutFlagIds.contains(sourceId)) {
                return;
            }
            boolean allAccepted = flagIds.stream().allMatch(acceptedFlagIds::contains);
            if (!allAccepted) {
                excluded.add(sourceId);
            }
        });
        return excluded;
    }

    private void applySubjectEvent(Event event, Binding binding, Map<String, StateInstance> states) {
        String shapeRole = binding.shapeRole(event.shapeRef());
        if (shapeRole == null) {
            return;
        }
        String key = subjectKey(event, binding);
        StateInstance current = states.get(key);
        JsonNode transition = matchingTransition(event, binding, current, shapeRole, null);
        if (transition == null) {
            return;
        }
        StateInstance next = current == null
                ? StateInstance.subject(key, event.subjectRef().deepCopy(), binding)
                : current;
        applyTransition(next, event, binding, transition, shapeRole);
        states.put(key, next);
    }

    private void applyEventEvent(Event event, Binding binding, Map<String, StateInstance> states) {
        String activationRole = binding.activationRole(event.shapeRef());
        String shapeRole = binding.shapeRole(event.shapeRef());
        String sourceEventId = activationRole != null ? event.id().toString() : sourceEventId(event.payload());
        if (sourceEventId == null && shapeRole != null) {
            sourceEventId = event.id().toString();
        }
        if (sourceEventId == null) {
            return;
        }

        String key = eventKey(sourceEventId, binding);
        StateInstance current = states.get(key);
        JsonNode transition = matchingTransition(event, binding, current, shapeRole, activationRole);
        if (transition == null) {
            return;
        }
        StateInstance next = current == null
                ? StateInstance.event(key, sourceEventId, event.subjectRef().deepCopy(), binding)
                : current;
        applyTransition(next, event, binding, transition, shapeRole);
        states.put(key, next);
    }

    private JsonNode matchingTransition(Event event,
                                        Binding binding,
                                        StateInstance current,
                                        String shapeRole,
                                        String activationRole) {
        JsonNode transitions = binding.definition().get("transitions");
        if (transitions == null || !transitions.isArray()) {
            return null;
        }
        for (JsonNode transition : transitions) {
            if (!event.type().equals(transition.path("event_type").asText())) {
                continue;
            }
            if (transition.has("activation_role")) {
                if (activationRole == null || !activationRole.equals(transition.path("activation_role").asText())) {
                    continue;
                }
            } else if (transition.has("shape_role")) {
                if (shapeRole == null || !shapeRole.equals(transition.path("shape_role").asText())) {
                    continue;
                }
            } else {
                continue;
            }
            if (!fromMatches(transition.get("from"), current)) {
                continue;
            }
            if (transition.path("requires_existing_instance").asBoolean(false) && current == null) {
                continue;
            }
            if (!branchMatches(transition.get("branch"), event.payload(), current, binding)) {
                continue;
            }
            return transition;
        }
        return null;
    }

    private boolean fromMatches(JsonNode from, StateInstance current) {
        if (from == null || from.isNull()) {
            return current == null;
        }
        if (current == null || current.currentState == null) {
            return false;
        }
        if (from.isTextual()) {
            return "any".equals(from.asText()) || current.currentState.equals(from.asText());
        }
        if (from.isArray()) {
            for (JsonNode item : from) {
                if (item.isTextual() && current.currentState.equals(item.asText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean branchMatches(JsonNode branch, JsonNode payload, StateInstance current, Binding binding) {
        if (branch == null || branch.isNull()) {
            return true;
        }
        JsonNode field = branch.get("field");
        if (field != null && field.isTextual() && branch.has("equals")) {
            JsonNode actual = payload == null ? null : payload.get(field.asText());
            JsonNode expected = branch.get("equals");
            if (actual == null || !actual.equals(expected)) {
                return false;
            }
        }
        JsonNode whenLevel = branch.get("when_level");
        if (whenLevel != null && whenLevel.isTextual()) {
            int currentLevel = current == null ? -1 : current.intAttribute("level", -1);
            int levels = binding.parameters().path("levels").asInt(-1);
            JsonNode eventLevel = payload == null ? null : payload.get("level");
            if (eventLevel != null && eventLevel.canConvertToInt() && eventLevel.asInt() != currentLevel) {
                return false;
            }
            if ("less_than_levels".equals(whenLevel.asText())) {
                return currentLevel > 0 && levels > 0 && currentLevel < levels;
            }
            if ("equals_levels".equals(whenLevel.asText())) {
                return currentLevel > 0 && levels > 0 && currentLevel == levels;
            }
        }
        return true;
    }

    private void applyTransition(StateInstance state,
                                 Event event,
                                 Binding binding,
                                 JsonNode transition,
                                 String shapeRole) {
        String previousState = state.currentState;
        OffsetDateTime previousPendingSince = state.pendingSince;
        String to = transition.path("to").asText();
        String nextState = "same".equals(to) ? previousState : to;
        Map<String, Object> nextAttributes = new LinkedHashMap<>(state.attributes);
        JsonNode attributes = transition.get("attributes");
        if (attributes != null && attributes.isObject()) {
            attributes.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                if (value.isTextual() && "current_plus_1".equals(value.asText())) {
                    nextAttributes.put(entry.getKey(), state.intAttribute(entry.getKey(), 0) + 1);
                } else if (value.isIntegralNumber()) {
                    nextAttributes.put(entry.getKey(), value.asInt());
                } else if (value.isTextual()) {
                    nextAttributes.put(entry.getKey(), value.asText());
                }
            });
        }

        state.currentState = nextState;
        state.attributes.clear();
        state.attributes.putAll(nextAttributes);
        boolean stateChanging = transition.path("effect").asText("").startsWith("SC");
        if (previousPendingSince == null || stateChanging) {
            state.pendingSince = event.timestamp();
        }
        applyPatternSpecific(state, event, binding, shapeRole, previousState);
    }

    private void applyPatternSpecific(StateInstance state,
                                      Event event,
                                      Binding binding,
                                      String shapeRole,
                                      String previousState) {
        switch (binding.ref()) {
            case "capture_with_review/v1" -> {
                if ("review_decision".equals(shapeRole)) {
                    String decision = text(event.payload(), "decision");
                    if (decision != null) {
                        state.patternSpecific.put("latest_review_outcome", decision);
                    }
                }
                state.unsupportedPatternSpecific.add("pending_review_count");
                state.unsupportedPatternSpecific.add("accepted_count");
                state.unsupportedPatternSpecific.add("returned_count");
            }
            case "multi_step_approval/v1" -> {
                if ("submission".equals(shapeRole) && "capture".equals(event.type())) {
                    state.patternSpecific.put("submission_count",
                            ((Number) state.patternSpecific.getOrDefault("submission_count", 0)).intValue() + 1);
                    state.patternSpecific.put("approval_chain", objectMapper.createArrayNode());
                }
                if ("level_decision".equals(shapeRole) && "review".equals(event.type())) {
                    ArrayNode chain = (ArrayNode) state.patternSpecific.computeIfAbsent(
                            "approval_chain", ignored -> objectMapper.createArrayNode());
                    ObjectNode item = objectMapper.createObjectNode();
                    item.put("level", event.payload().path("level").asInt(state.intAttribute("level", 1)));
                    item.set("actor_ref", event.actorRef().deepCopy());
                    item.put("decision", text(event.payload(), "decision"));
                    item.put("timestamp", formatProjectionTimestamp(event.timestamp()));
                    chain.add(item);
                }
                if ("pending".equals(state.currentState)) {
                    state.patternSpecific.put("current_level", state.intAttribute("level", 1));
                } else {
                    state.patternSpecific.put("current_level", null);
                }
                state.patternSpecific.put("time_at_current_level", "pending".equals(state.currentState));
            }
            case "transfer_with_acknowledgment/v1" -> {
                if ("dispatch".equals(shapeRole)) {
                    state.attributes.put("dispatch_timestamp", event.timestamp());
                }
                if ("receipt".equals(shapeRole)) {
                    Object dispatchTimestamp = state.attributes.get("dispatch_timestamp");
                    if (dispatchTimestamp instanceof OffsetDateTime ts) {
                        long seconds = Duration.between(ts, event.timestamp()).getSeconds();
                        state.patternSpecific.put("time_in_transit", Math.max(0, seconds));
                    }
                }
                state.unsupportedPatternSpecific.add("items_dispatched");
                state.unsupportedPatternSpecific.add("items_received");
                state.unsupportedPatternSpecific.add("discrepancy_summary");
            }
            default -> {
            }
        }
    }

    private ObjectNode toJson(StateInstance state, OffsetDateTime asOf) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("composition", state.composition);
        ObjectNode key = node.putObject("state_key");
        if ("subject".equals(state.composition)) {
            key.set("subject_ref", state.subjectRef.deepCopy());
            key.put("activity_ref", state.binding.activityRef());
        } else {
            key.put("source_event_id", state.sourceEventId);
        }
        key.put("binding_ref", state.binding.ref());
        node.put("current_state", state.currentState);
        node.put("pending_since", formatProjectionTimestamp(state.pendingSince));
        node.put("time_in_state", Math.max(0, Duration.between(state.pendingSince, asOf).getSeconds()));
        ObjectNode specific = node.putObject("pattern_specific");
        state.patternSpecific.forEach((name, value) -> putValue(specific, name, value, state, asOf));
        if (!state.unsupportedPatternSpecific.isEmpty()) {
            ArrayNode unsupported = node.putArray("unsupported_pattern_specific_fields");
            state.unsupportedPatternSpecific.stream().sorted().forEach(unsupported::add);
        }
        return node;
    }

    private void putValue(ObjectNode target, String name, Object value, StateInstance state, OffsetDateTime asOf) {
        if ("time_at_current_level".equals(name) && value instanceof Boolean include) {
            if (include && state.pendingSince != null) {
                target.put(name, Math.max(0, Duration.between(state.pendingSince, asOf).getSeconds()));
            }
            return;
        }
        if (value == null) {
            target.putNull(name);
        } else if (value instanceof Integer i) {
            target.put(name, i);
        } else if (value instanceof Long l) {
            target.put(name, l);
        } else if (value instanceof String s) {
            target.put(name, s);
        } else if (value instanceof JsonNode json) {
            target.set(name, json.deepCopy());
        }
    }

    private static boolean isProjectionMetadata(Event event) {
        return NON_DOMAIN_SHAPE_PREFIXES.stream().anyMatch(prefix -> event.shapeRef().startsWith(prefix));
    }

    private static boolean isIntegrityFlag(Event event) {
        return event.shapeRef().startsWith("conflict_detected/");
    }

    private static boolean isIntegrityResolution(Event event) {
        return event.shapeRef().startsWith("conflict_resolved/");
    }

    private static String subjectKey(Event event, Binding binding) {
        return "subject|" + event.subjectRef().path("type").asText()
                + "|" + event.subjectRef().path("id").asText()
                + "|" + binding.activityRef()
                + "|" + binding.ref();
    }

    private static String eventKey(String sourceEventId, Binding binding) {
        return "event|" + sourceEventId + "|" + binding.ref();
    }

    private static String sourceEventId(JsonNode payload) {
        String source = text(payload, "source_event_id");
        if (source != null) {
            return source;
        }
        JsonNode ref = payload == null ? null : payload.get("source_event_ref");
        if (ref == null || ref.isNull()) {
            return null;
        }
        if (ref.isTextual()) {
            return ref.asText();
        }
        return text(ref, "id");
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value != null && value.isTextual() ? value.asText() : null;
    }

    private static String formatProjectionTimestamp(OffsetDateTime timestamp) {
        return PROJECTION_TIMESTAMP_FORMAT.format(timestamp.toInstant());
    }

    private record Binding(
            String activityRef,
            String ref,
            String composition,
            JsonNode definition,
            Map<String, String> shapeRolesByRef,
            Map<String, String> activationRolesByRef,
            JsonNode parameters
    ) {
        String shapeRole(String shapeRef) {
            return shapeRolesByRef.get(shapeRef);
        }

        String activationRole(String shapeRef) {
            return activationRolesByRef.get(shapeRef);
        }
    }

    private static class StateInstance {
        private final String key;
        private final String composition;
        private final String sourceEventId;
        private final JsonNode subjectRef;
        private final Binding binding;
        private String currentState;
        private OffsetDateTime pendingSince;
        private final Map<String, Object> attributes = new LinkedHashMap<>();
        private final Map<String, Object> patternSpecific = new LinkedHashMap<>();
        private final Set<String> unsupportedPatternSpecific = new LinkedHashSet<>();

        private StateInstance(String key, String composition, String sourceEventId, JsonNode subjectRef, Binding binding) {
            this.key = key;
            this.composition = composition;
            this.sourceEventId = sourceEventId;
            this.subjectRef = subjectRef;
            this.binding = binding;
        }

        static StateInstance subject(String key, JsonNode subjectRef, Binding binding) {
            return new StateInstance(key, "subject", null, subjectRef, binding);
        }

        static StateInstance event(String key, String sourceEventId, JsonNode subjectRef, Binding binding) {
            return new StateInstance(key, "event", sourceEventId, subjectRef, binding);
        }

        String sortKey() {
            return key;
        }

        int intAttribute(String name, int fallback) {
            Object value = attributes.get(name);
            return value instanceof Number number ? number.intValue() : fallback;
        }
    }
}
