package dev.datarun.server.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.authorization.ActorTokenRepository;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.ServerIdentity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Dev-only bootstrap controller for provisioning device credentials.
 * Creates an actor with token + root-scope assignment so a mobile device
 * can sync immediately without Keycloak.
 *
 * NOT for production — will be replaced by Keycloak integration.
 */
@Controller
@RequestMapping("/admin/dev")
public class DevBootstrapController {

    private final ActorTokenRepository tokenRepository;
    private final EventRepository eventRepository;
    private final ServerIdentity serverIdentity;
    private final ObjectMapper objectMapper;

    public DevBootstrapController(ActorTokenRepository tokenRepository,
                                  EventRepository eventRepository,
                                  ServerIdentity serverIdentity,
                                  ObjectMapper objectMapper) {
        this.tokenRepository = tokenRepository;
        this.eventRepository = eventRepository;
        this.serverIdentity = serverIdentity;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/provision")
    public String provisionForm(Model model) {
        return "dev/provision";
    }

    @PostMapping("/provision")
    public String provision(@RequestParam(defaultValue = "field_worker") String role,
                            RedirectAttributes redirectAttributes) {
        UUID actorId = UUID.randomUUID();
        String token = tokenRepository.createToken(actorId);

        // Create root-scope assignment (no geographic/subject/activity constraint)
        createRootAssignment(actorId, role);

        redirectAttributes.addFlashAttribute("actorId", actorId.toString());
        redirectAttributes.addFlashAttribute("token", token);
        redirectAttributes.addFlashAttribute("role", role);
        return "redirect:/admin/dev/provision";
    }

    private void createRootAssignment(UUID targetActorId, String role) {
        UUID assignmentId = UUID.randomUUID();

        ObjectNode payload = objectMapper.createObjectNode();

        ObjectNode targetActor = objectMapper.createObjectNode();
        targetActor.put("type", "actor");
        targetActor.put("id", targetActorId.toString());
        payload.set("target_actor", targetActor);

        payload.put("role", role);

        ObjectNode scope = objectMapper.createObjectNode();
        scope.putNull("geographic");
        scope.putNull("subject_list");
        scope.putNull("activity");
        payload.set("scope", scope);

        payload.put("valid_from", OffsetDateTime.now(ZoneOffset.UTC).toString());
        payload.putNull("valid_to");

        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "assignment");
        subjectRef.put("id", assignmentId.toString());

        // Use a system actor ref for bootstrap
        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "system");
        actorRef.put("id", "dev-bootstrap");

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
    }
}
