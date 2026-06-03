package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.event.Event;
import dev.datarun.server.projection.PatternStateProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 4 P04 scenario-grade responsibility binding gate.
 *
 * <p>This keeps the existing responsibility model honest across the combined
 * campaign flow: overlapping geographic responsibility, activity scoping,
 * mid-campaign reassignment, live-sync contraction/expansion, and role-action
 * authority across the reassignment boundary.
 */
class ResponsibilityBindingScenarioIntegrationTest extends AbstractIntegrationTest {

    private static final UUID ADMIN = TEST_ACTOR_ID;
    private static final UUID WORKER_A = UUID.fromString("aaaa4000-0000-0000-0000-000000000001");
    private static final UUID WORKER_B = UUID.fromString("bbbb4000-0000-0000-0000-000000000002");
    private static final UUID SUPERVISOR = UUID.fromString("cccc4000-0000-0000-0000-000000000003");

    private static final UUID DEVICE_A = UUID.fromString("dddd4000-0000-0000-0000-000000000001");
    private static final UUID DEVICE_B = UUID.fromString("dddd4000-0000-0000-0000-000000000002");
    private static final UUID DEVICE_SUPERVISOR = UUID.fromString("dddd4000-0000-0000-0000-000000000003");
    private static final UUID DEVICE_ADMIN = UUID.fromString("dddd4000-0000-0000-0000-000000000004");

    private static final String CAMPAIGN_ACTIVITY = "campaign_capture";
    private static final String INVENTORY_ACTIVITY = "inventory";
    private static final String REVIEW_ACTIVITY = "supervisor_review_activity";
    private static final String LOGISTICS_TRANSFER_ACTIVITY = "logistics_transfer_activity";

    @Autowired private TestRestTemplate rest;
    @Autowired private AssignmentService assignmentService;
    @Autowired private LocationRepository locationRepository;
    @Autowired private ActorTokenRepository actorTokenRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PatternStateProjection patternStateProjection;

    private UUID region;
    private UUID districtA;
    private UUID districtB;
    private UUID villageA1;
    private UUID villageA2;
    private UUID villageB1;
    private String tokenA;
    private String tokenB;
    private String supervisorToken;
    private int seqCounter;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("DELETE FROM actor_tokens");
        jdbcTemplate.execute("DELETE FROM subject_locations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM device_sync_state");
        jdbcTemplate.execute("DELETE FROM activities");
        jdbcTemplate.execute("DELETE FROM locations");
        provisionTestToken();

        region = UUID.randomUUID();
        districtA = UUID.randomUUID();
        districtB = UUID.randomUUID();
        villageA1 = UUID.randomUUID();
        villageA2 = UUID.randomUUID();
        villageB1 = UUID.randomUUID();
        locationRepository.insert(region, "Region", null, "region");
        locationRepository.insert(districtA, "District A", region, "district");
        locationRepository.insert(districtB, "District B", region, "district");
        locationRepository.insert(villageA1, "Village A1", districtA, "village");
        locationRepository.insert(villageA2, "Village A2", districtA, "village");
        locationRepository.insert(villageB1, "Village B1", districtB, "village");

