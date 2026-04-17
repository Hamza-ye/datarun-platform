package dev.datarun.server.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EventRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public EventRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbc;
    }

    /**
     * Insert an event. Returns true if inserted, false if duplicate (by id).
     * sync_watermark is assigned by the database (BIGSERIAL DEFAULT).
     * Uses ON CONFLICT DO NOTHING for transaction-safe deduplication.
     */
    public boolean insert(Event event) {
        try {
            int rows = jdbc.update("""
                    INSERT INTO events (id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                                        device_id, device_seq, timestamp, payload)
                    VALUES (?::uuid, ?, ?, ?, ?::jsonb, ?::jsonb, ?::uuid, ?, ?::timestamptz, ?::jsonb)
                    ON CONFLICT (id) DO NOTHING
                    """,
                    event.id().toString(),
                    event.type(),
                    event.shapeRef(),
                    event.activityRef(),
                    toJson(event.subjectRef()),
                    toJson(event.actorRef()),
                    event.deviceId().toString(),
                    event.deviceSeq(),
                    event.timestamp().toString(),
                    toJson(event.payload()));
            return rows > 0;
        } catch (DuplicateKeyException e) {
            // device_id + device_seq unique constraint violation
            return false;
        }
    }

    /**
     * Retrieve the assigned sync_watermark for a persisted event.
     */
    public Long getSyncWatermark(UUID eventId) {
        return jdbc.queryForObject(
                "SELECT sync_watermark FROM events WHERE id = ?::uuid",
                Long.class,
                eventId.toString());
    }

    /**
     * Check if a subject has events from devices OTHER than the given device
     * with sync_watermark strictly greater than the given horizon.
     * Used by ConflictDetector for concurrent_state_change detection.
     */
    public boolean hasNewerEventsFromOtherDevices(UUID subjectId, UUID excludeDeviceId, long watermarkHorizon) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM events
                WHERE subject_ref->>'id' = ?
                  AND device_id != ?::uuid
                  AND sync_watermark > ?
                  AND type NOT IN ('conflict_detected', 'conflict_resolved', 'subjects_merged', 'subject_split')
                """,
                Integer.class,
                subjectId.toString(),
                excludeDeviceId.toString(),
                watermarkHorizon);
        return count != null && count > 0;
    }

    /**
     * Pull events with sync_watermark > sinceWatermark, ordered ascending, limited.
     */
    public List<Event> findSince(long sinceWatermark, int limit) {
        return jdbc.query("""
                SELECT id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                       device_id, device_seq, sync_watermark, timestamp, payload
                FROM events
                WHERE sync_watermark > ?
                ORDER BY sync_watermark ASC
                LIMIT ?
                """,
                eventRowMapper(),
                sinceWatermark, limit);
    }

    /**
     * Find all events for a given subject, ordered by sync_watermark.
     */
    public List<Event> findBySubjectId(UUID subjectId) {
        return jdbc.query("""
                SELECT id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                       device_id, device_seq, sync_watermark, timestamp, payload
                FROM events
                WHERE subject_ref->>'id' = ?
                ORDER BY sync_watermark ASC
                """,
                eventRowMapper(),
                subjectId.toString());
    }

    /**
     * Check if an event with this ID already exists.
     */
    public boolean existsById(UUID id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE id = ?::uuid",
                Integer.class,
                id.toString());
        return count != null && count > 0;
    }

    private RowMapper<Event> eventRowMapper() {
        return (rs, rowNum) -> mapRow(rs);
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        try {
            return new Event(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("type"),
                    rs.getString("shape_ref"),
                    rs.getString("activity_ref"),
                    objectMapper.readTree(rs.getString("subject_ref")),
                    objectMapper.readTree(rs.getString("actor_ref")),
                    UUID.fromString(rs.getString("device_id")),
                    rs.getInt("device_seq"),
                    rs.getObject("sync_watermark", Long.class),
                    rs.getTimestamp("timestamp").toInstant().atOffset(ZoneOffset.UTC),
                    objectMapper.readTree(rs.getString("payload"))
            );
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to parse JSON column", e);
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
