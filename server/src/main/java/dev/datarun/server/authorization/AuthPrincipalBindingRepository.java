package dev.datarun.server.authorization;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class AuthPrincipalBindingRepository {

    private final JdbcTemplate jdbc;

    public AuthPrincipalBindingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public UUID resolveActor(String issuer, String subject) {
        List<UUID> results = jdbc.query("""
                SELECT actor_id
                FROM auth_principal_bindings
                WHERE issuer = ?
                  AND subject = ?
                  AND active = TRUE
                ORDER BY id DESC
                LIMIT 1
                """,
                (rs, rowNum) -> UUID.fromString(rs.getString("actor_id")),
                issuer, subject);
        return results.isEmpty() ? null : results.get(0);
    }

    public void lockProvisioning() {
        jdbc.query("SELECT pg_advisory_xact_lock(?)", rs -> null, 40280040L);
    }

    public BindingRow findActiveBindingForUpdate(String issuer, String subject) {
        List<BindingRow> rows = jdbc.query("""
                SELECT id, issuer, subject, actor_id
                FROM auth_principal_bindings
                WHERE issuer = ?
                  AND subject = ?
                  AND active = TRUE
                ORDER BY id DESC
                LIMIT 1
                FOR UPDATE
                """,
                (rs, rowNum) -> new BindingRow(
                        rs.getLong("id"),
                        rs.getString("issuer"),
                        rs.getString("subject"),
                        UUID.fromString(rs.getString("actor_id"))),
                issuer, subject);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public AppliedOperation findAppliedOperation(String operationId) {
        List<AppliedOperation> rows = jdbc.query("""
                SELECT operation_id, operation_hash
                FROM auth_principal_binding_operations
                WHERE operation_id = ?
                """,
                (rs, rowNum) -> new AppliedOperation(
                        rs.getString("operation_id"),
                        rs.getString("operation_hash")),
                operationId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public long insertActiveBinding(String issuer, String subject, UUID actorId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO auth_principal_bindings (issuer, subject, actor_id)
                    VALUES (?, ?, ?::uuid)
                    """, new String[]{"id"});
            ps.setString(1, issuer);
            ps.setString(2, subject);
            ps.setString(3, actorId.toString());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public void deactivateBinding(long bindingId) {
        jdbc.update("""
                UPDATE auth_principal_bindings
                SET active = FALSE,
                    deactivated_at = NOW()
                WHERE id = ?
                  AND active = TRUE
                """, bindingId);
    }

    public void insertOperationAudit(ProvisionedOperationAudit audit) {
        jdbc.update("""
                INSERT INTO auth_principal_binding_operations (
                    operation_id,
                    operation_hash,
                    manifest_version,
                    manifest_source,
                    manifest_content_hash,
                    applied_by,
                    issuer,
                    subject,
                    target_actor_id,
                    desired_active,
                    reason,
                    previous_active_binding_id,
                    previous_actor_id,
                    resulting_binding_id,
                    changed
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::uuid, ?, ?, ?, ?::uuid, ?, ?)
                """,
                audit.operationId(),
                audit.operationHash(),
                audit.manifestVersion(),
                audit.manifestSource(),
                audit.manifestContentHash(),
                audit.appliedBy(),
                audit.issuer(),
                audit.subject(),
                audit.targetActorId().toString(),
                audit.desiredActive(),
                audit.reason(),
                audit.previousActiveBindingId(),
                audit.previousActorId() == null ? null : audit.previousActorId().toString(),
                audit.resultingBindingId(),
                audit.changed());
    }

    public record BindingRow(long id, String issuer, String subject, UUID actorId) {}

    public record AppliedOperation(String operationId, String operationHash) {}

    public record ProvisionedOperationAudit(
            String operationId,
            String operationHash,
            String manifestVersion,
            String manifestSource,
            String manifestContentHash,
            String appliedBy,
            String issuer,
            String subject,
            UUID targetActorId,
            boolean desiredActive,
            String reason,
            Long previousActiveBindingId,
            UUID previousActorId,
            Long resultingBindingId,
            boolean changed) {}
}
