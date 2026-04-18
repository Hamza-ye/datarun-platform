package dev.datarun.server.subject;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record SubjectSummary(
        String id,
        @JsonProperty("latest_event_type") String latestEventType,
        @JsonProperty("latest_timestamp") OffsetDateTime latestTimestamp,
        @JsonProperty("event_count") int eventCount,
        @JsonProperty("flag_count") int flagCount
) {
}
