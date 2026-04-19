package dev.datarun.server.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

/**
 * A shape definition: versioned, immutable snapshot. (IDR-017)
 * Primary key: (name, version). Lookup via shape_ref "{name}/v{version}".
 */
public record Shape(
        String name,
        int version,
        String status,
        String sensitivity,
        @JsonProperty("schema_json") JsonNode schemaJson,
        @JsonProperty("created_at") OffsetDateTime createdAt
) {
    public String shapeRef() {
        return name + "/v" + version;
    }
}
