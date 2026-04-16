package dev.datarun.server.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * The 11-field event envelope. Immutable once created.
 */
public record Event(
        UUID id,
        String type,
        @JsonProperty("shape_ref") String shapeRef,
        @JsonProperty("activity_ref") String activityRef,
        @JsonProperty("subject_ref") JsonNode subjectRef,
        @JsonProperty("actor_ref") JsonNode actorRef,
        @JsonProperty("device_id") UUID deviceId,
        @JsonProperty("device_seq") int deviceSeq,
        @JsonProperty("sync_watermark") Long syncWatermark,
        OffsetDateTime timestamp,
        JsonNode payload
) {
}
