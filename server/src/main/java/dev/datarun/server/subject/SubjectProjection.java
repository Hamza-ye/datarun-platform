package dev.datarun.server.subject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

/**
 * Projection: per-subject summary derived from the event stream.
 * Alias resolution: retired subject IDs are resolved to surviving IDs via subject_aliases table.
 * Flag exclusion: events targeted by a conflict_detected flag are excluded from state derivation.
 * Flagged events remain visible in timeline (findBySubjectId) but don't contribute to projected state.
 */
@Component
public class SubjectProjection {

    private final JdbcTemplate jdbc;

    public SubjectProjection(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * List all subjects with latest event summary.
     * Alias resolution: events referencing retired subject IDs are attributed to the surviving ID.
     * Excludes flagged events from state derivation (event_count, latest_event_type, latest_timestamp
     * reflect only unflagged domain events).
     * System events (conflict_detected, conflict_resolved, subjects_merged, subject_split) are also
     * excluded from the subject summary — they're metadata, not domain state.
     * Archived subjects (those that appear only as retired_id in alias table) are excluded from the list.
     */
    public List<SubjectSummary> listSubjects() {
        return jdbc.query("""
                WITH flagged_event_ids AS (
                    SELECT payload->>'source_event_id' AS flagged_id
                    FROM events
                    WHERE type = 'conflict_detected'
                      AND NOT EXISTS (
                          SELECT 1 FROM events cr
                          WHERE cr.type = 'conflict_resolved'
                            AND cr.payload->>'flag_event_id' = events.id::text
                            AND cr.payload->>'resolution' IN ('accepted', 'reclassified')
                      )
                ),
                domain_events AS (
                    SELECT e.*,
                           COALESCE(sa.surviving_id::text, e.subject_ref->>'id') AS canonical_subject_id
                    FROM events e
                    LEFT JOIN subject_aliases sa ON (e.subject_ref->>'id')::uuid = sa.retired_id
                    WHERE e.type NOT IN ('conflict_detected', 'conflict_resolved', 'subjects_merged', 'subject_split')
                      AND e.id::text NOT IN (SELECT flagged_id FROM flagged_event_ids WHERE flagged_id IS NOT NULL)
                ),
                subject_summary AS (
                    SELECT
                        canonical_subject_id AS subject_id,
                        MAX(timestamp) AS latest_timestamp,
                        COUNT(*) AS event_count
                    FROM domain_events
                    GROUP BY canonical_subject_id
                ),
                unresolved_flags AS (
                    SELECT
                        COALESCE(sa2.surviving_id::text, cd.subject_ref->>'id') AS canonical_subject_id
                    FROM events cd
                    LEFT JOIN subject_aliases sa2 ON (cd.subject_ref->>'id')::uuid = sa2.retired_id
                    WHERE cd.type = 'conflict_detected'
                      AND NOT EXISTS (
                          SELECT 1 FROM events cr
                          WHERE cr.type = 'conflict_resolved'
                            AND cr.payload->>'flag_event_id' = cd.id::text
                      )
                )
                SELECT
                    ss.subject_id,
                    (SELECT de2.type FROM domain_events de2
                     WHERE de2.canonical_subject_id = ss.subject_id
                     ORDER BY de2.sync_watermark DESC LIMIT 1) AS latest_event_type,
                    ss.latest_timestamp,
                    ss.event_count,
                    (SELECT COUNT(*) FROM unresolved_flags uf
                     WHERE uf.canonical_subject_id = ss.subject_id) AS flag_count
                FROM subject_summary ss
                ORDER BY ss.latest_timestamp DESC
                """,
                (rs, rowNum) -> new SubjectSummary(
                        rs.getString("subject_id"),
                        rs.getString("latest_event_type"),
                        rs.getTimestamp("latest_timestamp").toInstant().atOffset(ZoneOffset.UTC),
                        rs.getInt("event_count"),
                        rs.getInt("flag_count")
                ));
    }
}
