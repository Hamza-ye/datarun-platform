package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.config.ActivityService;
import dev.datarun.server.config.AdminCommandCapabilityPolicy;
import dev.datarun.server.config.ShapeService;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.ServerIdentity;
import dev.datarun.server.integrity.ConflictResolutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WebAdminOperationalReportSnapshotIntegrationTest extends AbstractIntegrationTest {

    private static final String ISSUER = "https://issuer.test/datarun";
    private static final UUID ADMIN =
            UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID REVIEWER =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FIELD_ACTOR =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID OUT_OF_SCOPE_REVIEWER =
            UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID SUBJECT_IN_SCOPE =
            UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SUBJECT_OUT_OF_SCOPE =
            UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AssignmentService assignmentService;
    @Autowired private ActorTokenRepository actorTokenRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private ConflictResolutionService conflictResolutionService;
    @Autowired private LocationRepository locationRepository;
    @Autowired private SubjectLocationRepository subjectLocationRepository;
    @Autowired private ServerIdentity serverIdentity;
    @Autowired private ShapeService shapeService;
    @Autowired private ActivityService activityService;
    @Autowired private ScopedOperationalReportSnapshotService reportSnapshotService;

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
        jdbcTemplate.execute("DELETE FROM config_packages");
        jdbcTemplate.execute("DELETE FROM expression_rules");
        jdbcTemplate.execute("DELETE FROM activities");
        jdbcTemplate.execute("DELETE FROM shapes");
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
        mvc.perform(get("/web-admin/operational/report"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web-admin/login"));
    }

    @Test
    void missingWebAdminAccessIsDenied() throws Exception {
        MockHttpSession session = webAdminSession(REVIEWER);
        configureAdminCommands(REVIEWER, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);

        mvc.perform(get("/web-admin/operational/report").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingScopedReadIsDenied() throws Exception {
        MockHttpSession session = webAdminSession(REVIEWER);
        configureAdminCommands(REVIEWER, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);

        mvc.perform(get("/web-admin/operational/report").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void scopeIsAppliedBeforeAggregatesLatestTimesAndIssueCounts() throws Exception {
        setupReviewerScope();
        configureReportCommands(REVIEWER);
        Event visible = createVisitRecord(
                SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");
        for (int i = 0; i < 5; i++) {
            Event hidden = createVisitRecord(
                    UUID.randomUUID(), districtB, "Hidden Site Visit " + i,
                    "hidden_activity");
            createAttentionFlag(hidden, "scope_violation", ADMIN);
        }

        MvcResult result = mvc.perform(get("/web-admin/operational/report")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body)
                .contains("Scoped Operational Report Snapshot")
                .contains("snapshot_as_of")
                .contains("known_latest_input")
                .contains("Current scoped standing only. Coverage not measured.")
                .contains(receivedAt(visible).toString())
                .doesNotContain("Hidden Site Visit")
                .doesNotContain("Hidden Activity")
                .doesNotContain(SUBJECT_OUT_OF_SCOPE.toString())
                .doesNotContain("hidden out-of-scope")
                .doesNotContain("all devices current")
                .doesNotContain("real-time")
                .doesNotContain("SLA")
                .doesNotContain("complete")
                .doesNotContain("<form");
        assertActivityRow(body, "Assigned Visit", 1, 0, 0);
    }

    @Test
    void configuredWorkEvidenceDetailShowsSelectedStandardConfiguredWork()
            throws Exception {
        publishStockOperationsFixtureConfig();
        setupReviewerScope("stock_operations");
        configureReportCommands(REVIEWER);
        Event visible = createStocktakeLineRecord(
                SUBJECT_IN_SCOPE, districtA, "mids_kit", 42);
        createStocktakeLineRecord(
                SUBJECT_OUT_OF_SCOPE, districtB, "hidden_kit", 99);
        MockHttpSession session = webAdminSession(REVIEWER);

        String report = mvc.perform(get("/web-admin/operational/report")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(report)
                .contains("Open configured work evidence")
                .doesNotContain("Configured work details")
                .doesNotContain("Field values")
                .doesNotContain("stocktake_date")
                .doesNotContain("mids_kit")
                .doesNotContain(">42<")
                .doesNotContain("hidden_kit")
                .doesNotContain(">99<");

        String body = mvc.perform(get(configuredWorkEvidencePath(report))
                        .session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body)
                .contains("Configured Work Evidence")
                .contains("Visible Work Evidence")
                .contains("Activity")
                .contains("Record type")
                .contains("Configured work details")
                .contains("Latest synced/received")
                .contains("stock_operations")
                .contains("stocktake_line/v1")
                .contains("stocktake_date")
                .contains("stock_category")
                .contains("quantity")
                .contains("2026-06-23")
                .contains("mids_kit")
                .contains(">42<")
                .contains(receivedAt(visible).toString())
                .contains("Visible through current assignment scope")
                .contains("Latest synced/received time is scoped and not a guarantee.")
                .doesNotContain("hidden_kit")
                .doesNotContain(">99<")
                .doesNotContain(SUBJECT_IN_SCOPE.toString())
                .doesNotContain(SUBJECT_OUT_OF_SCOPE.toString())
                .doesNotContain("Stocktake Line Details")
                .doesNotContain("stocktake-line details")
                .doesNotContain("stock ledger")
                .doesNotContain("review workflow")
                .doesNotContain("<form");
    }

    @Test
    void configuredWorkEvidenceShowsCandidateAssetAsNeedsReviewOnly()
            throws Exception {
        publishFieldAssetConfig();
        setupReviewerScope("field_asset_inspection");
        assignmentService.createAssignment(
                ADMIN, FIELD_ACTOR, "field_worker", districtA,
                List.of(SUBJECT_IN_SCOPE), List.of("field_asset_inspection"),
                past(), null);
        assignmentService.createAssignment(
                ADMIN, OUT_OF_SCOPE_REVIEWER, "supervisor", districtB,
                null, List.of("field_asset_inspection"), past(), null);
        configureReportCommandsForActors(List.of(REVIEWER, OUT_OF_SCOPE_REVIEWER));

        ObjectNode knownAssetPayload = objectMapper.createObjectNode();
        knownAssetPayload.put("field_asset", SUBJECT_IN_SCOPE.toString());
        knownAssetPayload.put("name", "Known pump");
        createConfiguredWorkRecord(
                SUBJECT_IN_SCOPE, districtA, "asset_check/v1",
                "field_asset_inspection", knownAssetPayload);

        UUID generatedCandidateSubject = UUID.randomUUID();
        UUID candidateEvent = pushFieldAssetCandidateEvidence(
                actorTokenRepository.createToken(FIELD_ACTOR),
                generatedCandidateSubject);
        assertThat(eventRepository.getLocationPath(candidateEvent))
                .isEqualTo(locationRepository.findPathById(districtA));
        assertThat(eventPayloadHasKey(candidateEvent, "field_asset")).isFalse();
        assertThat(knownFieldAssetBindings())
                .contains(SUBJECT_IN_SCOPE.toString())
                .doesNotContain(generatedCandidateSubject.toString());

        ScopedOperationalReportSnapshotService.ConfiguredWorkEvidence directEvidence =
                reportSnapshotService.configuredWorkEvidence(REVIEWER, candidateEvent);
        assertThat(directEvidence.visible()).isTrue();
        assertThat(directEvidence.activityRef()).isEqualTo("field_asset_inspection");
        assertThat(directEvidence.shapeRef()).isEqualTo("asset_check/v1");
        assertThat(directEvidence.candidateEvidence()).isNotNull();
        assertThat(directEvidence.candidateEvidence().reviewLabel())
                .isEqualTo("Candidate asset");
        assertThat(directEvidence.candidateEvidence().displayLabel())
                .isEqualTo("Unknown pump");
        assertThat(directEvidence.candidateEvidence().standing())
                .isEqualTo("Needs review before it can be used as a known asset.");
        assertThat(directEvidence.candidateEvidence().lookupStanding())
                .contains("You are using the last saved asset list.")
                .contains("Lookup standing: offline, incomplete.");

        ScopedOperationalReportSnapshotService.ConfiguredWorkEvidence outOfScopeDirect =
                reportSnapshotService.configuredWorkEvidence(
                        OUT_OF_SCOPE_REVIEWER, candidateEvent);
        assertThat(outOfScopeDirect.visible()).isFalse();

        MockHttpSession reviewerSession = webAdminSession(REVIEWER);
        String report = mvc.perform(get("/web-admin/operational/report")
                        .session(reviewerSession))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String evidencePath = configuredWorkEvidencePath(report);

        String body = mvc.perform(get(evidencePath).session(reviewerSession))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body)
                .contains("Configured Work Evidence")
                .contains("Activity")
                .contains("Field Asset Inspection")
                .contains("field_asset_inspection")
                .contains("Record type")
                .contains("Asset Check")
                .contains("asset_check/v1")
                .contains("Candidate evidence")
                .contains("Candidate asset")
                .contains("Unknown pump")
                .contains("Standing")
                .contains("Needs review before it can be used as a known asset.")
                .contains("Lookup standing")
                .contains("You are using the last saved asset list.")
                .contains("Lookup standing: offline, incomplete.")
                .contains("Capture time")
                .contains("2026-06-27T10:00:00Z")
                .contains("Actor/session provenance")
                .contains("Authenticated actor session recorded this candidate evidence.")
                .contains("Assignment/scope context")
                .contains("Assignment and scope context was preserved with this record.")
                .contains("Original submitted record")
                .contains("This configured work record contains the candidate evidence.")
                .doesNotContain(FIELD_ACTOR.toString())
                .doesNotContain(SUBJECT_IN_SCOPE.toString())
                .doesNotContain("official asset")
                .doesNotContain("<form");
        assertThat(body.toLowerCase(java.util.Locale.ROOT))
                .doesNotContain("approve")
                .doesNotContain("reject")
                .doesNotContain("promote")
                .doesNotContain("create official asset")
                .doesNotContain("create asset")
                .doesNotContain("lifecycle")
                .doesNotContain("duplicate")
                .doesNotContain("merge")
                .doesNotContain("split");

        String copiedTokenBody = mvc.perform(get(evidencePath)
                        .session(webAdminSession(OUT_OF_SCOPE_REVIEWER)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(copiedTokenBody)
                .contains("No visible configured work evidence is available for this session.")
                .doesNotContain("Unknown pump")
                .doesNotContain("Candidate asset");

        String outOfScope = mvc.perform(get("/web-admin/operational/report")
                        .session(webAdminSession(OUT_OF_SCOPE_REVIEWER)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(outOfScope)
                .doesNotContain("Unknown pump")
                .doesNotContain("/web-admin/operational/evidence");
    }

    @Test
    void configuredWorkEvidenceDoesNotRenderRawJsonPayloadValues()
            throws Exception {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.putNull("subject_binding");
        schema.putNull("uniqueness");
        ArrayNode fields = schema.putArray("fields");
        addField(fields, "scalar_value", "text", true);
        addMultiSelectField(fields, "multi_value", false, "alpha", "beta");
        addField(fields, "object_value", "text", false);
        addField(fields, "array_text_value", "text", false);
        assertThat(shapeService.createShape("inspection_line", "standard", schema))
                .isEmpty();

        ObjectNode activityConfig = objectMapper.createObjectNode();
        activityConfig.putArray("shapes").add("inspection_line/v1");
        activityConfig.putObject("roles").putArray("field_worker").add("capture");
        assertThat(activityService.createActivity(
                "configured_activity", "standard", activityConfig)).isEmpty();

        setupReviewerScope("configured_activity");
        configureReportCommands(REVIEWER);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("scalar_value", "visible scalar");
        payload.putArray("multi_value").add("alpha").add("beta");
        payload.putObject("object_value").put("secret", "raw-object-secret");
        payload.putArray("array_text_value").add("unexpected-array-secret");
        createConfiguredWorkRecord(
                SUBJECT_IN_SCOPE, districtA,
                "inspection_line/v1", "configured_activity", payload);

        String body = configuredWorkEvidenceBody(webAdminSession(REVIEWER));

        assertThat(body)
                .contains("Configured Work Evidence")
                .contains("visible scalar")
                .contains("alpha, beta")
                .contains("object_value")
                .contains("array_text_value")
                .contains("Unsupported value")
                .doesNotContain("raw-object-secret")
                .doesNotContain("unexpected-array-secret")
                .doesNotContain("{&quot;secret&quot;")
                .doesNotContain("{\"secret\"");
    }

    @Test
    void configuredWorkEvidenceRouteRequiresAccessAndScopedRead()
            throws Exception {
        mvc.perform(get("/web-admin/operational/evidence"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web-admin/login"));

        MockHttpSession accessOnly = webAdminSession(REVIEWER);
        configureAdminCommands(REVIEWER, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS);
        mvc.perform(get("/web-admin/operational/evidence")
                        .param("workToken", "missing")
                        .session(accessOnly))
                .andExpect(status().isForbidden());

        MockHttpSession readOnly = webAdminSession(REVIEWER);
        configureAdminCommands(REVIEWER, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);
        mvc.perform(get("/web-admin/operational/evidence")
                        .param("workToken", "missing")
                        .session(readOnly))
                .andExpect(status().isForbidden());

        configureReportCommands(REVIEWER);
        String body = mvc.perform(get("/web-admin/operational/evidence")
                        .param("workToken", "missing")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(body)
                .contains("No visible configured work evidence is available for this session.")
                .doesNotContain("missing");
    }

    @Test
    void configuredWorkEvidenceRechecksScopeBeforeRendering()
            throws Exception {
        publishStockOperationsFixtureConfig();
        Event assignment = setupReviewerScope("stock_operations");
        configureReportCommands(REVIEWER);
        Event visible = createStocktakeLineRecord(
                SUBJECT_IN_SCOPE, districtA, "mids_kit", 42);
        MockHttpSession session = webAdminSession(REVIEWER);

        String report = mvc.perform(get("/web-admin/operational/report")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String evidencePath = configuredWorkEvidencePath(report);

        assignmentService.endAssignment(assignmentId(assignment), ADMIN, "route closed");
        String body = mvc.perform(get(evidencePath).session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body)
                .contains("No visible configured work evidence is available for this session.")
                .doesNotContain("mids_kit")
                .doesNotContain(">42<")
                .doesNotContain(visible.id().toString())
                .doesNotContain(SUBJECT_IN_SCOPE.toString());
    }

    @Test
    void configuredWorkEvidenceHidesInternalIdentifiers()
            throws Exception {
        publishStockOperationsFixtureConfig();
        setupReviewerScope("stock_operations");
        configureReportCommands(REVIEWER);
        Event visible = createStocktakeLineRecord(
                SUBJECT_IN_SCOPE, districtA, "mids_kit", 42);

        String body = configuredWorkEvidenceBody(webAdminSession(REVIEWER));

        assertThat(body)
                .contains("Configured Work Evidence")
                .contains("mids_kit")
                .doesNotContain(visible.id().toString())
                .doesNotContain(SUBJECT_IN_SCOPE.toString())
                .doesNotContain(FIELD_ACTOR.toString())
                .doesNotContain(serverIdentity.getDeviceId().toString())
                .doesNotContain(locationRepository.findPathById(districtA))
                .doesNotContain("subject_ref")
                .doesNotContain("actor_ref")
                .doesNotContain("device_id")
                .doesNotContain("device_seq")
                .doesNotContain("sync_watermark")
                .doesNotContain("location_path")
                .doesNotContain("payload");
    }

    @Test
    void configuredWorkEvidenceSuppressesElevatedOrRestrictedFieldValues()
            throws Exception {
        publishGenericConfiguredWorkConfig(
                "inspection_line", "elevated",
                "configured_activity", "standard",
                "sensitive_value");
        setupReviewerScope("configured_activity");
        configureReportCommands(REVIEWER);
        createConfiguredWorkRecord(
                SUBJECT_IN_SCOPE,
                districtA,
                "inspection_line/v1",
                "configured_activity",
                "sensitive_value",
                "restricted evidence value");

        String body = configuredWorkEvidenceBody(webAdminSession(REVIEWER));

        assertThat(body)
                .contains("Configured field values are suppressed for this evidence.")
                .contains("configured_activity")
                .contains("inspection_line/v1")
                .doesNotContain("sensitive_value")
                .doesNotContain("restricted evidence value");
    }

    @Test
    void reportPageDoesNotBecomeConfiguredRecordListBrowser()
            throws Exception {
        publishStockOperationsFixtureConfig();
        setupReviewerScope("stock_operations");
        configureReportCommands(REVIEWER);
        createStocktakeLineRecord(SUBJECT_IN_SCOPE, districtA, "mids_kit", 42);
        createStocktakeLineRecord(SUBJECT_IN_SCOPE, districtA, "rapid_test_kit", 7);

        String report = mvc.perform(get("/web-admin/operational/report")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(report)
                .contains("Scoped Operational Report Snapshot")
                .contains("Open configured work evidence")
                .doesNotContain("Configured work details")
                .doesNotContain("Field values")
                .doesNotContain("stocktake_date")
                .doesNotContain("stock_category")
                .doesNotContain("quantity")
                .doesNotContain("mids_kit")
                .doesNotContain("rapid_test_kit")
                .doesNotContain(">42<")
                .doesNotContain(">7<");
        assertActivityRow(report, "Stock Operations", 2, 0, 0);
    }

    @Test
    void assignedActivityWithoutSourceWorkRendersZeroStandingRow() throws Exception {
        setupReviewerScope();
        configureReportCommands(REVIEWER);

        MvcResult result = mvc.perform(get("/web-admin/operational/report")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("no_visible_input")
                .contains("No visible input")
                .contains("Freshness unknown")
                .contains("Coverage not measured")
                .doesNotContain("Current scoped standing has no visible source work")
                .doesNotContain("complete")
                .doesNotContain("all clear");
        assertActivityRow(result.getResponse().getContentAsString(),
                "Assigned Visit", 0, 0, 0);
    }

    @Test
    void unresolvedAttentionExcludesSourceWorkAndCountsVisibleIssue()
            throws Exception {
        setupReviewerScope();
        configureReportCommands(REVIEWER);
        Event visible = createVisitRecord(
                SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");
        createAttentionFlag(visible, "scope_violation", REVIEWER);

        String body = mvc.perform(get("/web-admin/operational/report")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body)
                .contains("Needs attention")
                .contains("Freshness unknown");
        assertActivityRow(body, "Assigned Visit", 0, 1, 1);
    }

    @Test
    void acceptedResolutionReIncludesSourceWorkAsClean() throws Exception {
        setupReviewerScope();
        configureReportCommands(REVIEWER);
        Event visible = createVisitRecord(
                SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");
        Event flag = createAttentionFlag(visible, "scope_violation", REVIEWER);
        conflictResolutionService.resolve(
                flag.id(), "accepted", null, REVIEWER, "Accepted for snapshot");

        String body = mvc.perform(get("/web-admin/operational/report")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertActivityRow(body, "Assigned Visit", 1, 0, 0);
    }

    @Test
    void rejectedResolutionDoesNotCountAsCleanSuccess() throws Exception {
        setupReviewerScope();
        configureReportCommands(REVIEWER);
        Event visible = createVisitRecord(
                SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");
        Event flag = createAttentionFlag(visible, "scope_violation", REVIEWER);
        conflictResolutionService.resolve(
                flag.id(), "rejected", null, REVIEWER, "Rejected source work");

        String body = mvc.perform(get("/web-admin/operational/report")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertActivityRow(body, "Assigned Visit", 0, 1, 0);
    }

    @Test
    void nonDesignatedResolutionDoesNotCountAsCleanSuccess() throws Exception {
        setupReviewerScope();
        configureReportCommands(REVIEWER);
        Event visible = createVisitRecord(
                SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");
        Event flag = createAttentionFlag(visible, "scope_violation", ADMIN);
        conflictResolutionService.resolve(
                flag.id(), "accepted", null, REVIEWER, "Not the designated reviewer");

        String body = mvc.perform(get("/web-admin/operational/report")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertActivityRow(body, "Assigned Visit", 0, 1, 1);
    }

    @Test
    void resolverUnassignedAttentionDoesNotCountAsCleanSuccess()
            throws Exception {
        setupReviewerScope();
        configureReportCommands(REVIEWER);
        Event visible = createVisitRecord(
                SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");
        createAttentionFlag(
                visible, "scope_violation",
                "system:resolver_unassigned/scope_violation");

        String body = mvc.perform(get("/web-admin/operational/report")
                        .session(webAdminSession(REVIEWER)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertActivityRow(body, "Assigned Visit", 0, 1, 1);
        assertThat(body)
                .doesNotContain("root")
                .doesNotContain("override")
                .doesNotContain("reassign");
    }

    @Test
    void traceTargetDoesNotOpenConfiguredEvidenceForUnconfiguredWork()
            throws Exception {
        Event assignment = setupReviewerScope();
        configureReportCommands(REVIEWER);
        createVisitRecord(SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");
        MockHttpSession session = webAdminSession(REVIEWER);

        String report = mvc.perform(get("/web-admin/operational/report").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(report)
                .contains("Trace Context")
                .doesNotContain("/web-admin/operational/evidence")
                .doesNotContain("Open configured work evidence");

        assignmentService.endAssignment(assignmentId(assignment), ADMIN, "route closed");
        String operational = mvc.perform(get("/web-admin/operational").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(operational)
                .contains(WebAdminOperationalViewService.NO_SCOPED_WORK_FRESHNESS)
                .doesNotContain(SUBJECT_IN_SCOPE.toString())
                .doesNotContain("Site Visit");
    }

    @Test
    void reportPageIsReadOnlyAndNoMutationRouteIsIntroduced() throws Exception {
        setupReviewerScope();
        configureReportCommands(REVIEWER);
        createVisitRecord(SUBJECT_IN_SCOPE, districtA, "Site Visit", "assigned_visit");
        MockHttpSession session = webAdminSession(REVIEWER);
        int before = eventCount();
        MvcResult shell = mvc.perform(get("/web-admin/shell").session(session))
                .andExpect(status().isOk())
                .andReturn();
        CsrfToken csrf = csrfToken(shell);

        String report = mvc.perform(get("/web-admin/operational/report").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(report).doesNotContain("<form");

        mvc.perform(post("/web-admin/operational/report")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken()))
                .andExpect(status().isMethodNotAllowed());

        assertThat(eventCount()).isEqualTo(before);
    }

    private String configuredWorkEvidenceBody(MockHttpSession session) throws Exception {
        String report = mvc.perform(get("/web-admin/operational/report").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mvc.perform(get(configuredWorkEvidencePath(report)).session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String configuredWorkEvidencePath(String html) {
        java.util.regex.Matcher matcher = Pattern.compile(
                        "href=\"([^\"]*/web-admin/operational/evidence\\?workToken=[^\"]+)\"")
                .matcher(html);
        assertThat(matcher.find())
                .as("configured work evidence link")
                .isTrue();
        return matcher.group(1);
    }

    private void publishStockOperationsFixtureConfig() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.putNull("subject_binding");
        schema.putNull("uniqueness");
        ArrayNode fields = schema.putArray("fields");
        addField(fields, "stocktake_date", "date", true);
        addSelectField(fields, "stock_category", true,
                "mids_kit", "rapid_test_kit", "itn_bale");
        addIntegerField(fields, "quantity", true, 0);

        assertThat(shapeService.createShape("stocktake_line", "standard", schema))
                .isEmpty();

        ObjectNode activityConfig = objectMapper.createObjectNode();
        activityConfig.putArray("shapes").add("stocktake_line/v1");
        activityConfig.putObject("roles").putArray("field_worker").add("capture");
        assertThat(activityService.createActivity(
                "stock_operations", "standard", activityConfig)).isEmpty();
    }

    private void publishFieldAssetConfig() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("subject_binding", "field_asset");
        schema.putNull("uniqueness");
        ArrayNode fields = schema.putArray("fields");
        addField(fields, "field_asset", "subject_ref", true);
        addField(fields, "name", "text", true);

        assertThat(shapeService.createShape("asset_check", "standard", schema))
                .isEmpty();

        ObjectNode activityConfig = objectMapper.createObjectNode();
        activityConfig.putArray("shapes").add("asset_check/v1");
        activityConfig.putObject("roles").putArray("field_worker").add("capture");
        assertThat(activityService.createActivity(
                "field_asset_inspection", "standard", activityConfig))
                .isEmpty();
    }

    private void publishGenericConfiguredWorkConfig(
            String shapeName,
            String shapeSensitivity,
            String activityRef,
            String activitySensitivity,
            String fieldName) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.putNull("subject_binding");
        schema.putNull("uniqueness");
        ArrayNode fields = schema.putArray("fields");
        addField(fields, fieldName, "text", true);

        assertThat(shapeService.createShape(shapeName, shapeSensitivity, schema))
                .isEmpty();

        ObjectNode activityConfig = objectMapper.createObjectNode();
        activityConfig.putArray("shapes").add(shapeName + "/v1");
        activityConfig.putObject("roles").putArray("field_worker").add("capture");
        assertThat(activityService.createActivity(
                activityRef, activitySensitivity, activityConfig)).isEmpty();
    }

    private void addField(ArrayNode fields, String name, String type,
                          boolean required) {
        ObjectNode field = fields.addObject();
        field.put("name", name);
        field.put("type", type);
        field.put("required", required);
        field.put("deprecated", false);
        field.put("display_order", fields.size());
    }

    private void addSelectField(ArrayNode fields, String name, boolean required,
                                String... options) {
        ObjectNode field = fields.addObject();
        field.put("name", name);
        field.put("type", "select");
        field.put("required", required);
        field.put("deprecated", false);
        field.put("display_order", fields.size());
        ArrayNode optionArray = field.putArray("options");
        for (String option : options) {
            optionArray.add(option);
        }
    }

    private void addMultiSelectField(ArrayNode fields, String name, boolean required,
                                     String... options) {
        ObjectNode field = fields.addObject();
        field.put("name", name);
        field.put("type", "multi_select");
        field.put("required", required);
        field.put("deprecated", false);
        field.put("display_order", fields.size());
        ArrayNode optionArray = field.putArray("options");
        for (String option : options) {
            optionArray.add(option);
        }
    }

    private void addIntegerField(ArrayNode fields, String name, boolean required,
                                 int min) {
        ObjectNode field = fields.addObject();
        field.put("name", name);
        field.put("type", "integer");
        field.put("required", required);
        field.put("deprecated", false);
        field.put("display_order", fields.size());
        field.putObject("validation").put("min", min);
    }

    private Event setupReviewerScope() {
        return setupReviewerScope("assigned_visit");
    }

    private Event setupReviewerScope(String activityRef) {
        assignmentService.createInitialBootstrapAssignment(
                ADMIN, "admin", null, null, null, past(), null);
        return assignmentService.createAssignment(
                ADMIN, REVIEWER, "supervisor", districtA, null,
                List.of(activityRef), past(), null);
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

    private Event createStocktakeLineRecord(UUID subjectId, UUID locationId,
                                            String category, int quantity) {
        subjectLocationRepository.upsert(
                subjectId, locationId, locationRepository.findPathById(locationId));
        ObjectNode payload = stocktakePayload(category, quantity);

        Event event = new Event(
                UUID.randomUUID(),
                "capture",
                "stocktake_line/v1",
                "stock_operations",
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

    private ObjectNode stocktakePayload(String category, int quantity) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("stocktake_date", "2026-06-23");
        payload.put("stock_category", category);
        payload.put("quantity", quantity);
        return payload;
    }

    private Event createConfiguredWorkRecord(UUID subjectId,
                                             UUID locationId,
                                             String shapeRef,
                                             String activityRef,
                                             String fieldName,
                                             String value) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put(fieldName, value);
        return createConfiguredWorkRecord(
                subjectId, locationId, shapeRef, activityRef, payload);
    }

    private Event createConfiguredWorkRecord(UUID subjectId,
                                             UUID locationId,
                                             String shapeRef,
                                             String activityRef,
                                             ObjectNode payload) {
        subjectLocationRepository.upsert(
                subjectId, locationId, locationRepository.findPathById(locationId));

        Event event = new Event(
                UUID.randomUUID(),
                "capture",
                shapeRef,
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

    private UUID pushFieldAssetCandidateEvidence(String token, UUID subjectId)
            throws Exception {
        UUID eventId = UUID.randomUUID();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("name", "Candidate evidence record");
        ObjectNode candidate = payload.putObject("asset_candidate_evidence");
        candidate.put("standing", "candidate");
        candidate.put("review_label", "Candidate asset");
        candidate.put("display_label", "Unknown pump");
        candidate.put("candidate_standing",
                "Needs review before it can be used as a known asset.");
        candidate.put("capture_timestamp", "2026-06-27T10:00:00Z");
        candidate.putObject("lookup_standing")
                .put("state", "offline_saved_list")
                .put("message", "You are using the last saved asset list.")
                .put("offline", true)
                .put("stale", false)
                .put("incomplete", true)
                .put("unavailable", false);
        candidate.putObject("actor_session_provenance")
                .put("actor_id", FIELD_ACTOR.toString())
                .put("session", "local_actor_session");
        candidate.putArray("assignment_scope_context")
                .addObject()
                .put("role", "field_worker")
                .put("geographic_scope", districtA.toString());
        candidate.putObject("original_submitted_record_ref")
                .put("type", "event")
                .put("id", eventId.toString());

        ObjectNode event = objectMapper.createObjectNode();
        event.put("id", eventId.toString());
        event.put("type", "capture");
        event.put("shape_ref", "asset_check/v1");
        event.put("activity_ref", "field_asset_inspection");
        event.set("subject_ref", subjectRef(subjectId));
        event.set("actor_ref", actorRef(FIELD_ACTOR));
        event.put("device_id", UUID.randomUUID().toString());
        event.put("device_seq", 1);
        event.putNull("sync_watermark");
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.set("payload", payload);

        ObjectNode request = objectMapper.createObjectNode();
        request.putArray("events").add(event);

        mvc.perform(post("/api/sync/push")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isOk());
        return eventId;
    }

    private boolean eventPayloadHasKey(UUID eventId, String fieldName) {
        Boolean hasKey = jdbcTemplate.queryForObject(
                "SELECT jsonb_exists(payload, ?) FROM events WHERE id = ?::uuid",
                Boolean.class,
                fieldName,
                eventId.toString());
        return Boolean.TRUE.equals(hasKey);
    }

    private List<String> knownFieldAssetBindings() {
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT payload->>'field_asset'
                FROM events
                WHERE payload ? 'field_asset'
                ORDER BY payload->>'field_asset'
                """, String.class);
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

    private UUID assignmentId(Event assignmentEvent) {
        return UUID.fromString(assignmentEvent.subjectRef().path("id").asText());
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

    private void configureReportCommands(UUID actorId) throws Exception {
        configureAdminCommands(
                actorId,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);
    }

    private void configureReportCommandsForActors(List<UUID> actorIds)
            throws Exception {
        configureAdminCommandsForActors(
                actorIds,
                AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS,
                AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED);
    }

    private void configureAdminCommands(UUID actorId, String... commands)
            throws Exception {
        configureAdminCommandsForActors(List.of(actorId), commands);
    }

    private void configureAdminCommandsForActors(List<UUID> actorIds,
                                                 String... commands)
            throws Exception {
        ObjectNode policy = objectMapper.createObjectNode();
        policy.put("schema_version", 1);
        ObjectNode actors = policy.putObject("actors");
        for (UUID actorId : actorIds) {
            ArrayNode grants = actors.putArray(actorId.toString());
            for (String command : commands) {
                grants.add(command);
            }
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
                actorIds.get(0).toString());
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

    private void assertActivityRow(String html, String activity, long clean,
                                   long excluded, long unresolved) {
        Pattern row = Pattern.compile(
                "<tr>\\s*<td>" + Pattern.quote(activity) + "</td>\\s*"
                        + "<td>" + clean + "</td>\\s*"
                        + "<td>" + excluded + "</td>\\s*"
                        + "<td>" + unresolved + "</td>",
                Pattern.DOTALL);
        assertThat(row.matcher(html).find())
                .as("activity row %s clean=%s excluded=%s unresolved=%s",
                        activity, clean, excluded, unresolved)
                .isTrue();
    }
}
