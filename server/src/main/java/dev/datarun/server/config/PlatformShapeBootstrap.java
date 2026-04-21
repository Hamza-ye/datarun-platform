package dev.datarun.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registers the 4 platform-bundled internal shapes on server start.
 *
 * <p>These shapes govern integrity and identity events emitted by the server itself
 * ({@code conflict_detected/v1}, {@code conflict_resolved/v1}, {@code subjects_merged/v1},
 * {@code subject_split/v1}). Registration is idempotent: a shape already present at the
 * same version is left alone.
 *
 * <p>Canonical JSON Schema definitions live under {@code contracts/shapes/*.schema.json}
 * for cross-platform reference; the in-memory {@code fields} representation below is the
 * server-side mirror used by {@link ShapePayloadValidator}. Deliberate minimal fields — the
 * shapes are emitted by server code and we validate payloads at the emission site, not via
 * deployer-facing DtV. A payload validator mismatch must never reject a server-emitted flag,
 * so fields are permissive (required → only the two or three keys that are always present).
 */
@Component
public class PlatformShapeBootstrap {

    private static final Logger log = LoggerFactory.getLogger(PlatformShapeBootstrap.class);
    static final String PLATFORM_SENSITIVITY = "standard";

    private final ShapeService shapeService;
    private final ShapeRepository shapeRepository;
    private final ObjectMapper objectMapper;

    public PlatformShapeBootstrap(ShapeService shapeService,
                                  ShapeRepository shapeRepository,
                                  ObjectMapper objectMapper) {
        this.shapeService = shapeService;
        this.shapeRepository = shapeRepository;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerPlatformShapes() {
        register("conflict_detected", buildConflictDetectedSchema());
        register("conflict_resolved", buildConflictResolvedSchema());
        register("subjects_merged", buildSubjectsMergedSchema());
        register("subject_split", buildSubjectSplitSchema());
    }

    private void register(String name, ObjectNode schemaJson) {
        if (shapeRepository.exists(name, 1)) {
            return;
        }
        List<String> violations = shapeService.createShape(name, PLATFORM_SENSITIVITY, schemaJson);
        if (!violations.isEmpty()) {
            log.warn("Platform shape {} failed to register: {}", name, violations);
        } else {
            log.info("Registered platform shape {}/v1", name);
        }
    }

    private ObjectNode buildConflictDetectedSchema() {
        ArrayNode fields = objectMapper.createArrayNode();
        fields.add(field("source_event_id", "text", true));
        fields.add(field("flag_category", "text", true));
        fields.add(field("resolvability", "text", false));
        fields.add(field("reason", "narrative", false));
        ObjectNode schema = objectMapper.createObjectNode();
        schema.set("fields", fields);
        return schema;
    }

    private ObjectNode buildConflictResolvedSchema() {
        ArrayNode fields = objectMapper.createArrayNode();
        fields.add(field("flag_event_id", "text", true));
        fields.add(field("source_event_id", "text", true));
        fields.add(field("resolution", "text", true));
        fields.add(field("reclassified_subject_id", "text", false));
        fields.add(field("reason", "narrative", false));
        ObjectNode schema = objectMapper.createObjectNode();
        schema.set("fields", fields);
        return schema;
    }

    private ObjectNode buildSubjectsMergedSchema() {
        ArrayNode fields = objectMapper.createArrayNode();
        fields.add(field("surviving_id", "text", true));
        fields.add(field("retired_id", "text", true));
        fields.add(field("reason", "narrative", false));
        ObjectNode schema = objectMapper.createObjectNode();
        schema.set("fields", fields);
        return schema;
    }

    private ObjectNode buildSubjectSplitSchema() {
        ArrayNode fields = objectMapper.createArrayNode();
        fields.add(field("source_id", "text", true));
        fields.add(field("successor_id", "text", true));
        fields.add(field("reason", "narrative", false));
        ObjectNode schema = objectMapper.createObjectNode();
        schema.set("fields", fields);
        return schema;
    }

    private ObjectNode field(String name, String type, boolean required) {
        ObjectNode field = objectMapper.createObjectNode();
        field.put("name", name);
        field.put("type", type);
        field.put("required", required);
        return field;
    }
}
