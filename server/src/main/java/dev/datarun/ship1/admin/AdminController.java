package dev.datarun.ship1.admin;

import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Minimal Ship-1 admin UI: full event stream + unresolved flag list.
 * Closes the loop from spec §6.3 ("Admin UI: lists all events, lists unresolved flags").
 * Resolution actions are out of scope for Ship-1.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EventRepository events;

    public AdminController(EventRepository events) {
        this.events = events;
    }

    @GetMapping({"", "/"})
    public String home() {
        return "redirect:/admin/events";
    }

    @GetMapping("/events")
    public String events(Model model) {
        List<Event> all = events.findAll();
        model.addAttribute("events", all);
        return "admin/events";
    }

    @GetMapping("/flags")
    public String flags(Model model) {
        // Ship-1 has no resolution pipeline, so "unresolved flags" == all conflict_detected events.
        List<Event> flagList = events.findByShapeRefPrefix("conflict_detected/");
        model.addAttribute("flags", flagList);
        return "admin/flags";
    }
}
