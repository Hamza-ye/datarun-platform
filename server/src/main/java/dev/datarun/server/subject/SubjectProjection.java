package dev.datarun.server.subject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Minimal projection: per-subject summary derived from the event stream.
 * Phase 0 — full replay, no materialized views.
 */
@Component
public class SubjectProjection {

    private final JdbcTemplate jdbc;

    public SubjectProjection(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * List all subjects with latest event summary.
     * Full table scan + group — acceptable for Phase 0 scale.
     */
    public List<SubjectSummary> listSubjects() {
        return jdbc.query("""
                SELECT
                    subject_ref->>'id' AS subject_id,
                    (SELECT e2.type FROM events e2
                     WHERE e2.subject_ref->>'id' = subject_ref->>'id'
                     ORDER BY e2.sync_watermark DESC LIMIT 1) AS latest_event_type,
                    MAX(timestamp) AS latest_timestamp,
                    COUNT(*) AS event_count
                FROM events
                GROUP BY subject_ref->>'id'
                ORDER BY MAX(timestamp) DESC
                """,
                (rs, rowNum) -> new SubjectSummary(
                        rs.getString("subject_id"),
                        rs.getString("latest_event_type"),
                        rs.getTimestamp("latest_timestamp").toInstant().atOffset(ZoneOffset.UTC),
                        rs.getInt("event_count")
                ));
    }
}
