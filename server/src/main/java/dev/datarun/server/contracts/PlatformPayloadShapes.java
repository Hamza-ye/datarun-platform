package dev.datarun.server.contracts;

import java.util.Map;
import java.util.Set;

/**
 * Platform-owned event payload shapes backed by contracts/shapes/*.schema.json.
 */
public final class PlatformPayloadShapes {

    public static final Map<String, String> RESOURCE_BY_SHAPE_REF = Map.of(
            "assignment_created/v1", "shapes/assignment_created.schema.json",
            "assignment_ended/v1", "shapes/assignment_ended.schema.json",
            "conflict_detected/v1", "shapes/conflict_detected.schema.json",
            "conflict_resolved/v1", "shapes/conflict_resolved.schema.json",
            "subjects_merged/v1", "shapes/subjects_merged.schema.json",
            "subject_split/v1", "shapes/subject_split.schema.json"
    );

    public static final Set<String> SHAPE_REFS = RESOURCE_BY_SHAPE_REF.keySet();
    public static final Set<String> SHAPE_NAMES = Set.of(
            "assignment_created",
            "assignment_ended",
            "conflict_detected",
            "conflict_resolved",
            "subjects_merged",
            "subject_split"
    );

    private PlatformPayloadShapes() {
    }

    public static boolean isPlatformShapeName(String name) {
        return name != null && SHAPE_NAMES.contains(name);
    }

    public static boolean isPlatformShapeRef(String shapeRef) {
        if (shapeRef == null) {
            return false;
        }
        int separator = shapeRef.lastIndexOf("/v");
        if (separator <= 0) {
            return false;
        }
        return isPlatformShapeName(shapeRef.substring(0, separator));
    }
}
