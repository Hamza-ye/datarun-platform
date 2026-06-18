package dev.datarun.server.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class WebAdminSessionService {

    public static final String ACTOR_ID_ATTR = "webAdmin.actorId";
    public static final String ISSUER_ATTR = "webAdmin.issuer";
    public static final String SUBJECT_ATTR = "webAdmin.subject";
    public static final String AUTH_SOURCE_ATTR = "webAdmin.authSource";
    public static final String LOGIN_TIME_ATTR = "webAdmin.loginTime";
    public static final String LAST_SEEN_TIME_ATTR = "webAdmin.lastSeenTime";
    public static final String EXPIRES_AT_ATTR = "webAdmin.expiresAt";
    public static final String SESSION_CORRELATION_ID_ATTR = "webAdmin.sessionCorrelationId";

    private static final String LOGIN_STATE_ATTR = "webAdmin.loginState";
    private static final String LOGIN_STATE_EXPIRES_AT_ATTR = "webAdmin.loginStateExpiresAt";
    private static final String LOGIN_NONCE_ATTR = "webAdmin.loginNonce";
    private static final String AUTH_SOURCE = "oidc-jwks-principal";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebAdminSessionService.class);

    private final AuthProperties authProperties;
    private final WebAdminSessionProperties sessionProperties;
    private final OidcJwksTokenValidator tokenValidator;
    private final AuthPrincipalBindingRepository bindingRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Autowired
    public WebAdminSessionService(AuthProperties authProperties,
                                  WebAdminSessionProperties sessionProperties,
                                  OidcJwksTokenValidator tokenValidator,
                                  AuthPrincipalBindingRepository bindingRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this(authProperties, sessionProperties, tokenValidator, bindingRepository,
                eventPublisher, Clock.systemUTC());
    }

    WebAdminSessionService(AuthProperties authProperties,
                           WebAdminSessionProperties sessionProperties,
                           OidcJwksTokenValidator tokenValidator,
                           AuthPrincipalBindingRepository bindingRepository,
                           ApplicationEventPublisher eventPublisher,
                           Clock clock) {
        this.authProperties = authProperties;
        this.sessionProperties = sessionProperties;
        this.tokenValidator = tokenValidator;
        this.bindingRepository = bindingRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public String beginLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        session.setAttribute(LOGIN_STATE_ATTR, state);
        session.setAttribute(LOGIN_NONCE_ATTR, nonce);
        session.setAttribute(LOGIN_STATE_EXPIRES_AT_ATTR,
                clock.instant().plus(sessionProperties.loginStateTtl()));

        String redirectUri = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/web-admin/oidc/callback")
                .replaceQuery(null)
                .build()
                .toUriString();

        return UriComponentsBuilder
                .fromUriString(sessionProperties.oidcAuthorizationUri(authProperties.oidcIssuer()))
                .queryParam("response_type", "id_token")
                .queryParam("response_mode", "form_post")
                .queryParam("scope", "openid")
                .queryParam("client_id", sessionProperties.oidcClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .queryParam("nonce", nonce)
                .build()
                .encode()
                .toUriString();
    }

    public WebAdminSessionContext completeLogin(
            HttpServletRequest request, String state, String idToken) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            audit("web_admin_login_failed", null, null, null, null, "missing_login_state");
            throw new WebAdminSessionException("missing_login_state", true);
        }
        validateLoginState(session, state);
        if (idToken == null || idToken.isBlank()) {
            invalidate(session);
            audit("web_admin_login_failed", null, null, null, null, "missing_id_token");
            throw new WebAdminSessionException("missing_id_token", true);
        }
        String expectedNonce = (String) session.getAttribute(LOGIN_NONCE_ATTR);

        JwtPrincipal principal;
        try {
            principal = tokenValidator.validateLoginToken(idToken, expectedNonce);
        } catch (AuthResolutionException e) {
            invalidate(session);
            audit("web_admin_login_failed", null, null, null, null, e.getMessage());
            throw new WebAdminSessionException("invalid_oidc_principal", true);
        }

        UUID actorId = bindingRepository.resolveActor(principal.issuer(), principal.subject());
        if (actorId == null) {
            invalidate(session);
            audit("web_admin_login_failed", null, principal.issuer(), principal.subject(),
                    null, "unmapped_principal");
            throw new WebAdminSessionException("unmapped_principal", true);
        }

        try {
            request.changeSessionId();
        } catch (IllegalStateException ignored) {
            // The session may already have been rotated by the container.
        }

        Instant now = clock.instant();
        Instant expiresAt = now.plus(sessionProperties.sessionTimeout());
        String sessionCorrelationId = UUID.randomUUID().toString();
        session.removeAttribute(LOGIN_STATE_ATTR);
        session.removeAttribute(LOGIN_STATE_EXPIRES_AT_ATTR);
        session.removeAttribute(LOGIN_NONCE_ATTR);
        session.setMaxInactiveInterval(Math.toIntExact(sessionProperties.sessionTimeout().toSeconds()));
        session.setAttribute(ACTOR_ID_ATTR, actorId.toString());
        session.setAttribute(ISSUER_ATTR, principal.issuer());
        session.setAttribute(SUBJECT_ATTR, principal.subject());
        session.setAttribute(AUTH_SOURCE_ATTR, AUTH_SOURCE);
        session.setAttribute(LOGIN_TIME_ATTR, now);
        session.setAttribute(LAST_SEEN_TIME_ATTR, now);
        session.setAttribute(EXPIRES_AT_ATTR, expiresAt);
        session.setAttribute(SESSION_CORRELATION_ID_ATTR, sessionCorrelationId);

        WebAdminSessionContext context = new WebAdminSessionContext(
                actorId,
                principal.issuer(),
                principal.subject(),
                AUTH_SOURCE,
                sessionCorrelationId,
                now,
                now,
                expiresAt);
        audit("web_admin_login_succeeded", actorId, principal.issuer(), principal.subject(),
                sessionCorrelationId, null);
        return context;
    }

    public WebAdminSessionContext requireContext(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new WebAdminSessionException("missing_session", true);
        }

        WebAdminSessionContext context = readContext(session);
        Instant now = clock.instant();
        if (!context.expiresAt().isAfter(now)) {
            audit("web_admin_session_expired", context.actorId(), context.issuer(),
                    context.subject(), context.sessionCorrelationId(), "expired");
            invalidate(session);
            throw new WebAdminSessionException("session_expired", true);
        }

        UUID activeActor = bindingRepository.resolveActor(context.issuer(), context.subject());
        if (!Objects.equals(activeActor, context.actorId())) {
            audit("web_admin_session_denied", context.actorId(), context.issuer(),
                    context.subject(), context.sessionCorrelationId(), "principal_binding_changed");
            invalidate(session);
            throw new WebAdminSessionException("principal_binding_changed", true);
        }

        session.setAttribute(LAST_SEEN_TIME_ATTR, now);
        return new WebAdminSessionContext(
                context.actorId(),
                context.issuer(),
                context.subject(),
                context.authSource(),
                context.sessionCorrelationId(),
                context.loginTime(),
                now,
                context.expiresAt());
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        WebAdminSessionContext context = tryReadContext(session);
        if (context != null) {
            audit("web_admin_logout", context.actorId(), context.issuer(), context.subject(),
                    context.sessionCorrelationId(), null);
        }
        invalidate(session);
    }

    private void validateLoginState(HttpSession session, String state) {
        String expectedState = (String) session.getAttribute(LOGIN_STATE_ATTR);
        Instant expiresAt = (Instant) session.getAttribute(LOGIN_STATE_EXPIRES_AT_ATTR);
        if (expectedState == null || expiresAt == null) {
            invalidate(session);
            audit("web_admin_login_failed", null, null, null, null, "missing_login_state");
            throw new WebAdminSessionException("missing_login_state", true);
        }
        if (!expiresAt.isAfter(clock.instant())) {
            invalidate(session);
            audit("web_admin_login_failed", null, null, null, null, "login_state_expired");
            throw new WebAdminSessionException("login_state_expired", true);
        }
        if (state == null || !expectedState.equals(state)) {
            invalidate(session);
            audit("web_admin_login_failed", null, null, null, null, "login_state_mismatch");
            throw new WebAdminSessionException("login_state_mismatch", true);
        }
    }

    private WebAdminSessionContext readContext(HttpSession session) {
        WebAdminSessionContext context = tryReadContext(session);
        if (context == null) {
            invalidate(session);
            throw new WebAdminSessionException("invalid_session_state", true);
        }
        return context;
    }

    private WebAdminSessionContext tryReadContext(HttpSession session) {
        try {
            String actorId = (String) session.getAttribute(ACTOR_ID_ATTR);
            String issuer = (String) session.getAttribute(ISSUER_ATTR);
            String subject = (String) session.getAttribute(SUBJECT_ATTR);
            String authSource = (String) session.getAttribute(AUTH_SOURCE_ATTR);
            String sessionCorrelationId = (String) session.getAttribute(SESSION_CORRELATION_ID_ATTR);
            Instant loginTime = (Instant) session.getAttribute(LOGIN_TIME_ATTR);
            Instant lastSeenTime = (Instant) session.getAttribute(LAST_SEEN_TIME_ATTR);
            Instant expiresAt = (Instant) session.getAttribute(EXPIRES_AT_ATTR);
            if (actorId == null || issuer == null || subject == null || authSource == null
                    || sessionCorrelationId == null || loginTime == null || lastSeenTime == null
                    || expiresAt == null) {
                return null;
            }
            return new WebAdminSessionContext(
                    UUID.fromString(actorId),
                    issuer,
                    subject,
                    authSource,
                    sessionCorrelationId,
                    loginTime,
                    lastSeenTime,
                    expiresAt);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void invalidate(HttpSession session) {
        try {
            session.invalidate();
        } catch (IllegalStateException ignored) {
            // Already invalidated.
        }
    }

    private void audit(String eventType,
                       UUID actorId,
                       String issuer,
                       String subject,
                       String sessionCorrelationId,
                       String reason) {
        WebAdminSessionAuditEvent event = new WebAdminSessionAuditEvent(
                eventType, actorId, issuer, subject, sessionCorrelationId, reason, clock.instant());
        eventPublisher.publishEvent(event);
        LOGGER.info("event={} actor_id={} issuer={} subject={} session_correlation_id={} reason={}",
                eventType, actorId, issuer, subject, sessionCorrelationId, reason);
    }
}
