package dev.datarun.ship1.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Per-shape, per-version payload validation. Shapes loaded from classpath
 * {@code schemas/shapes/*.schema.json} at boot.
 *
 * <p>File-name conventions (both supported simultaneously, Ship-3 §6.1 sub-decision 3 —
 * JAR-bundled fixture continued):
 * <ul>
 *   <li>{@code <name>.schema.json} → {@code <name>/v1} (Ship-1 single-version layout).</li>
 *   <li>{@code <name>.v<N>.schema.json} → {@code <name>/v<N>} (Ship-3 multi-version layout).</li>
 * </ul>
 *
 * <p>Validation routes on the envelope's {@code shape_ref} ({@code name/vN}). Unknown
 * {@code shape_ref} → REJECT (Ship-1 strict-unknown choice; SyncController surfaces this as
 * HTTP 400 with {@code shape_unknown}). All registered versions remain valid forever per
 * ADR-004 §S10 — once v2 lands, v1 is never dropped.
 *
 * <p>No projection cache (FP-002 (a) pattern). Per-request validation against the loaded schema.
 */
@Component
public class ShapePayloadValidator {

    private static final Pattern VERSIONED_FILE = Pattern.compile("^(.+)\\.v(\\d+)\\.schema\\.json$");
    private static final Pattern PLAIN_FILE = Pattern.compile("^(.+)\\.schema\\.json$");

    private final Map<String, JsonSchema> shapesByRef = new HashMap<>();

    @PostConstruct
    void init() throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] files = resolver.getResources("classpath:schemas/shapes/*.schema.json");
        for (Resource r : files) {
            String filename = Objects.requireNonNull(r.getFilename());
            String shapeRef = parseShapeRef(filename);
            try (InputStream in = r.getInputStream()) {
                shapesByRef.put(shapeRef, factory.getSchema(in));
            }
        }
    }

    /** Parse {@code household_observation.v2.schema.json} → {@code household_observation/v2};
     *  {@code assignment_created.schema.json} → {@code assignment_created/v1}. */
    static String parseShapeRef(String filename) {
        Matcher v = VERSIONED_FILE.matcher(filename);
        if (v.matches()) {
            return v.group(1) + "/v" + v.group(2);
        }
        Matcher p = PLAIN_FILE.matcher(filename);
        if (p.matches()) {
            return p.group(1) + "/v1";
        }
        throw new IllegalArgumentException("unrecognised shape filename: " + filename);
    }

    /** Empty list = valid. Single-element list with the {@code shape_unknown:} marker = unknown
     *  shape_ref. SyncController surfaces this as HTTP 400 with the marker visible in the response
     *  body so callers can discriminate unknown-shape from payload-validation failures. */
    public List<String> validate(String shapeRef, JsonNode payload) {
        JsonSchema schema = shapesByRef.get(shapeRef);
        if (schema == null) {
            return List.of("shape_unknown: " + shapeRef);
        }
        Set<ValidationMessage> errors = schema.validate(payload);
        List<String> out = new ArrayList<>(errors.size());
        for (ValidationMessage m : errors) out.add(m.getMessage());
        return out;
    }

    public boolean isKnown(String shapeRef) {
        return shapesByRef.containsKey(shapeRef);
    }

    public Set<String> knownShapeRefs() {
        return Collections.unmodifiableSet(shapesByRef.keySet());
    }
}
