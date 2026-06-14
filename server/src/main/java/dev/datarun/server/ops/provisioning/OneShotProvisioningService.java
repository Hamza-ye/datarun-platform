package dev.datarun.server.ops.provisioning;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.authorization.AssignmentService;
import dev.datarun.server.authorization.PrincipalBindingManifestProvisioner;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class OneShotProvisioningService {

    private final ObjectMapper objectMapper;
    private final PrincipalBindingManifestProvisioner principalBindingProvisioner;
    private final ReviewedConfigProvisioner reviewedConfigProvisioner;
    private final AssignmentService assignmentService;

    public OneShotProvisioningService(
            ObjectMapper objectMapper,
            PrincipalBindingManifestProvisioner principalBindingProvisioner,
            ReviewedConfigProvisioner reviewedConfigProvisioner,
            AssignmentService assignmentService) {
        this.objectMapper = objectMapper;
        this.principalBindingProvisioner = principalBindingProvisioner;
        this.reviewedConfigProvisioner = reviewedConfigProvisioner;
        this.assignmentService = assignmentService;
    }

    public ObjectNode execute(
            String command, String inputJson, UUID operatorId, String evidenceId) {
        if (inputJson == null) {
            throw new ProvisioningCommandException("input is required");
        }
        return execute(command, inputJson.getBytes(StandardCharsets.UTF_8),
                operatorId, evidenceId);
    }

    public ObjectNode execute(
            String command, byte[] inputBytes, UUID operatorId, String evidenceId) {
        if (operatorId == null) {
            throw new ProvisioningCommandException("operator_id is required");
        }
        if (inputBytes == null || inputBytes.length == 0) {
            throw new ProvisioningCommandException("input is required");
        }
        requireEvidence(evidenceId);
        String inputJson = decodeUtf8(inputBytes);
        String normalized = command == null
                ? "" : command.trim().toLowerCase(Locale.ROOT);
        ObjectNode result = objectMapper.createObjectNode();
        result.put("command", normalized);
        result.put("status", "succeeded");
        result.put("operator_id", operatorId.toString());
        result.put("evidence_id", evidenceId.trim());

        switch (normalized) {
            case "principal-bindings" -> {
                var applied = principalBindingProvisioner.applyManifestJson(
                        inputJson, auditIdentity(operatorId, evidenceId));
                result.put("applied_operations", applied.appliedOperations());
                result.put("skipped_operations", applied.skippedOperations());
                result.put("changed_operations", applied.changedOperations());
            }
            case "config-publish" -> {
                var published = reviewedConfigProvisioner.applyReviewedConfig(
                        inputJson, operatorId);
                result.put("config_version", published.configVersion());
                result.put("published", published.published());
                result.put("changed_authoring_rows", published.changedAuthoringRows());
            }
            case "assignment-bootstrap" -> {
                BootstrapManifest input = parseBootstrap(inputJson);
                validateBootstrap(input);
                var bootstrap = assignmentService.ensureInitialBootstrapAssignment(
                        input.targetActorId(), input.role(), input.geographicId(),
                        input.subjectList(), input.activityList(),
                        input.validFrom(), input.validTo());
                result.put("assignment_event_id", bootstrap.eventId().toString());
                result.put("created", bootstrap.created());
            }
            default -> throw new ProvisioningCommandException(
                    "unsupported provisioning command");
        }
        result.put("input_sha256", sha256(inputBytes));
        return result;
    }

    private String decodeUtf8(byte[] inputBytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(inputBytes))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new ProvisioningCommandException("input must be valid UTF-8");
        }
    }

    private String sha256(byte[] inputBytes) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(inputBytes));
        } catch (Exception exception) {
            throw new ProvisioningCommandException(
                    "input fingerprint could not be calculated");
        }
    }

    private BootstrapManifest parseBootstrap(String inputJson) {
        if (inputJson == null || inputJson.isBlank()) {
            throw new ProvisioningCommandException("bootstrap input is empty");
        }
        try {
            return objectMapper.copy()
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(inputJson, BootstrapManifest.class);
        } catch (Exception exception) {
            throw new ProvisioningCommandException("invalid bootstrap JSON", exception);
        }
    }

    private void validateBootstrap(BootstrapManifest input) {
        if (input.schemaVersion() != 1) {
            throw new ProvisioningCommandException("schema_version must be 1");
        }
        if (input.source() == null || input.source().isBlank()) {
            throw new ProvisioningCommandException("source is required");
        }
        if (input.targetActorId() == null) {
            throw new ProvisioningCommandException("target_actor_id is required");
        }
        if (input.role() == null || input.role().isBlank()) {
            throw new ProvisioningCommandException("role is required");
        }
        if (input.validFrom() == null) {
            throw new ProvisioningCommandException("valid_from is required");
        }
        if (input.validTo() != null && !input.validTo().isAfter(input.validFrom())) {
            throw new ProvisioningCommandException("valid_to must be after valid_from");
        }
    }

    private void requireEvidence(String evidenceId) {
        if (evidenceId == null || evidenceId.isBlank()) {
            throw new ProvisioningCommandException("evidence_id is required");
        }
        if (evidenceId.length() > 200
                || !evidenceId.matches("[A-Za-z0-9][A-Za-z0-9._:/-]*")) {
            throw new ProvisioningCommandException("evidence_id has invalid format");
        }
    }

    private String auditIdentity(UUID operatorId, String evidenceId) {
        return "operator:" + operatorId + ";evidence:" + evidenceId.trim();
    }

    public record BootstrapManifest(
            @JsonProperty("schema_version") int schemaVersion,
            String source,
            @JsonProperty("target_actor_id") UUID targetActorId,
            String role,
            @JsonProperty("geographic_id") UUID geographicId,
            @JsonProperty("subject_list") List<UUID> subjectList,
            @JsonProperty("activity_list") List<String> activityList,
            @JsonProperty("valid_from") OffsetDateTime validFrom,
            @JsonProperty("valid_to") OffsetDateTime validTo) {}
}
