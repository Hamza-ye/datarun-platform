package dev.datarun.server.ops.provisioning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.authorization.ActorTokenRepository;
import dev.datarun.server.authorization.LocationRepository;
import dev.datarun.server.authorization.ScopedOperationalReportSnapshotService;
import dev.datarun.server.config.ConfigPackager;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldAssetSetupSeedPackageIntegrationTest extends AbstractIntegrationTest {

    private static final UUID OPERATOR =
            UUID.fromString("17400000-0000-4000-8000-000000000099");
    private static final UUID FIELD_ACTOR =
            UUID.fromString("17400000-0000-4000-8000-000000000011");
    private static final UUID REVIEWER =
            UUID.fromString("17400000-0000-4000-8000-000000000012");
    private static final UUID OUT_OF_SCOPE_ACTOR =
            UUID.fromString("17400000-0000-4000-8000-000000000013");
    private static final UUID DEVICE =
            UUID.fromString("17400000-0000-4000-8000-000000000020");

    @Autowired
    private OneShotProvisioningService provisioningService;

    @Autowired
    private ConfigPackager configPackager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ActorTokenRepository actorTokenRepository;

    @Autowired
    private ScopedOperationalReportSnapshotService reportSnapshotService;

    @Autowired
    private TestRestTemplate rest;

    private String fieldToken;
    private String reviewerToken;
    private String outOfScopeToken;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM actor_tokens");
        jdbcTemplate.update("DELETE FROM subject_locations");
        jdbcTemplate.update("DELETE FROM auth_principal_binding_operations");
        jdbcTemplate.update("DELETE FROM auth_principal_bindings");
        jdbcTemplate.update("DELETE FROM config_packages");
        jdbcTemplate.update("DELETE FROM deployment_config");
        jdbcTemplate.update("DELETE FROM expression_rules");
        jdbcTemplate.update("DELETE FROM activities");
        jdbcTemplate.update("DELETE FROM shapes");
        jdbcTemplate.update("DELETE FROM events");
        jdbcTemplate.update("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM device_sync_state");
        jdbcTemplate.update("DELETE FROM locations");

        fieldToken = actorTokenRepository.createToken(FIELD_ACTOR);
        reviewerToken = actorTokenRepository.createToken(REVIEWER);
        outOfScopeToken = actorTokenRepository.createToken(OUT_OF_SCOPE_ACTOR);
    }

    @Test
    void fieldAssetSetupSeedPackageUsesExistingScopeAndSyncPaths()
            throws Exception {
        JsonNode seed = objectMapper.readTree(packageFile("seeded-field-assets.synthetic.json"));

        JsonNode configPublish = provisioningService.execute(
                "config-publish",
                packageFile("reviewed-config.json"),
                OPERATOR,
                "NW-174-config");
        assertThat(configPublish.path("config_version").asInt()).isEqualTo(1);
        assertThat(configPublish.path("published").asBoolean()).isTrue();

        JsonNode packageJson = configPackager.getLatest().orElseThrow().packageJson();
        assertThat(packageJson.at("/shapes/asset_check~1v1/subject_binding").asText())
                .isEqualTo("field_asset");
        assertThat(packageJson.at("/activities/field_asset_inspection/shapes/0").asText())
                .isEqualTo("asset_check/v1");
        assertThat(packageJson.toString())
                .doesNotContain("promote")
                .doesNotContain("reject")
                .doesNotContain("lifecycle")
                .doesNotContain("duplicate")
                .doesNotContain("merge")
                .doesNotContain("split");

        JsonNode bootstrap = provisioningService.execute(
                "assignment-bootstrap",
                packageFile("assignment-bootstrap.setup-owner.json"),
                OPERATOR,
                "NW-174-bootstrap");
        assertThat(bootstrap.path("created").asBoolean()).isTrue();

        List<AssetSeed> assets = assetSeeds(seed);
        assertThat(assets).hasSize(2);
        AssetSeed assigned = assets.get(0);
        AssetSeed hidden = assets.get(1);

        long seedCursor = latestWatermark();
        JsonNode seedApply = provisioningService.execute(
                "field-assets-seed",
                packageFile("seeded-field-assets.synthetic.json"),
                OPERATOR,
                "NW-178-seed");
        assertThat(seedApply.path("locations_created").asInt()).isEqualTo(2);
        assertThat(seedApply.path("locations_reused").asInt()).isZero();
        assertThat(seedApply.path("subject_locations_created").asInt()).isEqualTo(2);
        assertThat(seedApply.path("subject_locations_reused").asInt()).isZero();
        assertThat(seedApply.path("assignments_created").asInt()).isEqualTo(3);
        assertThat(seedApply.path("assignments_reused").asInt()).isZero();
        assertThat(seedApply.path("seed_events_inserted").asInt()).isEqualTo(2);
        assertThat(seedApply.path("seed_events_reused").asInt()).isZero();
        assertThat(countFieldAssetSeedEvents()).isEqualTo(2);
        assertThat(countFieldAssetProvisionedAssignments()).isEqualTo(3);

        JsonNode exactReapply = provisioningService.execute(
                "field-assets-seed",
                packageFile("seeded-field-assets.synthetic.json"),
                OPERATOR,
                "NW-178-reapply");
        assertThat(exactReapply.path("locations_created").asInt()).isZero();
        assertThat(exactReapply.path("locations_reused").asInt()).isEqualTo(2);
        assertThat(exactReapply.path("subject_locations_created").asInt()).isZero();
        assertThat(exactReapply.path("subject_locations_reused").asInt()).isEqualTo(2);
        assertThat(exactReapply.path("assignments_created").asInt()).isZero();
        assertThat(exactReapply.path("assignments_reused").asInt()).isEqualTo(3);
        assertThat(exactReapply.path("seed_events_inserted").asInt()).isZero();
        assertThat(exactReapply.path("seed_events_reused").asInt()).isEqualTo(2);
        assertThat(countFieldAssetSeedEvents()).isEqualTo(2);
        assertThat(countFieldAssetProvisionedAssignments()).isEqualTo(3);

        ObjectNode drift = (ObjectNode) objectMapper.readTree(
                packageFile("seeded-field-assets.synthetic.json"));
        ((ObjectNode) drift.path("assets").get(0))
                .put("display_label", "Changed pilot pump");
        assertThatThrownBy(() -> provisioningService.execute(
                "field-assets-seed",
                objectMapper.writeValueAsString(drift),
                OPERATOR,
                "NW-178-drift"))
                .isInstanceOf(ProvisioningCommandException.class)
                .hasMessageContaining("seed event drift");
        assertThat(countFieldAssetSeedEvents()).isEqualTo(2);
        assertThat(countFieldAssetProvisionedAssignments()).isEqualTo(3);

        ResponseEntity<JsonNode> fieldPull = pullEvents(fieldToken, seedCursor, 100);
        assertThat(fieldPull.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureSubjectIds(fieldPull.getBody().path("events")))
                .containsExactly(assigned.subjectId().toString());
        assertThat(captureNames(fieldPull.getBody().path("events")))
                .containsExactly(assigned.displayLabel());

        ResponseEntity<JsonNode> reviewerPull = pullEvents(reviewerToken, seedCursor, 100);
        assertThat(reviewerPull.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureSubjectIds(reviewerPull.getBody().path("events")))
                .containsExactlyInAnyOrder(
                        assigned.subjectId().toString(),
                        hidden.subjectId().toString());

        ResponseEntity<JsonNode> outOfScopePull =
                pullEvents(outOfScopeToken, seedCursor, 100);
        assertThat(outOfScopePull.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(captureSubjectIds(outOfScopePull.getBody().path("events")))
                .containsExactly(hidden.subjectId().toString())
                .doesNotContain(assigned.subjectId().toString());

        UUID selectedEventId = UUID.randomUUID();
        Map<String, Object> selectedPayload = new LinkedHashMap<>();
        selectedPayload.put("field_asset", assigned.subjectId().toString());
        selectedPayload.put("name", "Selected " + assigned.displayLabel());
        ResponseEntity<JsonNode> selectedResponse = pushConfiguredEventAs(
                fieldToken, FIELD_ACTOR, assigned.subjectId(), selectedEventId,
                "asset_check/v1", "field_asset_inspection", selectedPayload);
        assertThat(selectedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eventSubjectId(selectedEventId)).isEqualTo(assigned.subjectId().toString());
        assertThat(eventPayloadText(selectedEventId, "field_asset"))
                .isEqualTo(assigned.subjectId().toString());
        assertThat(eventPayloadHasKey(selectedEventId, "asset_candidate_evidence"))
                .isFalse();
        assertThat(eventLocationPath(selectedEventId))
                .isEqualTo(locationRepository.findPathById(assigned.locationId()));

        long candidateCursor = latestWatermark();
        UUID candidateSubject = UUID.randomUUID();
        UUID candidateEventId = pushCandidateEvidenceEvent(candidateSubject);
        assertThat(eventLocationPath(candidateEventId))
                .isEqualTo(locationRepository.findPathById(assigned.locationId()));
        assertThat(eventPayloadHasKey(candidateEventId, "field_asset")).isFalse();
        assertThat(knownFieldAssetBindings())
                .contains(assigned.subjectId().toString(), hidden.subjectId().toString())
                .doesNotContain(candidateSubject.toString());

        ResponseEntity<JsonNode> candidateReviewerPull =
                pullEvents(reviewerToken, candidateCursor, 100);
        assertThat(captureEventIds(candidateReviewerPull.getBody().path("events")))
                .contains(candidateEventId.toString());
        JsonNode candidate = eventById(
                candidateReviewerPull.getBody().path("events"), candidateEventId)
                .path("payload")
                .path("asset_candidate_evidence");
        assertThat(candidate.path("standing").asText()).isEqualTo("candidate");
        assertThat(candidate.path("display_label").asText()).isEqualTo("Unknown pump");
        assertThat(candidate.has("promoted")).isFalse();
        assertThat(candidate.has("rejected")).isFalse();
        assertThat(candidate.has("lifecycle_state")).isFalse();
        assertThat(candidate.has("duplicate_resolution")).isFalse();
        assertThat(candidate.has("merge")).isFalse();
        assertThat(candidate.has("split")).isFalse();

        ScopedOperationalReportSnapshotService.ConfiguredWorkEvidence reviewerEvidence =
                reportSnapshotService.configuredWorkEvidence(REVIEWER, candidateEventId);
        assertThat(reviewerEvidence.visible()).isTrue();
        assertThat(reviewerEvidence.activityRef()).isEqualTo("field_asset_inspection");
        assertThat(reviewerEvidence.shapeRef()).isEqualTo("asset_check/v1");
        assertThat(reviewerEvidence.candidateEvidence()).isNotNull();
        assertThat(reviewerEvidence.candidateEvidence().reviewLabel())
                .isEqualTo("Candidate asset");
        assertThat(reviewerEvidence.candidateEvidence().displayLabel())
                .isEqualTo("Unknown pump");
        assertThat(reviewerEvidence.candidateEvidence().standing())
                .isEqualTo("Needs review before it can be used as a known asset.");
        assertThat(reviewerEvidence.candidateEvidence().lookupStanding())
                .contains("No matching assets in your assigned work.")
                .contains("Lookup standing: incomplete.");
        assertThat(reviewerEvidence.candidateEvidence().captureTimestamp())
                .isNotEqualTo("Not recorded");
        assertThat(reviewerEvidence.candidateEvidence().actorSessionProvenance())
                .isEqualTo("Authenticated actor session recorded this candidate evidence.");
        assertThat(reviewerEvidence.candidateEvidence().assignmentScopeContext())
                .isEqualTo("Assignment and scope context was preserved with this record.");
        assertThat(reviewerEvidence.candidateEvidence().originalRecordReference())
                .isEqualTo("This configured work record contains the candidate evidence.");

        ScopedOperationalReportSnapshotService.ConfiguredWorkEvidence outOfScopeEvidence =
                reportSnapshotService.configuredWorkEvidence(
                        OUT_OF_SCOPE_ACTOR, candidateEventId);
        assertThat(outOfScopeEvidence.visible()).isFalse();

        ResponseEntity<JsonNode> candidateOutOfScopePull =
                pullEvents(outOfScopeToken, candidateCursor, 100);
        assertThat(captureEventIds(candidateOutOfScopePull.getBody().path("events")))
                .doesNotContain(candidateEventId.toString());
    }

    private String packageFile(String fileName) throws Exception {
        return Files.readString(Path.of(
                "..",
                "deploy",
                "reference",
                "pilot-packages",
                "field-assets",
                fileName));
    }

    private List<AssetSeed> assetSeeds(JsonNode seed) {
        return StreamSupport.stream(seed.path("assets").spliterator(), false)
                .map(asset -> new AssetSeed(
                        UUID.fromString(asset.path("subject_id").asText()),
                        UUID.fromString(asset.path("seed_event_id").asText()),
                        asset.path("display_label").asText(),
                        UUID.fromString(asset.path("location_id").asText())))
                .toList();
    }

    private UUID pushCandidateEvidenceEvent(UUID subjectId) {
        UUID eventId = UUID.randomUUID();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "Candidate evidence record");
        payload.put("asset_candidate_evidence",
                candidateEvidence(eventId, "Unknown pump"));
        ResponseEntity<JsonNode> response = pushConfiguredEventAs(
                fieldToken, FIELD_ACTOR, subjectId, eventId,
                "asset_check/v1", "field_asset_inspection", payload);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return eventId;
    }

    private Map<String, Object> candidateEvidence(UUID eventId, String label) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("standing", "candidate");
        evidence.put("review_label", "Candidate asset");
        evidence.put("display_label", label);
        evidence.put("candidate_standing",
                "Needs review before it can be used as a known asset.");
        evidence.put("activity_context", Map.of(
                "activity_ref", "field_asset_inspection",
                "shape_ref", "asset_check/v1"));
        evidence.put("lookup_standing", Map.of(
                "state", "online_empty",
                "message", "No matching assets in your assigned work.",
                "offline", false,
                "stale", false,
                "incomplete", true,
                "unavailable", false));
        evidence.put("actor_session_provenance", Map.of(
                "actor_id", FIELD_ACTOR.toString(),
                "session", "local_actor_session"));
        evidence.put("assignment_scope_context", List.of(Map.of(
                "role", "field_worker",
                "activity_list", List.of("field_asset_inspection"))));
        evidence.put("capture_timestamp",
                OffsetDateTime.now(ZoneOffset.UTC).toString());
        evidence.put("original_submitted_record_ref", Map.of(
                "type", "event",
                "id", eventId.toString()));
        return evidence;
    }

    private ResponseEntity<JsonNode> pushConfiguredEventAs(
            String token, UUID actorId, UUID subjectId, UUID eventId,
            String shapeRef, String activityRef, Map<String, Object> payload) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", eventId.toString());
        event.put("type", "capture");
        event.put("shape_ref", shapeRef);
        event.put("activity_ref", activityRef);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", actorId.toString()));
        event.put("device_id", DEVICE.toString());
        event.put("device_seq", (int) (System.nanoTime() % Integer.MAX_VALUE));
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", payload);

        Map<String, Object> request = Map.of("events", List.of(event));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange("/api/sync/push",
                HttpMethod.POST, new HttpEntity<>(request, headers), JsonNode.class);
    }

    private ResponseEntity<JsonNode> pullEvents(
            String token, long sinceWatermark, int limit) {
        Map<String, Object> request = Map.of(
                "since_watermark", sinceWatermark,
                "limit", limit);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange("/api/sync/pull",
                HttpMethod.POST, new HttpEntity<>(request, headers), JsonNode.class);
    }

    private long latestWatermark() {
        Long watermark = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(sync_watermark), 0) FROM events",
                Long.class);
        return watermark == null ? 0 : watermark;
    }

    private List<String> captureSubjectIds(JsonNode events) {
        List<String> ids = new ArrayList<>();
        for (JsonNode event : events) {
            if ("capture".equals(event.path("type").asText())) {
                ids.add(event.path("subject_ref").path("id").asText());
            }
        }
        return ids;
    }

    private List<String> captureEventIds(JsonNode events) {
        List<String> ids = new ArrayList<>();
        for (JsonNode event : events) {
            if ("capture".equals(event.path("type").asText())) {
                ids.add(event.path("id").asText());
            }
        }
        return ids;
    }

    private List<String> captureNames(JsonNode events) {
        List<String> names = new ArrayList<>();
        for (JsonNode event : events) {
            if ("capture".equals(event.path("type").asText())) {
                names.add(event.path("payload").path("name").asText());
            }
        }
        return names;
    }

    private JsonNode eventById(JsonNode events, UUID eventId) {
        for (JsonNode event : events) {
            if (eventId.toString().equals(event.path("id").asText())) {
                return event;
            }
        }
        return objectMapper.createObjectNode();
    }

    private String eventLocationPath(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT location_path FROM events WHERE id = ?::uuid",
                String.class,
                eventId.toString());
    }

    private String eventSubjectId(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT subject_ref->>'id' FROM events WHERE id = ?::uuid",
                String.class,
                eventId.toString());
    }

    private String eventPayloadText(UUID eventId, String fieldName) {
        return jdbcTemplate.queryForObject(
                "SELECT payload ->> ? FROM events WHERE id = ?::uuid",
                String.class,
                fieldName,
                eventId.toString());
    }

    private boolean eventPayloadHasKey(UUID eventId, String fieldName) {
        Boolean hasKey = jdbcTemplate.queryForObject(
                "SELECT jsonb_exists(payload, ?) FROM events WHERE id = ?::uuid",
                Boolean.class,
                fieldName,
                eventId.toString());
        return Boolean.TRUE.equals(hasKey);
    }

    private Set<String> knownFieldAssetBindings() {
        return new LinkedHashSet<>(jdbcTemplate.queryForList("""
                SELECT DISTINCT payload->>'field_asset'
                FROM events
                WHERE payload ? 'field_asset'
                ORDER BY payload->>'field_asset'
                """, String.class));
    }

    private int countFieldAssetSeedEvents() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE type = 'capture'
                  AND shape_ref = 'asset_check/v1'
                  AND activity_ref = 'field_asset_inspection'
                  AND payload ? 'field_asset'
                """, Integer.class);
        return count == null ? 0 : count;
    }

    private int countFieldAssetProvisionedAssignments() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_created/v1'
                  AND actor_ref->>'id' = '17400000-0000-4000-8000-000000000010'
                  AND payload->'target_actor'->>'id' IN (
                    '17400000-0000-4000-8000-000000000011',
                    '17400000-0000-4000-8000-000000000012',
                    '17400000-0000-4000-8000-000000000013'
                  )
                """, Integer.class);
        return count == null ? 0 : count;
    }

    private record AssetSeed(
            UUID subjectId,
            UUID seedEventId,
            String displayLabel,
            UUID locationId
    ) {}
}
