package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.config.AssignmentAdminCapabilityPolicy;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.ServerIdentity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Assignment management: creates assignment events through the Event Store.
 * Enforces scope containment on create (ADR-003 S5): new.scope <= creator.scope.
 * Online-only (same precedent as merge/split: ADR-002 S10).
 */
@Service
public class AssignmentService {

    private static final String INITIAL_BOOTSTRAP_ACTOR = "system:assignment_bootstrap/initial";

    private final EventRepository eventRepository;
    private final ServerIdentity serverIdentity;
    private final ScopeResolver scopeResolver;
    private final AssignmentAdminCapabilityService assignmentAdminCapabilityService;
    private final LocationRepository locationRepository;
    private final ObjectMapper objectMapper;

    public AssignmentService(EventRepository eventRepository,
                             ServerIdentity serverIdentity,
                             ScopeResolver scopeResolver,
                             AssignmentAdminCapabilityService assignmentAdminCapabilityService,
                             LocationRepository locationRepository,
                             ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.serverIdentity = serverIdentity;
        this.scopeResolver = scopeResolver;
        this.assignmentAdminCapabilityService = assignmentAdminCapabilityService;
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
        validateAssignmentScopeInput(subjectList, activityList);
        validateCreateAuthority(creatorActorId, geographicId, subjectList, activityList);

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", creatorActorId.toString());

        return insertAssignmentCreatedEvent(actorRef, targetActorId, role,
                geographicId, subjectList, activityList, validFrom, validTo);
    }

