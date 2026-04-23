package dev.datarun.ship1.integrity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
import dev.datarun.ship1.scope.ScopeResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Server-side conflict detection, per ADR-006 §S1/§S4. Emits one {@code conflict_detected/v1} flag
 * event per detected anomaly, with envelope {@code type=alert} and
 * {@code actor_ref.id = "system:conflict_detector/<category>"}.
 *
 * Ship-1 covers two categories:
 * <ul>
 *   <li>{@code scope_violation} — household_observation payload's {@code village_ref} is not in the
 *       capturing actor's active geographic scope at event time.</li>
 *   <li>{@code identity_conflict} — two household_observation captures in the same village with the
 *       same normalized household_name but different subject_ids (suspected duplicate household
 *       registered by two CHVs).</li>
 * </ul>
 *
 * Flags are emitted alongside the capture — never in place of (accept-and-flag, ADR-006 §S1).
 * No resolution is performed in Ship-1 (Ship-2 territory per spec §2).
 */
@Component
public class ConflictDetector {

    private static final String HOUSEHOLD_SHAPE = "household_observation/v1";
    private static final String FLAG_SHAPE = "conflict_detected/v1";
    private static final UUID SERVER_DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final EventRepository events;
    private final ScopeResolver scopes;
    private final ObjectMapper mapper;
    private final JdbcTemplate jdbc;

    public ConflictDetector(EventRepository events, ScopeResolver scopes, ObjectMapper mapper, JdbcTemplate jdbc) {
        this.events = events;
        this.scopes = scopes;
        this.mapper = mapper;
        this.jdbc = jdbc;
    }

    /** Called after a capture event is persisted. May emit 0..N flag events. */
    public List<Event> detect(Event capture) {
        if (!HOUSEHOLD_SHAPE.equals(capture.shapeRef())) return List.of();
        List<Event> flagsEmitted = new ArrayList<>();

        UUID villageRef = optUuid(capture.payload().path("village_ref").asText(null));
        String householdName = normalize(capture.payload().path("household_name").asText(""));

        // ---- scope_violation ---------------------------------------------------------------
        Set<UUID> activeScopes = scopes.activeGeographicScopes(capture.actorId(), capture.timestamp());
        if (villageRef != null && !activeScopes.contains(villageRef)) {
            Event flag = buildFlag(
                    capture,
                    "scope_violation",
                    "manual_only",
                    "capture's village_ref is outside capturing actor's assigned scope at event time");
            events.insert(flag);
            flagsEmitted.add(flag);
        }

        // ---- identity_conflict -------------------------------------------------------------
        // Look for prior household_observation events with same normalized name in same village
        // but different subject_id.
        if (villageRef != null && !householdName.isEmpty()) {
            for (Event prior : events.findByShapeRefPrefix("household_observation/")) {
                if (prior.id().equals(capture.id())) continue;
                if (prior.subjectId().equals(capture.subjectId())) continue;
                UUID priorVillage = optUuid(prior.payload().path("village_ref").asText(null));
                if (!Objects.equals(priorVillage, villageRef)) continue;
                String priorName = normalize(prior.payload().path("household_name").asText(""));
                if (!priorName.equals(householdName)) continue;
                // Duplicate suspected — emit flag pointing at the newly-arrived event, referencing the prior.
                Event flag = buildFlag(
                        capture,
                        "identity_conflict",
                        "manual_only",
                        "duplicate household name '%s' in same village; earlier capture id=%s subject=%s"
                                .formatted(householdName, prior.id(), prior.subjectId()));
                // Extra context in payload for the admin UI: both event ids + both subject ids.
                ObjectNode extraPayload = (ObjectNode) flag.payload();
                var relatedIds = mapper.createArrayNode();
                relatedIds.add(prior.id().toString());
                relatedIds.add(capture.id().toString());
                extraPayload.set("related_event_ids", relatedIds);
                var relatedSubjects = mapper.createArrayNode();
                relatedSubjects.add(prior.subjectId().toString());
                relatedSubjects.add(capture.subjectId().toString());
                extraPayload.set("related_subject_ids", relatedSubjects);
                events.insert(flag);
                flagsEmitted.add(flag);
                break; // one identity_conflict per new capture is enough for Ship-1
            }
        }
        return flagsEmitted;
    }

    private Event buildFlag(Event source, String category, String resolvability, String reason) {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("source_event_id", source.id().toString());
        payload.put("flag_category", category);
        payload.put("resolvability", resolvability);
        payload.put("reason", reason);
        ObjectNode designated = payload.putObject("designated_resolver");
        designated.put("type", "actor");
        designated.put("id", "system:conflict_detector/" + category);

        return new Event(
                UUID.randomUUID(),
                "alert",
                FLAG_SHAPE,
                null,
                source.subjectType(),
                source.subjectId(),
                "system:conflict_detector/" + category,
                SERVER_DEVICE_ID,
                nextServerSeq(),
                null,
                OffsetDateTime.now(),
                payload);
    }

    private synchronized long nextServerSeq() {
        Long v = jdbc.queryForObject("SELECT nextval('server_device_seq')", Long.class);
        return v == null ? 1L : v;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private static UUID optUuid(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return UUID.fromString(s); } catch (IllegalArgumentException ex) { return null; }
    }
}
