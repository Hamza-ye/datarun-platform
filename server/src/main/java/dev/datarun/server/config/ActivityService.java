package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Activity lifecycle management: create, update, deprecate.
 * DtV checks: activity name format, shape references must exist.
 */
@Service
public class ActivityService {

    private static final java.util.regex.Pattern NAME_PATTERN =
            java.util.regex.Pattern.compile("^[a-z][a-z0-9_]*$");

    private final ActivityRepository activityRepository;
    private final ShapeRepository shapeRepository;
    private final ObjectMapper objectMapper;

    public ActivityService(ActivityRepository activityRepository,
                           ShapeRepository shapeRepository,
                           ObjectMapper objectMapper) {
        this.activityRepository = activityRepository;
        this.shapeRepository = shapeRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Validate an activity's config_json. Returns violations (empty = valid).
     */
    public List<String> validateActivity(String name, JsonNode configJson) {
        List<String> violations = new ArrayList<>();

        if (!NAME_PATTERN.matcher(name).matches()) {
            violations.add("Invalid activity name '" + name + "': must match [a-z][a-z0-9_]*");
        }

        // Shapes array required
        JsonNode shapesNode = configJson.get("shapes");
        if (shapesNode == null || !shapesNode.isArray() || shapesNode.isEmpty()) {
            violations.add("Activity must have a non-empty 'shapes' array");
            return violations;
        }

        // Validate each shape reference exists
        for (int i = 0; i < shapesNode.size(); i++) {
            String shapeRef = shapesNode.get(i).asText("");
            String[] parsed = ShapeService.parseShapeRef(shapeRef);
            if (parsed == null) {
                violations.add("Invalid shape_ref format: '" + shapeRef + "'");
            } else if (!shapeRepository.exists(parsed[0], Integer.parseInt(parsed[1]))) {
                violations.add("Shape '" + shapeRef + "' does not exist");
            }
        }

        // Roles must be an object with string arrays
        JsonNode rolesNode = configJson.get("roles");
        if (rolesNode == null || !rolesNode.isObject()) {
            violations.add("Activity must have a 'roles' object");
        }

        return violations;
    }

    public List<String> createActivity(String name, String sensitivity, JsonNode configJson) {
        List<String> violations = validateActivity(name, configJson);
        if (!violations.isEmpty()) return violations;

        if (activityRepository.exists(name)) {
            return List.of("Activity '" + name + "' already exists");
        }

        Activity activity = new Activity(name, configJson, "active", sensitivity, null);
        activityRepository.insert(activity);
        return List.of();
    }

    public List<String> updateActivity(String name, String sensitivity, JsonNode configJson) {
        List<String> violations = validateActivity(name, configJson);
        if (!violations.isEmpty()) return violations;

        if (!activityRepository.exists(name)) {
            return List.of("Activity '" + name + "' does not exist");
        }

        Activity activity = new Activity(name, configJson, "active", sensitivity, null);
        activityRepository.update(activity);
        return List.of();
    }

    public void deprecate(String name) {
        if (!activityRepository.exists(name)) {
            throw new IllegalArgumentException("Activity '" + name + "' not found");
        }
        activityRepository.updateStatus(name, "deprecated");
    }

    public Optional<Activity> getActivity(String name) {
        return activityRepository.findByName(name);
    }

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public List<Activity> getActiveActivities() {
        return activityRepository.findActive();
    }
}
