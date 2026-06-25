package dev.datarun.server.authorization;

import dev.datarun.server.config.AdminCommandCapabilityPolicy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

import java.util.UUID;

@Controller
@RequestMapping("/web-admin/operational")
public class WebAdminOperationalViewController {

    static final String ATTENTION_TOKEN_PARAM = "attentionToken";
    static final String WORK_EVIDENCE_TOKEN_PARAM = "workToken";
    private static final String ATTENTION_TOKEN_ATTR = "webAdminOperationalAttentionToken";
    private static final String ATTENTION_FLAG_ID_ATTR = "webAdminOperationalAttentionFlagId";
    private static final String WORK_EVIDENCE_TOKEN_ATTR = "webAdminConfiguredWorkEvidenceToken";
    private static final String WORK_EVIDENCE_EVENT_ID_ATTR = "webAdminConfiguredWorkEvidenceEventId";

    private final WebAdminSessionService sessionService;
    private final AdminCommandCapabilityService adminCommandCapabilityService;
    private final WebAdminOperationalViewService operationalViewService;
    private final ScopedOperationalReportSnapshotService reportSnapshotService;
    private final OperationalResponsibilityHandoffService handoffService;

    public WebAdminOperationalViewController(
            WebAdminSessionService sessionService,
            AdminCommandCapabilityService adminCommandCapabilityService,
            WebAdminOperationalViewService operationalViewService,
            ScopedOperationalReportSnapshotService reportSnapshotService,
            OperationalResponsibilityHandoffService handoffService) {
        this.sessionService = sessionService;
        this.adminCommandCapabilityService = adminCommandCapabilityService;
        this.operationalViewService = operationalViewService;
        this.reportSnapshotService = reportSnapshotService;
        this.handoffService = handoffService;
    }

    @GetMapping
    public String index(HttpServletRequest request, Model model) {
        WebAdminSessionContext context = requireContextForPage(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);

        model.addAttribute("observation", operationalViewService.observe(context.actorId()));
        return "web-admin/operational";
    }

    @GetMapping("/report")
    public String report(HttpServletRequest request, Model model) {
        WebAdminSessionContext context = requireContextForPage(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);

        ScopedOperationalReportSnapshotService.ScopedOperationalReportSnapshot snapshot =
                reportSnapshotService.snapshot(context.actorId());
        model.addAttribute("snapshot", snapshot);
        bindWorkEvidenceToken(request, model, snapshot);
        return "web-admin/operational-report";
    }

    @GetMapping("/evidence")
    public String configuredWorkEvidence(
            HttpServletRequest request,
            @RequestParam(name = WORK_EVIDENCE_TOKEN_PARAM, required = false)
            String workToken,
            Model model) {
        WebAdminSessionContext context = requireContextForPage(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);

        model.addAttribute(
                "evidence",
                readWorkEvidenceEventId(request, workToken)
                        .map(eventId -> reportSnapshotService.configuredWorkEvidence(
                                context.actorId(), eventId))
                        .orElseGet(() ->
                                ScopedOperationalReportSnapshotService.ConfiguredWorkEvidence
                                        .notVisible(ScopedOperationalReportSnapshotService
                                                .NO_VISIBLE_CONFIGURED_WORK_EVIDENCE)));
        return "web-admin/configured-work-evidence";
    }

    @GetMapping("/handoff")
    public String handoff(HttpServletRequest request, Model model) {
        WebAdminSessionContext context = requireContextForPage(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);

        model.addAttribute("handoff", handoffService.context(context.actorId()));
        return "web-admin/operational-handoff";
    }

    @GetMapping("/attention")
    public String attention(HttpServletRequest request, Model model) {
        WebAdminSessionContext context = requireContextForPage(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);

        WebAdminOperationalViewService.AttentionReview review =
                operationalViewService.review(context.actorId());
        model.addAttribute("review", review);
        bindAttentionToken(request, model, review);
        return "web-admin/attention-review";
    }

