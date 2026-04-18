package dev.datarun.server.authorization;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class LocationRepository {

    private final JdbcTemplate jdbc;

    public LocationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Location findById(UUID id) {
        List<Location> results = jdbc.query(
                "SELECT id, name, parent_id, level, path FROM locations WHERE id = ?::uuid",
                locationRowMapper(), id.toString());
        return results.isEmpty() ? null : results.get(0);
    }

    public String findPathById(UUID id) {
        List<String> results = jdbc.query(
                "SELECT path FROM locations WHERE id = ?::uuid",
                (rs, rowNum) -> rs.getString("path"), id.toString());
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Location> findAll() {
        return jdbc.query(
                "SELECT id, name, parent_id, level, path FROM locations ORDER BY path",
                locationRowMapper());
    }

    public List<Location> findChildren(UUID parentId) {
        return jdbc.query(
                "SELECT id, name, parent_id, level, path FROM locations WHERE parent_id = ?::uuid ORDER BY name",
                locationRowMapper(), parentId.toString());
    }

    public List<Location> findRoots() {
        return jdbc.query(
                "SELECT id, name, parent_id, level, path FROM locations WHERE parent_id IS NULL ORDER BY name",
                locationRowMapper());
    }

    /**
     * Insert a new location. Computes the materialized path from parent.
     */
    public void insert(UUID id, String name, UUID parentId, String level) {
        String parentPath = "";
        if (parentId != null) {
            parentPath = findPathById(parentId);
            if (parentPath == null) {
                throw new IllegalArgumentException("Parent location not found: " + parentId);
            }
        }
        String path = parentPath + "/" + id;

        jdbc.update("""
                INSERT INTO locations (id, name, parent_id, level, path)
                VALUES (?::uuid, ?, ?::uuid, ?, ?)
                """,
                id.toString(), name,
                parentId != null ? parentId.toString() : null,
                level, path);
    }

    private RowMapper<Location> locationRowMapper() {
        return (rs, rowNum) -> new Location(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("parent_id") != null ? UUID.fromString(rs.getString("parent_id")) : null,
                rs.getString("level"),
                rs.getString("path")
        );
    }
}
