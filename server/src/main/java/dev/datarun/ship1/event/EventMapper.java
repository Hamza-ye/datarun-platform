package dev.datarun.ship1.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maps between the wire envelope (JSON per contracts/envelope.schema.json) and the Java
 * {@link Event} record. The wire form nests subject_ref and actor_ref; the record flattens them.
 */
public final class EventMapper {

    private EventMapper() {}

    public static Event fromEnvelope(JsonNode env, ObjectMapper mapper) {
        JsonNode subjectRef = env.path("subject_ref");
        JsonNode actorRef = env.path("actor_ref");
        Long watermark = env.path("sync_watermark").isIntegralNumber()
                ? env.path("sync_watermark").asLong() : null;
        return new Event(
                UUID.fromString(env.path("id").asText()),
                env.path("type").asText(),
                env.path("shape_ref").asText(),
                env.path("activity_ref").isNull() ? null : env.path("activity_ref").asText(null),
                subjectRef.path("type").asText(),
                UUID.fromString(subjectRef.path("id").asText()),
                actorRef.path("id").asText(),
                UUID.fromString(env.path("device_id").asText()),
                env.path("device_seq").asLong(),
                watermark,
                OffsetDateTime.parse(env.path("timestamp").asText()),
                env.path("payload"));
    }

    public static com.fasterxml.jackson.databind.node.ObjectNode toEnvelope(Event e, ObjectMapper mapper) {
        var node = mapper.createObjectNode();
        node.put("id", e.id().toString());
        node.put("type", e.type());
        node.put("shape_ref", e.shapeRef());
        if (e.activityRef() != null) node.put("activity_ref", e.activityRef());
        else node.putNull("activity_ref");
        var sref = node.putObject("subject_ref");
        sref.put("type", e.subjectType());
        sref.put("id", e.subjectId().toString());
        var aref = node.putObject("actor_ref");
        aref.put("type", "actor");
        aref.put("id", e.actorId());
        node.put("device_id", e.deviceId().toString());
        node.put("device_seq", e.deviceSeq());
        if (e.syncWatermark() != null) node.put("sync_watermark", e.syncWatermark());
        else node.putNull("sync_watermark");
        node.put("timestamp", e.timestamp().toString());
        node.set("payload", e.payload());
        return node;
    }
}
