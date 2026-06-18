package dev.datarun.server.authorization;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;
import java.util.Map;

@Controller
@RequestMapping("/web-admin")
public class WebAdminSessionController {

    private final WebAdminSessionService sessionService;

    public WebAdminSessionController(WebAdminSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<Void> index() {
        return redirect("/web-admin/shell");
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(sessionService.beginLogin(request)))
                .build();
    }

    @GetMapping("/oidc/callback")
    public ResponseEntity<String> oidcCallback(HttpServletRequest request,
                                               @RequestParam(required = false) String state,
                                               @RequestParam(required = false) String code) {
        try {
            sessionService.completeLogin(request, state, code);
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .location(URI.create("/web-admin/shell"))
                    .build();
        } catch (WebAdminSessionException e) {
            return denied(e.reason());
        }
    }

    @GetMapping(value = "/shell", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> shell(HttpServletRequest request) {
        WebAdminSessionContext context;
        try {
            context = sessionService.requireContext(request);
        } catch (WebAdminSessionException e) {
            if (e.loginRedirectAllowed()) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("/web-admin/login"))
                        .build();
            }
            return denied(e.reason());
        }

        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        String csrfInput = csrf == null ? "" : """
                <input type="hidden" name="%s" value="%s">
                """.formatted(
                HtmlUtils.htmlEscape(csrf.getParameterName()),
                HtmlUtils.htmlEscape(csrf.getToken()));
        String body = """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>Datarun Web Admin</title>
                </head>
                <body>
                  <main>
                    <h1>Datarun Web Admin</h1>
                    <dl>
                      <dt>Actor</dt><dd>%s</dd>
                      <dt>Auth source</dt><dd>%s</dd>
                    </dl>
                    <form method="post" action="/web-admin/session/probe">
                      %s
                      <button type="submit">Check session</button>
                    </form>
                    <form method="post" action="/web-admin/logout">
                      %s
                      <button type="submit">Sign out</button>
                    </form>
                  </main>
                </body>
                </html>
                """.formatted(
                HtmlUtils.htmlEscape(context.actorId().toString()),
                HtmlUtils.htmlEscape(context.authSource()),
                csrfInput,
                csrfInput);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(body);
    }

    @PostMapping(value = "/session/probe", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> sessionProbe(HttpServletRequest request) {
        try {
            WebAdminSessionContext context = sessionService.requireContext(request);
            return ResponseEntity.ok(Map.of(
                    "actor_id", context.actorId().toString(),
                    "auth_source", context.authSource(),
                    "session_correlation_id", context.sessionCorrelationId()));
        } catch (WebAdminSessionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.reason()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        sessionService.logout(request);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create("/web-admin/login"))
                .build();
    }

    private ResponseEntity<Void> redirect(String path) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(path))
                .build();
    }

    private ResponseEntity<String> denied(String reason) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":\"" + HtmlUtils.htmlEscape(reason) + "\"}");
    }
}
