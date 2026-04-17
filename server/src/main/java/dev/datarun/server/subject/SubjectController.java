package dev.datarun.server.subject;

import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.AliasCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final EventRepository eventRepository;
    private final SubjectProjection subjectProjection;
    private final AliasCache aliasCache;

    public SubjectController(EventRepository eventRepository,
                             SubjectProjection subjectProjection,
                             AliasCache aliasCache) {
        this.eventRepository = eventRepository;
        this.subjectProjection = subjectProjection;
        this.aliasCache = aliasCache;
    }

    @GetMapping
    public ResponseEntity<?> listSubjects() {
        List<SubjectSummary> subjects = subjectProjection.listSubjects();
        return ResponseEntity.ok(Map.of("subjects", subjects));
    }

    /**
     * Get all events for a subject, including events from alias chains.
     * If the requested ID is a surviving ID, also includes events from all retired aliases.
     * If the requested ID is a retired alias, resolves to the surviving ID and returns all.
     */
    @GetMapping("/{id}/events")
    public ResponseEntity<?> getSubjectEvents(@PathVariable UUID id) {
        // Resolve through alias: if id is retired, get the canonical surviving id
        UUID canonicalId = aliasCache.resolve(id);

        // Collect events from the canonical ID plus all retired aliases that point to it
        List<Event> events = new ArrayList<>(eventRepository.findBySubjectId(canonicalId));

        // Also find events from retired IDs that alias to this canonical ID
        List<UUID> retiredIds = findRetiredAliases(canonicalId);
        for (UUID retiredId : retiredIds) {
            events.addAll(eventRepository.findBySubjectId(retiredId));
        }

        // Sort by sync_watermark
        events.sort((a, b) -> {
            if (a.syncWatermark() == null && b.syncWatermark() == null) return 0;
            if (a.syncWatermark() == null) return -1;
            if (b.syncWatermark() == null) return 1;
            return Long.compare(a.syncWatermark(), b.syncWatermark());
        });

        if (events.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "subject_not_found"));
        }
        return ResponseEntity.ok(Map.of(
                "subject_id", canonicalId.toString(),
                "events", events
        ));
    }

    /**
     * Find all retired IDs that alias to the given canonical (surviving) ID.
     */
    private List<UUID> findRetiredAliases(UUID canonicalId) {
        return eventRepository.getJdbcTemplate().query(
                "SELECT retired_id FROM subject_aliases WHERE surviving_id = ?::uuid",
                (rs, rowNum) -> UUID.fromString(rs.getString("retired_id")),
                canonicalId.toString());
    }
}
