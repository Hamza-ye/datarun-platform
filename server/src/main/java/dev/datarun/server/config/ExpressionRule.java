package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

/**
 * An expression rule: L2 form logic stored externally from shapes (IDR-017 L1/L2, IDR-018).
 * Keyed by (activity_ref, shape_ref, field_name, rule_type) with a UNIQUE constraint.
 */
public record ExpressionRule(
        UUID id,
        String activityRef,
        String shapeRef,
        String fieldName,
        String ruleType,
        JsonNode expression,
        String message,
        Instant createdAt
) {
}
