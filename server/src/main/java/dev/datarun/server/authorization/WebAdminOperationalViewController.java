package dev.datarun.server.authorization;

import dev.datarun.server.config.AdminCommandCapabilityPolicy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/web-admin/operational")
public class WebAdminOperationalViewController {

    private final WebAdminSessionService sessionService;
    private final AdminCommandCapabilityService adminCommandCapabilityService;
    private final WebAdminOperationalViewService operationalViewService;

    public WebAdminOperationalViewController(
            WebAdminSessionService sessionService,
            AdminCommandCapabilityService adminCommandCapabilityService,
            WebAdminOperationalViewService operationalViewService) {
        this.sessionService = sessionService;
        this.adminCommandCapabilityService = adminCommandCapabilityService;
        this.operationalViewService = operationalViewService;
    }

    @GetMapping
    public String index(HttpServletRequest request, Model model) {
        WebAdminSessionContext context = requireContextForPage(request);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        requireCommand(context, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);

        model.addAttribute("actorId", context.actorId());
        model.addAttribute("observation", operationalViewService.observe(context.actorId()));
        return "web-admin/operational";
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
