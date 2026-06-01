package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.authorization.ActiveAssignment;
import dev.datarun.server.authorization.ScopeResolver;
import dev.datarun.server.authorization.SubjectLocationRepository;
import dev.datarun.server.config.Activity;
import dev.datarun.server.config.ActivityRepository;
import dev.datarun.server.event.Event;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class ResolverRoutingService {

    private static final String MULTIPLE_FLAGS = "multiple_flags";

    private final ScopeResolver scopeResolver;
    private final SubjectLocationRepository subjectLocationRepository;
    private final ActivityRepository activityRepository;
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ResolverRoutingService(ScopeResolver scopeResolver,
                                  SubjectLocationRepository subjectLocationRepository,
                                  ActivityRepository activityRepository,
                                  JdbcTemplate jdbc,
                                  ObjectMapper objectMapper) {
        this.scopeResolver = scopeResolver;
        this.subjectLocationRepository = subjectLocationRepository;
        this.activityRepository = activityRepository;
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public ResolverRef route(Event sourceEvent, String category) {
        return route(sourceEvent, List.of(category), List.of());
    }

    public ResolverRef route(Event sourceEvent, Collection<String> categories) {
        return route(sourceEvent, categories, List.of());
    }

    public ResolverRef route(Event sourceEvent,
                             Collection<String> categories,
                             List<Event> implicatedEvents) {
        ResolverRef existing = existingUnresolvedResolver(sourceEvent.id()).orElse(null);
        if (existing != null) {
            return existing;
        }

        List<EventContext> contexts = contextsFor(sourceEvent, implicatedEvents);
        if (contexts.isEmpty()) {
            return unassigned(unassignedCategory(categories));
        }

        List<ActiveAssignment> candidates = activeHumanAssignments().stream()
                .filter(assignment -> coversAll(assignment, contexts))
                .filter(assignment -> isEligibleSteward(assignment, sourceEvent))
                .sorted(nearestStewardFirst())
                .toList();

        if (candidates.isEmpty()) {
            return unassigned(unassignedCategory(categories));
        }

        return ResolverRef.actor(candidates.get(0).actorId().toString());
    }

    public ResolverRef routeManualIdentityConflict(Event sourceEvent, UUID creatorActorId) {
        ResolverRef existing = existingUnresolvedResolver(sourceEvent.id()).orElse(null);
        if (existing != null) {
            return existing;
        }

        if (creatorActorId != null) {
            List<EventContext> contexts = contextsFor(sourceEvent, List.of());
            if (!contexts.isEmpty()) {
                Optional<ActiveAssignment> creatorAssignment = scopeResolver.getActiveAssignments(creatorActorId)
                        .stream()
                        .filter(assignment -> coversAll(assignment, contexts))
                        .filter(assignment -> isStewardRole(assignment.role(), sourceEvent.activityRef()))
                        .sorted(nearestStewardFirst())
                        .findFirst();
                if (creatorAssignment.isPresent()) {
                    return ResolverRef.actor(creatorActorId.toString());
                }
            }
        }
        return route(sourceEvent, "identity_conflict");
    }

    public ResolverRef routeUnauthorizedResolution(Event originalFlag) {
        ResolverRef originalResolver = ResolverRef.fromJson(
                originalFlag.payload().get("designated_resolver"));
        if (originalResolver != null) {
            return originalResolver;
        }
        String category = originalFlag.payload().path("flag_category").asText("scope_violation");
        return unassigned(category);
    }

    public ResolverRef unassigned(String category) {
        return ResolverRef.actor("system:resolver_unassigned/" + category);
    }

    private Optional<ResolverRef> existingUnresolvedResolver(UUID sourceEventId) {
        List<String> resolvers = jdbc.queryForList("""
                SELECT cd.payload->'designated_resolver' AS resolver
                FROM events cd
                WHERE cd.shape_ref LIKE 'conflict_detected/%'
                  AND cd.payload->>'source_event_id' = ?
                  AND jsonb_typeof(cd.payload->'designated_resolver') = 'object'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM events cr
                      WHERE cr.shape_ref LIKE 'conflict_resolved/%'
                        AND cr.payload->>'flag_event_id' = cd.id::text
                        AND cr.actor_ref->>'type' = cd.payload->'designated_resolver'->>'type'
                        AND cr.actor_ref->>'id' = cd.payload->'designated_resolver'->>'id'
                  )
                ORDER BY cd.sync_watermark ASC
                LIMIT 1
                """, String.class, sourceEventId.toString());
        if (resolvers.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(ResolverRef.fromJson(objectMapper.readTree(resolvers.get(0))));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private List<EventContext> contextsFor(Event sourceEvent, List<Event> implicatedEvents) {
        List<EventContext> contexts = new ArrayList<>();
        addContext(contexts, sourceEvent);
        for (Event event : implicatedEvents) {
            addContext(contexts, event);
        }
        return contexts;
    }

    private void addContext(List<EventContext> contexts, Event event) {
        UUID subjectId = extractSubjectId(event);
        if (subjectId == null) {
            return;
        }
        contexts.add(new EventContext(
                subjectId,
                subjectLocationRepository.findPathBySubjectId(subjectId),
                event.activityRef()));
    }

    private UUID extractSubjectId(Event event) {
        JsonNode subjectRef = event.subjectRef();
        if (subjectRef == null || !"subject".equals(subjectRef.path("type").asText())) {
            return null;
        }
        try {
            return UUID.fromString(subjectRef.path("id").asText());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private List<ActiveAssignment> activeHumanAssignments() {
        Set<UUID> actorIds = new LinkedHashSet<>();
        jdbc.queryForList("""
                SELECT DISTINCT payload->'target_actor'->>'id' AS actor_id
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_created/v1'
                  AND payload->'target_actor'->>'type' = 'actor'
                """, String.class).forEach(id -> {
            if (id != null && !id.startsWith("system:")) {
                try {
                    actorIds.add(UUID.fromString(id));
                } catch (IllegalArgumentException ignored) {
                    // Ignore malformed historical assignment target IDs.
                }
            }
        });

        List<ActiveAssignment> assignments = new ArrayList<>();
        for (UUID actorId : actorIds) {
            assignments.addAll(scopeResolver.getActiveAssignments(actorId));
        }
        return assignments;
    }

    private boolean coversAll(ActiveAssignment assignment, List<EventContext> contexts) {
        return contexts.stream().allMatch(context ->
                assignment.containsGeographically(context.subjectLocationPath())
                        && assignment.containsSubject(context.subjectId())
                        && assignment.containsActivity(context.activityRef()));
    }

    private boolean isEligibleSteward(ActiveAssignment assignment, Event sourceEvent) {
        String sourceActorId = sourceEvent.actorRef() != null
                ? sourceEvent.actorRef().path("id").asText(null)
                : null;
        if (sourceActorId != null && sourceActorId.equals(assignment.actorId().toString())) {
            return false;
        }
        return isStewardRole(assignment.role(), sourceEvent.activityRef());
    }

    private boolean isStewardRole(String role, String activityRef) {
        if (role == null || role.isBlank()) {
            return false;
        }
        if (activityRef != null && permitsReviewAction(activityRef, role)) {
            return true;
        }
        String normalized = role.toLowerCase(Locale.ROOT).replace('-', '_');
        return normalized.contains("admin")
                || normalized.contains("supervisor")
                || normalized.contains("coordinator")
                || normalized.contains("steward")
                || normalized.contains("reviewer")
                || normalized.contains("manager")
                || normalized.contains("lead")
                || normalized.contains("resolver");
    }

    private boolean permitsReviewAction(String activityRef, String role) {
        return activityRepository.findByName(activityRef)
                .map(Activity::configJson)
                .map(config -> config.get("roles"))
                .filter(JsonNode::isObject)
                .map(roles -> roles.get(role))
                .filter(JsonNode::isArray)
                .map(actions -> {
                    for (JsonNode action : actions) {
                        if (action.isTextual() && "review".equals(action.asText())) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    private Comparator<ActiveAssignment> nearestStewardFirst() {
        return Comparator
                .comparingInt((ActiveAssignment assignment) ->
                        assignment.geographicPath() == null ? 0 : assignment.geographicPath().length())
                .reversed()
                .thenComparing(assignment -> assignment.subjectList() == null)
                .thenComparingInt(assignment -> assignment.subjectList() == null
                        ? Integer.MAX_VALUE : assignment.subjectList().size())
                .thenComparing(assignment -> assignment.activityList() == null)
                .thenComparingInt(assignment -> assignment.activityList() == null
                        ? Integer.MAX_VALUE : assignment.activityList().size())
                .thenComparing(assignment -> assignment.actorId().toString());
    }

    private String unassignedCategory(Collection<String> categories) {
        Set<String> unique = new LinkedHashSet<>(categories);
        if (unique.size() > 1) {
            return MULTIPLE_FLAGS;
        }
        return unique.stream().findFirst().orElse("scope_violation");
    }

    private record EventContext(UUID subjectId, String subjectLocationPath, String activityRef) {}
}
