package dev.datarun.server.authorization;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents an active assignment reconstructed from the assignment event timeline.
 * PE-derived: created from assignment_created events, ended by assignment_ended events
 * sharing the same subject_ref.id (the assignment UUID).
 */
public record ActiveAssignment(
        UUID assignmentId,
        UUID actorId,
        String role,
        UUID geographicScope,
        String geographicPath,  // denormalized from locations for fast prefix match
        List<UUID> subjectList,
        List<String> activityList,
        OffsetDateTime validFrom,
        OffsetDateTime validTo,
        boolean ended
) {

    /**
     * Check if this assignment is currently active (not ended, not expired).
     */
    public boolean isActive() {
        if (ended) return false;
        if (validTo != null && validTo.isBefore(OffsetDateTime.now())) return false;
        return true;
    }

    /**
     * Check geographic containment: does the given subject path start with this
     * assignment's geographic scope path? (btree prefix match — IDR-014)
     */
    public boolean containsGeographically(String subjectLocationPath) {
        if (geographicPath == null) return true;  // null = no geographic restriction
        if (subjectLocationPath == null) return false;  // subject has no location
        return subjectLocationPath.startsWith(geographicPath);
    }

    /**
     * Check subject list containment.
     */
    public boolean containsSubject(UUID subjectId) {
        if (subjectList == null || subjectList.isEmpty()) return true;  // null = no restriction
        return subjectList.contains(subjectId);
    }

    /**
     * Check activity containment.
     */
    public boolean containsActivity(String activityRef) {
        if (activityList == null || activityList.isEmpty()) return true;  // null = no restriction
        if (activityRef == null) return true;  // event with no activity_ref passes
        return activityList.contains(activityRef);
    }
}
