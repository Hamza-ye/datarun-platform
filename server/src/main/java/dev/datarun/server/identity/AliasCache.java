package dev.datarun.server.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory alias cache: retired_id → surviving_id.
 * Full table loaded at startup. Refreshed after each merge event.
 * At hundreds-to-thousands of entries (<100KB), zero DB round-trips per event resolution.
 * Single-hop lookup guaranteed by eager transitive closure in the alias table.
 */
@Component
public class AliasCache {

    private static final Logger log = LoggerFactory.getLogger(AliasCache.class);

    private final JdbcTemplate jdbc;
    private final ConcurrentHashMap<UUID, UUID> cache = new ConcurrentHashMap<>();

    public AliasCache(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    void loadFromDb() {
        refresh();
    }

    /**
     * Resolve a subject ID through the alias table.
     * Returns the canonical (surviving) ID, or the input if no alias exists.
     */
    public UUID resolve(UUID subjectId) {
        UUID surviving = cache.get(subjectId);
        return surviving != null ? surviving : subjectId;
    }

    /**
     * Check if a subject ID is a retired alias.
     */
    public boolean isRetired(UUID subjectId) {
        return cache.containsKey(subjectId);
    }

    /**
     * Reload the full alias table from DB. Called after merge operations.
     */
    public void refresh() {
        ConcurrentHashMap<UUID, UUID> fresh = new ConcurrentHashMap<>();
        jdbc.query("SELECT retired_id, surviving_id FROM subject_aliases",
                (rs) -> {
                    fresh.put(
                            UUID.fromString(rs.getString("retired_id")),
                            UUID.fromString(rs.getString("surviving_id")));
                });
        cache.clear();
        cache.putAll(fresh);
        log.info("Alias cache loaded: {} entries", cache.size());
    }
}
