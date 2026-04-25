package dev.datarun.ship1.admin;

import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * On-demand projection of subject lifecycle from {@code subjects_merged/v1} and
 * {@code subject_split/v1} events. FP-002 path (a) — locked at Ship-2 spec close 2026-04-25.
 *
 * <p>NO TABLE. NO CACHE. Same shape as {@link dev.datarun.ship1.scope.ScopeResolver}: read the
 * relevant event slice, fold to current state. If a future Ship's fixture surfaces read cost,
 * ADR-001 §S2 escape hatch B→C remains available; FP-002's gate is already specified.
 *
 * <p>A subject is {@code archived} iff it appears as {@code retired_id} in some
 * {@code subjects_merged/v1} OR as {@code source_id} in some {@code subject_split/v1}.
 */
@Component
public class SubjectLifecycleProjector {

    private final EventRepository events;

    public SubjectLifecycleProjector(EventRepository events) {
        this.events = events;
    }

    public boolean isArchived(UUID subjectId) {
        return archivedIds().contains(subjectId);
    }

    private Set<UUID> archivedIds() {
        Set<UUID> archived = new HashSet<>();
        for (Event e : events.findByShapeRefPrefix("subjects_merged/")) {
            String s = e.payload().path("retired_id").asText(null);
            if (s != null) try { archived.add(UUID.fromString(s)); } catch (IllegalArgumentException ignore) {}
        }
        for (Event e : events.findByShapeRefPrefix("subject_split/")) {
            String s = e.payload().path("source_id").asText(null);
            if (s != null) try { archived.add(UUID.fromString(s)); } catch (IllegalArgumentException ignore) {}
        }
        return archived;
    }
}