    @PostMapping("/attention/resolve")
    public String resolveAttention(HttpServletRequest request,
                                   @RequestParam(ATTENTION_TOKEN_PARAM) String attentionToken,
                                   @RequestParam String resolution,
                                   @RequestParam(required = false) String reason,
                                   RedirectAttributes redirectAttributes) {
        WebAdminSessionContext context = requireContextForPage(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);

        try {
            UUID flagId = requireAttentionFlagId(request, attentionToken);
            operationalViewService.resolveAttention(
                    context.actorId(), flagId, resolution, reason);
            clearAttentionToken(request);
            redirectAttributes.addFlashAttribute(
                    "success", "Review recorded. The item is resolved.");
            return "redirect:/web-admin/operational";
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    private void bindAttentionToken(HttpServletRequest request, Model model,
                                    WebAdminOperationalViewService.AttentionReview review) {
        HttpSession session = request.getSession(false);
        if (session == null || !review.hasItem()) {
            clearAttentionToken(request);
            return;
        }
        String token = UUID.randomUUID().toString();
        session.setAttribute(ATTENTION_TOKEN_ATTR, token);
        session.setAttribute(ATTENTION_FLAG_ID_ATTR, review.item().flagId().toString());
        model.addAttribute(ATTENTION_TOKEN_PARAM, token);
    }

    private void bindWorkEvidenceToken(
            HttpServletRequest request,
            Model model,
            ScopedOperationalReportSnapshotService.ScopedOperationalReportSnapshot snapshot) {
        HttpSession session = request.getSession(false);
        if (session == null || !snapshot.hasConfiguredWorkEvidenceTarget()) {
            clearWorkEvidenceToken(request);
            return;
        }
        String token = UUID.randomUUID().toString();
        session.setAttribute(WORK_EVIDENCE_TOKEN_ATTR, token);
        session.setAttribute(WORK_EVIDENCE_EVENT_ID_ATTR,
                snapshot.traceContext().eventId().toString());
        model.addAttribute("configuredWorkEvidencePath",
                "/web-admin/operational/evidence?"
                        + WORK_EVIDENCE_TOKEN_PARAM + "=" + token);
    }

    private UUID requireAttentionFlagId(HttpServletRequest request, String attentionToken) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalArgumentException("No reviewed attention item is bound to this session.");
        }
        Object expectedToken = session.getAttribute(ATTENTION_TOKEN_ATTR);
        Object flagId = session.getAttribute(ATTENTION_FLAG_ID_ATTR);
        if (!(expectedToken instanceof String expected)
                || !(flagId instanceof String flag)
                || attentionToken == null
                || !expected.equals(attentionToken)) {
            throw new IllegalArgumentException("No reviewed attention item is bound to this session.");
        }
        return UUID.fromString(flag);
    }

    private void clearAttentionToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(ATTENTION_TOKEN_ATTR);
            session.removeAttribute(ATTENTION_FLAG_ID_ATTR);
        }
    }

    private java.util.Optional<UUID> readWorkEvidenceEventId(
            HttpServletRequest request, String workToken) {
        HttpSession session = request.getSession(false);
        if (session == null || workToken == null || workToken.isBlank()) {
            return java.util.Optional.empty();
        }
        Object expectedToken = session.getAttribute(WORK_EVIDENCE_TOKEN_ATTR);
        Object eventId = session.getAttribute(WORK_EVIDENCE_EVENT_ID_ATTR);
        if (!(expectedToken instanceof String expected)
                || !(eventId instanceof String event)
                || !expected.equals(workToken)) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(UUID.fromString(event));
        } catch (RuntimeException e) {
            return java.util.Optional.empty();
        }
    }

    private void clearWorkEvidenceToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WORK_EVIDENCE_TOKEN_ATTR);
            session.removeAttribute(WORK_EVIDENCE_EVENT_ID_ATTR);
        }
    }

    private WebAdminSessionContext requireContextForPage(HttpServletRequest request) {
        try {
            return sessionService.requireContext(request);
        } catch (WebAdminSessionException e) {
            throw new LoginRedirectException();
        }
    }

    private void requireCommand(WebAdminSessionContext context, String command) {
        if (!adminCommandCapabilityService.actorGrants(context.actorId(), command)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "missing admin command: " + command);
        }
    }

    @ExceptionHandler(LoginRedirectException.class)
    public String loginRedirect() {
        return "redirect:/web-admin/login";
    }

    private static class LoginRedirectException extends RuntimeException {}
}
