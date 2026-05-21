package dev.datarun.server.identity;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Materialized subject alias projection derived from subjects_merged events.
 * The event stream is the source of truth; this table keeps ADR-002 single-hop reads cheap.
 */
@Component
public class SubjectAliasProjection {

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactionTemplate;

    public SubjectAliasProjection(JdbcTemplate jdbc, TransactionTemplate transactionTemplate) {
        this.jdbc = jdbc;
        this.transactionTemplate = transactionTemplate;
    }

    public void upsertAlias(UUID retiredId, UUID survivingId, OffsetDateTime mergedAt) {
        jdbc.update("""
                UPDATE subject_aliases SET surviving_id = ?::uuid
                WHERE surviving_id = ?::uuid
                """, survivingId.toString(), retiredId.toString());

        jdbc.update("""
                INSERT INTO subject_aliases (retired_id, surviving_id, merged_at)
                VALUES (?::uuid, ?::uuid, ?::timestamptz)
                ON CONFLICT (retired_id) DO UPDATE
                SET surviving_id = EXCLUDED.surviving_id,
                    merged_at = EXCLUDED.merged_at
                """, retiredId.toString(), survivingId.toString(), mergedAt.toString());
    }

    public UUID resolve(UUID subjectId) {
        List<UUID> survivingIds = jdbc.query("""
                SELECT surviving_id
                FROM subject_aliases
                WHERE retired_id = ?::uuid
                """,
                (rs, rowNum) -> UUID.fromString(rs.getString("surviving_id")),
                subjectId.toString());
        return survivingIds.isEmpty() ? subjectId : survivingIds.get(0);
    }

    public List<UUID> findRetiredAliases(UUID survivingId) {
        return jdbc.query("""
                SELECT retired_id
                FROM subject_aliases
                WHERE surviving_id = ?::uuid
                """,
                (rs, rowNum) -> UUID.fromString(rs.getString("retired_id")),
                survivingId.toString());
    }

    public void rebuildFromEvents() {
        transactionTemplate.executeWithoutResult(status -> {
            List<MergeAlias> mergeEvents = jdbc.query("""
                    SELECT payload->>'retired_id' AS retired_id,
                           payload->>'surviving_id' AS surviving_id,
                           timestamp
                    FROM events
                    WHERE shape_ref LIKE 'subjects_merged/%'
                      AND payload->>'retired_id' IS NOT NULL
                      AND payload->>'surviving_id' IS NOT NULL
                    ORDER BY sync_watermark ASC
                    """,
                    (rs, rowNum) -> new MergeAlias(
                            UUID.fromString(rs.getString("retired_id")),
                            UUID.fromString(rs.getString("surviving_id")),
                            rs.getTimestamp("timestamp").toInstant().atOffset(ZoneOffset.UTC)));

            jdbc.update("DELETE FROM subject_aliases");
            for (MergeAlias mergeEvent : mergeEvents) {
                upsertAlias(mergeEvent.retiredId(), mergeEvent.survivingId(), mergeEvent.mergedAt());
            }
        });
    }

    private record MergeAlias(UUID retiredId, UUID survivingId, OffsetDateTime mergedAt) {}
}
