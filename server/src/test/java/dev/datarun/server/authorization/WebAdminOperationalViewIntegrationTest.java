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
class WebAdminOperationalViewIntegrationTest extends AbstractIntegrationTest {

    private static final String ISSUER = "https://issuer.test/datarun";
    private static final UUID ADMIN =
            UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID REVIEWER =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FIELD_ACTOR =
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
        mvc.perform(get("/web-admin/operational"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web-admin/login"));
    }

    @Test
    void authenticatedActorWithoutRequiredCommandsIsDenied() throws Exception {
        MockHttpSession session = webAdminSession(REVIEWER);

        configureAdminCommands(REVIEWER, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);
        mvc.perform(get("/web-admin/operational").session(session))
                .andExpect(status().isForbidden());

        configureAdminCommands(REVIEWER, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        mvc.perform(get("/web-admin/operational").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorizedScopedViewShowsLatestVisibleWorkWithoutCrossScopeLeakage()
            throws Exception {
        setupReviewerScope();
        configureAdminCommands(REVIEWER,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);

        Event visible = createVisitRecord(
                SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");
        createVisitRecord(SUBJECT_OUT_OF_SCOPE, districtB, "Hidden Site Visit",
                "assigned_visit");
        Long visibleWatermark = eventRepository.getSyncWatermark(visible.id());

        MvcResult result = mvc.perform(get("/web-admin/operational")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body)
                .contains("Operational View")
                .contains("Visit Record")
                .contains("Assigned Visit")
                .contains(SUBJECT_IN_SCOPE.toString())
                .contains("Latest visible synced work in Datarun. Freshness marker: "
                        + visibleWatermark
                        + ". This does not prove all devices are current.")
                .doesNotContain(SUBJECT_OUT_OF_SCOPE.toString())
                .doesNotContain("Hidden Site Visit");
    }

    @Test
    void noAttentionCueShownWhenNoUnresolvedAttentionItemExists() throws Exception {
        setupReviewerScope();
        configureAdminCommands(REVIEWER,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);
        createVisitRecord(SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");

        MvcResult result = mvc.perform(get("/web-admin/operational")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain(WebAdminOperationalViewService.NEEDS_REVIEW_LABEL)
                .doesNotContain(WebAdminOperationalViewService.NEEDS_REVIEW_COPY);
    }

    @Test
    void atMostOneReadOnlyNeedsReviewCueIsShownWhenAttentionItemsExist()
            throws Exception {
        setupReviewerScope();
        configureAdminCommands(REVIEWER,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);
        Event visible = createVisitRecord(
                SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");
        Event firstFlag = createAttentionFlag(visible, "scope_violation");
        Event secondFlag = createAttentionFlag(visible, "role_stale");

        MvcResult result = mvc.perform(get("/web-admin/operational")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(countOccurrences(body, WebAdminOperationalViewService.NEEDS_REVIEW_LABEL))
                .isEqualTo(1);
        assertThat(body)
                .contains(WebAdminOperationalViewService.NEEDS_REVIEW_COPY)
                .doesNotContain(firstFlag.id().toString())
                .doesNotContain(secondFlag.id().toString())
                .doesNotContain("scope_violation")
                .doesNotContain("role_stale")
                .doesNotContain("<form");
    }

    @Test
    void noMutationPathExistsFromOperationalView() throws Exception {
        setupReviewerScope();
        configureAdminCommands(REVIEWER,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);
        MockHttpSession session = webAdminSession(REVIEWER);
        createVisitRecord(SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");
        int before = eventCount();
        CsrfToken csrf = csrfToken(mvc.perform(get("/web-admin/shell").session(session))
                .andExpect(status().isOk())
                .andReturn());

        mvc.perform(post("/web-admin/operational")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken()))
                .andExpect(status().isMethodNotAllowed());

        assertThat(eventCount()).isEqualTo(before);
    }

    @Test
    void noScopedWorkUsesProductSafeEmptyFreshnessWording() throws Exception {
        setupReviewerScope();
        configureAdminCommands(REVIEWER,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);

        MvcResult result = mvc.perform(get("/web-admin/operational")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains(WebAdminOperationalViewService.NO_SCOPED_WORK_FRESHNESS);
    }

    private void setupReviewerScope() {
        assignmentService.createInitialBootstrapAssignment(
                ADMIN, "admin", null, null, null, past(), null);
        assignmentService.createAssignment(
                ADMIN, REVIEWER, "supervisor", districtA, null,
                List.of("assigned_visit"), past(), null);
    }

    private Event createVisitRecord(UUID subjectId, UUID locationId,
                                    String siteName, String activityRef) {
        subjectLocationRepository.upsert(
                subjectId, locationId, locationRepository.findPathById(locationId));
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("organization", "Example Organization");
        payload.put("setup", "Field Activity Setup");
        payload.put("site", siteName);
        payload.put("work", "Assigned Visit");
        payload.put("record", "Visit Record");

        Event event = new Event(
                UUID.randomUUID(),
                "activity",
                "visit_record/v1",
                activityRef,
                subjectRef(subjectId),
                actorRef(FIELD_ACTOR),
                serverIdentity.getDeviceId(),
                (int) serverIdentity.nextDeviceSeq(),
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                payload);
        assertThat(eventRepository.insert(event)).isTrue();
        return event;
    }

    private Event createAttentionFlag(Event source, String category) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("source_event_id", source.id().toString());
        payload.put("flag_category", category);
        payload.put("resolvability", "manual_only");
        payload.set("designated_resolver", actorRef(REVIEWER));
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
        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", actorId.toString());
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

    private CsrfToken csrfToken(MvcResult result) {
        CsrfToken csrf = (CsrfToken) result.getRequest()
                .getAttribute(CsrfToken.class.getName());
        assertThat(csrf).isNotNull();
        return csrf;
    }

    private OffsetDateTime past() {
        return OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
    }

    private int eventCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events", Integer.class);
        return count == null ? 0 : count;
    }

    private int countOccurrences(String value, String token) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}
