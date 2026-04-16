package dev.datarun.server.subject;

import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final EventRepository eventRepository;
    private final SubjectProjection subjectProjection;

    public SubjectController(EventRepository eventRepository, SubjectProjection subjectProjection) {
        this.eventRepository = eventRepository;
        this.subjectProjection = subjectProjection;
    }

    @GetMapping
    public ResponseEntity<?> listSubjects() {
        List<SubjectSummary> subjects = subjectProjection.listSubjects();
        return ResponseEntity.ok(Map.of("subjects", subjects));
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<?> getSubjectEvents(@PathVariable UUID id) {
        List<Event> events = eventRepository.findBySubjectId(id);
        if (events.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "subject_not_found"));
        }
        return ResponseEntity.ok(Map.of(
                "subject_id", id.toString(),
                "events", events
        ));
    }
}
