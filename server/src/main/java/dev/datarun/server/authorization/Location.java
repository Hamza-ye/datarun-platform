package dev.datarun.server.authorization;

import java.util.UUID;

/**
 * Location in the geographic hierarchy. Static reference data with materialized path
 * for O(log N) containment tests (IDR-014).
 */
public record Location(
        UUID id,
        String name,
        UUID parentId,
        String level,
        String path
) {
}
