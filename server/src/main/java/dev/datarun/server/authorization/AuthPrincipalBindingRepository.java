package dev.datarun.server.authorization;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Transactional
    public void bind(String issuer, String subject, UUID actorId) {
        jdbc.update("""
                UPDATE auth_principal_bindings
                SET active = FALSE,
                    deactivated_at = NOW()
                WHERE issuer = ?
                  AND subject = ?
                  AND active = TRUE
                """, issuer, subject);
        jdbc.update("""
                INSERT INTO auth_principal_bindings (issuer, subject, actor_id)
                VALUES (?, ?, ?::uuid)
                """, issuer, subject, actorId.toString());
    }
}
