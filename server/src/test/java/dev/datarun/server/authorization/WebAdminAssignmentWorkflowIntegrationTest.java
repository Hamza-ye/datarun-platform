package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.config.AdminCommandCapabilityPolicy;
import dev.datarun.server.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WebAdminAssignmentWorkflowIntegrationTest extends AbstractIntegrationTest {

    private static final String ISSUER = "https://issuer.test/datarun";
    private static final UUID ADMIN =
            UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID ACTOR =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TARGET =
            UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SECOND_TARGET =
            UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AssignmentService assignmentService;
    @Autowired private LocationRepository locationRepository;

    private UUID region;
    private UUID districtX;
    private UUID districtY;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("DELETE FROM actor_tokens");
        jdbcTemplate.execute("DELETE FROM subject_locations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM device_sync_state");
        jdbcTemplate.execute("DELETE FROM deployment_config");
        jdbcTemplate.execute("DELETE FROM locations");
        jdbcTemplate.execute("DELETE FROM auth_principal_binding_operations");
        jdbcTemplate.execute("DELETE FROM auth_principal_bindings");
        configureDefaultAssignmentAdminCapabilities();

        region = UUID.randomUUID();
        districtX = UUID.randomUUID();
        districtY = UUID.randomUUID();
        locationRepository.insert(region, "Region", null, "region");
        locationRepository.insert(districtX, "District X", region, "district");
        locationRepository.insert(districtY, "District Y", region, "district");
    }

    @Test
    void unauthenticatedUsersRedirectedToLogin() throws Exception {
        mvc.perform(get("/web-admin/assignments"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web-admin/login"));
    }

    @Test
    void unauthenticatedCreateAndEndPostsReturnUnauthorizedWithoutMutation()
            throws Exception {
        bootstrapAdmin();
        Event targetAssignment = assignmentService.createAssignment(
                ADMIN, TARGET, "field_worker", districtX, null, null, past(), null);
        UUID assignmentId = assignmentId(targetAssignment);
        configureAdminCommands(ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        MockHttpSession createSession = webAdminSession(ACTOR);
        CsrfToken createCsrf = csrfFromShell(createSession);
        clearWebAdminContext(createSession);

        mvc.perform(post("/web-admin/assignments/create")
                        .session(createSession)
                        .param(createCsrf.getParameterName(), createCsrf.getToken())
                        .param("target_actor_id", SECOND_TARGET.toString())
                        .param("role", "field_worker")
                        .param("geographic_scope", districtX.toString())
                        .param("valid_from", past().toString()))
                .andExpect(status().isUnauthorized());
        assertThat(assignmentCreatedCountForTarget(SECOND_TARGET)).isZero();

        MockHttpSession endSession = webAdminSession(ACTOR);
        CsrfToken endCsrf = csrfFromShell(endSession);
        clearWebAdminContext(endSession);

        mvc.perform(post("/web-admin/assignments/end")
                        .session(endSession)
                        .param(endCsrf.getParameterName(), endCsrf.getToken())
                        .param("assignment_id", assignmentId.toString())
                        .param("reason", "unauthenticated"))
                .andExpect(status().isUnauthorized());
        assertThat(assignmentEndedCount(assignmentId)).isZero();
    }

    @Test
    void authenticatedActorWithoutWebAdminAccessDeniedForPageAndPost()
            throws Exception {
        MockHttpSession session = webAdminSession(ACTOR);
        configureAdminCommands(ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        CsrfToken csrf = csrfFromShell(session);
        configureAdminCommands(ACTOR);

        mvc.perform(get("/web-admin/assignments").session(session))
                .andExpect(status().isForbidden());

        mvc.perform(post("/web-admin/assignments/create")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("target_actor_id", TARGET.toString())
                        .param("role", "field_worker")
                        .param("geographic_scope", districtX.toString())
                        .param("valid_from", past().toString()))
                .andExpect(status().isForbidden());
        assertThat(assignmentCreatedCountForTarget(TARGET)).isZero();
    }

    @Test
    void webAdminAccessWithoutAssignmentCreateCommandReturnsForbiddenWithoutMutation()
            throws Exception {
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, ACTOR, "field_worker",
                region, null, null, past(), null);
        configureAdminCommands(ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        MockHttpSession session = webAdminSession(ACTOR);
        CsrfToken csrf = csrfFromShell(session);

        mvc.perform(post("/web-admin/assignments/create")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("target_actor_id", TARGET.toString())
                        .param("role", "field_worker")
                        .param("geographic_scope", districtX.toString())
                        .param("valid_from", past().toString()))
                .andExpect(status().isForbidden());

        assertThat(assignmentCreatedCountForTarget(TARGET)).isZero();
    }

    @Test
    void webAdminAccessWithoutAssignmentEndCommandReturnsForbiddenWithoutMutation()
            throws Exception {
        bootstrapAdmin();
        Event targetAssignment = assignmentService.createAssignment(
                ADMIN, TARGET, "field_worker", districtX, null, null, past(), null);
        UUID assignmentId = assignmentId(targetAssignment);
        assignmentService.createAssignment(ADMIN, ACTOR, "field_worker",
                region, null, null, past(), null);
        configureAdminCommands(ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        MockHttpSession session = webAdminSession(ACTOR);
        CsrfToken csrf = csrfFromShell(session);

        mvc.perform(post("/web-admin/assignments/end")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("assignment_id", assignmentId.toString())
                        .param("reason", "missing end command"))
                .andExpect(status().isForbidden());

        assertThat(assignmentEndedCount(assignmentId)).isZero();
    }

    @Test
    void createDoesNotCombineCommandAndScopeAcrossAssignments() throws Exception {
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, ACTOR, "coordinator",
                districtY, null, null, past(), null);
        assignmentService.createAssignment(ADMIN, ACTOR, "field_worker",
                districtX, null, null, past(), null);
        configureAdminCommands(ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        MockHttpSession session = webAdminSession(ACTOR);
        CsrfToken csrf = csrfFromAssignmentsPage(session);

        mvc.perform(post("/web-admin/assignments/create")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("target_actor_id", TARGET.toString())
                        .param("role", "field_worker")
                        .param("geographic_scope", districtX.toString())
                        .param("valid_from", past().toString()))
                .andExpect(status().isForbidden());

        assertThat(assignmentCreatedCountForTarget(TARGET)).isZero();
    }

    @Test
    void endDoesNotCombineCommandAndScopeAcrossAssignments() throws Exception {
        bootstrapAdmin();
        Event targetAssignment = assignmentService.createAssignment(
                ADMIN, TARGET, "field_worker", districtX, null, null, past(), null);
        UUID assignmentId = assignmentId(targetAssignment);
        assignmentService.createAssignment(ADMIN, ACTOR, "coordinator",
                districtY, null, null, past(), null);
        assignmentService.createAssignment(ADMIN, ACTOR, "field_worker",
                districtX, null, null, past(), null);
        configureAdminCommands(ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        MockHttpSession session = webAdminSession(ACTOR);
        CsrfToken csrf = csrfFromAssignmentsPage(session);

        mvc.perform(post("/web-admin/assignments/end")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("assignment_id", assignmentId.toString())
                        .param("reason", "split command and scope"))
                .andExpect(status().isForbidden());

        assertThat(assignmentEndedCount(assignmentId)).isZero();
    }

    @Test
    void browserFormsUseSessionActorAndIgnoreSpoofedActorParams()
            throws Exception {
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, ACTOR, "coordinator",
                region, null, null, past(), null);
        configureAdminCommands(ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        MockHttpSession session = webAdminSession(ACTOR);
        CsrfToken csrf = csrfFromAssignmentsPage(session);

        mvc.perform(post("/web-admin/assignments/create")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("target_actor_id", SECOND_TARGET.toString())
                        .param("role", "field_worker")
                        .param("geographic_scope", districtX.toString())
                        .param("valid_from", past().toString())
                        .param("actor_id", ADMIN.toString())
                        .param("creator_actor_id", ADMIN.toString())
                        .param("ui_selected_actor_id", ADMIN.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web-admin/assignments"));

        UUID createdAssignmentId = assignmentCreatedIdForTarget(SECOND_TARGET);
        assertThat(assignmentActorRef(createdAssignmentId, "assignment_created/v1"))
                .isEqualTo(ACTOR.toString());

        mvc.perform(post("/web-admin/assignments/end")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("assignment_id", createdAssignmentId.toString())
                        .param("reason", "finished")
                        .param("actor_id", ADMIN.toString())
                        .param("creator_actor_id", ADMIN.toString())
                        .param("ui_selected_actor_id", ADMIN.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web-admin/assignments"));

        assertThat(assignmentActorRef(createdAssignmentId, "assignment_ended/v1"))
                .isEqualTo(ACTOR.toString());
    }

    @Test
    void csrfRequiredForCreateAndEndActions() throws Exception {
        bootstrapAdmin();
        assignmentService.createAssignment(ADMIN, ACTOR, "coordinator",
                region, null, null, past(), null);
        Event targetAssignment = assignmentService.createAssignment(
                ADMIN, TARGET, "field_worker", districtX, null, null, past(), null);
        UUID assignmentId = assignmentId(targetAssignment);
        configureAdminCommands(ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        MockHttpSession session = webAdminSession(ACTOR);

        mvc.perform(post("/web-admin/assignments/create")
                        .session(session)
                        .param("target_actor_id", SECOND_TARGET.toString())
                        .param("role", "field_worker")
                        .param("geographic_scope", districtX.toString())
                        .param("valid_from", past().toString()))
                .andExpect(status().isForbidden());
        assertThat(assignmentCreatedCountForTarget(SECOND_TARGET)).isZero();

        mvc.perform(post("/web-admin/assignments/end")
                        .session(session)
                        .param("assignment_id", assignmentId.toString())
                        .param("reason", "missing csrf"))
                .andExpect(status().isForbidden());
        assertThat(assignmentEndedCount(assignmentId)).isZero();
    }

    private void bootstrapAdmin() {
        assignmentService.createInitialBootstrapAssignment(
                ADMIN, "admin", null, null, null, past(), null);
    }

    private MockHttpSession webAdminSession(UUID actorId) {
        bindPrincipal(actorId);
        MockHttpSession session = new MockHttpSession();
        Instant now = Instant.now();
        session.setAttribute(WebAdminSessionService.ACTOR_ID_ATTR, actorId.toString());
        session.setAttribute(WebAdminSessionService.ISSUER_ATTR, ISSUER);
        session.setAttribute(WebAdminSessionService.SUBJECT_ATTR, subjectFor(actorId));
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
                ON CONFLICT DO NOTHING
                """, ISSUER, subjectFor(actorId), actorId.toString());
    }

    private String subjectFor(UUID actorId) {
        return "principal-" + actorId;
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

    private CsrfToken csrfFromShell(MockHttpSession session) throws Exception {
        return csrfToken(mvc.perform(get("/web-admin/shell").session(session))
                .andExpect(status().isOk())
                .andReturn());
    }

    private CsrfToken csrfFromAssignmentsPage(MockHttpSession session)
            throws Exception {
        return csrfToken(mvc.perform(get("/web-admin/assignments").session(session))
                .andExpect(status().isOk())
                .andReturn());
    }

    private CsrfToken csrfToken(MvcResult result) {
        CsrfToken csrf = (CsrfToken) result.getRequest()
                .getAttribute(CsrfToken.class.getName());
        assertThat(csrf).isNotNull();
        return csrf;
    }

    private void clearWebAdminContext(MockHttpSession session) {
        session.removeAttribute(WebAdminSessionService.ACTOR_ID_ATTR);
        session.removeAttribute(WebAdminSessionService.ISSUER_ATTR);
        session.removeAttribute(WebAdminSessionService.SUBJECT_ATTR);
        session.removeAttribute(WebAdminSessionService.AUTH_SOURCE_ATTR);
        session.removeAttribute(WebAdminSessionService.LOGIN_TIME_ATTR);
        session.removeAttribute(WebAdminSessionService.LAST_SEEN_TIME_ATTR);
        session.removeAttribute(WebAdminSessionService.EXPIRES_AT_ATTR);
        session.removeAttribute(WebAdminSessionService.SESSION_CORRELATION_ID_ATTR);
    }

    private OffsetDateTime past() {
        return OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
    }

    private UUID assignmentId(Event event) {
        return UUID.fromString(event.subjectRef().path("id").asText());
    }

    private int assignmentCreatedCountForTarget(UUID targetActorId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_created/v1'
                  AND payload->'target_actor'->>'id' = ?
                """, Integer.class, targetActorId.toString());
        return count == null ? 0 : count;
    }

    private UUID assignmentCreatedIdForTarget(UUID targetActorId) {
        String assignmentId = jdbcTemplate.queryForObject("""
                SELECT subject_ref->>'id'
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_created/v1'
                  AND payload->'target_actor'->>'id' = ?
                ORDER BY sync_watermark DESC
                LIMIT 1
                """, String.class, targetActorId.toString());
        return UUID.fromString(assignmentId);
    }

    private int assignmentEndedCount(UUID assignmentId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_ended/v1'
                  AND subject_ref->>'id' = ?
                """, Integer.class, assignmentId.toString());
        return count == null ? 0 : count;
    }

    private String assignmentActorRef(UUID assignmentId, String shapeRef) {
        return jdbcTemplate.queryForObject("""
                SELECT actor_ref->>'id'
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = ?
                  AND subject_ref->>'id' = ?
                ORDER BY sync_watermark DESC
                LIMIT 1
                """, String.class, shapeRef, assignmentId.toString());
    }
}
