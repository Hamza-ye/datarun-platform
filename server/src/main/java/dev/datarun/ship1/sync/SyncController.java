package dev.datarun.ship1.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.event.*;
import dev.datarun.ship1.integrity.ConflictDetector;
import dev.datarun.ship1.scope.ScopeResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Push/pull for Ship-1. Protocol documented in {@code contracts/sync-protocol.md}.
 *
 * Push path: validate batch (envelope + shape) → reject whole batch on any validation error
 * → insert events one-by-one (dedupe by id) → for each newly-inserted {@code type=capture}
 * event, run conflict detection which may emit flag events.
 *
 * Pull path: scope-filter events by the caller's active assignments reconstructed at event
 * time from the event stream. No cache (FP-001 gate for Ship-1).
 */
@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final EnvelopeValidator envelopeValidator;
    private final ShapePayloadValidator shapeValidator;
    private final EventRepository events;
    private final ConflictDetector detector;
    private final ScopeResolver scopes;
    private final ObjectMapper mapper;

    public SyncController(EnvelopeValidator envelopeValidator, ShapePayloadValidator shapeValidator,
                          EventRepository events, ConflictDetector detector, ScopeResolver scopes,
                          ObjectMapper mapper) {
        this.envelopeValidator = envelopeValidator;
        this.shapeValidator = shapeValidator;
        this.events = events;
        this.detector = detector;
        this.scopes = scopes;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------------- push
    @PostMapping("/push")
    public ResponseEntity<?> push(@RequestBody JsonNode body, HttpServletRequest request) {
        JsonNode eventsNode = body.path("events");
        if (!eventsNode.isArray() || eventsNode.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "empty_batch"));
        }

        // --- Phase 1: validate the entire batch ---
        List<String> allErrors = new ArrayList<>();
        List<Event> toInsert = new ArrayList<>(eventsNode.size());
        for (int i = 0; i < eventsNode.size(); i++) {
            JsonNode env = eventsNode.get(i);
            List<String> envErrors = envelopeValidator.validate(env);
            if (!envErrors.isEmpty()) {
                for (String e : envErrors) allErrors.add("[event " + i + "] envelope: " + e);
                continue;
            }
            String shapeRef = env.path("shape_ref").asText();
            List<String> shapeErrors = shapeValidator.validate(shapeRef, env.path("payload"));
            if (!shapeErrors.isEmpty()) {
                for (String e : shapeErrors) allErrors.add("[event " + i + "] payload(" + shapeRef + "): " + e);
                continue;
            }
            toInsert.add(EventMapper.fromEnvelope(env, mapper));
        }
        if (!allErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "validation_failed", "details", allErrors));
        }

        // --- Phase 2: persist + detect ---
        int accepted = 0;
        int duplicates = 0;
        int flagsRaised = 0;
        for (Event e : toInsert) {
            EventRepository.InsertResult r = events.insert(e);
            if (r == EventRepository.InsertResult.DUPLICATE) { duplicates++; continue; }
            accepted++;
            if ("capture".equals(e.type())) {
                // Re-read to get assigned sync_watermark before detection (detection may compare to other
                // persisted events which have watermarks).
                Event persisted = events.findById(e.id()).orElse(e);
                List<Event> flags = detector.detect(persisted);
                flagsRaised += flags.size();
            }
        }
        return ResponseEntity.ok(Map.of(
                "accepted", accepted,
                "duplicates", duplicates,
                "flags_raised", flagsRaised));
    }

    // ---------------------------------------------------------------------- pull
    @PostMapping("/pull")
    public ResponseEntity<?> pull(@RequestBody JsonNode body, HttpServletRequest request) {
        UUID actorId = (UUID) request.getAttribute(ActorAuthInterceptor.ATTR_ACTOR_ID);
        long since = body.path("since_watermark").asLong(0);
        int limit = body.path("limit").asInt(100);
        if (limit > 500) limit = 500;

        // Reconstruct the actor's scope (at now). Ship-1 uses at-now for the pull window — the
        // per-event-time check is used on the push path (scope_violation flag). For read-side
        // filtering, "at now" is sufficient for Ship-1 (spec §6.3).
        OffsetDateTime now = OffsetDateTime.now();
        Set<UUID> activeScopes = scopes.activeGeographicScopes(actorId.toString(), now);
        Set<UUID> scopeSubjectIds = subjectsInScopes(activeScopes);

        ArrayNode out = mapper.createArrayNode();
        long latest = since;
        int emitted = 0;
        // Pull in ascending watermark order, applying scope filter; stop when emitted == limit.
        // We over-fetch pages to fill the limit.
        while (emitted < limit) {
            List<Event> page = events.findSince(latest, 500);
            if (page.isEmpty()) break;
            for (Event e : page) {
                latest = Math.max(latest, e.syncWatermark());
                if (!inScopeForPull(e, actorId, activeScopes, scopeSubjectIds)) continue;
                out.add(EventMapper.toEnvelope(e, mapper));
                emitted++;
                if (emitted >= limit) break;
            }
            if (page.size() < 500) break;
        }

        ObjectNode resp = mapper.createObjectNode();
        resp.set("events", out);
        resp.put("latest_watermark", latest);
        return ResponseEntity.ok(resp);
    }

    /** A subject is in scope iff its first household_observation event's village_ref is in scope. */
    private Set<UUID> subjectsInScopes(Set<UUID> activeScopes) {
        Set<UUID> result = new HashSet<>();
        for (Event e : events.findByShapeRefPrefix("household_observation/")) {
            JsonNode v = e.payload().path("village_ref");
            if (v.isTextual()) {
                try {
                    UUID village = UUID.fromString(v.asText());
                    if (activeScopes.contains(village)) result.add(e.subjectId());
                } catch (IllegalArgumentException ignore) {}
            }
        }
        return result;
    }

    private boolean inScopeForPull(Event e, UUID actorId, Set<UUID> activeScopes, Set<UUID> scopeSubjectIds) {
        // 1. Actor always sees their own assignment events.
        if ("assignment_changed".equals(e.type())) {
            JsonNode targetActor = e.payload().path("target_actor");
            return actorId.toString().equals(targetActor.path("id").asText());
        }
        // 2. Household captures: in scope iff the capture's village_ref is in the actor's scope.
        if ("household_observation/v1".equals(e.shapeRef())) {
            JsonNode v = e.payload().path("village_ref");
            if (!v.isTextual()) return false;
            try { return activeScopes.contains(UUID.fromString(v.asText())); }
            catch (IllegalArgumentException ex) { return false; }
        }
        // 3. Flag events and resolution events: in scope iff the subject they reference is in scope.
        if ("conflict_detected/v1".equals(e.shapeRef())) {
            return scopeSubjectIds.contains(e.subjectId());
        }
        // Default-deny for Ship-1.
        return false;
    }
}
