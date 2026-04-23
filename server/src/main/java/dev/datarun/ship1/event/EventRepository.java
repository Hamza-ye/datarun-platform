package dev.datarun.ship1.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Only writer of {@code events}. Append-only. {@code sync_watermark} assigned by BIGSERIAL on insert.
 * Deduplication is by {@code id} (the primary key) — returns {@link InsertResult#DUPLICATE} without
 * changing the row. Per ADR-001 §S1/§S4 events are never modified or deleted.
 */
@Repository
public class EventRepository {

    public enum InsertResult { INSERTED, DUPLICATE }

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public EventRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    /** Inserts an event. Returns DUPLICATE if an event with the same id already exists. */
    public InsertResult insert(Event e) {
        String sql = """
                INSERT INTO events (
                    id, type, shape_ref, activity_ref,
                    subject_type, subject_id, actor_id,
                    device_id, device_seq, timestamp, payload
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (id) DO NOTHING
                """;
        int rows;
        try {
            rows = jdbc.update(sql,
                    e.id(), e.type(), e.shapeRef(), e.activityRef(),
                    e.subjectType(), e.subjectId(), e.actorId(),
                    e.deviceId(), e.deviceSeq(), e.timestamp(),
                    mapper.writeValueAsString(e.payload()));
        } catch (Exception ex) {
            throw new RuntimeException("insert failed: " + ex.getMessage(), ex);
        }
        return rows == 1 ? InsertResult.INSERTED : InsertResult.DUPLICATE;
    }

    public Optional<Event> findById(UUID id) {
        List<Event> out = jdbc.query(
                "SELECT * FROM events WHERE id = ?", ROW_MAPPER, id);
        return out.isEmpty() ? Optional.empty() : Optional.of(out.get(0));
    }

    public List<Event> findSince(long sinceWatermark, int limit) {
        return jdbc.query(
                "SELECT * FROM events WHERE sync_watermark > ? ORDER BY sync_watermark ASC LIMIT ?",
                ROW_MAPPER, sinceWatermark, limit);
    }

    public List<Event> findAll() {
        return jdbc.query("SELECT * FROM events ORDER BY sync_watermark ASC", ROW_MAPPER);
    }

    public List<Event> findByShapeRefPrefix(String prefix) {
        return jdbc.query(
                "SELECT * FROM events WHERE shape_ref LIKE ? ORDER BY sync_watermark ASC",
                ROW_MAPPER, prefix + "%");
    }

    public List<Event> findBySubjectId(UUID subjectId) {
        return jdbc.query(
                "SELECT * FROM events WHERE subject_id = ? ORDER BY sync_watermark ASC",
                ROW_MAPPER, subjectId);
    }

    private final RowMapper<Event> ROW_MAPPER = this::mapRow;

    private Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            JsonNode payload = mapper.readTree(rs.getString("payload"));
            OffsetDateTime ts = rs.getObject("timestamp", OffsetDateTime.class);
            return new Event(
                    (UUID) rs.getObject("id"),
                    rs.getString("type"),
                    rs.getString("shape_ref"),
                    rs.getString("activity_ref"),
                    rs.getString("subject_type"),
                    (UUID) rs.getObject("subject_id"),
                    rs.getString("actor_id"),
                    (UUID) rs.getObject("device_id"),
                    rs.getLong("device_seq"),
                    rs.getLong("sync_watermark"),
                    ts,
                    payload);
        } catch (Exception ex) {
            throw new SQLException("mapRow failed: " + ex.getMessage(), ex);
        }
    }
}
