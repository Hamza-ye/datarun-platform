package dev.datarun.ship1.sync;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Actor token lookup. Tokens are deployer-provisioned at device bootstrap.
 * Ship-1 uses a simple bearer scheme — auth model is not pre-decided (retro-documented, not an ADR).
 */
@Repository
public class ActorTokenRepository {

    private final JdbcTemplate jdbc;

    public ActorTokenRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void createToken(String token, UUID actorId) {
        jdbc.update("INSERT INTO actor_tokens (token, actor_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                token, actorId);
    }

    public Optional<UUID> resolveToken(String token) {
        List<UUID> out = jdbc.query(
                "SELECT actor_id FROM actor_tokens WHERE token = ?",
                (rs, i) -> (UUID) rs.getObject("actor_id"),
                token);
        return out.isEmpty() ? Optional.empty() : Optional.of(out.get(0));
    }
}
