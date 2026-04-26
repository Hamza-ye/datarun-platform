package dev.datarun.ship1.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
import dev.datarun.ship1.event.ServerEmission;
import dev.datarun.ship1.event.ShapePayloadValidator;
import dev.datarun.ship1.sync.CoordinatorAuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Coordinator-only merge/split endpoints (Ship-2 §6 commitments 2/3/7).
 *
 * <p>Authed by {@link CoordinatorAuthInterceptor} on {@code /admin/subjects/**}. Pre-write
 * validation per ADR-002 §S9 returns 4xx structural error — distinct from §S14 accept-and-flag
 * for capture events. On success, emits a server-authored {@code subjects_merged/v1} or
 * {@code subject_split/v1} event:
 *
 * <ul>
 *   <li>envelope {@code type=capture} (vocabulary closed at six per ADR-007 §S1; F2)</li>
 *   <li>{@code actor_ref} = coordinator's UUID, NOT {@code system:} (F-B4: human-authored)</li>
 *   <li>{@code device_id} = {@link ServerEmission#SERVER_DEVICE_ID} (single reserved server UUID)</li>
 *   <li>{@code device_seq} from {@code server_device_seq} sequence</li>
 *   <li>{@code sync_watermark} = null (server-authored; no upstream to catch up to)</li>
 * </ul>
 *
 * <p>NO synthetic A→C alias edge is written on subsequent merges. Eager transitive closure is a
 * READ-time concern (Session 3) — writing synthetic edges would be re-reference, forbidden by §S6.
 */
@RestController
@RequestMapping("/admin/subjects")
public class AdminSubjectsController {

    private final ObjectMapper mapper;
    private final EventRepository events;
    private final ShapePayloadValidator shapeValidator;
    private final ServerEmission serverEmission;
    private final SubjectLifecycleProjector lifecycle;
    private final SubjectAliasProjector aliases;

    public AdminSubjectsController(ObjectMapper mapper, EventRepository events,
                                   ShapePayloadValidator shapeValidator,
                                   ServerEmission serverEmission,
                                   SubjectLifecycleProjector lifecycle,
                                   SubjectAliasProjector aliases) {
        this.mapper = mapper;
        this.events = events;
        this.shapeValidator = shapeValidator;
        this.serverEmission = serverEmission;
        this.lifecycle = lifecycle;
        this.aliases = aliases;
    }

    // -------------------------------------------------------- canonical (read)
    /**
     * Resolves a subject UUID to its ultimate-surviving id under eager transitive
     * closure of {@code subjects_merged/v1} events (Ship-2 §6 commitment 5; ADR-002
     * §S6). Returns {@code {requested_id, canonical_id, alias_chain_length}}. If
     * {@code id} is not retired, {@code canonical_id == requested_id} and
     * {@code alias_chain_length == 0}. The underlying historical events are NOT
     * rewritten — this is a parallel read-side projection (W-3 "raw, not rewritten").
     */
    @GetMapping("/{id}/canonical")
    public ResponseEntity<?> canonical(@PathVariable("id") String idStr) {
        UUID id;
        try { id = UUID.fromString(idStr); }
        catch (IllegalArgumentException ex) {
            return badRequest("id must be a valid UUID");
        }
        UUID canonical = aliases.canonicalId(id);
        int hops = aliases.aliasChainLength(id);
        ObjectNode resp = mapper.createObjectNode();
        resp.put("requested_id", id.toString());
        resp.put("canonical_id", canonical.toString());
        resp.put("alias_chain_length", hops);
        return ResponseEntity.ok(resp);
    }

    // ----------------------------------------------------------------- merge
    @PostMapping("/merge")
    public ResponseEntity<?> merge(@RequestBody JsonNode body, HttpServletRequest request) {
        UUID surviving = parseUuid(body.path("surviving_id"));
        UUID retired = parseUuid(body.path("retired_id"));
        if (surviving == null || retired == null) {
            return badRequest("surviving_id and retired_id must be valid UUIDs");
        }
        if (surviving.equals(retired)) {
            return badRequest("surviving_id and retired_id must differ");
        }
        if (lifecycle.isArchived(surviving)) {
            return badRequest("surviving_id is archived; cannot merge into an archived subject (ADR-002 §S9)");
        }
        if (lifecycle.isArchived(retired)) {
            return badRequest("retired_id is archived; cannot re-merge an archived subject (ADR-002 §S9)");
        }

        UUID coordinatorId = (UUID) request.getAttribute(CoordinatorAuthInterceptor.ATTR_COORDINATOR_ID);

        ObjectNode payload = mapper.createObjectNode();
        payload.put("surviving_id", surviving.toString());
        payload.put("retired_id", retired.toString());

        return emit("subjects_merged/v1", surviving, payload, coordinatorId);
    }

    // ----------------------------------------------------------------- split
    @PostMapping("/split")
    public ResponseEntity<?> split(@RequestBody JsonNode body, HttpServletRequest request) {
        UUID source = parseUuid(body.path("source_id"));
        if (source == null) {
            return badRequest("source_id must be a valid UUID");
        }
        JsonNode successors = body.path("successor_ids");
        if (successors.isMissingNode() || successors.isNull() || !successors.isArray() || successors.size() < 2) {
            return badRequest("successor_ids must be an array of at least 2 UUIDs (ADR-002 §S7 wrong-merge correction)");
        }
        List<UUID> successorIds = new ArrayList<>(successors.size());
        Set<UUID> dedupe = new HashSet<>();
        for (JsonNode n : successors) {
            UUID s = parseUuid(n);
            if (s == null) return badRequest("successor_ids must contain valid UUIDs");
            if (s.equals(source)) return badRequest("successor_id must not equal source_id");
            if (!dedupe.add(s)) return badRequest("successor_ids must be unique");
            successorIds.add(s);
        }
        if (lifecycle.isArchived(source)) {
            return badRequest("source_id is archived; cannot split an archived subject (ADR-002 §S9)");
        }

        UUID coordinatorId = (UUID) request.getAttribute(CoordinatorAuthInterceptor.ATTR_COORDINATOR_ID);

        ObjectNode payload = mapper.createObjectNode();
        payload.put("source_id", source.toString());
        ArrayNode arr = payload.putArray("successor_ids");
        for (UUID s : successorIds) arr.add(s.toString());

        return emit("subject_split/v1", source, payload, coordinatorId);
    }

    // ----------------------------------------------------------------- helpers
    private ResponseEntity<?> emit(String shapeRef, UUID subjectId, ObjectNode payload, UUID coordinatorId) {
        // Validate payload against shape schema before insert (single write-path discipline; F3).
        List<String> errors = shapeValidator.validate(shapeRef, payload);
        if (!errors.isEmpty()) {
            return badRequest("payload(" + shapeRef + "): " + String.join("; ", errors));
        }

        Event e = new Event(
                UUID.randomUUID(),
                "capture",                       // §6 commitment 6 / F2: vocabulary closed at six
                shapeRef,
                null,
                "subject",
                subjectId,
                coordinatorId.toString(),        // §6 commitment 7 / F-B4: human UUID, NOT system:
                serverEmission.serverDeviceId(), // §6 commitment 7: single reserved server UUID
                serverEmission.nextServerDeviceSeq(),
                null,                            // sync_watermark null on server-authored events
                OffsetDateTime.now(),
                payload);
        events.insert(e);

        ObjectNode resp = mapper.createObjectNode();
        resp.put("event_id", e.id().toString());
        resp.put("shape_ref", shapeRef);
        return ResponseEntity.ok(resp);
    }

    private static UUID parseUuid(JsonNode n) {
        if (n == null || n.isMissingNode() || n.isNull() || !n.isTextual()) return null;
        try { return UUID.fromString(n.asText()); }
        catch (IllegalArgumentException ex) { return null; }
    }

    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", "structural", "message", message));
    }
}