        tokenA = actorTokenRepository.createToken(WORKER_A);
        tokenB = actorTokenRepository.createToken(WORKER_B);
        supervisorToken = actorTokenRepository.createToken(SUPERVISOR);
        configureCampaignActivity();
        seqCounter = 1;
    }

    @Test
    void coordinatedCampaignReassignmentPreservesResponsibilityBinding() {
        Event workerAInitial = assignmentService.createAssignment(ADMIN, WORKER_A, "field_worker",
                villageA1, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, WORKER_B, "field_worker",
                villageA2, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, SUPERVISOR, "supervisor",
                districtA, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        long workerAKnowledge = latestWatermark(pullEvents(tokenA, 0, 100));
        long workerBKnowledge = latestWatermark(pullEvents(tokenB, 0, 100));
        long supervisorKnowledge = latestWatermark(pullEvents(supervisorToken, 0, 100));

        UUID subjectA1 = subjectAt(villageA1);
        UUID subjectA2 = subjectAt(villageA2);
        UUID subjectB1 = subjectAt(villageB1);
        UUID wrongActivityA1 = subjectAt(villageA1);

        UUID a1Initial = pushEvent(WORKER_A, DEVICE_A, subjectA1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "A1 baseline", workerAKnowledge);
        UUID a2Initial = pushEvent(WORKER_B, DEVICE_B, subjectA2, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "A2 baseline", workerBKnowledge);
        pushEvent(ADMIN, DEVICE_ADMIN, wrongActivityA1, INVENTORY_ACTIVITY,
                "capture", "basic_capture/v1", "wrong activity", 0);
        UUID b1BeforeReassignment = pushEvent(ADMIN, DEVICE_ADMIN, subjectB1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "outside district A", 0);

        assertVisibleCaptures(pullEvents(tokenA, 0, 100), a1Initial);
        assertVisibleCaptures(pullEvents(tokenB, 0, 100), a2Initial);
        assertVisibleCaptures(pullEvents(supervisorToken, 0, 100), a1Initial, a2Initial);

        UUID workerAInitialId = UUID.fromString(workerAInitial.subjectRef().get("id").asText());
        assignmentService.endAssignment(workerAInitialId, ADMIN, "campaign reassignment");
        assignmentService.createAssignment(ADMIN, WORKER_A, "field_worker",
                villageB1, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1), null);
        assignmentService.createAssignment(ADMIN, WORKER_B, "field_worker",
                villageA1, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1), null);

        long workerAAfterReassignment = latestWatermark(pullEvents(tokenA, workerAKnowledge, 100));
        long workerBAfterReassignment = latestWatermark(pullEvents(tokenB, workerBKnowledge, 100));
        long supervisorAfterReassignment = latestWatermark(
                pullEvents(supervisorToken, supervisorKnowledge, 100));

        UUID a1AfterReassignment = pushEvent(WORKER_B, DEVICE_B, subjectA1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "A1 after reassignment", workerBAfterReassignment);
        UUID b1AfterReassignment = pushEvent(WORKER_A, DEVICE_A, subjectB1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "B1 after reassignment", workerAAfterReassignment);
        UUID supervisorReview = pushEvent(SUPERVISOR, DEVICE_SUPERVISOR, subjectA1, CAMPAIGN_ACTIVITY,
                "review", "basic_review/v1", "Supervisor review", supervisorAfterReassignment);
        UUID workerAOfflineA1 = pushEvent(WORKER_A, DEVICE_A, subjectA1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "A1 offline after reassignment", workerAKnowledge);

        ResponseEntity<JsonNode> workerAExpandedPull = pullEvents(tokenA, workerAKnowledge, 100);
        assertCaptureContains(workerAExpandedPull, b1BeforeReassignment, b1AfterReassignment);
        assertCaptureExcludes(workerAExpandedPull, a1AfterReassignment);

        ResponseEntity<JsonNode> workerBExpandedPull = pullEvents(tokenB, workerBKnowledge, 100);
        assertCaptureContains(workerBExpandedPull, a1Initial, a2Initial, a1AfterReassignment);
        assertCaptureExcludes(workerBExpandedPull, b1AfterReassignment);

        ResponseEntity<JsonNode> supervisorDistrictPull = pullEvents(supervisorToken, supervisorKnowledge, 100);
        assertCaptureContains(supervisorDistrictPull, a1Initial, a2Initial, a1AfterReassignment);
        assertCaptureExcludes(supervisorDistrictPull, b1BeforeReassignment, b1AfterReassignment);

        assertThat(flagSources("temporal_authority_expired")).contains(workerAOfflineA1.toString());
        assertThat(flagSources("scope_violation")).doesNotContain(workerAOfflineA1.toString());
        assertThat(flagSources("role_stale")).contains(workerAOfflineA1.toString());
        assertThat(flagSources("role_stale")).doesNotContain(a1AfterReassignment.toString());
        assertThat(flagSources("role_stale")).doesNotContain(b1AfterReassignment.toString());
        assertThat(flagSources("role_stale")).doesNotContain(supervisorReview.toString());
    }

    @Test
    void s19OfflineStaleAuthorityPersistsFlagsAndKeepsScopedSyncBoundaries() {
        Event workerAInitial = assignmentService.createAssignment(ADMIN, WORKER_A, "field_worker",
                villageA1, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        UUID workerAInitialId = UUID.fromString(workerAInitial.subjectRef().get("id").asText());

        UUID subjectA1 = subjectAt(villageA1);
        UUID subjectB1 = subjectAt(villageB1);

        UUID historicalB1 = pushEvent(ADMIN, DEVICE_ADMIN, subjectB1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "B1 history before reassignment", 0);
        UUID visibleA1 = pushEvent(WORKER_A, DEVICE_A, subjectA1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "A1 baseline before offline window", 0);

        ResponseEntity<JsonNode> initialPull = pullEvents(tokenA, 0, 100, DEVICE_A);
        long workerAKnowledge = latestWatermark(initialPull);
        assertThat(workerAKnowledge).isGreaterThan(syncWatermark(historicalB1));
        assertCaptureContains(initialPull, visibleA1);
        assertCaptureExcludes(initialPull, historicalB1);
        long liveDeviceWatermarkBeforeBackfill = deviceWatermark(DEVICE_A);
        assertThat(liveDeviceWatermarkBeforeBackfill).isEqualTo(workerAKnowledge);

        assignmentService.endAssignment(workerAInitialId, ADMIN, "S19 stale authority");
        assignmentService.createAssignment(ADMIN, WORKER_A, "field_worker",
                villageB1, null, List.of(CAMPAIGN_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1), null);

        UUID staleOfflineA1 = pushEvent(WORKER_A, DEVICE_A, subjectA1, CAMPAIGN_ACTIVITY,
                "capture", "basic_capture/v1", "A1 stale offline capture", workerAKnowledge);

        assertThat(eventCount(staleOfflineA1)).isEqualTo(1);
        assertThat(flagSources("temporal_authority_expired")).contains(staleOfflineA1.toString());
        assertThat(flagSources("role_stale")).contains(staleOfflineA1.toString());
        assertThat(flagSources("scope_violation")).doesNotContain(staleOfflineA1.toString());

        ResponseEntity<JsonNode> pullAfterReassignment = pullEvents(tokenA, workerAKnowledge, 100, DEVICE_A);
        assertThat(pullAfterReassignment.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertCaptureExcludes(pullAfterReassignment, visibleA1, staleOfflineA1);
        assertThat(captureEventIds(pullAfterReassignment.getBody().get("events")))
                .doesNotContain(historicalB1.toString());
        assertThat(pullAfterReassignment.getBody().get("latest_watermark").asLong())
                .isGreaterThanOrEqualTo(workerAKnowledge);

        long liveDeviceWatermarkAfterPull = deviceWatermark(DEVICE_A);
        ResponseEntity<JsonNode> oldScopeBackfill =
                subjectHistory(tokenA, subjectA1, CAMPAIGN_ACTIVITY, 0, 100);
        assertThat(oldScopeBackfill.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<JsonNode> newScopeBackfill =
                subjectHistory(tokenA, subjectB1, CAMPAIGN_ACTIVITY, 0, 100);
        assertThat(newScopeBackfill.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureEventIds(newScopeBackfill.getBody().get("events")))
                .contains(historicalB1.toString());
        assertThat(newScopeBackfill.getBody().get("next_cursor").asLong())
                .isGreaterThanOrEqualTo(syncWatermark(historicalB1));
        assertThat(deviceWatermark(DEVICE_A)).isEqualTo(liveDeviceWatermarkAfterPull);
    }

    @Test
    void s21SupervisorReviewUsesScopedVisibilityPatternStateAndExactResolverSemantics() {
        configureSupervisorReviewActivity();
        assignmentService.createAssignment(ADMIN, WORKER_A, "field_worker",
                villageA1, null, List.of(REVIEW_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, SUPERVISOR, "supervisor",
                districtA, null, List.of(REVIEW_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        UUID subjectA1 = subjectAt(villageA1);
        UUID secondSubjectA1 = subjectAt(villageA1);
        UUID outsideSubjectB1 = subjectAt(villageB1);

        long workerKnowledge = latestWatermark(pullEvents(tokenA, 0, 100, DEVICE_A));
        UUID sourceCapture = pushEvent(WORKER_A, DEVICE_A, subjectA1, REVIEW_ACTIVITY,
                "capture", "chv_visit/v1", "CHV visit ready for supervisor review", workerKnowledge);
        UUID reviewableCapture = pushEvent(WORKER_A, DEVICE_A, secondSubjectA1, REVIEW_ACTIVITY,
                "capture", "chv_visit/v1", "Questionable CHV visit", workerKnowledge);
        UUID outsideCapture = pushEvent(ADMIN, DEVICE_ADMIN, outsideSubjectB1, REVIEW_ACTIVITY,
                "capture", "chv_visit/v1", "Outside supervisor district", 0);

        ResponseEntity<JsonNode> supervisorPull = pullEvents(supervisorToken, 0, 100, DEVICE_SUPERVISOR);
        assertCaptureContains(supervisorPull, sourceCapture, reviewableCapture);
        assertCaptureExcludes(supervisorPull, outsideCapture);
        long supervisorKnowledge = latestWatermark(supervisorPull);

        UUID supervisorReview = pushEventWithPayload(SUPERVISOR, DEVICE_SUPERVISOR, subjectA1,
                REVIEW_ACTIVITY, "review", "chv_visit_review/v1",
                Map.of("source_event_id", sourceCapture.toString(),
                        "decision", "accepted",
                        "notes", "supervisor accepted visit record"),
                supervisorKnowledge).id();

        JsonNode acceptedReviewState = eventState(sourceCapture, "capture_with_review/v1");
        assertThat(acceptedReviewState.get("current_state").asText()).isEqualTo("accepted");
        assertThat(acceptedReviewState.at("/pattern_specific/latest_review_outcome").asText())
                .isEqualTo("accepted");
        assertThat(flagSources("role_stale")).doesNotContain(supervisorReview.toString());

        PushedEvent unauthorizedReview = pushEventWithPayload(WORKER_A, DEVICE_A, secondSubjectA1,
                REVIEW_ACTIVITY, "review", "chv_visit_review/v1",
                Map.of("source_event_id", reviewableCapture.toString(),
                        "decision", "returned",
                        "notes", "field worker attempted review"),
                workerKnowledge);

        assertThat(unauthorizedReview.response().getBody().get("accepted").asInt()).isEqualTo(1);
        assertThat(flagSources("role_stale")).contains(unauthorizedReview.id().toString());
        JsonNode roleFlag = findFlagFor(unauthorizedReview.id(), "role_stale");
        assertThat(roleFlag.at("/payload/designated_resolver/type").asText()).isEqualTo("actor");
        assertThat(roleFlag.at("/payload/designated_resolver/id").asText())
                .isEqualTo(SUPERVISOR.toString());

        JsonNode unresolvedReviewState = eventState(reviewableCapture, "capture_with_review/v1");
        assertThat(unresolvedReviewState.get("current_state").asText()).isEqualTo("pending_review");
        assertThat(unresolvedReviewState.at("/pattern_specific/latest_review_outcome").isMissingNode())
                .isTrue();

        ResponseEntity<JsonNode> nonDesignatedResolution = resolveFlag(
                UUID.fromString(roleFlag.get("id").asText()), tokenA, "accepted");
        assertThat(nonDesignatedResolution.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(flagSources("scope_violation"))
                .contains(nonDesignatedResolution.getBody().get("event_id").asText());
        JsonNode nonDesignatedState = eventState(reviewableCapture, "capture_with_review/v1");
        assertThat(nonDesignatedState.get("current_state").asText()).isEqualTo("pending_review");

        ResponseEntity<JsonNode> exactResolution = resolveFlag(
                UUID.fromString(roleFlag.get("id").asText()), supervisorToken, "accepted");
        assertThat(exactResolution.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode resolvedReviewState = eventState(reviewableCapture, "capture_with_review/v1");
        assertThat(resolvedReviewState.get("current_state").asText()).isEqualTo("returned");
        assertThat(resolvedReviewState.at("/pattern_specific/latest_review_outcome").asText())
                .isEqualTo("returned");
    }

    @Test
    void s27LogisticsTransferUsesScopedSyncManualReviewAndProjectionDerivedState() {
        configureLogisticsTransferActivity();
        assignmentService.createAssignment(ADMIN, WORKER_A, "sender",
                villageA1, null, List.of(LOGISTICS_TRANSFER_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, WORKER_B, "receiver",
                villageA1, null, List.of(LOGISTICS_TRANSFER_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);
        assignmentService.createAssignment(ADMIN, SUPERVISOR, "supervisor",
                districtA, null, List.of(LOGISTICS_TRANSFER_ACTIVITY),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), null);

        UUID transferBatch = subjectAt(villageA1);
        UUID invalidReceiptBatch = subjectAt(villageA1);
        UUID outsideBatch = subjectAt(villageB1);

        long senderKnowledge = latestWatermark(pullEvents(tokenA, 0, 100, DEVICE_A));
        UUID dispatch = pushEventWithPayload(WORKER_A, DEVICE_A, transferBatch,
                LOGISTICS_TRANSFER_ACTIVITY, "capture", "logistics_dispatch/v1",
                Map.of("dispatch_code", "DIST-A-001",
                        "item", "water_filter_kits",
                        "quantity_dispatched", 100),
                senderKnowledge).id();
        UUID outsideDispatch = pushEventWithPayload(ADMIN, DEVICE_ADMIN, outsideBatch,
                LOGISTICS_TRANSFER_ACTIVITY, "capture", "logistics_dispatch/v1",
                Map.of("dispatch_code", "DIST-B-001",
                        "item", "blankets",
                        "quantity_dispatched", 20),
                0).id();

        ResponseEntity<JsonNode> receiverPull = pullEvents(tokenB, 0, 100, DEVICE_B);
        assertCaptureContains(receiverPull, dispatch);
        assertCaptureExcludes(receiverPull, outsideDispatch);
        long receiverKnowledge = latestWatermark(receiverPull);

        UUID partialReceipt = pushEventWithPayload(WORKER_B, DEVICE_B, transferBatch,
                LOGISTICS_TRANSFER_ACTIVITY, "capture", "logistics_receipt/v1",
                Map.of("quantity_received", 70,
                        "discrepancies", true,
                        "notes", "thirty kits still in transit"),
                receiverKnowledge).id();
        JsonNode partialState = subjectState(transferBatch, LOGISTICS_TRANSFER_ACTIVITY,
                "transfer_with_acknowledgment/v1");
        assertThat(partialState.get("current_state").asText()).isEqualTo("partial_receipt");

        UUID discrepancyReport = pushEventWithPayload(WORKER_B, DEVICE_B, transferBatch,
                LOGISTICS_TRANSFER_ACTIVITY, "capture", "logistics_discrepancy_report/v1",
                Map.of("difference", 30,
                        "reason", "short shipment recorded at district store"),
                receiverKnowledge).id();
        JsonNode disputedState = subjectState(transferBatch, LOGISTICS_TRANSFER_ACTIVITY,
                "transfer_with_acknowledgment/v1");
        assertThat(disputedState.get("current_state").asText()).isEqualTo("disputed");

        PushedEvent prematureResolution = pushEventWithPayload(WORKER_B, DEVICE_B, transferBatch,
                LOGISTICS_TRANSFER_ACTIVITY, "review", "logistics_discrepancy_resolution/v1",
                Map.of("resolution", "accept receiver count",
                        "notes", "receiver attempted to close the discrepancy"),
                receiverKnowledge);
        assertThat(prematureResolution.response().getBody().get("accepted").asInt()).isEqualTo(1);
        assertThat(flagSources("role_stale")).contains(prematureResolution.id().toString());
        JsonNode roleFlag = findFlagFor(prematureResolution.id(), "role_stale");
        assertThat(roleFlag.at("/payload/designated_resolver/id").asText())
                .isEqualTo(SUPERVISOR.toString());
        JsonNode unresolvedState = subjectState(transferBatch, LOGISTICS_TRANSFER_ACTIVITY,
                "transfer_with_acknowledgment/v1");
        assertThat(unresolvedState.get("current_state").asText()).isEqualTo("disputed");

        ResponseEntity<JsonNode> rejectedPrematureReview = resolveFlag(
                UUID.fromString(roleFlag.get("id").asText()), supervisorToken, "rejected");
        assertThat(rejectedPrematureReview.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode rejectedState = subjectState(transferBatch, LOGISTICS_TRANSFER_ACTIVITY,
                "transfer_with_acknowledgment/v1");
        assertThat(rejectedState.get("current_state").asText()).isEqualTo("disputed");

        long supervisorKnowledge = latestWatermark(
                pullEvents(supervisorToken, 0, 100, DEVICE_SUPERVISOR));
        UUID supervisorResolution = pushEventWithPayload(SUPERVISOR, DEVICE_SUPERVISOR, transferBatch,
                LOGISTICS_TRANSFER_ACTIVITY, "review", "logistics_discrepancy_resolution/v1",
                Map.of("resolution", "manual district adjustment",
                        "notes", "supervisor reviewed dispatch, receipt, and discrepancy report"),
                supervisorKnowledge).id();
        JsonNode resolvedState = subjectState(transferBatch, LOGISTICS_TRANSFER_ACTIVITY,
                "transfer_with_acknowledgment/v1");
        assertThat(resolvedState.get("current_state").asText()).isEqualTo("resolved");
        assertThat(flagSources("role_stale")).doesNotContain(supervisorResolution.toString());

        PushedEvent outOfOrderReceipt = pushEventWithPayload(WORKER_B, DEVICE_B, invalidReceiptBatch,
                LOGISTICS_TRANSFER_ACTIVITY, "capture", "logistics_receipt/v1",
                Map.of("quantity_received", 15,
                        "discrepancies", false,
                        "notes", "receipt arrived before dispatch record"),
                receiverKnowledge);
        assertThat(outOfOrderReceipt.response().getBody().get("accepted").asInt()).isEqualTo(1);
        assertThat(flagSources("transition_violation")).contains(outOfOrderReceipt.id().toString());
        assertThat(subjectStateExists(invalidReceiptBatch, LOGISTICS_TRANSFER_ACTIVITY,
                "transfer_with_acknowledgment/v1")).isFalse();

        ResponseEntity<JsonNode> supervisorPull = pullEvents(supervisorToken, 0, 100, DEVICE_SUPERVISOR);
        assertCaptureContains(supervisorPull, dispatch, partialReceipt, discrepancyReport);
        assertCaptureExcludes(supervisorPull, outsideDispatch);
    }

    private void configureCampaignActivity() {
        ObjectNode config = objectMapper.createObjectNode();
        config.putArray("shapes").add("basic_capture/v1").add("basic_review/v1");
        ObjectNode roles = config.putObject("roles");
        ArrayNode fieldWorker = roles.putArray("field_worker");
        fieldWorker.add("capture");
        ArrayNode supervisor = roles.putArray("supervisor");
        supervisor.add("capture");
        supervisor.add("review");
        jdbcTemplate.update("""
                INSERT INTO activities (name, config_json, status, sensitivity)
                VALUES (?, ?::jsonb, 'active', 'standard')
                """, CAMPAIGN_ACTIVITY, config.toString());
    }

    private void configureSupervisorReviewActivity() {
        String config = """
                {
                  "name": "supervisor_review_activity",
                  "sensitivity": "standard",
                  "roles": {
                    "field_worker": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": null,
                    "event": [
                      {
                        "ref": "capture_with_review/v1",
                        "composition": "event",
                        "shape_roles": {
                          "review_decision": ["chv_visit_review/v1"]
                        },
                        "activation_roles": {
                          "on_shapes": ["chv_visit/v1"]
                        },
                        "participant_roles": {
                          "capturer": ["field_worker"],
                          "reviewer": ["supervisor"]
                        },
                        "parameters": {}
                      }
                    ]
                  }
                }
                """;
        jdbcTemplate.update("""
                INSERT INTO activities (name, config_json, status, sensitivity)
                VALUES (?, ?::jsonb, 'active', 'standard')
                """, REVIEW_ACTIVITY, config);
    }

    private void configureLogisticsTransferActivity() {
        String config = """
                {
                  "name": "logistics_transfer_activity",
                  "sensitivity": "standard",
                  "roles": {
                    "sender": ["capture"],
                    "receiver": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": {
                      "ref": "transfer_with_acknowledgment/v1",
                      "composition": "subject",
                      "shape_roles": {
                        "dispatch": ["logistics_dispatch/v1"],
                        "receipt": ["logistics_receipt/v1"],
                        "discrepancy_report": ["logistics_discrepancy_report/v1"],
                        "discrepancy_resolution": ["logistics_discrepancy_resolution/v1"]
                      },
                      "participant_roles": {
                        "sender": ["sender"],
                        "receiver": ["receiver"],
                        "supervisor": ["supervisor"]
                      },
                      "parameters": {}
                    },
                    "event": []
                  }
                }
                """;
        jdbcTemplate.update("""
                INSERT INTO activities (name, config_json, status, sensitivity)
                VALUES (?, ?::jsonb, 'active', 'standard')
                """, LOGISTICS_TRANSFER_ACTIVITY, config);
    }

    private UUID subjectAt(UUID locationId) {
        UUID subject = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO subject_locations (subject_id, location_id, path)
                VALUES (?::uuid, ?::uuid, (SELECT path FROM locations WHERE id = ?::uuid))
                """, subject.toString(), locationId.toString(), locationId.toString());
        return subject;
    }

    private UUID pushEvent(UUID actorId, UUID deviceId, UUID subjectId, String activityRef,
                           String type, String shapeRef, String notes, long lastPullWatermark) {
        return pushEventWithPayload(actorId, deviceId, subjectId, activityRef, type, shapeRef,
                Map.of("name", "Subject", "category", "campaign", "notes", notes),
                lastPullWatermark).id();
    }

    private PushedEvent pushEventWithPayload(UUID actorId, UUID deviceId, UUID subjectId,
                                             String activityRef, String type, String shapeRef,
                                             Map<String, Object> payload, long lastPullWatermark) {
        UUID eventId = UUID.randomUUID();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", eventId.toString());
        event.put("type", type);
        event.put("shape_ref", shapeRef);
        event.put("activity_ref", activityRef);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", actorId.toString()));
        event.put("device_id", deviceId.toString());
        event.put("device_seq", seqCounter++);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", payload);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("events", List.of(event));
        request.put("last_pull_watermark", lastPullWatermark);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JsonNode> response = rest.exchange("/api/sync/push",
                HttpMethod.POST, new HttpEntity<>(request, headers), JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("accepted").asInt()).isEqualTo(1);
        return new PushedEvent(eventId, response);
    }

    private ResponseEntity<JsonNode> pullEvents(String token, long sinceWatermark, int limit) {
        return pullEvents(token, sinceWatermark, limit, null);
    }

    private ResponseEntity<JsonNode> pullEvents(String token, long sinceWatermark, int limit,
                                                UUID deviceId) {
        Map<String, Object> request = Map.of("since_watermark", sinceWatermark, "limit", limit);
        if (deviceId != null) {
            request = new LinkedHashMap<>(request);
            request.put("device_id", deviceId.toString());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange("/api/sync/pull", HttpMethod.POST,
                new HttpEntity<>(request, headers), JsonNode.class);
    }

    private ResponseEntity<JsonNode> subjectHistory(String token, UUID subjectId,
                                                    String activityRef, long cursor, int limit) {
        Map<String, Object> request = Map.of(
                "subject_id", subjectId.toString(),
                "activity_ref", activityRef,
                "cursor", cursor,
                "limit", limit
        );
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange("/api/sync/subject-history", HttpMethod.POST,
                new HttpEntity<>(request, headers), JsonNode.class);
    }

    private ResponseEntity<JsonNode> resolveFlag(UUID flagId, String token, String resolution) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange("/api/conflicts/" + flagId + "/resolve", HttpMethod.POST,
                new HttpEntity<>(Map.of("resolution", resolution, "reason", "scenario probe"), headers),
                JsonNode.class);
    }

    private long latestWatermark(ResponseEntity<JsonNode> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().get("latest_watermark").asLong();
    }

    private void assertVisibleCaptures(ResponseEntity<JsonNode> response, UUID... eventIds) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureEventIds(response.getBody().get("events")))
                .containsExactlyInAnyOrderElementsOf(
                        List.of(eventIds).stream().map(UUID::toString).toList());
    }

    private void assertCaptureContains(ResponseEntity<JsonNode> response, UUID... eventIds) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureEventIds(response.getBody().get("events")))
                .contains(List.of(eventIds).stream().map(UUID::toString).toArray(String[]::new));
    }

    private void assertCaptureExcludes(ResponseEntity<JsonNode> response, UUID... eventIds) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureEventIds(response.getBody().get("events")))
                .doesNotContain(List.of(eventIds).stream().map(UUID::toString).toArray(String[]::new));
    }

    private List<String> captureEventIds(JsonNode events) {
        List<String> ids = new ArrayList<>();
        for (JsonNode event : events) {
            if ("capture".equals(event.get("type").asText())) {
                ids.add(event.get("id").asText());
            }
        }
        return ids;
    }

    private List<String> flagSources(String category) {
        return jdbcTemplate.queryForList("""
                SELECT payload->>'source_event_id'
                FROM events
                WHERE shape_ref = 'conflict_detected/v1'
                  AND payload->>'flag_category' = ?
                ORDER BY sync_watermark ASC
                """, String.class, category);
    }

    private JsonNode findFlagFor(UUID sourceEventId, String category) {
        String eventJson = jdbcTemplate.queryForObject("""
                SELECT row_to_json(e)::text
                FROM events e
                WHERE e.shape_ref = 'conflict_detected/v1'
                  AND e.payload->>'flag_category' = ?
                  AND e.payload->>'source_event_id' = ?
                ORDER BY e.sync_watermark ASC
                LIMIT 1
                """, String.class, category, sourceEventId.toString());
        try {
            return objectMapper.readTree(eventJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode eventState(UUID sourceEventId, String bindingRef) {
        ArrayNode states = patternStateProjection.projectCurrent(OffsetDateTime.now(ZoneOffset.UTC));
        for (JsonNode state : states) {
            if (!"event".equals(state.get("composition").asText())) {
                continue;
            }
            JsonNode key = state.get("state_key");
            if (sourceEventId.toString().equals(key.get("source_event_id").asText())
                    && bindingRef.equals(key.get("binding_ref").asText())) {
                return state;
            }
        }
        throw new AssertionError("Missing event pattern state for " + sourceEventId + " " + bindingRef);
    }

    private JsonNode subjectState(UUID subjectId, String activityRef, String bindingRef) {
        ArrayNode states = patternStateProjection.projectCurrent(OffsetDateTime.now(ZoneOffset.UTC));
        for (JsonNode state : states) {
            if (!"subject".equals(state.get("composition").asText())) {
                continue;
            }
            JsonNode key = state.get("state_key");
            if (subjectId.toString().equals(key.at("/subject_ref/id").asText())
                    && activityRef.equals(key.get("activity_ref").asText())
                    && bindingRef.equals(key.get("binding_ref").asText())) {
                return state;
            }
        }
        throw new AssertionError("Missing subject pattern state for " + subjectId + " "
                + activityRef + " " + bindingRef);
    }

    private boolean subjectStateExists(UUID subjectId, String activityRef, String bindingRef) {
        ArrayNode states = patternStateProjection.projectCurrent(OffsetDateTime.now(ZoneOffset.UTC));
        for (JsonNode state : states) {
            if (!"subject".equals(state.get("composition").asText())) {
                continue;
            }
            JsonNode key = state.get("state_key");
            if (subjectId.toString().equals(key.at("/subject_ref/id").asText())
                    && activityRef.equals(key.get("activity_ref").asText())
                    && bindingRef.equals(key.get("binding_ref").asText())) {
                return true;
            }
        }
        return false;
    }

    private long syncWatermark(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT sync_watermark FROM events WHERE id = ?::uuid",
                Long.class,
                eventId.toString());
    }

    private long deviceWatermark(UUID deviceId) {
        return jdbcTemplate.queryForObject("""
                SELECT last_pull_watermark
                FROM device_sync_state
                WHERE device_id = ?::uuid
                """, Long.class, deviceId.toString());
    }

    private int eventCount(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events WHERE id = ?::uuid",
                Integer.class,
                eventId.toString());
    }

    private record PushedEvent(UUID id, ResponseEntity<JsonNode> response) {}
}
