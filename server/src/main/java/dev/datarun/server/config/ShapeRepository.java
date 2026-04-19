package dev.datarun.server.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class ShapeRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ShapeRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public void insert(Shape shape) {
        jdbc.update("""
                INSERT INTO shapes (name, version, status, sensitivity, schema_json)
                VALUES (?, ?, ?, ?, ?::jsonb)
                """,
                shape.name(),
                shape.version(),
                shape.status(),
                shape.sensitivity(),
                toJson(shape.schemaJson()));
    }

    public Optional<Shape> findByNameAndVersion(String name, int version) {
        List<Shape> results = jdbc.query("""
                SELECT name, version, status, sensitivity, schema_json, created_at
                FROM shapes WHERE name = ? AND version = ?
                """,
                shapeRowMapper(), name, version);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Shape> findByName(String name) {
        return jdbc.query("""
                SELECT name, version, status, sensitivity, schema_json, created_at
                FROM shapes WHERE name = ?
                ORDER BY version ASC
                """,
                shapeRowMapper(), name);
    }

    public List<Shape> findAll() {
        return jdbc.query("""
                SELECT name, version, status, sensitivity, schema_json, created_at
                FROM shapes
                ORDER BY name ASC, version ASC
                """,
                shapeRowMapper());
    }

    public List<Shape> findActive() {
        return jdbc.query("""
                SELECT name, version, status, sensitivity, schema_json, created_at
                FROM shapes WHERE status = 'active'
                ORDER BY name ASC, version ASC
                """,
                shapeRowMapper());
    }

    public Optional<Shape> findLatestVersion(String name) {
        List<Shape> results = jdbc.query("""
                SELECT name, version, status, sensitivity, schema_json, created_at
                FROM shapes WHERE name = ?
                ORDER BY version DESC LIMIT 1
                """,
                shapeRowMapper(), name);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public void updateStatus(String name, int version, String status) {
        jdbc.update("UPDATE shapes SET status = ? WHERE name = ? AND version = ?",
                status, name, version);
    }

    public boolean exists(String name, int version) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM shapes WHERE name = ? AND version = ?",
                Integer.class, name, version);
        return count != null && count > 0;
    }

    private RowMapper<Shape> shapeRowMapper() {
        return (rs, rowNum) -> mapRow(rs);
    }

    private Shape mapRow(ResultSet rs) throws SQLException {
        try {
            return new Shape(
                    rs.getString("name"),
                    rs.getInt("version"),
                    rs.getString("status"),
                    rs.getString("sensitivity"),
                    objectMapper.readTree(rs.getString("schema_json")),
                    rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC)
            );
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to parse schema_json", e);
        }
    }

    private String toJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize JSON", e);
        }
    }
}