    /**
     * Explicit initial bootstrap path. This is intentionally separate from the
     * actor command path so "creator has no assignments" is not production root.
     */
    public Event createInitialBootstrapAssignment(UUID targetActorId, String role,
                                                  UUID geographicId, List<UUID> subjectList,
                                                  List<String> activityList,
                                                  OffsetDateTime validFrom, OffsetDateTime validTo) {
        validateAssignmentScopeInput(subjectList, activityList);
        if (hasAnyAssignmentCreatedEvent()) {
            throw new IllegalArgumentException(
                    "Bootstrap authority unavailable: assignments already exist");
        }

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", INITIAL_BOOTSTRAP_ACTOR);

        return insertAssignmentCreatedEvent(actorRef, targetActorId, role,
                geographicId, subjectList, activityList, validFrom, validTo);
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
        List<ActiveAssignment> commandCapableAssignments = commandCapableAssignments(
                actorId, AssignmentAdminCapabilityPolicy.END_COMMAND);
        AssignmentScope targetScope = findAssignmentScope(assignmentId);
        if (assignmentEnded(assignmentId)) {
            throw new IllegalArgumentException("Assignment is already ended: " + assignmentId);
        }
        validateScopeContainment(commandCapableAssignments,
                targetScope.geographicId(), targetScope.geographicPath(),
                targetScope.subjectList(), targetScope.activityList(),
                "Assignment authority violation: actor cannot end assignment outside their scope through one active command-capable assignment");

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

    private Event insertAssignmentCreatedEvent(ObjectNode actorRef,
                                               UUID targetActorId, String role,
                                               UUID geographicId, List<UUID> subjectList,
                                               List<String> activityList,
                                               OffsetDateTime validFrom, OffsetDateTime validTo) {
        UUID assignmentId = UUID.randomUUID();

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

        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "assignment");
        subjectRef.put("id", assignmentId.toString());

        Event event = new Event(
                UUID.randomUUID(),
                "assignment_changed",
                "assignment_created/v1",
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

    private void validateCreateAuthority(UUID creatorActorId, UUID newGeoScopeId,
                                         List<UUID> subjectList, List<String> activityList) {
        List<ActiveAssignment> commandCapableAssignments = commandCapableAssignments(
                creatorActorId, AssignmentAdminCapabilityPolicy.CREATE_COMMAND);
        String newPath = null;
        if (newGeoScopeId != null) {
            newPath = locationRepository.findPathById(newGeoScopeId);
            if (newPath == null) {
                throw new IllegalArgumentException("Location not found: " + newGeoScopeId);
            }
        }

        validateScopeContainment(commandCapableAssignments, newGeoScopeId, newPath, subjectList, activityList,
                "Scope containment violation: new assignment scope is not within one active command-capable creator assignment");
    }

    private List<ActiveAssignment> commandCapableAssignments(UUID actorId, String command) {
        List<ActiveAssignment> actorAssignments = scopeResolver.getActiveAssignments(actorId);
        if (actorAssignments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Assignment command authority violation: actor has no active assignments granting " + command);
        }

        ObjectNode policy = assignmentAdminCapabilityService.getValidatedPolicy();
        List<ActiveAssignment> commandCapableAssignments = actorAssignments.stream()
                .filter(assignment -> AssignmentAdminCapabilityPolicy.roleGrants(
                        policy, assignment.role(), command))
                .toList();
        if (commandCapableAssignments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Assignment command authority violation: actor has no active assignment granting " + command);
        }
        return commandCapableAssignments;
    }

    private void validateScopeContainment(List<ActiveAssignment> candidateAssignments,
                                          UUID requestedGeoId, String requestedGeoPath,
                                          List<UUID> requestedSubjects,
                                          List<String> requestedActivities,
                                          String violationMessage) {
        boolean contained = candidateAssignments.stream()
                .anyMatch(assignment -> containsAssignmentScope(
                        assignment, requestedGeoId, requestedGeoPath,
                        requestedSubjects, requestedActivities));

        if (!contained) {
            throw new IllegalArgumentException(violationMessage);
        }
    }

    private boolean containsAssignmentScope(ActiveAssignment coveringAssignment,
                                            UUID requestedGeoId, String requestedGeoPath,
                                            List<UUID> requestedSubjects,
                                            List<String> requestedActivities) {
        return containsGeographicScope(coveringAssignment, requestedGeoId, requestedGeoPath)
                && containsSubjectScope(coveringAssignment, requestedSubjects)
                && containsActivityScope(coveringAssignment, requestedActivities);
    }

    private boolean containsGeographicScope(ActiveAssignment coveringAssignment,
                                            UUID requestedGeoId, String requestedGeoPath) {
        if (requestedGeoId == null) {
            return coveringAssignment.geographicPath() == null;
        }
        return coveringAssignment.containsGeographically(requestedGeoPath);
    }

    private boolean containsSubjectScope(ActiveAssignment coveringAssignment,
                                         List<UUID> requestedSubjects) {
        if (requestedSubjects == null) {
            return coveringAssignment.subjectList() == null;
        }
        if (coveringAssignment.subjectList() == null) {
            return true;
        }
        return coveringAssignment.subjectList().containsAll(requestedSubjects);
    }

    private boolean containsActivityScope(ActiveAssignment coveringAssignment,
                                          List<String> requestedActivities) {
        if (requestedActivities == null) {
            return coveringAssignment.activityList() == null;
        }
        if (coveringAssignment.activityList() == null) {
            return true;
        }
        return coveringAssignment.activityList().containsAll(requestedActivities);
    }

    private void validateAssignmentScopeInput(List<UUID> subjectList, List<String> activityList) {
        if (subjectList != null) {
            if (subjectList.isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid assignment scope: subject_list must be null or contain at least one subject id");
            }
            if (subjectList.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException(
                        "Invalid assignment scope: subject_list cannot contain null values");
            }
        }
        if (activityList != null) {
            if (activityList.isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid assignment scope: activity must be null or contain at least one activity_ref");
            }
            if (activityList.stream().anyMatch(activity -> activity == null || activity.isBlank())) {
                throw new IllegalArgumentException(
                        "Invalid assignment scope: activity cannot contain null or blank values");
            }
        }
    }

    private boolean hasAnyAssignmentCreatedEvent() {
        Integer count = eventRepository.getJdbcTemplate().queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_created/v1'
                """, Integer.class);
        return count != null && count > 0;
    }

    private boolean assignmentEnded(UUID assignmentId) {
        Integer count = eventRepository.getJdbcTemplate().queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_ended/v1'
                  AND subject_ref->>'id' = ?
                """, Integer.class, assignmentId.toString());
        return count != null && count > 0;
    }

    private AssignmentScope findAssignmentScope(UUID assignmentId) {
        List<Map<String, Object>> rows = eventRepository.getJdbcTemplate().queryForList("""
                SELECT payload
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_created/v1'
                  AND subject_ref->>'id' = ?
                ORDER BY sync_watermark ASC
                LIMIT 1
                """, assignmentId.toString());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Assignment not found: " + assignmentId);
        }

        try {
            JsonNode payload = objectMapper.readTree(rows.get(0).get("payload").toString());
            JsonNode scope = payload.path("scope");

            UUID geographicId = null;
            String geographicPath = null;
            JsonNode geographicNode = scope.path("geographic");
            if (!geographicNode.isMissingNode() && !geographicNode.isNull()) {
                geographicId = UUID.fromString(geographicNode.asText());
                geographicPath = locationRepository.findPathById(geographicId);
                if (geographicPath == null) {
                    throw new IllegalArgumentException("Location not found: " + geographicId);
                }
            }

            List<UUID> subjects = null;
            JsonNode subjectNode = scope.path("subject_list");
            if (!subjectNode.isMissingNode() && !subjectNode.isNull()) {
                if (!subjectNode.isArray()) {
                    throw new IllegalArgumentException("Assignment has invalid subject_list scope");
                }
                subjects = new ArrayList<>();
                for (JsonNode item : subjectNode) {
                    subjects.add(UUID.fromString(item.asText()));
                }
            }

            List<String> activities = null;
            JsonNode activityNode = scope.path("activity");
            if (!activityNode.isMissingNode() && !activityNode.isNull()) {
                if (!activityNode.isArray()) {
                    throw new IllegalArgumentException("Assignment has invalid activity scope");
                }
                activities = new ArrayList<>();
                for (JsonNode item : activityNode) {
                    activities.add(item.asText());
                }
            }

            return new AssignmentScope(geographicId, geographicPath, subjects, activities);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Assignment has invalid scope payload: " + assignmentId, e);
        }
    }

    private record AssignmentScope(
            UUID geographicId,
            String geographicPath,
            List<UUID> subjectList,
            List<String> activityList
    ) {}
}
