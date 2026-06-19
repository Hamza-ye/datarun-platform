package dev.datarun.server.authorization;

import dev.datarun.server.config.AdminCommandCapabilityPolicy;
import dev.datarun.server.config.AssignmentAdminCapabilityPolicy;
import dev.datarun.server.event.Event;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/web-admin/assignments")
public class WebAdminAssignmentController {

    private final WebAdminSessionService sessionService;
    private final AdminCommandCapabilityService adminCommandCapabilityService;
    private final AssignmentService assignmentService;
    private final ScopeResolver scopeResolver;
    private final WebAdminAssignmentAccessService assignmentAccessService;

    public WebAdminAssignmentController(
            WebAdminSessionService sessionService,
            AdminCommandCapabilityService adminCommandCapabilityService,
            AssignmentService assignmentService,
            ScopeResolver scopeResolver,
            WebAdminAssignmentAccessService assignmentAccessService) {
        this.sessionService = sessionService;
        this.adminCommandCapabilityService = adminCommandCapabilityService;
        this.assignmentService = assignmentService;
        this.scopeResolver = scopeResolver;
        this.assignmentAccessService = assignmentAccessService;
    }

    @GetMapping
    public String index(HttpServletRequest request, Model model) {
        WebAdminSessionContext context = requireContextForPage(request);
        requireWebAdminAccess(context);
        populateModel(model, context);
        return "web-admin/assignments";
    }

    @PostMapping("/create")
    public String createAssignment(HttpServletRequest request,
                                   @RequestParam("target_actor_id") String targetActorId,
                                   @RequestParam String role,
                                   @RequestParam(value = "geographic_scope", required = false)
                                           String geographicScope,
                                   @RequestParam(value = "subject_list", required = false)
                                           String subjectList,
                                   @RequestParam(value = "activity_list", required = false)
                                           String activityList,
                                   @RequestParam(value = "valid_from", required = false)
                                           String validFrom,
                                   @RequestParam(value = "valid_to", required = false)
                                           String validTo,
                                   RedirectAttributes redirectAttributes) {
        WebAdminSessionContext context = requireContextForAction(request);
        requireWebAdminAccess(context);
        try {
            Event event = assignmentService.createAssignment(
                    context.actorId(),
                    requiredUuid(targetActorId, "target_actor_id"),
                    requiredText(role, "role"),
                    optionalUuid(geographicScope, "geographic_scope"),
                    optionalUuidList(subjectList, "subject_list"),
                    optionalTextList(activityList),
                    optionalOffsetDateTime(validFrom, OffsetDateTime.now(ZoneOffset.UTC),
                            "valid_from"),
                    optionalOffsetDateTime(validTo, null, "valid_to"));
            redirectAttributes.addFlashAttribute("success",
                    "Assignment created: " + event.subjectRef().path("id").asText());
            return "redirect:/web-admin/assignments";
        } catch (IllegalArgumentException e) {
            throw responseForAssignmentFailure(e);
        }
    }

    @PostMapping("/end")
    public String endAssignment(HttpServletRequest request,
                                @RequestParam("assignment_id") String assignmentId,
                                @RequestParam(required = false) String reason,
                                RedirectAttributes redirectAttributes) {
        WebAdminSessionContext context = requireContextForAction(request);
        requireWebAdminAccess(context);
        try {
            assignmentService.endAssignment(
                    requiredUuid(assignmentId, "assignment_id"),
                    context.actorId(),
                    blankToNull(reason));
            redirectAttributes.addFlashAttribute("success",
                    "Assignment ended: " + assignmentId);
            return "redirect:/web-admin/assignments";
        } catch (IllegalArgumentException e) {
            throw responseForAssignmentFailure(e);
        }
    }

    private void populateModel(Model model, WebAdminSessionContext context) {
        List<AssignmentAuthorityView> activeAssignments = scopeResolver
                .getActiveAssignments(context.actorId())
                .stream()
                .map(this::toView)
                .toList();
        model.addAttribute("actorId", context.actorId());
        model.addAttribute("activeAssignments", activeAssignments);
        model.addAttribute("canCreate", assignmentAccessService.hasCommand(
                context.actorId(), AssignmentAdminCapabilityPolicy.CREATE_COMMAND));
        model.addAttribute("canEnd", assignmentAccessService.hasCommand(
                context.actorId(), AssignmentAdminCapabilityPolicy.END_COMMAND));
    }

