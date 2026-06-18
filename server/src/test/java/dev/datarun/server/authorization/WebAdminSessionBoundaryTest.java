package dev.datarun.server.authorization;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebAdminSessionController.class)
@Import({
        AuthProperties.class,
        WebAdminSessionProperties.class,
        WebAdminSessionService.class,
        WebAdminSecurityFoundationConfig.class,
        WebAdminSessionBoundaryTest.TestFakes.class
})
@RecordApplicationEvents
@TestPropertySource(properties = {
        "datarun.auth.mode=oidc-jwks",
        "datarun.auth.oidc.issuer=https://issuer.test/datarun",
        "datarun.auth.oidc.audience=datarun-web-admin",
        "datarun.auth.oidc.jwks-uri=http://127.0.0.1/jwks",
        "datarun.web-admin.oidc.authorization-uri=https://issuer.test/datarun/auth",
        "datarun.web-admin.oidc.client-id=datarun-web-admin",
        "datarun.web-admin.session.timeout=PT30M",
        "datarun.web-admin.login.state-ttl=PT5M"
})
class WebAdminSessionBoundaryTest {

    private static final String ISSUER = "https://issuer.test/datarun";
    private static final String SUBJECT = "admin-principal";
    private static final UUID ACTOR = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_ACTOR = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired private MockMvc mvc;
    @Autowired private ApplicationEvents applicationEvents;
    @Autowired private FakeOidcJwksTokenValidator tokenValidator;
    @Autowired private FakeAuthPrincipalBindingRepository bindingRepository;

    @BeforeEach
    void resetFakes() {
        tokenValidator.reset();
        bindingRepository.reset();
    }

