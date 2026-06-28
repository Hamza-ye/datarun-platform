package dev.datarun.server.ops.provisioning;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.authorization.AssignmentService;
import dev.datarun.server.authorization.Location;
import dev.datarun.server.authorization.LocationRepository;
import dev.datarun.server.authorization.SubjectLocationRepository;
import dev.datarun.server.config.ActivityRepository;
import dev.datarun.server.config.ShapePayloadValidator;
import dev.datarun.server.config.ShapeRepository;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import dev.datarun.server.identity.ServerIdentity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class FieldAssetSeedProvisioner {

    private static final long FIELD_ASSET_SEED_LOCK_ID = 0x4649454C44415354L;
    private static final String INITIAL_BOOTSTRAP_ACTOR =
            "system:assignment_bootstrap/initial";
    private static final UUID SETUP_ACTOR_ID =
            UUID.fromString("17400000-0000-4000-8000-000000000010");
    private static final String SETUP_OWNER_ROLE = "setup_owner";
    private static final OffsetDateTime DEFAULT_VALID_FROM =
            OffsetDateTime.parse("2026-06-27T00:00:00Z");
    private static final String SHAPE_REF = "asset_check/v1";
    private static final String SHAPE_NAME = "asset_check";
    private static final int SHAPE_VERSION = 1;
    private static final String ACTIVITY_REF = "field_asset_inspection";
    private static final Set<String> REQUIRED_NON_GOALS = Set.of(
            "candidate promotion",
            "candidate rejection",
            "lifecycle state",
            "duplicate resolution",
            "merge/split UX",
            "registry import/export",
            "new subject_ref type",
            "semantic location");

    private final ObjectMapper objectMapper;
    private final LocationRepository locationRepository;
    private final SubjectLocationRepository subjectLocationRepository;
    private final AssignmentService assignmentService;
    private final EventRepository eventRepository;
    private final ServerIdentity serverIdentity;
    private final ShapePayloadValidator shapePayloadValidator;
    private final ShapeRepository shapeRepository;
    private final ActivityRepository activityRepository;

    public FieldAssetSeedProvisioner(ObjectMapper objectMapper,
                                     LocationRepository locationRepository,
                                     SubjectLocationRepository subjectLocationRepository,
                                     AssignmentService assignmentService,
                                     EventRepository eventRepository,
                                     ServerIdentity serverIdentity,
                                     ShapePayloadValidator shapePayloadValidator,
                                     ShapeRepository shapeRepository,
                                     ActivityRepository activityRepository) {
        this.objectMapper = objectMapper;
        this.locationRepository = locationRepository;
        this.subjectLocationRepository = subjectLocationRepository;
        this.assignmentService = assignmentService;
        this.eventRepository = eventRepository;
        this.serverIdentity = serverIdentity;
        this.shapePayloadValidator = shapePayloadValidator;
        this.shapeRepository = shapeRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional
    public AppliedSeed apply(String inputJson) {
        SeedManifest manifest = parse(inputJson);
        validateManifest(manifest);
        requireReviewedFieldAssetConfig();

        eventRepository.getJdbcTemplate()
                .queryForList("SELECT pg_advisory_xact_lock(?)", FIELD_ASSET_SEED_LOCK_ID);

        Geography geography = manifest.geography();
        requireSetupOwnerBootstrap(geography);

        int locationsCreated = 0;
        int locationsReused = 0;
        int subjectLocationsCreated = 0;
        int subjectLocationsReused = 0;
        int assignmentsCreated = 0;
        int assignmentsReused = 0;
        int seedEventsInserted = 0;
        int seedEventsReused = 0;

        String regionPath = "/" + geography.regionId();
        if (ensureLocation(geography.regionId(), geography.regionName(), null,
                "region", regionPath)) {
            locationsCreated++;
        } else {
            locationsReused++;
        }

        String assignedPath = regionPath + "/" + geography.assignedLocationId();
        if (ensureLocation(geography.assignedLocationId(),
                geography.assignedLocationName(), geography.regionId(),
                "district", assignedPath)) {
            locationsCreated++;
        } else {
            locationsReused++;
        }

        for (AssetSeed asset : manifest.assets()) {
            String locationPath = locationRepository.findPathById(asset.locationId());
            if (locationPath == null) {
                throw new ProvisioningCommandException(
                        "asset location not found: " + asset.locationId());
            }
            if (ensureSubjectLocation(asset.subjectId(), asset.locationId(),
                    locationPath)) {
                subjectLocationsCreated++;
            } else {
                subjectLocationsReused++;
            }
        }

        for (SeedAssignment assignment : List.of(
                manifest.fieldAssignment(),
                manifest.reviewAssignment(),
                manifest.outOfScopeAssignment())) {
            if (ensureAssignment(assignment)) {
                assignmentsCreated++;
            } else {
                assignmentsReused++;
            }
        }

        for (AssetSeed asset : manifest.assets()) {
            String locationPath = locationRepository.findPathById(asset.locationId());
            if (ensureSeedEvent(asset, locationPath)) {
                seedEventsInserted++;
            } else {
                seedEventsReused++;
            }
        }

        return new AppliedSeed(
                locationsCreated,
                locationsReused,
                subjectLocationsCreated,
                subjectLocationsReused,
                assignmentsCreated,
                assignmentsReused,
                seedEventsInserted,
                seedEventsReused);
    }

    private SeedManifest parse(String inputJson) {
        if (inputJson == null || inputJson.isBlank()) {
            throw new ProvisioningCommandException("field asset seed input is empty");
        }
        try {
            return objectMapper.copy()
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(inputJson, SeedManifest.class);
        } catch (Exception exception) {
            throw new ProvisioningCommandException(
                    "invalid field asset seed JSON", exception);
        }
    }

    private void validateManifest(SeedManifest manifest) {
        if (manifest.schemaVersion() != 1) {
            throw new ProvisioningCommandException("schema_version must be 1");
        }
        requireNonBlank(manifest.source(), "source");
        requireEqual(SHAPE_REF, manifest.shapeRef(), "shape_ref");
        requireEqual(ACTIVITY_REF, manifest.activityRef(), "activity_ref");
        if (manifest.geography() == null) {
            throw new ProvisioningCommandException("geography is required");
        }
        validateGeography(manifest.geography());
        if (manifest.assets() == null || manifest.assets().isEmpty()) {
            throw new ProvisioningCommandException("assets must be non-empty");
        }

        Set<UUID> subjectIds = new LinkedHashSet<>();
        Set<UUID> seedEventIds = new LinkedHashSet<>();
        for (AssetSeed asset : manifest.assets()) {
            validateAssetSeed(asset, manifest.geography().assignedLocationId());
            if (!subjectIds.add(asset.subjectId())) {
                throw new ProvisioningCommandException(
                        "duplicate asset subject_id: " + asset.subjectId());
            }
            if (!seedEventIds.add(asset.seedEventId())) {
                throw new ProvisioningCommandException(
                        "duplicate seed_event_id: " + asset.seedEventId());
            }
        }

        validateAssignment("field_assignment", manifest.fieldAssignment(),
                true, subjectIds, manifest.geography().assignedLocationId());
        validateAssignment("review_assignment", manifest.reviewAssignment(),
                false, subjectIds, manifest.geography().assignedLocationId());
        validateAssignment("out_of_scope_assignment", manifest.outOfScopeAssignment(),
                true, subjectIds, manifest.geography().assignedLocationId());
        if (manifest.nonGoals() == null
                || !new LinkedHashSet<>(manifest.nonGoals())
                .containsAll(REQUIRED_NON_GOALS)) {
            throw new ProvisioningCommandException(
                    "non_goals must preserve field asset setup guardrails");
        }
    }

    private void validateGeography(Geography geography) {
        requireUuid(geography.regionId(), "geography.region_id");
        requireNonBlank(geography.regionName(), "geography.region_name");
        requireUuid(geography.assignedLocationId(),
                "geography.assigned_location_id");
        requireNonBlank(geography.assignedLocationName(),
                "geography.assigned_location_name");
        if (geography.regionId().equals(geography.assignedLocationId())) {
            throw new ProvisioningCommandException(
                    "assigned_location_id must differ from region_id");
        }
    }

    private void validateAssetSeed(AssetSeed asset, UUID assignedLocationId) {
        if (asset == null) {
            throw new ProvisioningCommandException("assets cannot contain null");
        }
        requireUuid(asset.subjectId(), "asset.subject_id");
        requireUuid(asset.seedEventId(), "asset.seed_event_id");
        requireNonBlank(asset.displayLabel(), "asset.display_label");
        requireUuid(asset.locationId(), "asset.location_id");
        if (!assignedLocationId.equals(asset.locationId())) {
            throw new ProvisioningCommandException(
                    "asset.location_id must match assigned_location_id for this bounded package");
        }
    }

    private void validateAssignment(String label, SeedAssignment assignment,
                                    boolean requireSubjectList,
                                    Set<UUID> assetSubjectIds,
                                    UUID assignedLocationId) {
        if (assignment == null) {
            throw new ProvisioningCommandException(label + " is required");
        }
        requireUuid(assignment.targetActorId(), label + ".target_actor_id");
        requireNonBlank(assignment.role(), label + ".role");
        if (!assignedLocationId.equals(assignment.geographicId())) {
            throw new ProvisioningCommandException(
                    label + ".geographic_id must match assigned_location_id");
        }
        if (!List.of(ACTIVITY_REF).equals(assignment.activityList())) {
            throw new ProvisioningCommandException(
                    label + ".activity_list must be exactly [" + ACTIVITY_REF + "]");
        }
        if (requireSubjectList
                && (assignment.subjectList() == null
                || assignment.subjectList().isEmpty())) {
            throw new ProvisioningCommandException(
                    label + ".subject_list must be non-empty");
        }
        if (assignment.subjectList() != null) {
            if (assignment.subjectList().isEmpty()) {
                throw new ProvisioningCommandException(
                        label + ".subject_list must be null or non-empty");
            }
            if (assignment.subjectList().stream().anyMatch(Objects::isNull)) {
                throw new ProvisioningCommandException(
                        label + ".subject_list cannot contain null");
            }
            if (!assetSubjectIds.containsAll(assignment.subjectList())) {
                throw new ProvisioningCommandException(
                        label + ".subject_list must reference seeded assets only");
            }
        }
        if (validTo(assignment) != null
                && !validTo(assignment).isAfter(validFrom(assignment))) {
            throw new ProvisioningCommandException(
                    label + ".valid_to must be after valid_from");
        }
    }

    private void requireReviewedFieldAssetConfig() {
        var shape = shapeRepository.findByNameAndVersion(SHAPE_NAME, SHAPE_VERSION)
                .orElseThrow(() -> new ProvisioningCommandException(
                        "reviewed field asset shape is not published"));
        if (!"active".equals(shape.status())
                || !"field_asset".equals(
                shape.schemaJson().path("subject_binding").asText(null))) {
            throw new ProvisioningCommandException(
                    "reviewed field asset shape is not active field_asset config");
        }

        var activity = activityRepository.findByName(ACTIVITY_REF)
                .orElseThrow(() -> new ProvisioningCommandException(
                        "reviewed field asset activity is not published"));
        JsonNode shapes = activity.configJson().path("shapes");
        boolean containsShape = false;
        if (shapes.isArray()) {
            for (JsonNode shapeRef : shapes) {
                containsShape = containsShape || SHAPE_REF.equals(shapeRef.asText());
            }
        }
        if (!"active".equals(activity.status()) || !containsShape) {
            throw new ProvisioningCommandException(
                    "reviewed field asset activity is not active for asset_check/v1");
        }
    }

    private void requireSetupOwnerBootstrap(Geography geography) {
        List<Event> candidates = eventRepository.findByType("assignment_changed")
                .stream()
                .filter(event -> "assignment_created/v1".equals(event.shapeRef()))
                .filter(this::isSetupOwnerBootstrapRelated)
                .toList();
        List<Event> exactMatches = candidates.stream()
                .filter(event -> matchesSetupOwnerBootstrap(
                        event, geography.assignedLocationId()))
                .toList();
        if (exactMatches.size() == 1 && candidates.size() == 1) {
            return;
        }
        if (candidates.isEmpty()) {
            throw new ProvisioningCommandException(
                    "field asset setup-owner bootstrap assignment is required before field-assets-seed");
        }
        throw new ProvisioningCommandException(
                "field asset setup-owner bootstrap assignment drift");
    }

    private boolean isSetupOwnerBootstrapRelated(Event event) {
        return INITIAL_BOOTSTRAP_ACTOR.equals(
                event.actorRef().path("id").asText(null))
                || SETUP_ACTOR_ID.toString().equals(
                event.payload().path("target_actor").path("id").asText(null));
    }

    private boolean matchesSetupOwnerBootstrap(Event event, UUID assignedLocationId) {
        try {
            JsonNode payload = event.payload();
            JsonNode scope = payload.path("scope");
            return "actor".equals(event.actorRef().path("type").asText(null))
                    && INITIAL_BOOTSTRAP_ACTOR.equals(
                    event.actorRef().path("id").asText(null))
                    && SETUP_ACTOR_ID.toString().equals(
                    payload.path("target_actor").path("id").asText(null))
                    && SETUP_OWNER_ROLE.equals(payload.path("role").asText(null))
                    && Objects.equals(assignedLocationId,
                    nullableUuid(scope.path("geographic")))
                    && nullableUuidArray(scope.path("subject_list")) == null
                    && Objects.equals(List.of(ACTIVITY_REF),
                    nullableTextArray(scope.path("activity")))
                    && sameTime(DEFAULT_VALID_FROM, payload.path("valid_from"))
                    && sameNullableTime(null, payload.path("valid_to"))
                    && !assignmentEnded(event);
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean assignmentEnded(Event assignmentCreated) {
        JsonNode subjectRef = assignmentCreated.subjectRef();
        String assignmentId = subjectRef == null
                ? null
                : subjectRef.path("id").asText(null);
        if (assignmentId == null || assignmentId.isBlank()) {
            return true;
        }
        Integer count = eventRepository.getJdbcTemplate().queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE type = 'assignment_changed'
                  AND shape_ref = 'assignment_ended/v1'
                  AND subject_ref->>'id' = ?
                """, Integer.class, assignmentId);
        return count != null && count > 0;
    }

    private boolean ensureLocation(UUID id, String name, UUID parentId,
                                   String level, String expectedPath) {
        Location existing = locationRepository.findById(id);
        if (existing == null) {
            locationRepository.insert(id, name, parentId, level);
            return true;
        }
        if (!name.equals(existing.name())
                || !Objects.equals(parentId, existing.parentId())
                || !level.equals(existing.level())
                || !expectedPath.equals(existing.path())) {
            throw new ProvisioningCommandException(
                    "location drift for field asset seed: " + id);
        }
        return false;
    }

    private boolean ensureSubjectLocation(UUID subjectId, UUID locationId,
                                          String expectedPath) {
        List<SubjectLocationRow> rows = eventRepository.getJdbcTemplate().query("""
                SELECT location_id, path
                FROM subject_locations
                WHERE subject_id = ?::uuid
                """,
                (rs, rowNum) -> new SubjectLocationRow(
                        UUID.fromString(rs.getString("location_id")),
                        rs.getString("path")),
                subjectId.toString());
        if (rows.isEmpty()) {
            subjectLocationRepository.upsert(subjectId, locationId, expectedPath);
            return true;
        }
        SubjectLocationRow existing = rows.get(0);
        if (!locationId.equals(existing.locationId())
                || !expectedPath.equals(existing.path())) {
            throw new ProvisioningCommandException(
                    "subject location drift for field asset seed: " + subjectId);
        }
        return false;
    }

    private boolean ensureAssignment(SeedAssignment assignment) {
        List<Event> matchingAssignments = eventRepository.findByType("assignment_changed")
                .stream()
                .filter(event -> "assignment_created/v1".equals(event.shapeRef()))
                .filter(event -> assignment.targetActorId().toString().equals(
                        event.payload().path("target_actor").path("id").asText(null)))
                .toList();
        if (matchingAssignments.isEmpty()) {
            assignmentService.createAssignment(
                    SETUP_ACTOR_ID,
                    assignment.targetActorId(),
                    assignment.role(),
                    assignment.geographicId(),
                    assignment.subjectList(),
                    assignment.activityList(),
                    validFrom(assignment),
                    validTo(assignment));
            return true;
        }

        List<Event> exactMatches = matchingAssignments.stream()
                .filter(event -> matchesAssignment(event, assignment))
                .toList();
        if (exactMatches.size() == 1) {
            return false;
        }
        throw new ProvisioningCommandException(
                "assignment drift for field asset seed target_actor: "
                        + assignment.targetActorId());
    }

    private boolean matchesAssignment(Event event, SeedAssignment assignment) {
        JsonNode payload = event.payload();
        JsonNode scope = payload.path("scope");
        return "actor".equals(event.actorRef().path("type").asText(null))
                && SETUP_ACTOR_ID.toString().equals(
                event.actorRef().path("id").asText(null))
                && assignment.role().equals(payload.path("role").asText(null))
                && assignment.targetActorId().toString().equals(
                payload.path("target_actor").path("id").asText(null))
                && Objects.equals(assignment.geographicId(),
                nullableUuid(scope.path("geographic")))
                && Objects.equals(assignment.subjectList(),
                nullableUuidArray(scope.path("subject_list")))
                && Objects.equals(assignment.activityList(),
                nullableTextArray(scope.path("activity")))
                && sameTime(validFrom(assignment), payload.path("valid_from"))
                && sameNullableTime(validTo(assignment), payload.path("valid_to"));
    }

    private boolean ensureSeedEvent(AssetSeed asset, String expectedLocationPath) {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", asset.subjectId().toString());

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", SETUP_ACTOR_ID.toString());

        ObjectNode payload = seedPayload(asset);
        validateSeedPayload(payload);

        Event existing = eventRepository.findById(asset.seedEventId());
        if (existing != null) {
            requireMatchingSeedEvent(existing, subjectRef, actorRef, payload,
                    expectedLocationPath);
            return false;
        }

        Event event = new Event(
                asset.seedEventId(),
                "capture",
                SHAPE_REF,
                ACTIVITY_REF,
                subjectRef,
                actorRef,
                serverIdentity.getDeviceId(),
                (int) serverIdentity.nextDeviceSeq(),
                null,
                DEFAULT_VALID_FROM,
                payload);
        boolean inserted = eventRepository.insert(event);
        Event stored = eventRepository.findById(asset.seedEventId());
        if (!inserted && stored == null) {
            throw new ProvisioningCommandException(
                    "seed event insert conflict for field asset seed: "
                            + asset.seedEventId());
        }
        requireMatchingSeedEvent(stored, subjectRef, actorRef, payload,
                expectedLocationPath);
        return inserted;
    }

    private ObjectNode seedPayload(AssetSeed asset) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("field_asset", asset.subjectId().toString());
        payload.put("name", asset.displayLabel());
        return payload;
    }

    private void validateSeedPayload(ObjectNode payload) {
        List<String> violations = shapePayloadValidator.validate(SHAPE_REF, payload);
        if (!violations.isEmpty()) {
            throw new ProvisioningCommandException(
                    "field asset seed payload is invalid: "
                            + String.join("; ", violations));
        }
    }

    private void requireMatchingSeedEvent(Event event, ObjectNode subjectRef,
                                          ObjectNode actorRef, ObjectNode payload,
                                          String expectedLocationPath) {
        if (event == null
                || !"capture".equals(event.type())
                || !SHAPE_REF.equals(event.shapeRef())
                || !ACTIVITY_REF.equals(event.activityRef())
                || !subjectRef.equals(event.subjectRef())
                || !actorRef.equals(event.actorRef())
                || !payload.equals(event.payload())) {
            throw new ProvisioningCommandException(
                    "seed event drift for field asset seed");
        }
        String actualPath = eventRepository.getLocationPath(event.id());
        if (!expectedLocationPath.equals(actualPath)) {
            throw new ProvisioningCommandException(
                    "seed event location drift for field asset seed: " + event.id());
        }
    }

    private OffsetDateTime validFrom(SeedAssignment assignment) {
        return assignment.validFrom() == null
                ? DEFAULT_VALID_FROM
                : assignment.validFrom();
    }

    private OffsetDateTime validTo(SeedAssignment assignment) {
        return assignment.validTo();
    }

    private boolean sameTime(OffsetDateTime expected, JsonNode actual) {
        return actual != null
                && actual.isTextual()
                && expected.toInstant().equals(OffsetDateTime.parse(actual.asText())
                .toInstant());
    }

    private boolean sameNullableTime(OffsetDateTime expected, JsonNode actual) {
        if (expected == null) {
            return actual == null || actual.isMissingNode() || actual.isNull();
        }
        return sameTime(expected, actual);
    }

    private UUID nullableUuid(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return UUID.fromString(node.asText());
    }

    private List<UUID> nullableUuidArray(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        List<UUID> values = new ArrayList<>();
        node.forEach(value -> values.add(UUID.fromString(value.asText())));
        return values;
    }

    private List<String> nullableTextArray(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        List<String> values = new ArrayList<>();
        node.forEach(value -> values.add(value.asText()));
        return values;
    }

    private void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ProvisioningCommandException(field + " is required");
        }
    }

    private void requireUuid(UUID value, String field) {
        if (value == null) {
            throw new ProvisioningCommandException(field + " is required");
        }
    }

    private void requireEqual(String expected, String actual, String field) {
        if (!expected.equals(actual)) {
            throw new ProvisioningCommandException(
                    field + " must be " + expected);
        }
    }

    public record AppliedSeed(
            int locationsCreated,
            int locationsReused,
            int subjectLocationsCreated,
            int subjectLocationsReused,
            int assignmentsCreated,
            int assignmentsReused,
            int seedEventsInserted,
            int seedEventsReused) {}

    private record SubjectLocationRow(UUID locationId, String path) {}

    private record SeedManifest(
            @JsonProperty("schema_version") int schemaVersion,
            String source,
            @JsonProperty("seed_method") String seedMethod,
            @JsonProperty("shape_ref") String shapeRef,
            @JsonProperty("activity_ref") String activityRef,
            Geography geography,
            List<AssetSeed> assets,
            @JsonProperty("field_assignment") SeedAssignment fieldAssignment,
            @JsonProperty("review_assignment") SeedAssignment reviewAssignment,
            @JsonProperty("out_of_scope_assignment") SeedAssignment outOfScopeAssignment,
            @JsonProperty("non_goals") List<String> nonGoals) {}

    private record Geography(
            @JsonProperty("region_id") UUID regionId,
            @JsonProperty("region_name") String regionName,
            @JsonProperty("assigned_location_id") UUID assignedLocationId,
            @JsonProperty("assigned_location_name") String assignedLocationName) {}

    private record AssetSeed(
            @JsonProperty("subject_id") UUID subjectId,
            @JsonProperty("seed_event_id") UUID seedEventId,
            @JsonProperty("display_label") String displayLabel,
            @JsonProperty("location_id") UUID locationId) {}

    private record SeedAssignment(
            @JsonProperty("target_actor_id") UUID targetActorId,
            String role,
            @JsonProperty("geographic_id") UUID geographicId,
            @JsonProperty("subject_list") List<UUID> subjectList,
            @JsonProperty("activity_list") List<String> activityList,
            @JsonProperty("valid_from") OffsetDateTime validFrom,
            @JsonProperty("valid_to") OffsetDateTime validTo) {}
}
