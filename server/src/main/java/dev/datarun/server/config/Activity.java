package dev.datarun.server.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

/**
 * An activity definition: organizational grouping of shapes with role-action mappings. (IDR-017)
 * NOT individually versioned — config-package-level versioning.
 */
public record Activity(
        String name,
        @JsonProperty("config_json") JsonNode configJson,
        String status,
        String sensitivity,
        @JsonProperty("created_at") OffsetDateTime createdAt
) {
}
