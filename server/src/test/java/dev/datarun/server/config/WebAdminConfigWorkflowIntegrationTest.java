package dev.datarun.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.authorization.WebAdminSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WebAdminConfigWorkflowIntegrationTest extends AbstractIntegrationTest {

    private static final String ISSUER = "https://issuer.test/datarun";
    private static final String SUBJECT = "setup-owner";
    private static final UUID ACTOR =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_ACTOR =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID EXPRESSION_ID =
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WebAdminConfigCandidateService candidateService;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("DELETE FROM web_admin_config_candidate_history");
        jdbcTemplate.execute("DELETE FROM web_admin_config_candidates");
        jdbcTemplate.execute("DELETE FROM config_packages");
        jdbcTemplate.execute("DELETE FROM deployment_config");
        jdbcTemplate.execute("DELETE FROM expression_rules");
        jdbcTemplate.execute("DELETE FROM activities");
        jdbcTemplate.execute("DELETE FROM shapes");
        jdbcTemplate.execute("DELETE FROM auth_principal_binding_operations");
        jdbcTemplate.execute("DELETE FROM auth_principal_bindings");
        bindPrincipal(ACTOR);
    }

    @Test
    void unauthenticatedUsersCannotReachWebAdminConfig() throws Exception {
        mvc.perform(get("/web-admin/config"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web-admin/login"));
    }

    @Test
    void webAdminAccessWithoutAnyConfigCommandCannotReachPageOrPostActions()
            throws Exception {
        configureAdminCommands(ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        MockHttpSession session = webAdminSession(ACTOR);

        mvc.perform(get("/web-admin/config").session(session))
                .andExpect(status().isForbidden());

        MvcResult shell = mvc.perform(get("/web-admin/shell").session(session))
                .andExpect(status().isOk())
                .andReturn();
        CsrfToken csrf = csrfToken(shell);

        assertPostDenied(post("/web-admin/config/draft")
                .param("candidateJson", validCandidateJson())
                .param("expectedHash", ""), session, csrf);
        assertPostDenied(post("/web-admin/config/validate")
                .param("expectedHash", ""), session, csrf);
        assertPostDenied(post("/web-admin/config/readiness")
                .param("readinessStatus", "ready")
                .param("expectedHash", ""), session, csrf);
        assertPostDenied(post("/web-admin/config/approve")
                .param("expectedHash", ""), session, csrf);
        assertPostDenied(post("/web-admin/config/publish")
                .param("expectedHash", ""), session, csrf);
        assertThat(count("web_admin_config_candidates")).isZero();
    }

    @Test
    void configCommandWithoutWebAdminAccessCannotReachConfigWorkflow()
            throws Exception {
        configureAdminCommands(ACTOR, AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR);
        MockHttpSession session = webAdminSession(ACTOR);

        mvc.perform(get("/web-admin/config").session(session))
                .andExpect(status().isForbidden());

        configureAdminCommands(ACTOR,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR);
        CsrfToken csrf = csrfToken(mvc.perform(get("/web-admin/config")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn());
        configureAdminCommands(ACTOR, AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR);

        mvc.perform(post("/web-admin/config/draft")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("expectedHash", "")
                        .param("candidateJson", validCandidateJson()))
                .andExpect(status().isForbidden());
        assertThat(count("web_admin_config_candidates")).isZero();
    }

    @Test
    void differentConfigCommandDoesNotAuthorizeOtherPostActions()
            throws Exception {
        MockHttpSession session = webAdminSession(ACTOR);

        assertPostDeniedWithCommands(post("/web-admin/config/draft")
                        .param("candidateJson", validCandidateJson())
                        .param("expectedHash", ""),
                session,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_VALIDATE);
        assertPostDeniedWithCommands(post("/web-admin/config/validate")
                        .param("expectedHash", ""),
                session,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR);
        assertPostDeniedWithCommands(post("/web-admin/config/readiness")
                        .param("readinessStatus", "ready")
                        .param("expectedHash", ""),
                session,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_VALIDATE);
        assertPostDeniedWithCommands(post("/web-admin/config/approve")
                        .param("expectedHash", ""),
                session,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_READINESS_REVIEW);
        assertPostDeniedWithCommands(post("/web-admin/config/publish")
                        .param("expectedHash", ""),
                session,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_APPROVE);

        assertThat(count("web_admin_config_candidates")).isZero();
        assertThat(count("config_packages")).isZero();
    }

    @Test
    void shellDiscoversConfigPageForAnyConfigLaneAndPageRendersExactActions()
            throws Exception {
        configureAdminCommands(ACTOR,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_VALIDATE);
        MockHttpSession session = webAdminSession(ACTOR);

        MvcResult shell = mvc.perform(get("/web-admin/shell").session(session))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(shell.getResponse().getContentAsString())
                .contains("/web-admin/config");

        MvcResult page = mvc.perform(get("/web-admin/config").session(session))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(page.getResponse().getContentAsString())
                .contains("/web-admin/config/validate")
                .doesNotContain("/web-admin/config/draft")
                .doesNotContain("/web-admin/config/approve")
                .doesNotContain("/web-admin/config/publish");
    }

    @Test
    void matchingExactCommandLanesWorkIndependentlyThroughBrowserForms()
            throws Exception {
        MockHttpSession session = webAdminSession(ACTOR);

        configureAdminCommands(ACTOR,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR);
        CsrfToken csrf = csrfToken(mvc.perform(get("/web-admin/config")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn());
        mvc.perform(post("/web-admin/config/draft")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("expectedHash", "")
                        .param("candidateJson", validCandidateJson())
                        .param("actor_id", OTHER_ACTOR.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web-admin/config"));

        assertThat(count("shapes")).isZero();
        assertThat(count("config_packages")).isZero();
        assertThat(historyActors()).containsExactly(ACTOR);
        String hash = candidateField("content_hash");

        configureAdminCommands(ACTOR,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_VALIDATE);
        csrf = csrfToken(mvc.perform(get("/web-admin/config").session(session))
                .andExpect(status().isOk())
                .andReturn());
        mvc.perform(post("/web-admin/config/validate")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("expectedHash", hash)
                        .param("actor_id", OTHER_ACTOR.toString()))
                .andExpect(status().is3xxRedirection());
        assertThat(count("shapes")).isZero();
        assertThat(count("config_packages")).isZero();
        assertThat(candidateField("validation_status")).isEqualTo("passed");
        assertThat(candidateField("content_hash"))
                .isEqualTo(candidateField("validated_hash"));

        configureAdminCommands(ACTOR,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_READINESS_REVIEW);
        csrf = csrfToken(mvc.perform(get("/web-admin/config").session(session))
                .andExpect(status().isOk())
                .andReturn());
        mvc.perform(post("/web-admin/config/readiness")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("expectedHash", hash)
                        .param("readinessStatus", "ready")
                        .param("note", "ready for field rollout")
                        .param("actor_id", OTHER_ACTOR.toString()))
                .andExpect(status().is3xxRedirection());
        assertThat(candidateField("readiness_status")).isEqualTo("ready");

        configureAdminCommands(ACTOR,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_APPROVE);
        csrf = csrfToken(mvc.perform(get("/web-admin/config").session(session))
                .andExpect(status().isOk())
                .andReturn());
        mvc.perform(post("/web-admin/config/approve")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("expectedHash", hash)
                        .param("actor_id", OTHER_ACTOR.toString()))
                .andExpect(status().is3xxRedirection());
        assertThat(candidateField("approval_hash"))
                .isEqualTo(candidateField("content_hash"));

        configureAdminCommands(ACTOR,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_PUBLISH);
        csrf = csrfToken(mvc.perform(get("/web-admin/config").session(session))
                .andExpect(status().isOk())
                .andReturn());
        mvc.perform(post("/web-admin/config/publish")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("expectedHash", hash)
                        .param("actor_id", OTHER_ACTOR.toString()))
                .andExpect(status().is3xxRedirection());

        assertThat(count("shapes")).isEqualTo(1);
        assertThat(count("activities")).isEqualTo(1);
        assertThat(count("expression_rules")).isEqualTo(1);
        assertThat(count("config_packages")).isEqualTo(1);
        assertThat(configPublishedBy()).isEqualTo(ACTOR);
        assertThat(candidateInt("published_config_version")).isEqualTo(1);
        assertThat(historyActors()).containsOnly(ACTOR);
        assertThat(historyActions()).containsExactly(
                "draft_saved",
                "validation_passed",
                "readiness_recorded",
                "approved",
                "published");
    }

    @Test
    void staleExpectedHashRejectsBrowserWorkflowActions() throws Exception {
        configureAdminCommands(ACTOR,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_VALIDATE);
        MockHttpSession session = webAdminSession(ACTOR);
        CsrfToken csrf = csrfToken(mvc.perform(get("/web-admin/config")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn());

        mvc.perform(post("/web-admin/config/draft")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("expectedHash", "")
                        .param("candidateJson", validCandidateJson()))
                .andExpect(status().is3xxRedirection());
        String staleHash = candidateField("content_hash");

        candidateService.saveDraft(candidateJson("capture", "web-admin-test-updated"), ACTOR);
        String currentHash = candidateField("content_hash");
        assertThat(currentHash).isNotEqualTo(staleHash);

        mvc.perform(post("/web-admin/config/validate")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("expectedHash", staleHash))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web-admin/config"));

        assertThat(candidateField("content_hash")).isEqualTo(currentHash);
        assertThat(candidateField("validation_status")).isEqualTo("not_run");
        assertThat(historyActions()).containsExactly("draft_saved", "draft_saved");
        assertThat(count("shapes")).isZero();
        assertThat(count("config_packages")).isZero();
    }

    @Test
    void unknownCandidateFieldsAreRejectedBeforeDraftPersistence()
            throws Exception {
        configureAdminCommands(ACTOR,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR);
        MockHttpSession session = webAdminSession(ACTOR);
        CsrfToken csrf = csrfToken(mvc.perform(get("/web-admin/config")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn());

        ObjectNode candidate = (ObjectNode) objectMapper.readTree(validCandidateJson());
        candidate.put("unexpected_root_field", true);
        mvc.perform(post("/web-admin/config/draft")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("expectedHash", "")
                        .param("candidateJson", objectMapper.writeValueAsString(candidate)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web-admin/config"));

        assertThat(count("web_admin_config_candidates")).isZero();
        assertThat(historyActions()).isEmpty();
    }

    @Test
    void candidateHashUsesCanonicalJsonRatherThanInputFieldOrder()
            throws Exception {
        CandidateHashPair hashes = saveSameCandidateWithDifferentRootFieldOrder();

        assertThat(hashes.secondHash()).isEqualTo(hashes.firstHash());
    }

    @Test
    void validationFailureHistoryPersistsAndDryRunRowsRollBack()
            throws Exception {
        candidateService.saveDraft(invalidCandidateJson(), ACTOR);

        var outcome = candidateService.validateCurrent(
                ACTOR, candidateField("content_hash"));

        assertThat(outcome.passed()).isFalse();
        assertThat(outcome.violations()).anySatisfy(violation ->
                assertThat(violation).contains("Unknown action 'not_an_action'"));
        assertThat(candidateField("validation_status")).isEqualTo("failed");
        assertThat(historyActions()).containsExactly("draft_saved", "validation_failed");
        assertThat(historyDetails()).anySatisfy(detail ->
                assertThat(detail).contains("Unknown action 'not_an_action'"));
        assertThat(count("shapes")).isZero();
        assertThat(count("activities")).isZero();
        assertThat(count("expression_rules")).isZero();
        assertThat(count("config_packages")).isZero();
    }

    @Test
    void editedCandidateInvalidatesApprovalAndCannotPublishUntilReapproved()
            throws Exception {
        configureAdminCommands(ACTOR,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_VALIDATE,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_READINESS_REVIEW,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_APPROVE,
                AdminCommandCapabilityPolicy.CONFIG_ADMIN_PUBLISH);

        candidateService.saveDraft(validCandidateJson(), ACTOR);
        assertThat(candidateService.validateCurrent(ACTOR).passed()).isTrue();
        candidateService.recordReadiness(ACTOR, "ready", "ready");
        candidateService.approveCurrent(ACTOR);

        candidateService.saveDraft(invalidCandidateJson(), ACTOR);

        assertThatThrownBy(() -> candidateService.publishApproved(ACTOR))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("candidate validation is not current");
        assertThat(count("config_packages")).isZero();

        var outcome = candidateService.validateCurrent(ACTOR);
        assertThat(outcome.passed()).isFalse();
        assertThat(outcome.violations()).anySatisfy(violation ->
                assertThat(violation).contains("Unknown action 'not_an_action'"));
        assertThat(count("shapes")).isZero();
        assertThat(count("activities")).isZero();
        assertThat(count("config_packages")).isZero();
    }

    private MockHttpSession webAdminSession(UUID actorId) {
        MockHttpSession session = new MockHttpSession();
        Instant now = Instant.now();
        session.setAttribute(WebAdminSessionService.ACTOR_ID_ATTR, actorId.toString());
        session.setAttribute(WebAdminSessionService.ISSUER_ATTR, ISSUER);
        session.setAttribute(WebAdminSessionService.SUBJECT_ATTR, SUBJECT);
        session.setAttribute(WebAdminSessionService.AUTH_SOURCE_ATTR, "oidc-jwks-principal");
        session.setAttribute(WebAdminSessionService.LOGIN_TIME_ATTR, now);
        session.setAttribute(WebAdminSessionService.LAST_SEEN_TIME_ATTR, now);
        session.setAttribute(WebAdminSessionService.EXPIRES_AT_ATTR, now.plusSeconds(1800));
        session.setAttribute(WebAdminSessionService.SESSION_CORRELATION_ID_ATTR,
                UUID.randomUUID().toString());
        return session;
    }

    private void bindPrincipal(UUID actorId) {
        jdbcTemplate.update("""
                INSERT INTO auth_principal_bindings (issuer, subject, actor_id)
                VALUES (?, ?, ?::uuid)
                """, ISSUER, SUBJECT, actorId.toString());
    }

    private void configureAdminCommands(UUID actorId, String... commands)
            throws Exception {
        ObjectNode policy = objectMapper.createObjectNode();
        policy.put("schema_version", 1);
        ArrayNode grants = policy.putObject("actors")
                .putArray(actorId.toString());
        for (String command : commands) {
            grants.add(command);
        }
        jdbcTemplate.update("""
                INSERT INTO deployment_config (config_key, config_json, updated_by, updated_at)
                VALUES (?, ?::jsonb, ?::uuid, NOW())
                ON CONFLICT (config_key) DO UPDATE
                SET config_json = EXCLUDED.config_json,
                    updated_by = EXCLUDED.updated_by,
                    updated_at = NOW()
                """,
                AdminCommandCapabilityPolicy.CONFIG_KEY,
                objectMapper.writeValueAsString(policy),
                actorId.toString());
    }

    private String validCandidateJson() throws Exception {
        return candidateJson("capture");
    }

    private String invalidCandidateJson() throws Exception {
        return candidateJson("not_an_action");
    }

    private String candidateJson(String action) throws Exception {
        return candidateJson(action, "web-admin-test");
    }

    private String candidateJson(String action, String source) throws Exception {
        ObjectNode schema = objectMapper.createObjectNode();
        ArrayNode fields = schema.putArray("fields");
        fields.addObject()
                .put("name", "notes")
                .put("type", "text")
                .put("required", false);
        schema.putNull("subject_binding");
        schema.putNull("uniqueness");

        ObjectNode activityConfig = objectMapper.createObjectNode();
        activityConfig.putArray("shapes").add("visit/v1");
        activityConfig.putObject("roles").putArray("worker").add(action);

        ObjectNode expression = objectMapper.createObjectNode();
        expression.putObject("value").put("ref", "context.actor.scope_name");

        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("schema_version", 1);
        candidate.put("source", source);
        candidate.put("shapes", List.of(Map.of(
                "name", "visit",
                "version", 1,
                "status", "active",
                "sensitivity", "standard",
                "schema_json", schema)));
        candidate.put("activities", List.of(Map.of(
                "name", "field_visit",
                "status", "active",
                "sensitivity", "standard",
                "config_json", activityConfig)));
        candidate.put("expressions", List.of(Map.of(
                "id", EXPRESSION_ID,
                "activity_ref", "field_visit",
                "shape_ref", "visit/v1",
                "field_name", "notes",
                "rule_type", "default",
                "expression", expression)));
        candidate.put("flag_severity_overrides", objectMapper.createObjectNode());
        return objectMapper.writeValueAsString(candidate);
    }

    private CandidateHashPair saveSameCandidateWithDifferentRootFieldOrder()
            throws Exception {
        String original = validCandidateJson();
        candidateService.saveDraft(original, ACTOR);
        String firstHash = candidateField("content_hash");

        ObjectNode parsed = (ObjectNode) objectMapper.readTree(original);
        ObjectNode reordered = objectMapper.createObjectNode();
        reordered.set("flag_severity_overrides", parsed.get("flag_severity_overrides"));
        reordered.set("expressions", parsed.get("expressions"));
        reordered.set("activities", parsed.get("activities"));
        reordered.set("shapes", parsed.get("shapes"));
        reordered.set("source", parsed.get("source"));
        reordered.set("schema_version", parsed.get("schema_version"));

        candidateService.saveDraft(objectMapper.writeValueAsString(reordered), ACTOR);
        return new CandidateHashPair(firstHash, candidateField("content_hash"));
    }

    private CsrfToken csrfToken(MvcResult result) {
        CsrfToken csrf = (CsrfToken) result.getRequest()
                .getAttribute(CsrfToken.class.getName());
        assertThat(csrf).isNotNull();
        return csrf;
    }

    private void assertPostDenied(
            MockHttpServletRequestBuilder request,
            MockHttpSession session,
            CsrfToken csrf) throws Exception {
        mvc.perform(request
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken()))
                .andExpect(status().isForbidden());
    }

    private void assertPostDeniedWithCommands(
            MockHttpServletRequestBuilder request,
            MockHttpSession session,
            String grantedConfigCommand) throws Exception {
        configureAdminCommands(ACTOR,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                grantedConfigCommand);
        CsrfToken csrf = csrfToken(mvc.perform(get("/web-admin/config")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn());
        assertPostDenied(request, session, csrf);
    }

    private int count(String table) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table, Integer.class);
        return count == null ? 0 : count;
    }

    private String candidateField(String field) {
        return jdbcTemplate.queryForObject(
                "SELECT " + field + " FROM web_admin_config_candidates WHERE candidate_key = 'current'",
                String.class);
    }

    private Integer candidateInt(String field) {
        return jdbcTemplate.queryForObject(
                "SELECT " + field + " FROM web_admin_config_candidates WHERE candidate_key = 'current'",
                Integer.class);
    }

    private UUID configPublishedBy() {
        return jdbcTemplate.queryForObject("""
                SELECT published_by
                FROM config_packages
                WHERE version = 1
                """, UUID.class);
    }

    private List<UUID> historyActors() {
        return jdbcTemplate.query("""
                SELECT actor_id
                FROM web_admin_config_candidate_history
                ORDER BY id
                """, (rs, rowNum) -> UUID.fromString(rs.getString("actor_id")));
    }

    private List<String> historyActions() {
        return jdbcTemplate.queryForList("""
                SELECT action
                FROM web_admin_config_candidate_history
                ORDER BY id
                """, String.class);
    }

    private List<String> historyDetails() {
        return jdbcTemplate.queryForList("""
                SELECT detail_json::text
                FROM web_admin_config_candidate_history
                ORDER BY id
                """, String.class);
    }

    private record CandidateHashPair(String firstHash, String secondHash) {}
}
