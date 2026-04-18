package dev.datarun.server.authorization;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Subject-to-location mapping. Carries denormalized path from locations table
 * for direct prefix matching without JOIN (IDR-014).
 */
@Repository
public class SubjectLocationRepository {

    private final JdbcTemplate jdbc;

    public SubjectLocationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Get the denormalized path for a subject. Returns null if subject has no location.
     */
    public String findPathBySubjectId(UUID subjectId) {
        List<String> results = jdbc.query(
                "SELECT path FROM subject_locations WHERE subject_id = ?::uuid",
                (rs, rowNum) -> rs.getString("path"), subjectId.toString());
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Assign or update a subject's location. Denormalizes path from locations table.
     */
    public void upsert(UUID subjectId, UUID locationId, String locationPath) {
        jdbc.update("""
                INSERT INTO subject_locations (subject_id, location_id, path)
                VALUES (?::uuid, ?::uuid, ?)
                ON CONFLICT (subject_id) DO UPDATE
                SET location_id = EXCLUDED.location_id, path = EXCLUDED.path
                """,
                subjectId.toString(), locationId.toString(), locationPath);
    }

    /**
     * Check if a subject has a location assigned.
     */
    public boolean exists(UUID subjectId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM subject_locations WHERE subject_id = ?::uuid",
                Integer.class, subjectId.toString());
        return count != null && count > 0;
    }
}
