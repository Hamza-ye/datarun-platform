package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record ResolverRef(String type, String id) {

    public static ResolverRef actor(String id) {
        return new ResolverRef("actor", id);
    }

    public void writeTo(ObjectNode node) {
        node.put("type", type);
        node.put("id", id);
    }

    public boolean matchesActorRef(JsonNode actorRef) {
        return actorRef != null
                && type.equals(actorRef.path("type").asText(null))
                && id.equals(actorRef.path("id").asText(null));
    }

    public static ResolverRef fromJson(JsonNode resolver) {
        if (resolver == null || !resolver.isObject()) {
            return null;
        }
        String type = resolver.path("type").asText(null);
        String id = resolver.path("id").asText(null);
        if (type == null || type.isBlank() || id == null || id.isBlank()) {
            return null;
        }
        return new ResolverRef(type, id);
    }
}