    @Test
    void unauthenticatedBrowserCannotReachProtectedShell() throws Exception {
        mvc.perform(get("/web-admin/shell"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/web-admin/login"))
                .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void loginInitiationRedirectsToOidcProviderWithStateAndNoActorAuthority()
            throws Exception {
        MvcResult result = mvc.perform(get("/web-admin/login"))
                .andExpect(status().isFound())
                .andReturn();

        URI location = URI.create(result.getResponse().getHeader(HttpHeaders.LOCATION));
        assertThat(location.toString()).startsWith("https://issuer.test/datarun/auth");
        var query = UriComponentsBuilder.fromUri(location).build().getQueryParams();
        assertThat(query.getFirst("response_type")).isEqualTo("id_token");
        assertThat(query.getFirst("response_mode")).isEqualTo("form_post");
        assertThat(query.getFirst("client_id")).isEqualTo("datarun-web-admin");
        assertThat(query.getFirst("state")).isNotBlank();

        HttpSession session = result.getRequest().getSession(false);
        assertThat(session.getAttribute(WebAdminSessionService.ACTOR_ID_ATTR)).isNull();
    }

    @Test
    void loginRejectsInvalidExpiredWrongIssuerWrongAudienceMalformedAndUnmappedPrincipals()
            throws Exception {
        for (String token : List.of("invalid", "expired", "wrong-issuer",
                "wrong-audience", "malformed")) {
            tokenValidator.rejectToken(token, "invalid_oidc_jwt");
            MvcResult login = mvc.perform(get("/web-admin/login")).andReturn();
            String state = stateFrom(login);

            MvcResult rejected = mvc.perform(post("/web-admin/oidc/callback")
                            .session((org.springframework.mock.web.MockHttpSession)
                                    login.getRequest().getSession(false))
                            .param("state", state)
                            .param("id_token", token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("invalid_oidc_principal"))
                    .andReturn();

            assertThat(rejected.getRequest().getSession(false)).isNull();
        }

        MvcResult login = mvc.perform(get("/web-admin/login")).andReturn();
        tokenValidator.mapLoginToken("unmapped",
                new JwtPrincipal(ISSUER, "unmapped-principal"), nonceFrom(login));
        mvc.perform(post("/web-admin/oidc/callback")
                        .session((org.springframework.mock.web.MockHttpSession)
                                login.getRequest().getSession(false))
                        .param("state", stateFrom(login))
                        .param("id_token", "unmapped"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("unmapped_principal"));
    }

    @Test
    void loginRejectsOidcTokenWithoutMatchingLoginNonce() throws Exception {
        MvcResult login = mvc.perform(get("/web-admin/login")).andReturn();
        tokenValidator.mapLoginToken("wrong-nonce-token",
                new JwtPrincipal(ISSUER, SUBJECT), "different-nonce");
        bindingRepository.bind(ISSUER, SUBJECT, ACTOR);

        mvc.perform(post("/web-admin/oidc/callback")
                        .session((org.springframework.mock.web.MockHttpSession)
                                login.getRequest().getSession(false))
                        .param("state", stateFrom(login))
                        .param("id_token", "wrong-nonce-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_oidc_principal"));
    }

    @Test
    void loginCreatesSessionOnlyForExplicitActiveBindingAndShellUsesSameActor()
            throws Exception {
        org.springframework.mock.web.MockHttpSession session = loginSession("valid-token", ACTOR);

        mvc.perform(get("/web-admin/shell").session(session))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE));

        MvcResult shell = mvc.perform(get("/web-admin/shell").session(session))
                .andExpect(status().isOk())
                .andReturn();
        CsrfToken csrf = csrfToken(shell);

        mvc.perform(post("/web-admin/session/probe")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("actor_id", OTHER_ACTOR.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actor_id").value(ACTOR.toString()))
                .andExpect(jsonPath("$.auth_source").value("oidc-jwks-principal"));
    }

    @Test
    void idpClaimsRolesAndJwtActorIdDoNotGrantWebAdminSessionWithoutBinding()
            throws Exception {
        MvcResult login = mvc.perform(get("/web-admin/login")).andReturn();
        tokenValidator.mapLoginToken("claims-only-token",
                new JwtPrincipal(ISSUER, "claims-only-principal"), nonceFrom(login));

        mvc.perform(post("/web-admin/oidc/callback")
                        .session((org.springframework.mock.web.MockHttpSession)
                                login.getRequest().getSession(false))
                        .param("state", stateFrom(login))
                        .param("id_token", "claims-only-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("unmapped_principal"));
    }

    @Test
    void stateChangingProtectedRequestRequiresCsrf() throws Exception {
        org.springframework.mock.web.MockHttpSession session = loginSession("csrf-token", ACTOR);

        mvc.perform(post("/web-admin/session/probe").session(session))
                .andExpect(status().isForbidden());

        MvcResult shell = mvc.perform(get("/web-admin/shell").session(session))
                .andExpect(status().isOk())
                .andReturn();
        CsrfToken csrf = csrfToken(shell);
        mvc.perform(post("/web-admin/session/probe")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        org.springframework.mock.web.MockHttpSession session = loginSession("logout-token", ACTOR);
        MvcResult shell = mvc.perform(get("/web-admin/shell").session(session))
                .andReturn();
        CsrfToken csrf = csrfToken(shell);

        mvc.perform(post("/web-admin/logout")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken()))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/web-admin/login"));

        mvc.perform(get("/web-admin/shell").session(session))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/web-admin/login"));
    }

    @Test
    void sessionExpiryDeniesBeforeProtectedAction() throws Exception {
        org.springframework.mock.web.MockHttpSession session = loginSession("expiry-token", ACTOR);
        MvcResult shell = mvc.perform(get("/web-admin/shell").session(session))
                .andReturn();
        CsrfToken csrf = csrfToken(shell);
        session.setAttribute(WebAdminSessionService.EXPIRES_AT_ATTR,
                Instant.now().minusSeconds(1));

        mvc.perform(post("/web-admin/session/probe")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("session_expired"));
    }

    @Test
    void principalBindingDeactivationOrRebindDeniesExistingSessionBeforeAction()
            throws Exception {
        org.springframework.mock.web.MockHttpSession session = loginSession("rebind-token", ACTOR);
        MvcResult shell = mvc.perform(get("/web-admin/shell").session(session))
                .andReturn();
        CsrfToken csrf = csrfToken(shell);
        bindingRepository.bind(ISSUER, SUBJECT, OTHER_ACTOR);

        mvc.perform(post("/web-admin/session/probe")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("principal_binding_changed"));
    }

    @Test
    void auditEventsAreSecretSafeAndDoNotContainProviderTokens() throws Exception {
        org.springframework.mock.web.MockHttpSession session = loginSession("secret-provider-token", ACTOR);
        MvcResult shell = mvc.perform(get("/web-admin/shell").session(session))
                .andReturn();
        CsrfToken csrf = csrfToken(shell);
        mvc.perform(post("/web-admin/logout")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken()))
                .andExpect(status().isSeeOther());

        List<WebAdminSessionAuditEvent> events = applicationEvents
                .stream(WebAdminSessionAuditEvent.class)
                .toList();
        assertThat(events).extracting(WebAdminSessionAuditEvent::eventType)
                .contains("web_admin_login_succeeded", "web_admin_logout");
        assertThat(events.toString()).doesNotContain("secret-provider-token");
    }

    private org.springframework.mock.web.MockHttpSession loginSession(String token, UUID actorId)
            throws Exception {
        bindingRepository.bind(ISSUER, SUBJECT, actorId);
        MvcResult login = mvc.perform(get("/web-admin/login")).andReturn();
        org.springframework.mock.web.MockHttpSession session =
                (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false);
        tokenValidator.mapLoginToken(token, new JwtPrincipal(ISSUER, SUBJECT), nonceFrom(login));

        mvc.perform(post("/web-admin/oidc/callback")
                        .session(session)
                        .param("state", stateFrom(login))
                        .param("id_token", token))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/web-admin/shell"));
        return session;
    }

    private String stateFrom(MvcResult login) {
        URI location = URI.create(login.getResponse().getHeader(HttpHeaders.LOCATION));
        return UriComponentsBuilder.fromUri(location).build().getQueryParams().getFirst("state");
    }

    private String nonceFrom(MvcResult login) {
        HttpSession session = login.getRequest().getSession(false);
        assertThat(session).isNotNull();
        Object nonce = session.getAttribute("webAdmin.loginNonce");
        assertThat(nonce).isInstanceOf(String.class);
        return (String) nonce;
    }

    private CsrfToken csrfToken(MvcResult result) {
        CsrfToken csrf = (CsrfToken) result.getRequest()
                .getAttribute(CsrfToken.class.getName());
        assertThat(csrf).isNotNull();
        return csrf;
    }

    @TestConfiguration
    static class TestFakes {

        @Bean
        @Primary
        FakeOidcJwksTokenValidator fakeOidcJwksTokenValidator(AuthProperties authProperties) {
            return new FakeOidcJwksTokenValidator(authProperties);
        }

        @Bean
        @Primary
        FakeAuthPrincipalBindingRepository fakeAuthPrincipalBindingRepository() {
            return new FakeAuthPrincipalBindingRepository();
        }

        @Bean
        @Primary
        AuthenticatedActorResolver fakeAuthenticatedActorResolver() {
            return new AuthenticatedActorResolver(null, null, null, null, null) {
                @Override
                public AuthenticatedActor resolve(String bearerToken) {
                    throw new AuthResolutionException("not_used_by_web_admin_session_test");
                }
            };
        }
    }

    static final class FakeOidcJwksTokenValidator extends OidcJwksTokenValidator {

        private final Map<String, TokenPrincipal> principals = new ConcurrentHashMap<>();
        private final Map<String, String> failures = new ConcurrentHashMap<>();

        FakeOidcJwksTokenValidator(AuthProperties authProperties) {
            super(authProperties);
        }

        @Override
        public JwtPrincipal validate(String token) {
            TokenPrincipal tokenPrincipal = tokenPrincipal(token);
            return tokenPrincipal.principal();
        }

        @Override
        public JwtPrincipal validateLoginToken(String token, String expectedNonce) {
            TokenPrincipal tokenPrincipal = tokenPrincipal(token);
            if (!Objects.equals(expectedNonce, tokenPrincipal.nonce())) {
                throw new AuthResolutionException("invalid_oidc_nonce");
            }
            return tokenPrincipal.principal();
        }

        private TokenPrincipal tokenPrincipal(String token) {
            String failure = failures.get(token);
            if (failure != null) {
                throw new AuthResolutionException(failure);
            }
            TokenPrincipal tokenPrincipal = principals.get(token);
            if (tokenPrincipal == null) {
                throw new AuthResolutionException("invalid_oidc_jwt");
            }
            return tokenPrincipal;
        }

        void mapLoginToken(String token, JwtPrincipal principal, String nonce) {
            principals.put(token, new TokenPrincipal(principal, nonce));
        }

        void rejectToken(String token, String reason) {
            failures.put(token, reason);
        }

        void reset() {
            principals.clear();
            failures.clear();
        }

        private record TokenPrincipal(JwtPrincipal principal, String nonce) {}
    }

    static final class FakeAuthPrincipalBindingRepository
            extends AuthPrincipalBindingRepository {

        private final Map<String, UUID> bindings = new ConcurrentHashMap<>();

        FakeAuthPrincipalBindingRepository() {
            super(null);
        }

        @Override
        public UUID resolveActor(String issuer, String subject) {
            return bindings.get(key(issuer, subject));
        }

        void bind(String issuer, String subject, UUID actorId) {
            bindings.put(key(issuer, subject), actorId);
        }

        void reset() {
            bindings.clear();
        }

        private String key(String issuer, String subject) {
            return issuer + "\n" + subject;
        }
    }
}
