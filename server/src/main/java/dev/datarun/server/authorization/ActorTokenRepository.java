package dev.datarun.server.authorization;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Actor token management (IDR-016). Simple token table for Phase 2.
 * Token: 32 bytes hex-encoded (64 chars), generated via SecureRandom.
 * Migration path to Keycloak: header convention unchanged, JWT replaces token lookup.
 */
@Repository
public class ActorTokenRepository {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final JdbcTemplate jdbc;

    public ActorTokenRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Generate a new token for an actor. Returns the token string.
     */
    public String createToken(UUID actorId) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);

        jdbc.update("""
                INSERT INTO actor_tokens (token, actor_id)
                VALUES (?, ?::uuid)
                """, token, actorId.toString());

        return token;
    }

    /**
     * Resolve a token to actor_id. Returns null if token not found or revoked.
     */
    public UUID resolveToken(String token) {
        List<UUID> results = jdbc.query(
                "SELECT actor_id FROM actor_tokens WHERE token = ? AND revoked = FALSE",
                (rs, rowNum) -> UUID.fromString(rs.getString("actor_id")),
                token);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Revoke a token.
     */
    public void revoke(String token) {
        jdbc.update("UPDATE actor_tokens SET revoked = TRUE WHERE token = ?", token);
    }

    /**
     * Revoke all tokens for an actor.
     */
    public void revokeAllForActor(UUID actorId) {
        jdbc.update("UPDATE actor_tokens SET revoked = TRUE WHERE actor_id = ?::uuid",
                actorId.toString());
    }

    /**
     * List active (non-revoked) tokens for an actor.
     */
    public List<String> findActiveTokensForActor(UUID actorId) {
        return jdbc.query(
                "SELECT token FROM actor_tokens WHERE actor_id = ?::uuid AND revoked = FALSE",
                (rs, rowNum) -> rs.getString("token"),
                actorId.toString());
    }
}
