package dev.datarun.server.sync;

import dev.datarun.server.authorization.ActiveAssignment;
import dev.datarun.server.authorization.ScopeResolver;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.SubjectAliasProjection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Subject-bound history replay for projection backfill.
 * This is not live sync and does not update device sync state.
 */
@Service
public class SubjectHistoryBackfillService {

    private final EventRepository eventRepository;
    private final ScopeResolver scopeResolver;
    private final SubjectAliasProjection subjectAliasProjection;
    private final JdbcTemplate jdbc;

    public SubjectHistoryBackfillService(EventRepository eventRepository,
                                         ScopeResolver scopeResolver,
                                         SubjectAliasProjection subjectAliasProjection,
                                         JdbcTemplate jdbc) {
        this.eventRepository = eventRepository;
        this.scopeResolver = scopeResolver;
        this.subjectAliasProjection = subjectAliasProjection;
        this.jdbc = jdbc;
    }

    public SubjectHistoryPage page(UUID actorId, UUID requestedSubjectId,
                                   String activityRef, long cursor, int limit) {
        SubjectHistoryScope scope = subjectHistoryScope(requestedSubjectId);
        if (!isAuthorized(actorId, scope.subjectIds(), activityRef)) {
            throw new UnauthorizedSubjectHistoryException();
        }

        List<Event> events = eventRepository.findSubjectHistoryBackfillPage(
                scope.subjectIds(), activityRef, cursor, limit);
        long nextCursor = events.isEmpty()
                ? cursor
                : events.get(events.size() - 1).syncWatermark();
        return new SubjectHistoryPage(
                requestedSubjectId,
                scope.canonicalSubjectId(),
                activityRef,
                cursor,
                nextCursor,
                events.size() == limit,
                events);
    }

    private SubjectHistoryScope subjectHistoryScope(UUID requestedSubjectId) {
        UUID canonicalSubjectId = subjectAliasProjection.resolve(requestedSubjectId);
        Set<UUID> subjectIds = new LinkedHashSet<>();
        subjectIds.add(canonicalSubjectId);
        subjectIds.addAll(subjectAliasProjection.findRetiredAliases(canonicalSubjectId));
        subjectIds.add(requestedSubjectId);
        return new SubjectHistoryScope(canonicalSubjectId, List.copyOf(subjectIds));
    }

    private boolean isAuthorized(UUID actorId, List<UUID> subjectIds, String activityRef) {
        List<ActiveAssignment> assignments = scopeResolver.getActiveAssignments(actorId);
        if (assignments.isEmpty()) {
            return false;
        }
        List<String> locationPaths = findSubjectLocationPaths(subjectIds);
        return assignments.stream().anyMatch(assignment ->
                assignment.containsActivity(activityRef)
                        && containsAnySubject(assignment, subjectIds)
                        && containsAnyLocationPath(assignment, locationPaths));
    }

    private boolean containsAnySubject(ActiveAssignment assignment, List<UUID> subjectIds) {
        if (assignment.subjectList() == null) {
            return true;
        }
        return subjectIds.stream().anyMatch(assignment.subjectList()::contains);
    }

    private boolean containsAnyLocationPath(ActiveAssignment assignment, List<String> locationPaths) {
        if (assignment.geographicPath() == null) {
            return true;
        }
        if (locationPaths.isEmpty()) {
            return false;
        }
        return locationPaths.stream().anyMatch(assignment::containsGeographically);
    }

    private List<String> findSubjectLocationPaths(List<UUID> subjectIds) {
        List<String> paths = new ArrayList<>();
        for (UUID subjectId : subjectIds) {
            paths.addAll(jdbc.queryForList("""
                    SELECT path
                    FROM subject_locations
                    WHERE subject_id = ?::uuid
                    """, String.class, subjectId.toString()));
        }
        return paths;
    }

    public static class UnauthorizedSubjectHistoryException extends RuntimeException {
    }

    private record SubjectHistoryScope(UUID canonicalSubjectId, List<UUID> subjectIds) {}

    public record SubjectHistoryPage(
            UUID requestedSubjectId,
            UUID subjectId,
            String activityRef,
            long cursor,
            long nextCursor,
            boolean hasMore,
            List<Event> events
    ) {}
}