    private AssignmentAuthorityView toView(ActiveAssignment assignment) {
        return new AssignmentAuthorityView(
                assignment.assignmentId(),
                assignment.role(),
                assignment.geographicScope() == null
                        ? "unrestricted"
                        : assignment.geographicScope().toString(),
                assignment.geographicPath(),
                assignment.subjectList() == null
                        ? "unrestricted"
                        : joinValues(assignment.subjectList().stream()
                        .map(UUID::toString)
                        .toList()),
                assignment.activityList() == null
                        ? "unrestricted"
                        : joinValues(assignment.activityList()),
                assignment.validFrom().toString(),
                assignment.validTo() == null ? "indefinite" : assignment.validTo().toString(),
                assignmentAccessService.roleGrants(
                        assignment.role(), AssignmentAdminCapabilityPolicy.CREATE_COMMAND),
                assignmentAccessService.roleGrants(
                        assignment.role(), AssignmentAdminCapabilityPolicy.END_COMMAND));
    }

    private WebAdminSessionContext requireContextForPage(HttpServletRequest request) {
        try {
            return sessionService.requireContext(request);
        } catch (WebAdminSessionException e) {
            throw new LoginRedirectException();
        }
    }

    private WebAdminSessionContext requireContextForAction(HttpServletRequest request) {
        try {
            return sessionService.requireContext(request);
        } catch (WebAdminSessionException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.reason(), e);
        }
    }

    private void requireWebAdminAccess(WebAdminSessionContext context) {
        if (!adminCommandCapabilityService.actorGrants(
                context.actorId(), AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "missing admin command: "
                    + AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        }
    }

    private ResponseStatusException responseForAssignmentFailure(IllegalArgumentException e) {
        HttpStatus status = isAssignmentAuthorityFailure(e.getMessage())
                ? HttpStatus.FORBIDDEN
                : HttpStatus.BAD_REQUEST;
        return new ResponseStatusException(status, e.getMessage(), e);
    }

    private boolean isAssignmentAuthorityFailure(String message) {
        return message != null
                && (message.contains("Assignment command authority violation")
                || message.contains("Scope containment violation")
                || message.contains("Assignment authority violation"));
    }

    private UUID requiredUuid(String value, String field) {
        String normalized = requiredText(value, field);
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + " must be a UUID");
        }
    }

    private UUID optionalUuid(String value, String field) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + " must be a UUID");
        }
    }

    private List<UUID> optionalUuidList(String value, String field) {
        List<String> values = optionalTextList(value);
        if (values == null) {
            return null;
        }
        try {
            return values.stream().map(UUID::fromString).toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + " must contain only UUID values");
        }
    }

    private List<String> optionalTextList(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return null;
        }
        List<String> values = Arrays.stream(normalized.split("[,\\n\\r]+"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
        return values.isEmpty() ? null : values;
    }

    private OffsetDateTime optionalOffsetDateTime(String value,
                                                  OffsetDateTime defaultValue,
                                                  String field) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return defaultValue;
        }
        try {
            return OffsetDateTime.parse(normalized);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(field + " must be an ISO-8601 offset timestamp");
        }
    }

    private String requiredText(String value, String field) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return normalized;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String joinValues(List<String> values) {
        return String.join(", ", values);
    }

    @ExceptionHandler(LoginRedirectException.class)
    public String loginRedirect() {
        return "redirect:/web-admin/login";
    }

    public record AssignmentAuthorityView(
            UUID assignmentId,
            String role,
            String geographicScope,
            String geographicPath,
            String subjectList,
            String activityList,
            String validFrom,
            String validTo,
            boolean grantsCreate,
            boolean grantsEnd
    ) {}

    private static class LoginRedirectException extends RuntimeException {}
}
