package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.config.AdminCommandCapabilityPolicy;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.ServerIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WebAdminOperationalHandoffIntegrationTest extends AbstractIntegrationTest {

    private static final String ISSUER = "https://issuer.test/datarun";
    private static final UUID ADMIN =
            UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID SUCCESSOR =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OUTGOING_ACTOR =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SUBJECT_IN_SCOPE =
            UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SUBJECT_OUT_OF_SCOPE =
            UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AssignmentService assignmentService;
    @Autowired private EventRepository eventRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private SubjectLocationRepository subjectLocationRepository;
    @Autowired private ServerIdentity serverIdentity;

    private UUID region;
    private UUID districtA;
    private UUID districtB;

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
        districtA = UUID.randomUUID();
        districtB = UUID.randomUUID();
        locationRepository.insert(region, "Assigned route", null, "region");
        locationRepository.insert(districtA, "Assigned route / coverage", region, "district");
        locationRepository.insert(districtB, "Other coverage", region, "district");
    }

    @Test
    void unauthenticatedUsersRedirectToLogin() throws Exception {
        mvc.perform(get("/web-admin/operational/handoff"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web-admin/login"));
    }

    @Test
    void handoffRequiresWebAdminAccessAndScopedRead() throws Exception {
        MockHttpSession session = webAdminSession(SUCCESSOR);

        configureAdminCommands(SUCCESSOR, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);
        mvc.perform(get("/web-admin/operational/handoff").session(session))
                .andExpect(status().isForbidden());

        configureAdminCommands(SUCCESSOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        mvc.perform(get("/web-admin/operational/handoff").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void handoffShowsCurrentWorkPriorContextAndLateStaleCaveatsInsideScope()
            throws Exception {
        setupSuccessorScope();
        configureHandoffCommands(SUCCESSOR);
        Event prior = createVisitRecord(
                SUBJECT_IN_SCOPE, districtA, "prior_visit/v1", OUTGOING_ACTOR);
        Event current = createVisitRecord(
                SUBJECT_IN_SCOPE, districtA, "visit_record/v1", OUTGOING_ACTOR);
        Event hidden = createVisitRecord(
                SUBJECT_OUT_OF_SCOPE, districtB, "hidden_visit/v1", OUTGOING_ACTOR);
        createAttentionFlag(current, "temporal_authority_expired", SUCCESSOR);
        createAttentionFlag(hidden, "temporal_authority_expired", SUCCESSOR);

        MvcResult result = mvc.perform(get("/web-admin/operational/handoff")
                        .session(webAdminSession(SUCCESSOR)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body)
                .contains("Operational Responsibility Handoff")
                .contains("Current assigned work")
                .contains("Assigned Visit")
                .contains("Current Work And Prior Context")
                .contains("Visit Record")
                .contains("Prior Visit")
                .contains(SUBJECT_IN_SCOPE.toString())
                .contains(receivedAt(current).toString())
                .contains(receivedAt(prior).toString())
                .contains("Context incomplete")
                .contains("Freshness unknown")
                .contains("Needs attention")
                .contains("Late synced work")
                .contains("work captured offline")
                .contains("Stale responsibility")
                .contains("Timing Review")
                .contains("You are the designated reviewer for this attention item.")
                .contains("Open scoped context")
                .contains("/web-admin/operational")
                .doesNotContain(SUBJECT_OUT_OF_SCOPE.toString())
                .doesNotContain("Hidden Visit")
                .doesNotContain("temporal_authority_expired")
                .doesNotContain("conflict_detected")
                .doesNotContain("sync_watermark")
                .doesNotContain("all devices current")
                .doesNotContain("all clear")
                .doesNotContain("complete history")
                .doesNotContain("<form");
    }

    @Test
    void outOfScopeWorkDoesNotAffectEmptyStateLatestTraceOrCaveats()
            throws Exception {
        setupSuccessorScope();
        configureHandoffCommands(SUCCESSOR);
        Event hidden = createVisitRecord(
                SUBJECT_OUT_OF_SCOPE, districtB, "hidden_visit/v1", OUTGOING_ACTOR);
        createAttentionFlag(hidden, "temporal_authority_expired", SUCCESSOR);

        String body = mvc.perform(get("/web-admin/operational/handoff")
                        .session(webAdminSession(SUCCESSOR)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body)
                .contains("No visible handoff context for the current actor.")
                .contains("Freshness unknown")
                .contains("Context incomplete")
                .doesNotContain(receivedAt(hidden).toString())
                .doesNotContain(SUBJECT_OUT_OF_SCOPE.toString())
                .doesNotContain("Hidden Visit")
                .doesNotContain("Late synced work")
                .doesNotContain("Stale responsibility")
                .doesNotContain("Needs attention")
                .doesNotContain("Trace Target")
                .doesNotContain("Open scoped context");
    }

    @Test
    void resolverUnassignedAttentionIsBlockedWithoutFallbackAuthority()
            throws Exception {
        setupSuccessorScope();
        configureHandoffCommands(SUCCESSOR);
        Event current = createVisitRecord(
                SUBJECT_IN_SCOPE, districtA, "visit_record/v1", OUTGOING_ACTOR);
        createAttentionFlag(
                current,
                "scope_violation",
                "system:resolver_unassigned/scope_violation");

        String body = mvc.perform(get("/web-admin/operational/handoff")
                        .session(webAdminSession(SUCCESSOR)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body)
                .contains("Needs attention")
                .contains("Scope Review")
                .contains("Not currently resolvable")
                .contains("no designated reviewer is currently assigned")
                .doesNotContain("root")
                .doesNotContain("override")
                .doesNotContain("reassign")
                .doesNotContain("<form");
    }

    @Test
    void handoffPageIsReadOnlyAndNoMutationRouteIsIntroduced() throws Exception {
        setupSuccessorScope();
        configureHandoffCommands(SUCCESSOR);
        createVisitRecord(SUBJECT_IN_SCOPE, districtA, "visit_record/v1", OUTGOING_ACTOR);
        MockHttpSession session = webAdminSession(SUCCESSOR);
        int before = eventCount();
        MvcResult shell = mvc.perform(get("/web-admin/shell").session(session))
                .andExpect(status().isOk())
                .andReturn();
        CsrfToken csrf = csrfToken(shell);

        String page = mvc.perform(get("/web-admin/operational/handoff").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(page).doesNotContain("<form");

        mvc.perform(post("/web-admin/operational/handoff")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken()))
                .andExpect(status().isMethodNotAllowed());

        assertThat(eventCount()).isEqualTo(before);
    }

    private void setupSuccessorScope() {
        assignmentService.createInitialBootstrapAssignment(
                ADMIN, "admin", null, null, null, past(), null);
        assignmentService.createAssignment(
                ADMIN, SUCCESSOR, "successor", districtA, null,
                List.of("assigned_visit"), past(), null);
    }

    private Event createVisitRecord(UUID subjectId, UUID locationId, String shapeRef,
                                    UUID actorId) {
        subjectLocationRepository.upsert(
                subjectId, locationId, locationRepository.findPathById(locationId));
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("organization", "Example Organization");
        payload.put("setup", "Field Activity Setup");
        payload.put("work", "Assigned Visit");
        payload.put("record", shapeRef);

        Event event = new Event(
                UUID.randomUUID(),
                "activity",
                shapeRef,
                "assigned_visit",
                subjectRef(subjectId),
                actorRef(actorId),
                serverIdentity.getDeviceId(),
                (int) serverIdentity.nextDeviceSeq(),
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                payload);
        assertThat(eventRepository.insert(event)).isTrue();
        return event;
    }

    private Event createAttentionFlag(Event source, String category, UUID resolverId) {
        return createAttentionFlag(source, category, resolverId.toString());
    }

    private Event createAttentionFlag(Event source, String category, String resolverId) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("source_event_id", source.id().toString());
        payload.put("flag_category", category);
        payload.put("resolvability", "manual_only");
        payload.set("designated_resolver", actorRef(resolverId));
        payload.put("reason", "Synthetic attention item");

        Event flag = new Event(
                UUID.randomUUID(),
                "alert",
                "conflict_detected/v1",
                null,
                source.subjectRef(),
                systemActorRef(category),
                serverIdentity.getDeviceId(),
                (int) serverIdentity.nextDeviceSeq(),
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                payload);
        assertThat(eventRepository.insert(flag)).isTrue();
        return flag;
    }

    private ObjectNode subjectRef(UUID subjectId) {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", subjectId.toString());
        return subjectRef;
    }

    private ObjectNode actorRef(UUID actorId) {
        return actorRef(actorId.toString());
    }

    private ObjectNode actorRef(String actorId) {
        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", actorId);
        return actorRef;
    }

    private ObjectNode systemActorRef(String category) {
        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", "system:conflict_detector/" + category);
        return actorRef;
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

    private void configureHandoffCommands(UUID actorId) throws Exception {
        configureAdminCommands(
                actorId,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);
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

    private OffsetDateTime past() {
        return OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
    }

    private int eventCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events", Integer.class);
        return count == null ? 0 : count;
    }

    private OffsetDateTime receivedAt(Event event) {
        Timestamp received = jdbcTemplate.queryForObject(
                "SELECT received_at FROM events WHERE id = ?::uuid",
                Timestamp.class,
                event.id().toString());
        assertThat(received).isNotNull();
        return received.toInstant().atOffset(ZoneOffset.UTC);
    }

    private CsrfToken csrfToken(MvcResult result) {
        CsrfToken csrf = (CsrfToken) result.getRequest()
                .getAttribute(CsrfToken.class.getName());
        assertThat(csrf).isNotNull();
        return csrf;
    }
}
