package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Scope Resolver: the single authority source for "what can this actor see?"
 * 
 * Reconstructs active assignments from the assignment event timeline (ADR-3 S3).
 * Scope-containment test: actor.assignment.scope ⊇ subject.location (ADR-3 S1).
 * 
 * Contract C6: PE → SR: authority context as projection.
 */
@Component
public class ScopeResolver {

    private static final Logger log = LoggerFactory.getLogger(ScopeResolver.class);
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final LocationRepository locationRepository;

    public ScopeResolver(JdbcTemplate jdbc, ObjectMapper objectMapper,
                         LocationRepository locationRepository) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.locationRepository = locationRepository;
    }

    /**
     * Compute active assignments for an actor by reconstructing from the event timeline.
     * An assignment is active if:
     *   1. There's an assignment_created event for it (subject_ref.id = assignment UUID)
     *   2. No corresponding assignment_ended event shares the same subject_ref.id
     *   3. valid_to is null or in the future
     */
    public List<ActiveAssignment> getActiveAssignments(UUID actorId) {
        // Find all assignment_created events targeting this actor
        List<Map<String, Object>> created = jdbc.queryForList("""
                SELECT e.subject_ref->>'id' AS assignment_id,
                       e.payload,
                       e.sync_watermark
                FROM events e
                WHERE e.type = 'assignment_changed'
                  AND e.shape_ref = 'assignment_created/v1'
                  AND e.payload->'target_actor'->>'id' = ?
                ORDER BY e.sync_watermark ASC
                """, actorId.toString());

        if (created.isEmpty()) {
            return List.of();
        }

        // Find all assignment_ended events (by subject_ref.id = assignment UUID)
        Set<String> endedAssignments = new HashSet<>(jdbc.queryForList("""
                SELECT e.subject_ref->>'id' AS assignment_id
                FROM events e
                WHERE e.type = 'assignment_changed'
                  AND e.shape_ref = 'assignment_ended/v1'
                  AND e.payload->'target_actor'->>'id' = ?
                """, String.class, actorId.toString()));

        // Wait — assignment_ended doesn't have target_actor. It targets via subject_ref.id.
        // Let me fix this: ended events share subject_ref.id with created events.
        // We need to find ended events where subject_ref.id matches any created assignment_id.
        List<String> assignmentIds = created.stream()
                .map(row -> (String) row.get("assignment_id"))
                .toList();

        endedAssignments.clear();
        if (!assignmentIds.isEmpty()) {
            String placeholders = String.join(",", Collections.nCopies(assignmentIds.size(), "?"));
            Object[] params = assignmentIds.toArray();
            endedAssignments.addAll(jdbc.queryForList(
                    "SELECT subject_ref->>'id' FROM events WHERE type = 'assignment_changed' " +
                    "AND shape_ref = 'assignment_ended/v1' AND subject_ref->>'id' IN (" + placeholders + ")",
                    String.class, params));
        }

        List<ActiveAssignment> active = new ArrayList<>();
        for (Map<String, Object> row : created) {
            String assignmentId = (String) row.get("assignment_id");
            if (endedAssignments.contains(assignmentId)) {
                continue;
            }

            try {
                JsonNode payload = objectMapper.readTree(row.get("payload").toString());
                
                UUID geoScope = null;
                String geoPath = null;
                JsonNode geoNode = payload.path("scope").path("geographic");
                if (!geoNode.isNull() && geoNode.isTextual()) {
                    geoScope = UUID.fromString(geoNode.asText());
                    geoPath = locationRepository.findPathById(geoScope);
                }

                List<UUID> subjectList = null;
                JsonNode slNode = payload.path("scope").path("subject_list");
                if (!slNode.isNull() && slNode.isArray()) {
                    subjectList = new ArrayList<>();
                    for (JsonNode item : slNode) {
                        subjectList.add(UUID.fromString(item.asText()));
                    }
                }

                List<String> activityList = null;
                JsonNode actNode = payload.path("scope").path("activity");
                if (!actNode.isNull() && actNode.isArray()) {
                    activityList = new ArrayList<>();
                    for (JsonNode item : actNode) {
                        activityList.add(item.asText());
                    }
                }

                OffsetDateTime validFrom = OffsetDateTime.parse(payload.get("valid_from").asText());
                OffsetDateTime validTo = null;
                if (!payload.get("valid_to").isNull()) {
                    validTo = OffsetDateTime.parse(payload.get("valid_to").asText());
                }

                ActiveAssignment assignment = new ActiveAssignment(
                        UUID.fromString(assignmentId),
                        actorId,
                        payload.get("role").asText(),
                        geoScope, geoPath,
                        subjectList, activityList,
                        validFrom, validTo,
                        false
                );

                if (assignment.isActive()) {
                    active.add(assignment);
                }
            } catch (Exception e) {
                log.warn("Skipping malformed assignment event {}: {}", assignmentId, e.getMessage());
            }
        }

        return active;
    }

    /**
     * Check if an event is within ANY of the actor's active assignments' scope.
     * AND within assignment: all non-null dimensions must pass.
     * OR across assignments: passes if ANY assignment passes.
     */
    public boolean isInScope(List<ActiveAssignment> assignments, String subjectLocationPath,
                             UUID subjectId, String activityRef) {
        for (ActiveAssignment assignment : assignments) {
            if (assignment.containsGeographically(subjectLocationPath)
                    && assignment.containsSubject(subjectId)
                    && assignment.containsActivity(activityRef)) {
                return true;
            }
        }
        return false;
    }
}
