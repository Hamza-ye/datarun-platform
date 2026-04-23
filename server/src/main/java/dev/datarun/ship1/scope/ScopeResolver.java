package dev.datarun.ship1.scope;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reconstructs an actor's authorization scope by replaying {@code assignment_changed} events,
 * per ADR-003 §S1-§S7 and Ship-1 §6.3 ("scope reconstruction from assignment events").
 *
 * <b>No cache.</b> FP-001 gate for Ship-1: scope is reconstructed at event time by replay; a
 * cache-based shortcut is a build-time red flag. Performance concern lives in retro risk R1.
 *
 * Ship-1 scope dimension used: {@code geographic} only. Assignment shapes supported:
 * {@code assignment_created/v1}. Ship-1 does not process {@code assignment_ended}; spec §2 lists
 * it out of scope.
 */
@Component
public class ScopeResolver {

    private final EventRepository events;

    public ScopeResolver(EventRepository events) {
        this.events = events;
    }

    /** Geographic village UUIDs the actor is currently assigned to (union across active assignments). */
    public Set<UUID> activeGeographicScopes(String actorId, OffsetDateTime at) {
        return activeAssignments(actorId, at).stream()
                .map(a -> a.geographic)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /** All active assignments for the actor at the given time. */
    public List<Assignment> activeAssignments(String actorId, OffsetDateTime at) {
        // Replay every assignment_created/v1 event; filter by target_actor.id and time validity.
        List<Event> assignmentEvents = events.findByShapeRefPrefix("assignment_created/");
        List<Assignment> out = new ArrayList<>();
        for (Event e : assignmentEvents) {
            JsonNode p = e.payload();
            JsonNode targetActor = p.path("target_actor");
            if (!actorId.equals(targetActor.path("id").asText())) continue;

            OffsetDateTime validFrom = OffsetDateTime.parse(p.path("valid_from").asText());
            OffsetDateTime validTo = p.path("valid_to").isNull() ? null
                    : OffsetDateTime.parse(p.path("valid_to").asText());
            if (at.isBefore(validFrom)) continue;
            if (validTo != null && !at.isBefore(validTo)) continue;

            UUID geographic = p.path("scope").path("geographic").isNull() ? null
                    : UUID.fromString(p.path("scope").path("geographic").asText());
            out.add(new Assignment(geographic, p.path("role").asText()));
        }
        return out;
    }

    public record Assignment(UUID geographic, String role) {}
}
