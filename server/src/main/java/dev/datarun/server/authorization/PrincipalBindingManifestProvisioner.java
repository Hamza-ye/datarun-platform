package dev.datarun.server.authorization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.authorization.AuthPrincipalBindingRepository.AppliedOperation;
import dev.datarun.server.authorization.AuthPrincipalBindingRepository.BindingRow;
import dev.datarun.server.authorization.AuthPrincipalBindingRepository.ProvisionedOperationAudit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class PrincipalBindingManifestProvisioner {

    private static final Set<String> ACTIVE_STATES = Set.of("active", "inactive");

    private final AuthPrincipalBindingRepository repository;
    private final ObjectMapper objectMapper;

    public PrincipalBindingManifestProvisioner(AuthPrincipalBindingRepository repository,
                                               ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProvisioningResult applyManifestJson(String manifestJson, String appliedBy) {
        if (manifestJson == null || manifestJson.isBlank()) {
            throw new PrincipalBindingProvisioningException("manifest is empty");
        }
        PrincipalBindingManifest manifest = parseManifest(manifestJson);
        validate(manifest, appliedBy);

        String manifestHash = sha256(manifestJson.getBytes(StandardCharsets.UTF_8));
        repository.lockProvisioning();

        int applied = 0;
        int skipped = 0;
        int changed = 0;
        for (PrincipalBindingOperation operation : manifest.operations()) {
            String operationHash = operationHash(operation);
            AppliedOperation existing = repository.findAppliedOperation(operation.operationId().trim());
            if (existing != null) {
                if (!existing.operationHash().equals(operationHash)) {
                    throw new PrincipalBindingProvisioningException(
                            "operation_id_conflict: " + operation.operationId());
                }
                skipped++;
                continue;
            }

            boolean desiredActive = "active".equals(normalize(operation.state()));
            ApplyOutcome outcome = desiredActive
                    ? applyActive(operation)
                    : applyInactive(operation);
            repository.insertOperationAudit(new ProvisionedOperationAudit(
                    operation.operationId().trim(),
                    operationHash,
                    manifest.manifestVersion().trim(),
                    manifest.source().trim(),
                    manifestHash,
                    appliedBy.trim(),
                    operation.issuer().trim(),
                    operation.subject().trim(),
                    parseUuid(operation.actorId()),
                    desiredActive,
                    operation.reason().trim(),
                    outcome.previousBindingId(),
                    outcome.previousActorId(),
                    outcome.resultingBindingId(),
                    outcome.changed()));
            applied++;
            if (outcome.changed()) {
                changed++;
            }
        }
        return new ProvisioningResult(applied, skipped, changed);
    }

    private PrincipalBindingManifest parseManifest(String manifestJson) {
        try {
            return objectMapper.readValue(manifestJson, PrincipalBindingManifest.class);
        } catch (Exception e) {
            throw new PrincipalBindingProvisioningException("invalid_manifest_json", e);
        }
    }

    private void validate(PrincipalBindingManifest manifest, String appliedBy) {
        if (manifest == null) {
            throw new PrincipalBindingProvisioningException("manifest is missing");
        }
        requirePresent(manifest.manifestVersion(), "manifest_version");
        requirePresent(manifest.source(), "source");
        requirePresent(appliedBy, "applied_by");
        if (manifest.operations() == null || manifest.operations().isEmpty()) {
            throw new PrincipalBindingProvisioningException("operations are missing");
        }

        Set<String> operationIds = new HashSet<>();
        Set<String> principals = new HashSet<>();
        for (int i = 0; i < manifest.operations().size(); i++) {
            PrincipalBindingOperation operation = manifest.operations().get(i);
            String prefix = "operations[" + i + "].";
            if (operation == null) {
                throw new PrincipalBindingProvisioningException("missing operations[" + i + "]");
            }
            requirePresent(operation.operationId(), prefix + "operation_id");
            requirePresent(operation.issuer(), prefix + "issuer");
            requirePresent(operation.subject(), prefix + "subject");
            requirePresent(operation.actorId(), prefix + "actor_id");
            requirePresent(operation.state(), prefix + "state");
            requirePresent(operation.reason(), prefix + "reason");
            parseUuid(operation.actorId());
            String state = normalize(operation.state());
            if (!ACTIVE_STATES.contains(state)) {
                throw new PrincipalBindingProvisioningException(prefix + "state must be active or inactive");
            }
            if (!operationIds.add(operation.operationId().trim())) {
                throw new PrincipalBindingProvisioningException(
                        "duplicate operation_id: " + operation.operationId().trim());
            }
            String principal = operation.issuer().trim() + "\n" + operation.subject().trim();
            if (!principals.add(principal)) {
                throw new PrincipalBindingProvisioningException(
                        "ambiguous operations for principal: " + operation.issuer().trim()
                                + " " + operation.subject().trim());
            }
        }
    }

    private ApplyOutcome applyActive(PrincipalBindingOperation operation) {
        UUID targetActor = parseUuid(operation.actorId());
        BindingRow previous = repository.findActiveBindingForUpdate(
                operation.issuer().trim(), operation.subject().trim());
        if (previous != null && previous.actorId().equals(targetActor)) {
            return new ApplyOutcome(previous.id(), previous.actorId(), previous.id(), false);
        }
        if (previous != null) {
            repository.deactivateBinding(previous.id());
        }
        long newBindingId = repository.insertActiveBinding(
                operation.issuer().trim(), operation.subject().trim(), targetActor);
        return new ApplyOutcome(
                previous == null ? null : previous.id(),
                previous == null ? null : previous.actorId(),
                newBindingId,
                true);
    }

    private ApplyOutcome applyInactive(PrincipalBindingOperation operation) {
        UUID targetActor = parseUuid(operation.actorId());
        BindingRow previous = repository.findActiveBindingForUpdate(
                operation.issuer().trim(), operation.subject().trim());
        if (previous == null) {
            return new ApplyOutcome(null, null, null, false);
        }
        if (!previous.actorId().equals(targetActor)) {
            throw new PrincipalBindingProvisioningException(
                    "inactive operation actor does not match active binding: "
                            + operation.operationId().trim());
        }
        repository.deactivateBinding(previous.id());
        return new ApplyOutcome(previous.id(), previous.actorId(), null, true);
    }

    private String operationHash(PrincipalBindingOperation operation) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("operation_id", operation.operationId().trim());
        normalized.put("issuer", operation.issuer().trim());
        normalized.put("subject", operation.subject().trim());
        normalized.put("actor_id", parseUuid(operation.actorId()).toString());
        normalized.put("state", normalize(operation.state()));
        normalized.put("reason", operation.reason().trim());
        try {
            return sha256(objectMapper.writeValueAsBytes(normalized));
        } catch (Exception e) {
            throw new PrincipalBindingProvisioningException("operation_hash_failed", e);
        }
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value == null ? "" : value.trim());
        } catch (Exception e) {
            throw new PrincipalBindingProvisioningException("malformed actor_id");
        }
    }

    private void requirePresent(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new PrincipalBindingProvisioningException("missing " + field);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new PrincipalBindingProvisioningException("hash_failed", e);
        }
    }

    public record PrincipalBindingManifest(
            @JsonProperty("manifest_version") String manifestVersion,
            String source,
            List<PrincipalBindingOperation> operations) {}

    public record PrincipalBindingOperation(
            @JsonProperty("operation_id") String operationId,
            String issuer,
            String subject,
            @JsonProperty("actor_id") String actorId,
            String state,
            String reason) {}

    public record ProvisioningResult(int appliedOperations, int skippedOperations, int changedOperations) {}

    private record ApplyOutcome(
            Long previousBindingId,
            UUID previousActorId,
            Long resultingBindingId,
            boolean changed) {}
}
