package dev.datarun.ship1.admin;

import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * On-demand alias projection from {@code subjects_merged/v1} events
 * (Ship-2 §6 commitment 5; ADR-002 §S6 — alias-in-projection, never re-reference).
 *
 * <p>NO TABLE. NO CACHE. Same shape as {@link SubjectLifecycleProjector} and
 * {@link dev.datarun.ship1.scope.ScopeResolver}: read the relevant event slice,
 * fold to the alias map, return. FP-002 path (a) — locked at Ship-2 spec close.
 *
 * <h3>Read-only, never re-references events</h3>
 * Historical events for a retired subject remain present with their original
 * {@code subject_id} (raw, not rewritten — §S6, W-3). This projector exposes a
 * <em>parallel</em> resolver; consumers that need canonical ids call
 * {@link #canonicalId(UUID)}. The Conflict Detector remains alias-blind by
 * construction (§S13) — it never calls into this class.
 *
 * <h3>Eager transitive closure</h3>
 * For raw merge edges {A→B, B→C}, this projector returns A→C and B→C in a single
 * hop. Consumers do not chain-walk. Closure is computed as a fixpoint over the
 * raw edge map after all events are folded — not by mutating a "current canonical"
 * during the sweep — so the result is deterministic regardless of the order in
 * which events are presented (modulo the partial order already enforced by
 * {@code sync_watermark}).
 *
 * <h3>Concurrent-merge race (R2)</h3>
 * If a {@code retired_id} appears as the retired side of two distinct merge
 * events (a "duplicate merge" — impossible under Ship-2's single-coordinator
 * online-only flow but defensively handled), the edge with the lower
 * {@code sync_watermark} wins. R2 is carried forward to Ship-5; this projector
 * does not flag the duplicate.
 */
@Component
public class SubjectAliasProjector {

    private final EventRepository events;

    public SubjectAliasProjector(EventRepository events) {
        this.events = events;
    }

    /**
     * Resolves {@code id} to its ultimate-surviving id under eager transitive
     * closure. If {@code id} is not the retired side of any merge event, returns
     * {@code id} itself. Never returns null.
     *
     * <p>Chosen over {@code Optional<UUID>} because (a) W-3 always wants a usable
     * id back and (b) we cannot cheaply distinguish "known canonical" from
     * "unknown id" without scanning the entire subject-bearing event corpus,
     * which would be a much heavier projection than this slice needs.
     */
    public UUID canonicalId(UUID id) {
        if (id == null) return null;
        return aliasMap().getOrDefault(id, id);
    }

    /**
     * Number of merge edges traversed from {@code id} to its canonical id.
     * Returns 0 if {@code id} is not retired (not in any merge event).
     * For the chain A→B→C, {@code aliasChainLength(A) == 2}, {@code (B) == 1},
     * {@code (C) == 0}.
     */
    public int aliasChainLength(UUID id) {
        if (id == null) return 0;
        Map<UUID, UUID> raw = rawEdges(events.findByShapeRefPrefix("subjects_merged/"));
        int hops = 0;
        UUID current = id;
        Set<UUID> visited = new HashSet<>();
        while (raw.containsKey(current)) {
            if (!visited.add(current)) break; // defensive cycle guard (impossible by §S9)
            current = raw.get(current);
            hops++;
        }
        return hops;
    }

    /** Closure map: {@code retired_id → ultimate_surviving_id}, eager. */
    public Map<UUID, UUID> aliasMap() {
        return buildAliasMap(events.findByShapeRefPrefix("subjects_merged/"));
    }

    // -------------------------------------------------------------- internals

    /**
     * Pure function: builds the eager-closure alias map from a list of
     * {@code subjects_merged/v1} events. Order-independent.
     *
     * <p>Step 1 — collect raw edges in {@code sync_watermark} order, oldest wins
     * on duplicate {@code retired_id} (R2; defensive). Step 2 — compute closure
     * as a fixpoint over the raw-edge map.
     */
    static Map<UUID, UUID> buildAliasMap(List<Event> mergeEvents) {
        Map<UUID, UUID> raw = rawEdges(mergeEvents);
        Map<UUID, UUID> closure = new HashMap<>(raw.size());
        for (Map.Entry<UUID, UUID> entry : raw.entrySet()) {
            UUID start = entry.getKey();
            UUID current = entry.getValue();
            Set<UUID> visited = new HashSet<>();
            visited.add(start);
            while (raw.containsKey(current)) {
                if (!visited.add(current)) break; // defensive cycle guard
                current = raw.get(current);
            }
            closure.put(start, current);
        }
        return closure;
    }

    /**
     * Raw {@code retired_id → surviving_id} edges, sorted by {@code sync_watermark}
     * ascending so that on duplicate-retired (R2) the earlier edge wins
     * deterministically. Server-authored events without a watermark sort first.
     */
    private static Map<UUID, UUID> rawEdges(List<Event> mergeEvents) {
        List<Event> sorted = new ArrayList<>(mergeEvents);
        sorted.sort(Comparator.comparingLong(e ->
                e.syncWatermark() == null ? Long.MIN_VALUE : e.syncWatermark()));
        Map<UUID, UUID> raw = new HashMap<>();
        for (Event e : sorted) {
            UUID retired = parseUuid(e.payload().path("retired_id").asText(null));
            UUID surviving = parseUuid(e.payload().path("surviving_id").asText(null));
            if (retired == null || surviving == null) continue;
            raw.putIfAbsent(retired, surviving); // earliest-watermark wins on duplicate retired
        }
        return raw;
    }

    private static UUID parseUuid(String s) {
        if (s == null) return null;
        try { return UUID.fromString(s); } catch (IllegalArgumentException ignore) { return null; }
    }
}
