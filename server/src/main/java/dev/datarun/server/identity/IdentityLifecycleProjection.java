package dev.datarun.server.identity;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Event-derived subject identity lifecycle.
 * No lifecycle table is maintained; active/archived is projected from identity events.
 */
@Component
public class IdentityLifecycleProjection {

    private final JdbcTemplate jdbc;

    public IdentityLifecycleProjection(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isArchived(UUID subjectId) {
        return findArchived(subjectId).isPresent();
    }

    public String stateOf(UUID subjectId) {
        return isArchived(subjectId) ? "archived" : "active";
    }

    public Optional<ArchivedSubject> findArchived(UUID subjectId) {
        List<ArchivedSubject> archived = jdbc.query("""
                SELECT archive_type, target_id
                FROM (
                    SELECT 'merged' AS archive_type,
                           payload->>'surviving_id' AS target_id,
                           sync_watermark
                    FROM events
                    WHERE shape_ref LIKE 'subjects_merged/%'
                      AND payload->>'retired_id' = ?
                    UNION ALL
                    SELECT 'split' AS archive_type,
                           payload->>'successor_id' AS target_id,
                           sync_watermark
                    FROM events
                    WHERE shape_ref LIKE 'subject_split/%'
                      AND payload->>'source_id' = ?
                ) archived
                ORDER BY sync_watermark DESC
                LIMIT 1
                """,
                (rs, rowNum) -> new ArchivedSubject(
                        subjectId,
                        ArchiveType.valueOf(rs.getString("archive_type").toUpperCase()),
                        UUID.fromString(rs.getString("target_id"))),
                subjectId.toString(),
                subjectId.toString());
        return archived.stream().findFirst();
    }

    public enum ArchiveType {
        MERGED,
        SPLIT
    }

    public record ArchivedSubject(UUID subjectId, ArchiveType type, UUID targetId) {}
}
