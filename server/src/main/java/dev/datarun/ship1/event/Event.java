package dev.datarun.ship1.event;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 11-field event envelope (ADR-001 §S3, ADR-008).
 * Server-side `sync_watermark` is assigned on insert; null on inbound push.
 */
public record Event(
        UUID id,
        String type,
        String shapeRef,
        String activityRef,
        String subjectType,
        UUID subjectId,
        String actorId,
        UUID deviceId,
        long deviceSeq,
        Long syncWatermark,
        OffsetDateTime timestamp,
        JsonNode payload
) {}
