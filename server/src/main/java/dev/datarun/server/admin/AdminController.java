package dev.datarun.server.admin;

import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.subject.SubjectProjection;
import dev.datarun.server.subject.SubjectSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final SubjectProjection subjectProjection;
    private final EventRepository eventRepository;

    public AdminController(SubjectProjection subjectProjection, EventRepository eventRepository) {
        this.subjectProjection = subjectProjection;
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public String subjectList(Model model) {
        List<SubjectSummary> subjects = subjectProjection.listSubjects();
        model.addAttribute("subjects", subjects);
        return "subject-list";
    }

    @GetMapping("/subjects/{id}")
    public String subjectDetail(@PathVariable UUID id, Model model) {
        List<Event> events = eventRepository.findBySubjectId(id);
        model.addAttribute("subjectId", id.toString());
        model.addAttribute("events", events);
        return "subject-detail";
    }
}
