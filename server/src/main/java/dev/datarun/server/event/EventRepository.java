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
            if (rows > 0) {
                // Resolve location_path for subject events (IDR-015)
                resolveLocationPath(event);
            }
            return rows > 0;
        } catch (DuplicateKeyException e) {
            // device_id + device_seq unique constraint violation
            return false;
        }
    }

    /**
     * Resolve and set location_path on a newly inserted event (IDR-015).
     * Only subject events get a location_path. Assignment/system events get NULL.
     */
    private void resolveLocationPath(Event event) {
        JsonNode subjectRef = event.subjectRef();
        if (subjectRef == null) return;
        String subjectType = subjectRef.path("type").asText("");
        if (!"subject".equals(subjectType)) return;

        String subjectId = subjectRef.path("id").asText(null);
        if (subjectId == null) return;

        try {
            List<String> paths = jdbc.queryForList(
                    "SELECT path FROM subject_locations WHERE subject_id = ?::uuid",
                    String.class, subjectId);
            if (!paths.isEmpty()) {
                jdbc.update("UPDATE events SET location_path = ? WHERE id = ?::uuid",
                        paths.get(0), event.id().toString());
            }
        } catch (Exception e) {
            // subject_locations table may not exist during early migrations — safe to skip
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
     * Scope-filtered pull (IDR-015). Three categories OR'd:
     * 1. Subject events matching geographic scope (LIKE prefix)
     * 2. Own assignment events (sync rule E9 — always included)
     * 3. System events in scope
     *
     * @param sinceWatermark watermark to pull from
     * @param limit page size
     * @param actorId the pulling actor
     * @param scopePaths geographic scope paths (one per active assignment)
     * @return events matching the actor's scope
     */
    public List<Event> findSinceScoped(long sinceWatermark, int limit,
                                        UUID actorId, List<String> scopePaths) {
        if (scopePaths == null || scopePaths.isEmpty()) {
            // Actor has no geographic scope — return only own assignment events (E9)
            return jdbc.query("""
                    SELECT id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                           device_id, device_seq, sync_watermark, timestamp, payload
                    FROM events
                    WHERE sync_watermark > ?
                      AND type = 'assignment_changed'
                      AND payload->'target_actor'->>'id' = ?
                    ORDER BY sync_watermark ASC
                    LIMIT ?
                    """,
                    eventRowMapper(),
                    sinceWatermark, actorId.toString(), limit);
        }

        // Build dynamic LIKE conditions for geographic scope paths
        StringBuilder sql = new StringBuilder();
        sql.append("""
                SELECT id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                       device_id, device_seq, sync_watermark, timestamp, payload
                FROM events
                WHERE sync_watermark > ?
                  AND (
                    -- Category 1: Subject events in geographic scope
                    (location_path IS NOT NULL AND (
                """);

        List<Object> params = new java.util.ArrayList<>();
        params.add(sinceWatermark);

        for (int i = 0; i < scopePaths.size(); i++) {
            if (i > 0) sql.append(" OR ");
            sql.append("location_path LIKE ?");
            params.add(scopePaths.get(i) + "%");
        }

        sql.append("""
                    ))
                    -- Category 2: Own assignment events (E9 — always included)
                    OR (type = 'assignment_changed' AND payload->'target_actor'->>'id' = ?)
                    -- Category 3: System events in scope
                    OR (type IN ('conflict_detected', 'conflict_resolved', 'subjects_merged', 'subject_split')
                        AND location_path IS NOT NULL AND (
                """);
        params.add(actorId.toString());

        for (int i = 0; i < scopePaths.size(); i++) {
            if (i > 0) sql.append(" OR ");
            sql.append("location_path LIKE ?");
            params.add(scopePaths.get(i) + "%");
        }

        sql.append("""
                    ))
                  )
                ORDER BY sync_watermark ASC
                LIMIT ?
                """);
        params.add(limit);

        return jdbc.query(sql.toString(), eventRowMapper(), params.toArray());
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

    /**
     * Find a single event by ID. Returns null if not found.
     */
    public Event findById(UUID id) {
        List<Event> results = jdbc.query("""
                SELECT id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                       device_id, device_seq, sync_watermark, timestamp, payload
                FROM events
                WHERE id = ?::uuid
                """,
                eventRowMapper(),
                id.toString());
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Find events by type, ordered by sync_watermark.
     */
    public List<Event> findByType(String type) {
        return jdbc.query("""
                SELECT id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                       device_id, device_seq, sync_watermark, timestamp, payload
                FROM events
                WHERE type = ?
                ORDER BY sync_watermark DESC
                """,
                eventRowMapper(),
                type);
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
