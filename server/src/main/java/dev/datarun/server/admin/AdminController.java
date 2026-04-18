package dev.datarun.server.admin;

import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.integrity.ConflictResolutionService;
import dev.datarun.server.subject.SubjectProjection;
import dev.datarun.server.subject.SubjectSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final SubjectProjection subjectProjection;
    private final EventRepository eventRepository;
    private final ConflictResolutionService resolutionService;

    public AdminController(SubjectProjection subjectProjection,
                           EventRepository eventRepository,
                           ConflictResolutionService resolutionService) {
        this.subjectProjection = subjectProjection;
        this.eventRepository = eventRepository;
        this.resolutionService = resolutionService;
    }

    @GetMapping
    public String subjectList(Model model) {
        List<SubjectSummary> subjects = subjectProjection.listSubjects();
        List<ConflictResolutionService.FlagSummary> flags = resolutionService.listUnresolvedFlags();
        // Subjects with unresolved flags
        Set<String> flaggedSubjects = flags.stream()
                .map(ConflictResolutionService.FlagSummary::subjectId)
                .collect(Collectors.toSet());
        model.addAttribute("subjects", subjects);
        model.addAttribute("flagCount", flags.size());
        model.addAttribute("flaggedSubjects", flaggedSubjects);
        return "subject-list";
    }

    @GetMapping("/subjects/{id}")
    public String subjectDetail(@PathVariable UUID id, Model model) {
        List<Event> events = eventRepository.findBySubjectId(id);
        // Find flagged event IDs for this subject (unresolved flags only)
        Set<String> flaggedEventIds = resolutionService.listUnresolvedFlags().stream()
                .filter(f -> id.toString().equals(f.subjectId()))
                .map(ConflictResolutionService.FlagSummary::sourceEventId)
                .collect(Collectors.toSet());
        model.addAttribute("subjectId", id.toString());
        model.addAttribute("events", events);
        model.addAttribute("flaggedEventIds", flaggedEventIds);
        return "subject-detail";
    }

    @GetMapping("/flags")
    public String flagList(Model model) {
        model.addAttribute("flags", resolutionService.listUnresolvedFlags());
        return "flag-list";
    }

    @GetMapping("/flags/{flagId}")
    public String flagDetail(@PathVariable UUID flagId, Model model) {
        ConflictResolutionService.FlagDetail detail = resolutionService.getFlagDetail(flagId);
        if (detail == null) {
            return "redirect:/admin/flags";
        }
        model.addAttribute("flag", detail);
        return "flag-detail";
    }

    @PostMapping("/flags/{flagId}/resolve")
    public String resolveFlag(@PathVariable UUID flagId,
                              @RequestParam String resolution,
                              @RequestParam(required = false) String reclassifiedSubjectId,
                              @RequestParam String reason,
                              RedirectAttributes redirectAttributes) {
        // Hardcoded actor for Phase 1 (no auth yet — Phase 2)
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        try {
            UUID reclassifiedId = (reclassifiedSubjectId != null && !reclassifiedSubjectId.isBlank())
                    ? UUID.fromString(reclassifiedSubjectId) : null;
            resolutionService.resolve(flagId, resolution, reclassifiedId, actorId, reason);
            redirectAttributes.addFlashAttribute("success",
                    "Flag resolved: " + resolution);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/flags/" + flagId;
        }
        return "redirect:/admin/flags";
    }
}
