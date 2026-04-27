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
 * <h3>Supported filename patterns (exhaustive)</h3>
 *
 * Both patterns below are supported simultaneously (Ship-3 §6.1 sub-decision 3 — JAR-bundled
 * fixture continued). Examples below cite the exact files present in
 * {@code server/src/main/resources/schemas/shapes/} at Ship-3 close (byte-identical to
 * {@code contracts/shapes/} per FP-007):
 * <ul>
 *   <li>{@code <name>.schema.json} → {@code <name>/v1} (Ship-1 single-version layout).
 *       Examples: {@code assignment_created.schema.json}, {@code assignment_ended.schema.json},
 *       {@code conflict_detected.schema.json}, {@code conflict_resolved.schema.json},
 *       {@code subject_split.schema.json}, {@code subjects_merged.schema.json}.</li>
 *   <li>{@code <name>.v<N>.schema.json} → {@code <name>/v<N>} (Ship-3 multi-version layout).
 *       Examples: {@code household_observation.v1.schema.json},
 *       {@code household_observation.v2.schema.json}.</li>
 * </ul>
 *
 * <p>A filename in {@code schemas/shapes/} that matches <em>neither</em> pattern causes
 * {@link #parseShapeRef(String)} to throw at boot (registry-load fails fast — the schema
 * never enters the live registry). A {@code shape_ref} on an inbound envelope whose
 * resolved name has no entry in the registry is rejected as <em>unknown shape_ref</em>:
 * {@link #validate(String, JsonNode)} returns the {@code shape_unknown:} marker, which
 * {@code SyncController} surfaces as HTTP 400 with {@code error: validation_failed}.
 *
 * <p><strong>Maintenance note:</strong> to add a new filename pattern, update both this
 * class's compiled patterns ({@link #VERSIONED_FILE}, {@link #PLAIN_FILE}, and
 * {@link #parseShapeRef(String)}) <em>and</em> this Javadoc. Adding a pattern in only one
 * place is the documented failure mode this Javadoc exists to prevent (G-10 / C3-05).
 *
 * <p>Validation routes on the envelope's {@code shape_ref} ({@code name/vN}). All registered
 * versions remain valid forever per ADR-004 §S10 — once v2 lands, v1 is never dropped.
 *
 * <p>No projection cache (FP-002 (a) pattern). Per-request validation against the loaded schema.
 *
 * <h3>ADR-004 §S13 budget enforcement (G-9)</h3>
 *
 * The 60-field per-shape hard limit (ADR-004 §S13 row 1, &quot;Fields per shape&quot;) is enforced by
 * {@link #validateShapeBudget(JsonNode)}. The guard runs at JAR-bundle load — the
 * {@link #init() @PostConstruct} sweep iterates every classpath shape resource and calls the
 * guard before the shape is registered — and the same method is callable from any future code
 * path that needs to validate a candidate shape schema.
 *
 * <p>The remaining §S13 budgets (predicates-per-condition = 3, triggers-per-event-type = 5,
 * triggers-per-deployment = 50, escalation depth = 2) describe expression-language and
 * trigger-graph dimensions that have no Ship-3 surface; they land with the deployer-authoring
 * surface (FP-012 / FP-012b) and are out of scope for this guard.
 *
 * <p><strong>Runtime HTTP enforcement of the shape-registration path lands with the
 * deployer-authoring surface (FP-012b). This guard is callable code; until then, enforcement
 * is at JAR-bundle load (startup) and unit-test verification.</strong>
 */
@Component
public class ShapePayloadValidator {

    /** ADR-004 §S13 row 1: hard limit, 60 fields per shape, enforced at registry-load. */
    public static final int MAX_FIELDS_PER_SHAPE = 60;

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
                JsonSchema schema = factory.getSchema(in);
                enforceFieldCountBudget(shapeRef, schema);
                shapesByRef.put(shapeRef, schema);
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

    /**
     * ADR-004 §S13 row-1 guard — count top-level {@code properties} keys on a candidate shape
     * schema; throw {@link IllegalStateException} if the count exceeds {@link #MAX_FIELDS_PER_SHAPE}.
     *
     * <p>Top-level-properties is the field-count interpretation chosen for Ship-3 (nested objects
     * are <em>not</em> flattened); this matches §S13's wording (&quot;Fields per shape&quot;) and
     * is recorded in the Ship-3 build report so the choice can be re-confirmed at retro before
     * further evolution.
     *
     * <p>This is the public, callable form of the §S13 row-1 guard. It is invoked from
     * {@link #init()} on every JAR-bundled shape at boot; any future shape-registration code path
     * (e.g. a {@code POST /api/shapes} endpoint that lands with FP-012b) <em>must</em> call this
     * method before persisting a candidate schema. Until that path exists, callers are limited to
     * the boot-time sweep and unit-test verification.
     *
     * @param shapeSchema the JSON Schema document for a candidate shape (the &quot;schema node&quot;
     *                    — i.e. the JSON object whose root contains {@code "properties": {...}}).
     *                    A non-object {@code shapeSchema} or one without a {@code properties}
     *                    object is treated as zero fields and accepted; structural validity is
     *                    enforced upstream by JSON-Schema parsing.
     * @throws IllegalStateException if the shape declares more than {@link #MAX_FIELDS_PER_SHAPE}
     *                               top-level {@code properties} entries.
     */
    public static void validateShapeBudget(JsonNode shapeSchema) {
        validateShapeBudget("<unnamed>", shapeSchema);
    }

    /** Internal overload that includes the {@code shape_ref} in the failure message — used by
     *  {@link #init()} so the offender is identifiable when registry-load fails. */
    static void validateShapeBudget(String shapeRef, JsonNode shapeSchema) {
        JsonNode props = shapeSchema == null ? null : shapeSchema.path("properties");
        int count = (props != null && props.isObject()) ? props.size() : 0;
        if (count > MAX_FIELDS_PER_SHAPE) {
            throw new IllegalStateException(
                    "shape " + shapeRef + " declares " + count + " top-level properties; " +
                            "ADR-004 §S13 hard limit is " + MAX_FIELDS_PER_SHAPE);
        }
    }

    /** Back-compatible static seam used by Ship-3 unit tests — delegates to
     *  {@link #validateShapeBudget(String, JsonNode)} on the schema's root node. */
    static void enforceFieldCountBudget(String shapeRef, JsonSchema schema) {
        validateShapeBudget(shapeRef, schema.getSchemaNode());
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
