package dev.datarun.ship1.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
import dev.datarun.ship1.event.ServerEmission;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Push-path alias-cycle guard. Implementation of
 * {@code docs/architecture/cycle-guard-contract.md} (authority: ADR-006-R §S5).
 *
 * <p>Builds a directed graph over subject UUIDs from {@code subjects_merged/v1} +
 * {@code subject_split/v1} events (persisted ∪ in-flight earlier-in-batch) and
 * detects cycle-closing alias edges before persistence. Accept-and-flag is
 * preserved: the triggering alias event is always inserted by the caller; this
 * guard returns verdicts and emits a {@code conflict_detected/v1} flag with
 * {@code payload.flag_category=cycle_violation} alongside.
 *
 * <p>Asymmetry vs. {@link ConflictDetector}: that component is post-persist and
 * per-event (over a fully-committed graph); this component is pre-persist and
 * batch-serial (over a graph that includes earlier-in-batch already-accepted
 * edges). See contract §3.2 for why conflating them would dilute both.
 *
 * <p>Flag construction is inline rather than reusing
 * {@link ConflictDetector}'s {@code buildFlag} because that helper hardcodes the
 * {@code system:conflict_detector/} actor-ref prefix in two places; the cycle
 * guard requires {@code system:cycle_guard/cycle_violation}. Lifting would
 * require parameterizing two existing call sites; inline mirrors the pattern
 * with strictly smaller blast radius. Permitted by contract §5 option (b).
 */
@Component
public class CycleGuard {

    private static final String MERGE_PREFIX = "subjects_merged/";
    private static final String SPLIT_PREFIX = "subject_split/";
    private static final String FLAG_SHAPE = "conflict_detected/v1";
    private static final String SYSTEM_ACTOR = "system:cycle_guard/cycle_violation";

    private final EventRepository events;
    private final ServerEmission serverEmission;
    private final ObjectMapper mapper;

    public CycleGuard(EventRepository events, ServerEmission serverEmission, ObjectMapper mapper) {
        this.events = events;
        this.serverEmission = serverEmission;
        this.mapper = mapper;
    }

    /**
     * Returns a map {triggering-event-id → canonical cycle_path} for every alias event
     * in {@code batch} whose emitted edges would close a cycle in the union graph
     * (persisted ∪ earlier-in-batch). Non-alias events and clean alias events are
     * absent from the returned map.
     *
     * <p>Per contract §4.1, an alias event is cycle-positive if ANY of its emitted
     * edges closes a cycle; the first found cycle_path is recorded; remaining edges
     * are still added to the graph (they may inform later-in-batch verdicts).
     */
    public Map<UUID, List<UUID>> checkBatch(List<Event> batch) {
        Map<UUID, Set<UUID>> graph = new HashMap<>();
        for (Event e : events.findByShapeRefPrefix(MERGE_PREFIX)) addEdges(graph, e);
        for (Event e : events.findByShapeRefPrefix(SPLIT_PREFIX)) addEdges(graph, e);

        Map<UUID, List<UUID>> verdicts = new LinkedHashMap<>();
        for (Event e : batch) {
            List<UUID[]> edges = projectEdges(e);
            if (edges.isEmpty()) continue;
            List<UUID> firstCycle = null;
            for (UUID[] edge : edges) {
                UUID from = edge[0], to = edge[1];
                if (firstCycle == null) {
                    firstCycle = detectCycle(graph, from, to);
                }
                addEdge(graph, from, to);
            }
            if (firstCycle != null) verdicts.put(e.id(), firstCycle);
        }
        return verdicts;
    }

    /**
     * Build and insert one {@code conflict_detected/v1} flag for the triggering
     * event. Caller must invoke AFTER the triggering event itself is persisted
     * so {@code payload.source_event_id} references an existing row.
     */
    public Event emitCycleFlag(Event triggering, List<UUID> cyclePath) {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("source_event_id", triggering.id().toString());
        payload.put("flag_category", "cycle_violation");
        payload.put("resolvability", "manual_only");
        payload.put("reason", "alias edge would close a cycle in the subject identity graph");
        ObjectNode designated = payload.putObject("designated_resolver");
        designated.put("type", "actor");
        designated.put("id", SYSTEM_ACTOR);
        ArrayNode arr = payload.putArray("cycle_path");
        for (UUID node : cyclePath) arr.add(node.toString());

        Event flag = new Event(
                UUID.randomUUID(),
                "alert",
                FLAG_SHAPE,
                null,
                triggering.subjectType(),
                triggering.subjectId(),
                SYSTEM_ACTOR,
                serverEmission.serverDeviceId(),
                serverEmission.nextServerDeviceSeq(),
                null,
                OffsetDateTime.now(),
                payload);
        events.insert(flag);
        return flag;
    }

    // ---------------------------------------------------------------- internals

    private static boolean isMerge(Event e) {
        return e.shapeRef() != null && e.shapeRef().startsWith(MERGE_PREFIX);
    }

    private static boolean isSplit(Event e) {
        return e.shapeRef() != null && e.shapeRef().startsWith(SPLIT_PREFIX);
    }

    /** Project an alias event into its directed edges per contract §3.3. */
    private static List<UUID[]> projectEdges(Event e) {
        if (isMerge(e)) {
            UUID from = optUuid(e.payload().path("retired_id").asText(null));
            UUID to = optUuid(e.payload().path("surviving_id").asText(null));
            if (from == null || to == null) return List.of();
            return Collections.singletonList(new UUID[]{from, to});
        }
        if (isSplit(e)) {
            UUID from = optUuid(e.payload().path("source_id").asText(null));
            JsonNode succ = e.payload().path("successor_ids");
            if (from == null || !succ.isArray()) return List.of();
            List<UUID[]> out = new ArrayList<>(succ.size());
            for (JsonNode s : succ) {
                UUID to = optUuid(s.asText(null));
                if (to != null) out.add(new UUID[]{from, to});
            }
            return out;
        }
        return List.of();
    }

    private static void addEdges(Map<UUID, Set<UUID>> graph, Event e) {
        for (UUID[] edge : projectEdges(e)) addEdge(graph, edge[0], edge[1]);
    }

    private static void addEdge(Map<UUID, Set<UUID>> graph, UUID from, UUID to) {
        graph.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    /**
     * DFS from {@code to} seeking {@code from} in the union graph. Returns the
     * canonical cycle_path {@code [to, intermediate..., from, to]} per contract
     * §4.3, or null if no cycle would close.
     */
    private static List<UUID> detectCycle(Map<UUID, Set<UUID>> graph, UUID from, UUID to) {
        if (Objects.equals(from, to)) return List.of(from, to); // self-loop

        Deque<List<UUID>> stack = new ArrayDeque<>();
        Set<UUID> visited = new HashSet<>();
        List<UUID> seed = new ArrayList<>();
        seed.add(to);
        stack.push(seed);
        visited.add(to);

        while (!stack.isEmpty()) {
            List<UUID> path = stack.pop();
            UUID node = path.get(path.size() - 1);
            Set<UUID> outgoing = graph.get(node);
            if (outgoing == null) continue;
            for (UUID next : outgoing) {
                if (Objects.equals(next, from)) {
                    List<UUID> result = new ArrayList<>(path);
                    result.add(from);
                    result.add(to);
                    return result;
                }
                if (visited.add(next)) {
                    List<UUID> ext = new ArrayList<>(path);
                    ext.add(next);
                    stack.push(ext);
                }
            }
        }
        return null;
    }

    private static UUID optUuid(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return UUID.fromString(s); } catch (IllegalArgumentException ex) { return null; }
    }
}
