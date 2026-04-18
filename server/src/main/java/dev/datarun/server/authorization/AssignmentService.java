package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.ServerIdentity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Assignment management: creates assignment events through the Event Store.
 * Enforces scope-containment on create (ADR-3 S5): new.scope ⊆ creator.scope.
 * Online-only (same precedent as merge/split: ADR-2 S10).
 */
@Service
public class AssignmentService {

    private final EventRepository eventRepository;
    private final ServerIdentity serverIdentity;
    private final ScopeResolver scopeResolver;
    private final LocationRepository locationRepository;
    private final ObjectMapper objectMapper;

    public AssignmentService(EventRepository eventRepository,
                             ServerIdentity serverIdentity,
                             ScopeResolver scopeResolver,
                             LocationRepository locationRepository,
                             ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.serverIdentity = serverIdentity;
        this.scopeResolver = scopeResolver;
        this.locationRepository = locationRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create an assignment. Produces an assignment_created event.
     *
     * @param creatorActorId the actor creating the assignment (must have containing scope)
     * @param targetActorId  the actor being assigned
     * @param role           opaque role label
     * @param geographicId   location UUID for geographic scope (nullable)
     * @param subjectList    explicit subject list (nullable)
     * @param activityList   permitted activities (nullable)
     * @param validFrom      when the assignment takes effect
     * @param validTo        when the assignment expires (null = indefinite)
     * @return the created event
     * @throws IllegalArgumentException if scope-containment validation fails (S5)
     */
    public Event createAssignment(UUID creatorActorId, UUID targetActorId, String role,
                                  UUID geographicId, List<UUID> subjectList, List<String> activityList,
                                  OffsetDateTime validFrom, OffsetDateTime validTo) {
        // S5: scope-containment validation — new.scope ⊆ creator.scope
        validateScopeContainment(creatorActorId, geographicId);

        UUID assignmentId = UUID.randomUUID();

        // Build payload per IDR-013
        ObjectNode payload = objectMapper.createObjectNode();
        
        ObjectNode targetActor = objectMapper.createObjectNode();
        targetActor.put("type", "actor");
        targetActor.put("id", targetActorId.toString());
        payload.set("target_actor", targetActor);
        
        payload.put("role", role);

        ObjectNode scope = objectMapper.createObjectNode();
        if (geographicId != null) {
            scope.put("geographic", geographicId.toString());
        } else {
            scope.putNull("geographic");
        }
        if (subjectList != null) {
            ArrayNode arr = objectMapper.createArrayNode();
            subjectList.forEach(id -> arr.add(id.toString()));
            scope.set("subject_list", arr);
        } else {
            scope.putNull("subject_list");
        }
        if (activityList != null) {
            ArrayNode arr = objectMapper.createArrayNode();
            activityList.forEach(arr::add);
            scope.set("activity", arr);
        } else {
            scope.putNull("activity");
        }
        payload.set("scope", scope);

        payload.put("valid_from", validFrom.toString());
        if (validTo != null) {
            payload.put("valid_to", validTo.toString());
        } else {
            payload.putNull("valid_to");
        }

        // Build envelope
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "assignment");
        subjectRef.put("id", assignmentId.toString());

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", creatorActorId.toString());

        Event event = new Event(
                UUID.randomUUID(),
                "assignment_changed",
                "assignment_created/v1",
                null,
                subjectRef,
                actorRef,
                serverIdentity.getDeviceId(),
                (int) serverIdentity.nextDeviceSeq(),
                null,  // sync_watermark assigned by DB
                OffsetDateTime.now(ZoneOffset.UTC),
                payload
        );

        eventRepository.insert(event);
        return event;
    }

    /**
     * End an assignment. Produces an assignment_ended event targeting the same assignment UUID.
     *
     * @param assignmentId the assignment UUID (subject_ref.id of the assignment_created event)
     * @param actorId      who is ending the assignment
     * @param reason       optional reason
     * @return the created event
     */
    public Event endAssignment(UUID assignmentId, UUID actorId, String reason) {
        ObjectNode payload = objectMapper.createObjectNode();
        if (reason != null) {
            payload.put("reason", reason);
        } else {
            payload.putNull("reason");
        }

        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "assignment");
        subjectRef.put("id", assignmentId.toString());

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", actorId.toString());

        Event event = new Event(
                UUID.randomUUID(),
                "assignment_changed",
                "assignment_ended/v1",
                null,
                subjectRef,
                actorRef,
                serverIdentity.getDeviceId(),
                (int) serverIdentity.nextDeviceSeq(),
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                payload
        );

        eventRepository.insert(event);
        return event;
    }

    /**
     * Validate scope containment (S5): the new assignment's geographic scope
     * must be within the creator's geographic scope.
     */
    private void validateScopeContainment(UUID creatorActorId, UUID newGeoScopeId) {
        if (newGeoScopeId == null) {
            // No geographic restriction in new assignment — only root/admin can do this
            // For Phase 2a: allow if creator has no assignments (bootstrap)
            // or if creator has a null geographic scope
            List<ActiveAssignment> creatorAssignments = scopeResolver.getActiveAssignments(creatorActorId);
            if (creatorAssignments.isEmpty()) {
                // Bootstrap: no assignments exist yet, allow creation
                return;
            }
            boolean hasUnrestrictedGeo = creatorAssignments.stream()
                    .anyMatch(a -> a.geographicPath() == null);
            if (!hasUnrestrictedGeo) {
                throw new IllegalArgumentException(
                        "Scope containment violation: cannot create assignment with unrestricted " +
                        "geographic scope when creator's scope is restricted");
            }
            return;
        }

        String newPath = locationRepository.findPathById(newGeoScopeId);
        if (newPath == null) {
            throw new IllegalArgumentException("Location not found: " + newGeoScopeId);
        }

        List<ActiveAssignment> creatorAssignments = scopeResolver.getActiveAssignments(creatorActorId);
        if (creatorAssignments.isEmpty()) {
            // Bootstrap: allow (no assignments yet in the system)
            return;
        }

        boolean contained = creatorAssignments.stream()
                .anyMatch(a -> a.containsGeographically(newPath));

        if (!contained) {
            throw new IllegalArgumentException(
                    "Scope containment violation: new geographic scope is not within creator's scope");
        }
    }
}
