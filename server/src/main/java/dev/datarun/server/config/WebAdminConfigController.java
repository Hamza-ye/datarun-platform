package dev.datarun.server.config;

import dev.datarun.server.authorization.AdminCommandCapabilityService;
import dev.datarun.server.authorization.WebAdminSessionContext;
import dev.datarun.server.authorization.WebAdminSessionException;
import dev.datarun.server.authorization.WebAdminSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/web-admin/config")
public class WebAdminConfigController {

    private static final List<String> CONFIG_ADMIN_COMMANDS = List.of(
            AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR,
            AdminCommandCapabilityPolicy.CONFIG_ADMIN_VALIDATE,
            AdminCommandCapabilityPolicy.CONFIG_ADMIN_READINESS_REVIEW,
            AdminCommandCapabilityPolicy.CONFIG_ADMIN_APPROVE,
            AdminCommandCapabilityPolicy.CONFIG_ADMIN_PUBLISH);

    private final WebAdminSessionService sessionService;
    private final AdminCommandCapabilityService adminCommandCapabilityService;
    private final WebAdminConfigCandidateService candidateService;
    private final ConfigPackager configPackager;

    public WebAdminConfigController(
            WebAdminSessionService sessionService,
            AdminCommandCapabilityService adminCommandCapabilityService,
            WebAdminConfigCandidateService candidateService,
            ConfigPackager configPackager) {
        this.sessionService = sessionService;
        this.adminCommandCapabilityService = adminCommandCapabilityService;
        this.candidateService = candidateService;
        this.configPackager = configPackager;
    }

    @GetMapping
    public String index(HttpServletRequest request, Model model) {
        WebAdminSessionContext context = requireContextForPage(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireAnyConfigCommand(context);
        populateModel(model, context);
        return "web-admin/config";
    }

    @PostMapping("/draft")
    public String saveDraft(HttpServletRequest request,
                            @RequestParam String candidateJson,
                            @RequestParam(required = false) String expectedHash,
                            RedirectAttributes redirectAttributes) {
        WebAdminSessionContext context = requireContextForAction(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR);
        try {
            var candidate = candidateService.saveDraft(
                    candidateJson, context.actorId(), expectedHash);
            redirectAttributes.addFlashAttribute("success",
                    "Draft saved for candidate " + shortHash(candidate.contentHash()));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web-admin/config";
    }

    @PostMapping("/validate")
    public String validateCandidate(HttpServletRequest request,
                                    @RequestParam(required = false) String expectedHash,
                                    RedirectAttributes redirectAttributes) {
        WebAdminSessionContext context = requireContextForAction(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.CONFIG_ADMIN_VALIDATE);
        try {
            var outcome = candidateService.validateCurrent(context.actorId(), expectedHash);
            if (outcome.passed()) {
                redirectAttributes.addFlashAttribute("success", "Candidate validation passed");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Candidate validation failed: "
                                + String.join("; ", outcome.violations()));
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web-admin/config";
    }

    @PostMapping("/readiness")
    public String recordReadiness(HttpServletRequest request,
                                  @RequestParam String readinessStatus,
                                  @RequestParam(required = false) String note,
                                  @RequestParam(required = false) String expectedHash,
                                  RedirectAttributes redirectAttributes) {
        WebAdminSessionContext context = requireContextForAction(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.CONFIG_ADMIN_READINESS_REVIEW);
        try {
            candidateService.recordReadiness(
                    context.actorId(), readinessStatus, note, expectedHash);
            redirectAttributes.addFlashAttribute("success",
                    "Readiness review recorded as " + readinessStatus);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web-admin/config";
    }

    @PostMapping("/approve")
    public String approveCandidate(HttpServletRequest request,
                                   @RequestParam(required = false) String expectedHash,
                                   RedirectAttributes redirectAttributes) {
        WebAdminSessionContext context = requireContextForAction(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.CONFIG_ADMIN_APPROVE);
        try {
            var candidate = candidateService.approveCurrent(context.actorId(), expectedHash);
            redirectAttributes.addFlashAttribute("success",
                    "Candidate approved for " + shortHash(candidate.approvalHash()));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web-admin/config";
    }

    @PostMapping("/publish")
    public String publishCandidate(HttpServletRequest request,
                                   @RequestParam(required = false) String expectedHash,
                                   RedirectAttributes redirectAttributes) {
        WebAdminSessionContext context = requireContextForAction(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.CONFIG_ADMIN_PUBLISH);
        try {
            var outcome = candidateService.publishApproved(context.actorId(), expectedHash);
            redirectAttributes.addFlashAttribute("success",
                    "Published config v" + outcome.configVersion()
                            + " (new package: " + outcome.published() + ")");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web-admin/config";
    }

    private void populateModel(Model model, WebAdminSessionContext context) {
        model.addAttribute("actorId", context.actorId());
        model.addAttribute("candidate", candidateService.currentOrEmpty());
        model.addAttribute("currentVersion", configPackager.getLatestVersion());
        model.addAttribute("canAuthor", grants(context, AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR));
        model.addAttribute("canValidate", grants(context, AdminCommandCapabilityPolicy.CONFIG_ADMIN_VALIDATE));
        model.addAttribute("canReadinessReview", grants(context, AdminCommandCapabilityPolicy.CONFIG_ADMIN_READINESS_REVIEW));
        model.addAttribute("canApprove", grants(context, AdminCommandCapabilityPolicy.CONFIG_ADMIN_APPROVE));
        model.addAttribute("canPublish", grants(context, AdminCommandCapabilityPolicy.CONFIG_ADMIN_PUBLISH));
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

    private void requireCommand(WebAdminSessionContext context, String command) {
        if (!grants(context, command)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "missing admin command: " + command);
        }
    }

    private void requireAnyConfigCommand(WebAdminSessionContext context) {
        if (CONFIG_ADMIN_COMMANDS.stream().noneMatch(command -> grants(context, command))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "missing config admin command");
        }
    }

    private boolean grants(WebAdminSessionContext context, String command) {
        return adminCommandCapabilityService.actorGrants(context.actorId(), command);
    }

    private String shortHash(String hash) {
        if (hash == null || hash.length() < 12) {
            return hash == null ? "" : hash;
        }
        return hash.substring(0, 12);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(LoginRedirectException.class)
    public String loginRedirect() {
        return "redirect:/web-admin/login";
    }

    private static class LoginRedirectException extends RuntimeException {}
}
