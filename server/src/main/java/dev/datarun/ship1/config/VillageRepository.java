package dev.datarun.ship1.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** Geographic seed table — villages used as the Ship-1 scope dimension. */
@Repository
public class VillageRepository {

    private final JdbcTemplate jdbc;

    public VillageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void upsert(UUID id, String districtName, String name) {
        jdbc.update("INSERT INTO villages (id, district_name, name) VALUES (?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET district_name = EXCLUDED.district_name, name = EXCLUDED.name",
                id, districtName, name);
    }

    public List<Village> findAll() {
        return jdbc.query("SELECT id, district_name, name FROM villages ORDER BY name",
                (rs, i) -> new Village(
                        (UUID) rs.getObject("id"),
                        rs.getString("district_name"),
                        rs.getString("name")));
    }

    public List<Village> findByIds(List<UUID> ids) {
        if (ids.isEmpty()) return List.of();
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        return jdbc.query("SELECT id, district_name, name FROM villages WHERE id IN (" + placeholders + ") ORDER BY name",
                (rs, i) -> new Village((UUID) rs.getObject("id"), rs.getString("district_name"), rs.getString("name")),
                ids.toArray());
    }

    public record Village(UUID id, String districtName, String name) {}
}
