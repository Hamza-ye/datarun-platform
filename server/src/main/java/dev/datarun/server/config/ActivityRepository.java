package dev.datarun.server.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class ActivityRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ActivityRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public void insert(Activity activity) {
        jdbc.update("""
                INSERT INTO activities (name, config_json, status, sensitivity)
                VALUES (?, ?::jsonb, ?, ?)
                """,
                activity.name(),
                toJson(activity.configJson()),
                activity.status(),
                activity.sensitivity());
    }

    public void update(Activity activity) {
        jdbc.update("""
                UPDATE activities SET config_json = ?::jsonb, status = ?, sensitivity = ?
                WHERE name = ?
                """,
                toJson(activity.configJson()),
                activity.status(),
                activity.sensitivity(),
                activity.name());
    }

    public Optional<Activity> findByName(String name) {
        List<Activity> results = jdbc.query("""
                SELECT name, config_json, status, sensitivity, created_at
                FROM activities WHERE name = ?
                """,
                activityRowMapper(), name);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Activity> findAll() {
        return jdbc.query("""
                SELECT name, config_json, status, sensitivity, created_at
                FROM activities ORDER BY name ASC
                """,
                activityRowMapper());
    }

    public List<Activity> findActive() {
        return jdbc.query("""
                SELECT name, config_json, status, sensitivity, created_at
                FROM activities WHERE status = 'active' ORDER BY name ASC
                """,
                activityRowMapper());
    }

    public boolean exists(String name) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM activities WHERE name = ?",
                Integer.class, name);
        return count != null && count > 0;
    }

    public void updateStatus(String name, String status) {
        jdbc.update("UPDATE activities SET status = ? WHERE name = ?", status, name);
    }

    private RowMapper<Activity> activityRowMapper() {
        return (rs, rowNum) -> mapRow(rs);
    }

    private Activity mapRow(ResultSet rs) throws SQLException {
        try {
            return new Activity(
                    rs.getString("name"),
                    objectMapper.readTree(rs.getString("config_json")),
                    rs.getString("status"),
                    rs.getString("sensitivity"),
                    rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC)
            );
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to parse config_json", e);
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
