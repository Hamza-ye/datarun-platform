package dev.datarun.server.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.authorization.AdminCommandCapabilityService;
import dev.datarun.server.authorization.AssignmentAdminCapabilityService;
import dev.datarun.server.ops.provisioning.ProvisioningCommandException;
import dev.datarun.server.ops.provisioning.ReviewedConfigProvisioner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class WebAdminConfigCandidateService {

    private static final String CANDIDATE_KEY = "current";

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final ReviewedConfigProvisioner reviewedConfigProvisioner;
    private final AssignmentAdminCapabilityService assignmentAdminCapabilityService;
    private final AdminCommandCapabilityService adminCommandCapabilityService;
    private final TransactionTemplate transactionTemplate;

    public WebAdminConfigCandidateService(
            JdbcTemplate jdbc,
            ObjectMapper objectMapper,
            ReviewedConfigProvisioner reviewedConfigProvisioner,
            AssignmentAdminCapabilityService assignmentAdminCapabilityService,
            AdminCommandCapabilityService adminCommandCapabilityService,
            TransactionTemplate transactionTemplate) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.reviewedConfigProvisioner = reviewedConfigProvisioner;
        this.assignmentAdminCapabilityService = assignmentAdminCapabilityService;
        this.adminCommandCapabilityService = adminCommandCapabilityService;
        this.transactionTemplate = transactionTemplate;
    }

    public CandidateView currentOrEmpty() {
        return current().stream()
                .findFirst()
                .orElseGet(() -> CandidateView.empty(defaultCandidateJson()));
    }

    @Transactional
    public CandidateView saveDraft(String candidateJson, UUID actorId) {
        return saveDraft(candidateJson, actorId, null);
    }

    @Transactional
    public CandidateView saveDraft(String candidateJson, UUID actorId, String expectedHash) {
        CandidateView existing = currentForUpdate().stream().findFirst().orElse(null);
        requireExpectedHash(existing, expectedHash);
        JsonNode parsed = parseCandidate(candidateJson);
        String canonical = canonicalJson(parsed);
        String hash = sha256(canonical);
        jdbc.update("""
                INSERT INTO web_admin_config_candidates (
                    candidate_key,
                    candidate_json,
                    content_hash,
                    validation_status,
                    validation_violations,
                    readiness_status,
                    created_at,
                    updated_at
                )
                VALUES (?, ?::jsonb, ?, 'not_run', '[]'::jsonb, 'not_reviewed', NOW(), NOW())
                ON CONFLICT (candidate_key) DO UPDATE
                SET candidate_json = EXCLUDED.candidate_json,
                    content_hash = EXCLUDED.content_hash,
                    validation_status = 'not_run',
                    validation_violations = '[]'::jsonb,
                    validated_hash = NULL,
                    validated_at = NULL,
                    validated_by = NULL,
                    readiness_status = 'not_reviewed',
                    readiness_note = NULL,
                    readiness_hash = NULL,
                    readiness_at = NULL,
                    readiness_by = NULL,
                    approval_hash = NULL,
                    approved_at = NULL,
                    approved_by = NULL,
                    published_config_version = NULL,
                    published_at = NULL,
                    published_by = NULL,
                    updated_at = NOW()
                """, CANDIDATE_KEY, canonical, hash);
        ObjectNode detail = objectMapper.createObjectNode();
        if (existing != null && existing.contentHash() != null) {
            detail.put("previous_hash", existing.contentHash());
        }
        recordHistory("draft_saved", actorId, hash, detail);
        return currentOrThrow();
    }

    public ValidationOutcome validateCurrent(UUID actorId) {
        return validateCurrent(actorId, null);
    }

    public ValidationOutcome validateCurrent(UUID actorId, String expectedHash) {
        CandidateView candidate = currentOrThrow();
        requireExpectedHash(candidate, expectedHash);
        List<String> violations = dryRun(candidate.candidateJson(), actorId);
        ArrayNode violationsJson = objectMapper.createArrayNode();
        violations.forEach(violationsJson::add);
        boolean passed = violations.isEmpty();
        ValidationOutcome outcome = transactionTemplate.execute(status -> {
            int updated = jdbc.update("""
                    UPDATE web_admin_config_candidates
                    SET validation_status = ?,
                        validation_violations = ?::jsonb,
                        validated_hash = ?,
                        validated_at = NOW(),
                        validated_by = ?::uuid,
                        readiness_status = CASE WHEN ? THEN readiness_status ELSE 'not_reviewed' END,
                        readiness_note = CASE WHEN ? THEN readiness_note ELSE NULL END,
                        readiness_hash = CASE WHEN ? THEN readiness_hash ELSE NULL END,
                        readiness_at = CASE WHEN ? THEN readiness_at ELSE NULL END,
                        readiness_by = CASE WHEN ? THEN readiness_by ELSE NULL END,
                        approval_hash = CASE WHEN ? THEN approval_hash ELSE NULL END,
                        approved_at = CASE WHEN ? THEN approved_at ELSE NULL END,
                        approved_by = CASE WHEN ? THEN approved_by ELSE NULL END,
                        updated_at = NOW()
                    WHERE candidate_key = ?
                      AND content_hash = ?
                    """,
                    passed ? "passed" : "failed",
                    violationsJson.toString(),
                    passed ? candidate.contentHash() : null,
                    actorId.toString(),
                    passed, passed, passed, passed, passed, passed, passed, passed,
                    CANDIDATE_KEY,
                    candidate.contentHash());
            if (updated != 1) {
                throw new IllegalStateException(
                        "candidate has changed since the page was loaded");
            }
            recordHistory(passed ? "validation_passed" : "validation_failed",
                    actorId, candidate.contentHash(), detailWithViolations(violationsJson));
            return new ValidationOutcome(passed, violations);
        });
        if (outcome == null) {
            throw new IllegalStateException("candidate validation status was not recorded");
        }
        return outcome;
    }

    @Transactional
    public CandidateView recordReadiness(UUID actorId, String readinessStatus, String note) {
        return recordReadiness(actorId, readinessStatus, note, null);
    }

    @Transactional
    public CandidateView recordReadiness(
            UUID actorId, String readinessStatus, String note, String expectedHash) {
        CandidateView candidate = currentForUpdateOrThrow();
        requireExpectedHash(candidate, expectedHash);
        requireValidatedCurrent(candidate);
        if (!"ready".equals(readinessStatus) && !"rejected".equals(readinessStatus)) {
            throw new IllegalArgumentException("readiness status must be ready or rejected");
        }
        int updated = jdbc.update("""
                UPDATE web_admin_config_candidates
                SET readiness_status = ?,
                    readiness_note = ?,
                    readiness_hash = ?,
                    readiness_at = NOW(),
                    readiness_by = ?::uuid,
                    approval_hash = NULL,
                    approved_at = NULL,
                    approved_by = NULL,
                    updated_at = NOW()
                WHERE candidate_key = ?
                  AND content_hash = ?
                """,
                readinessStatus,
                blankToNull(note),
                candidate.contentHash(),
                actorId.toString(),
                CANDIDATE_KEY,
                candidate.contentHash());
        if (updated != 1) {
            throw new IllegalStateException("candidate has changed since the page was loaded");
        }
        ObjectNode detail = objectMapper.createObjectNode();
        detail.put("readiness_status", readinessStatus);
        recordHistory("readiness_recorded", actorId, candidate.contentHash(), detail);
        return currentOrThrow();
    }

    @Transactional
    public CandidateView approveCurrent(UUID actorId) {
        return approveCurrent(actorId, null);
    }

    @Transactional
    public CandidateView approveCurrent(UUID actorId, String expectedHash) {
        CandidateView candidate = currentForUpdateOrThrow();
        requireExpectedHash(candidate, expectedHash);
        requireValidatedCurrent(candidate);
        if (!"ready".equals(candidate.readinessStatus())
                || !candidate.contentHash().equals(candidate.readinessHash())) {
            throw new IllegalStateException("candidate readiness is not current");
        }
        int updated = jdbc.update("""
                UPDATE web_admin_config_candidates
                SET approval_hash = ?,
                    approved_at = NOW(),
                    approved_by = ?::uuid,
                    updated_at = NOW()
                WHERE candidate_key = ?
                  AND content_hash = ?
                """,
                candidate.contentHash(), actorId.toString(), CANDIDATE_KEY,
                candidate.contentHash());
        if (updated != 1) {
            throw new IllegalStateException("candidate has changed since the page was loaded");
        }
        recordHistory("approved", actorId, candidate.contentHash(), objectMapper.createObjectNode());
        return currentOrThrow();
    }

    @Transactional
    public PublishOutcome publishApproved(UUID actorId) {
        return publishApproved(actorId, null);
    }

    @Transactional
    public PublishOutcome publishApproved(UUID actorId, String expectedHash) {
        CandidateView candidate = currentForUpdateOrThrow();
        requireExpectedHash(candidate, expectedHash);
        requireValidatedCurrent(candidate);
        if (!"ready".equals(candidate.readinessStatus())
                || !candidate.contentHash().equals(candidate.readinessHash())) {
            throw new IllegalStateException("candidate readiness is not current");
        }
        if (!candidate.contentHash().equals(candidate.approvalHash())) {
            throw new IllegalStateException("candidate approval is not current");
        }

        ReviewedConfigProvisioner.ConfigProvisioningResult result =
                reviewedConfigProvisioner.applyReviewedConfig(
                        fullReviewedConfig(candidate.candidateJson(), candidate.contentHash()),
                        actorId);
        int updated = jdbc.update("""
                UPDATE web_admin_config_candidates
                SET published_config_version = ?,
                    published_at = NOW(),
                    published_by = ?::uuid,
                    updated_at = NOW()
                WHERE candidate_key = ?
                  AND content_hash = ?
                """,
                result.configVersion(), actorId.toString(), CANDIDATE_KEY,
                candidate.contentHash());
        if (updated != 1) {
            throw new IllegalStateException("candidate has changed since the page was loaded");
        }
        ObjectNode detail = objectMapper.createObjectNode();
        detail.put("config_version", result.configVersion());
        detail.put("published", result.published());
        detail.put("changed_authoring_rows", result.changedAuthoringRows());
        recordHistory("published", actorId, candidate.contentHash(), detail);
        return new PublishOutcome(
                result.configVersion(), result.published(), result.changedAuthoringRows());
    }

    private List<String> dryRun(JsonNode candidateJson, UUID actorId) {
        try {
            transactionTemplate.execute(status -> {
                status.setRollbackOnly();
                reviewedConfigProvisioner.applyReviewedConfig(
                        fullReviewedConfig(candidateJson, sha256(canonicalJson(candidateJson))),
                        actorId);
                return null;
            });
            return List.of();
        } catch (ProvisioningCommandException | IllegalStateException | IllegalArgumentException e) {
            return List.of(e.getMessage());
        }
    }

    private String fullReviewedConfig(JsonNode candidateJson, String hash) {
        ObjectNode full = (ObjectNode) candidateJson.deepCopy();
        if (!full.hasNonNull("source") || full.path("source").asText().isBlank()) {
            full.put("source", "web-admin-candidate:" + hash);
        }
        full.set("assignment_admin_capabilities",
                assignmentAdminCapabilityService.getValidatedPolicy());
        full.set("admin_command_capabilities",
                adminCommandCapabilityService.getValidatedPolicy());
        return canonicalJson(full);
    }

    private JsonNode parseCandidate(String candidateJson) {
        if (candidateJson == null || candidateJson.isBlank()) {
            throw new IllegalArgumentException("candidate setup JSON is empty");
        }
        try {
            CandidateManifest manifest = objectMapper.copy()
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(candidateJson, CandidateManifest.class);
            validateManifestShape(manifest);
            return objectMapper.readTree(candidateJson);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid candidate setup JSON", e);
        }
    }

    private void validateManifestShape(CandidateManifest manifest) {
        if (manifest == null || manifest.schemaVersion() != 1) {
            throw new IllegalArgumentException("schema_version must be 1");
        }
        if (manifest.shapes() == null) {
            throw new IllegalArgumentException("shapes is required");
        }
        if (manifest.activities() == null) {
            throw new IllegalArgumentException("activities is required");
        }
        if (manifest.expressions() == null) {
            throw new IllegalArgumentException("expressions is required");
        }
        if (manifest.flagSeverityOverrides() == null) {
            throw new IllegalArgumentException("flag_severity_overrides is required");
        }
    }

    private void requireValidatedCurrent(CandidateView candidate) {
        if (!"passed".equals(candidate.validationStatus())
                || !candidate.contentHash().equals(candidate.validatedHash())) {
            throw new IllegalStateException("candidate validation is not current");
        }
    }

    private void requireExpectedHash(CandidateView candidate, String expectedHash) {
        if (expectedHash == null) {
            return;
        }
        String expected = expectedHash.trim();
        String currentHash = candidate == null ? null : candidate.contentHash();
        if (expected.isBlank()) {
            if (currentHash != null) {
                throw new IllegalStateException("candidate has changed since the page was loaded");
            }
            return;
        }
        if (!expected.equals(currentHash)) {
            throw new IllegalStateException("candidate has changed since the page was loaded");
        }
    }

    private List<CandidateView> current() {
        return current(false);
    }

    private List<CandidateView> currentForUpdate() {
        return current(true);
    }

    private List<CandidateView> current(boolean forUpdate) {
        String sql = """
                SELECT candidate_json::text,
                       content_hash,
                       validation_status,
                       validation_violations::text,
                       validated_hash,
                       validated_at,
                       validated_by,
                       readiness_status,
                       readiness_note,
                       readiness_hash,
                       readiness_at,
                       readiness_by,
                       approval_hash,
                       approved_at,
                       approved_by,
                       published_config_version,
                       published_at,
                       published_by,
                       updated_at
                FROM web_admin_config_candidates
                WHERE candidate_key = ?
                """ + (forUpdate ? " FOR UPDATE" : "");
        return jdbc.query(sql, candidateRowMapper(), CANDIDATE_KEY);
    }

    private CandidateView currentOrThrow() {
        return current().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("no candidate setup draft exists"));
    }

    private CandidateView currentForUpdateOrThrow() {
        return currentForUpdate().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("no candidate setup draft exists"));
    }

    private RowMapper<CandidateView> candidateRowMapper() {
        return (rs, rowNum) -> mapCandidate(rs);
    }

    private CandidateView mapCandidate(ResultSet rs) throws SQLException {
        try {
            JsonNode candidateJson = objectMapper.readTree(rs.getString("candidate_json"));
            JsonNode validationViolations =
                    objectMapper.readTree(rs.getString("validation_violations"));
            return new CandidateView(
                    true,
                    candidateJson,
                    prettyJson(candidateJson),
                    rs.getString("content_hash"),
                    rs.getString("validation_status"),
                    validationViolations,
                    rs.getString("validated_hash"),
                    timestamp(rs, "validated_at"),
                    uuid(rs, "validated_by"),
                    rs.getString("readiness_status"),
                    rs.getString("readiness_note"),
                    rs.getString("readiness_hash"),
                    timestamp(rs, "readiness_at"),
                    uuid(rs, "readiness_by"),
                    rs.getString("approval_hash"),
                    timestamp(rs, "approved_at"),
                    uuid(rs, "approved_by"),
                    boxedInt(rs, "published_config_version"),
                    timestamp(rs, "published_at"),
                    uuid(rs, "published_by"),
                    timestamp(rs, "updated_at"));
        } catch (Exception e) {
            throw new SQLException("Failed to parse web-admin config candidate", e);
        }
    }

    private void recordHistory(String action, UUID actorId, String hash, JsonNode detail) {
        jdbc.update("""
                INSERT INTO web_admin_config_candidate_history (
                    action, actor_id, candidate_hash, detail_json
                )
                VALUES (?, ?::uuid, ?, ?::jsonb)
                """,
                action,
                actorId.toString(),
                hash,
                detail == null ? "{}" : detail.toString());
    }

    private ObjectNode detailWithViolations(ArrayNode violations) {
        ObjectNode detail = objectMapper.createObjectNode();
        detail.set("violations", violations);
        return detail;
    }

    private String canonicalJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(canonicalNode(node));
        } catch (Exception e) {
            throw new IllegalArgumentException("could not serialize candidate setup JSON", e);
        }
    }

    private JsonNode canonicalNode(JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return node == null ? objectMapper.nullNode() : node.deepCopy();
        }
        if (node.isArray()) {
            ArrayNode canonical = objectMapper.createArrayNode();
            node.forEach(value -> canonical.add(canonicalNode(value)));
            return canonical;
        }
        if (node.isObject()) {
            ObjectNode canonical = objectMapper.createObjectNode();
            List<String> fieldNames = new ArrayList<>();
            node.fieldNames().forEachRemaining(fieldNames::add);
            Collections.sort(fieldNames);
            fieldNames.forEach(fieldName ->
                    canonical.set(fieldName, canonicalNode(node.get(fieldName))));
            return canonical;
        }
        return node.deepCopy();
    }

    private String prettyJson(JsonNode node) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            return canonicalJson(node);
        }
    }

    private String defaultCandidateJson() {
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
        activityConfig.putObject("roles").putArray("worker").add("capture");

        ObjectNode candidate = objectMapper.createObjectNode();
        candidate.put("schema_version", 1);
        candidate.put("source", "web-admin-candidate");
        ArrayNode shapes = candidate.putArray("shapes");
        ObjectNode shape = shapes.addObject();
        shape.put("name", "visit");
        shape.put("version", 1);
        shape.put("status", "active");
        shape.put("sensitivity", "standard");
        shape.set("schema_json", schema);
        ArrayNode activities = candidate.putArray("activities");
        ObjectNode activity = activities.addObject();
        activity.put("name", "field_visit");
        activity.put("status", "active");
        activity.put("sensitivity", "standard");
        activity.set("config_json", activityConfig);
        candidate.putArray("expressions");
        candidate.set("flag_severity_overrides", objectMapper.createObjectNode());
        return prettyJson(candidate);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private OffsetDateTime timestamp(ResultSet rs, String column) throws SQLException {
        var timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }

    private UUID uuid(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : UUID.fromString(value);
    }

    private Integer boxedInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    public record CandidateView(
            boolean exists,
            JsonNode candidateJson,
            String candidateJsonPretty,
            String contentHash,
            String validationStatus,
            JsonNode validationViolations,
            String validatedHash,
            OffsetDateTime validatedAt,
            UUID validatedBy,
            String readinessStatus,
            String readinessNote,
            String readinessHash,
            OffsetDateTime readinessAt,
            UUID readinessBy,
            String approvalHash,
            OffsetDateTime approvedAt,
            UUID approvedBy,
            Integer publishedConfigVersion,
            OffsetDateTime publishedAt,
            UUID publishedBy,
            OffsetDateTime updatedAt) {

        static CandidateView empty(String sampleJson) {
            return new CandidateView(
                    false,
                    null,
                    sampleJson,
                    null,
                    "not_run",
                    null,
                    null,
                    null,
                    null,
                    "not_reviewed",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }
    }

    public record ValidationOutcome(boolean passed, List<String> violations) {}

    public record PublishOutcome(
            int configVersion, boolean published, int changedAuthoringRows) {}

    private record CandidateManifest(
            @JsonProperty("schema_version") int schemaVersion,
            String source,
            List<ReviewedConfigProvisioner.ReviewedShape> shapes,
            List<ReviewedConfigProvisioner.ReviewedActivity> activities,
            List<ReviewedConfigProvisioner.ReviewedExpression> expressions,
            @JsonProperty("flag_severity_overrides") JsonNode flagSeverityOverrides) {}
}
