package dev.datarun.ship1.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

/**
 * Per-shape payload validation. Shapes loaded from classpath {@code schemas/shapes/*.json} at boot.
 * The shape key is derived from each file's {@code $id} tail ({@code <name>.schema.json} →
 * {@code <name>/v1}). Shape name on disk must match the {@code shape_ref} prefix in envelopes.
 *
 * Ship-1 scope: one deployer shape ({@code household_observation/v1}) plus the four platform-bundled
 * integrity/assignment shapes. Unknown shape_refs are REJECTED (ADR-001 §S5: payload is validated
 * against shape_ref). Ship-1 chooses strict-reject over accept-unknown.
 */
@Component
public class ShapePayloadValidator {

    private final Map<String, JsonSchema> shapesByRef = new HashMap<>();

    @PostConstruct
    void init() throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] files = resolver.getResources("classpath:schemas/shapes/*.schema.json");
        for (Resource r : files) {
            String filename = Objects.requireNonNull(r.getFilename());
            // household_observation.schema.json -> household_observation/v1
            String shapeName = filename.replace(".schema.json", "");
            String shapeRef = shapeName + "/v1";
            try (InputStream in = r.getInputStream()) {
                shapesByRef.put(shapeRef, factory.getSchema(in));
            }
        }
    }

    /** Empty list = valid. Null schema = unknown shape_ref (caller decides how strict to be). */
    public List<String> validate(String shapeRef, JsonNode payload) {
        JsonSchema schema = shapesByRef.get(shapeRef);
        if (schema == null) {
            return List.of("unknown shape_ref: " + shapeRef);
        }
        Set<ValidationMessage> errors = schema.validate(payload);
        List<String> out = new ArrayList<>(errors.size());
        for (ValidationMessage m : errors) out.add(m.getMessage());
        return out;
    }

    public Set<String> knownShapeRefs() {
        return Collections.unmodifiableSet(shapesByRef.keySet());
    }
}
