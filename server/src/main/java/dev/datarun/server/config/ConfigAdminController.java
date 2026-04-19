package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * Admin UI for shape authoring, activity management, and config publishing.
 * Thymeleaf-rendered (IDR-006).
 */
@Controller
@RequestMapping("/admin/config")
public class ConfigAdminController {

    private final ShapeService shapeService;
    private final ActivityService activityService;
    private final ConfigPackager configPackager;
    private final ExpressionRepository expressionRepository;
    private final DeployTimeValidator deployTimeValidator;
    private final ShapeRepository shapeRepository;
    private final ObjectMapper objectMapper;

    public ConfigAdminController(ShapeService shapeService,
                                 ActivityService activityService,
                                 ConfigPackager configPackager,
                                 ExpressionRepository expressionRepository,
                                 DeployTimeValidator deployTimeValidator,
                                 ShapeRepository shapeRepository,
                                 ObjectMapper objectMapper) {
        this.shapeService = shapeService;
        this.activityService = activityService;
        this.configPackager = configPackager;
        this.expressionRepository = expressionRepository;
        this.deployTimeValidator = deployTimeValidator;
        this.shapeRepository = shapeRepository;
        this.objectMapper = objectMapper;
    }

    // --- Shapes ---

    @GetMapping("/shapes")
    public String shapeList(Model model) {
        List<Shape> shapes = shapeService.getAllShapes();
        // Group by name
        Map<String, List<Shape>> grouped = new LinkedHashMap<>();
        for (Shape s : shapes) {
            grouped.computeIfAbsent(s.name(), k -> new ArrayList<>()).add(s);
        }
        model.addAttribute("shapeGroups", grouped);
        return "config/shape-list";
    }

    @GetMapping("/shapes/create")
    public String shapeCreateForm(Model model) {
        model.addAttribute("validTypes", ShapeService.VALID_TYPES_LIST);
        return "config/shape-create";
    }

    @PostMapping("/shapes/create")
    public String createShape(@RequestParam String name,
                              @RequestParam(defaultValue = "standard") String sensitivity,
                              @RequestParam String schemaJsonStr,
                              RedirectAttributes redirectAttributes) {
        try {
            JsonNode schemaJson = objectMapper.readTree(schemaJsonStr);
            List<String> violations = shapeService.createShape(name, sensitivity, schemaJson);
            if (!violations.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", String.join("; ", violations));
                return "redirect:/admin/config/shapes/create";
            }
            redirectAttributes.addFlashAttribute("success", "Shape '" + name + "/v1' created");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid JSON: " + e.getMessage());
            return "redirect:/admin/config/shapes/create";
        }
        return "redirect:/admin/config/shapes";
    }

    @GetMapping("/shapes/{name}")
    public String shapeDetail(@PathVariable String name, Model model) {
        List<Shape> versions = shapeService.getShapeVersions(name);
        if (versions.isEmpty()) return "redirect:/admin/config/shapes";
        model.addAttribute("shapeName", name);
        model.addAttribute("versions", versions);
        model.addAttribute("validTypes", ShapeService.VALID_TYPES_LIST);
        return "config/shape-detail";
    }

    @PostMapping("/shapes/{name}/new-version")
    public String createNewVersion(@PathVariable String name,
                                   @RequestParam(defaultValue = "standard") String sensitivity,
                                   @RequestParam String schemaJsonStr,
                                   RedirectAttributes redirectAttributes) {
        try {
            JsonNode schemaJson = objectMapper.readTree(schemaJsonStr);
            List<String> violations = shapeService.createVersion(name, sensitivity, schemaJson);
            if (!violations.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", String.join("; ", violations));
                return "redirect:/admin/config/shapes/" + name;
            }
            redirectAttributes.addFlashAttribute("success", "New version created");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid JSON: " + e.getMessage());
        }
        return "redirect:/admin/config/shapes/" + name;
    }

    @PostMapping("/shapes/{name}/v{version}/deprecate")
    public String deprecateShape(@PathVariable String name,
                                 @PathVariable int version,
                                 RedirectAttributes redirectAttributes) {
        try {
            shapeService.deprecate(name, version);
            redirectAttributes.addFlashAttribute("success",
                    "Shape '" + name + "/v" + version + "' deprecated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/config/shapes/" + name;
    }

    // --- Activities ---

    @GetMapping("/activities")
    public String activityList(Model model) {
        model.addAttribute("activities", activityService.getAllActivities());
        return "config/activity-list";
    }

    @GetMapping("/activities/create")
    public String activityCreateForm(Model model) {
        model.addAttribute("shapes", shapeService.getAllShapes());
        return "config/activity-create";
    }

    @PostMapping("/activities/create")
    public String createActivity(@RequestParam String name,
                                 @RequestParam(defaultValue = "standard") String sensitivity,
                                 @RequestParam String configJsonStr,
                                 RedirectAttributes redirectAttributes) {
        try {
            JsonNode configJson = objectMapper.readTree(configJsonStr);
            List<String> violations = activityService.createActivity(name, sensitivity, configJson);
            if (!violations.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", String.join("; ", violations));
                return "redirect:/admin/config/activities/create";
            }
            redirectAttributes.addFlashAttribute("success", "Activity '" + name + "' created");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid JSON: " + e.getMessage());
            return "redirect:/admin/config/activities/create";
        }
        return "redirect:/admin/config/activities";
    }

    @PostMapping("/activities/{name}/update")
    public String updateActivity(@PathVariable String name,
                                 @RequestParam(defaultValue = "standard") String sensitivity,
                                 @RequestParam String configJsonStr,
                                 RedirectAttributes redirectAttributes) {
        try {
            JsonNode configJson = objectMapper.readTree(configJsonStr);
            List<String> violations = activityService.updateActivity(name, sensitivity, configJson);
            if (!violations.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", String.join("; ", violations));
                return "redirect:/admin/config/activities";
            }
            redirectAttributes.addFlashAttribute("success", "Activity '" + name + "' updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid JSON: " + e.getMessage());
        }
        return "redirect:/admin/config/activities";
    }

    @PostMapping("/activities/{name}/deprecate")
    public String deprecateActivity(@PathVariable String name,
                                    RedirectAttributes redirectAttributes) {
        try {
            activityService.deprecate(name);
            redirectAttributes.addFlashAttribute("success", "Activity '" + name + "' deprecated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/config/activities";
    }

    // --- Expressions ---

    @GetMapping("/expressions")
    public String expressionList(Model model) {
        model.addAttribute("rules", expressionRepository.findAll());
        return "config/expression-list";
    }

    @GetMapping("/expressions/create")
    public String expressionCreateForm(Model model) {
        model.addAttribute("activities", activityService.getAllActivities());
        model.addAttribute("shapes", shapeService.getAllShapes());
        return "config/expression-create";
    }

    @PostMapping("/expressions/create")
    public String createExpression(@RequestParam String activityRef,
                                   @RequestParam String shapeRef,
                                   @RequestParam String fieldName,
                                   @RequestParam String ruleType,
                                   @RequestParam String expressionStr,
                                   @RequestParam(required = false) String message,
                                   RedirectAttributes redirectAttributes) {
        try {
            JsonNode expressionJson = objectMapper.readTree(expressionStr);
            UUID id = UUID.randomUUID();
            ExpressionRule rule = new ExpressionRule(
                    id, activityRef, shapeRef, fieldName, ruleType, expressionJson, message, null);

            // DtV validation
            String[] shapeParts = ShapeService.parseShapeRef(shapeRef);
            if (shapeParts == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid shape_ref format: " + shapeRef);
                return "redirect:/admin/config/expressions/create";
            }
            var shape = shapeRepository.findByNameAndVersion(shapeParts[0], Integer.parseInt(shapeParts[1]));
            if (shape.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Shape '" + shapeRef + "' not found");
                return "redirect:/admin/config/expressions/create";
            }

            List<String> violations = deployTimeValidator.validateRule(rule, shape.get());
            if (!violations.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", String.join("; ", violations));
                return "redirect:/admin/config/expressions/create";
            }

            expressionRepository.insert(rule);
            redirectAttributes.addFlashAttribute("success",
                    "Expression rule created for " + fieldName + " (" + ruleType + ")");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid JSON: " + e.getMessage());
            return "redirect:/admin/config/expressions/create";
        }
        return "redirect:/admin/config/expressions";
    }

    @PostMapping("/expressions/{id}/delete")
    public String deleteExpression(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        expressionRepository.delete(id);
        redirectAttributes.addFlashAttribute("success", "Expression rule deleted");
        return "redirect:/admin/config/expressions";
    }

    // --- Config Publishing ---

    @GetMapping("/publish")
    public String publishPage(Model model) {
        model.addAttribute("currentVersion", configPackager.getLatestVersion());
        model.addAttribute("shapes", shapeService.getAllShapes());
        model.addAttribute("activities", activityService.getAllActivities());
        return "config/publish";
    }

    @PostMapping("/publish")
    public String publishConfig(RedirectAttributes redirectAttributes) {
        try {
            int version = configPackager.publish(null);
            redirectAttributes.addFlashAttribute("success",
                    "Config v" + version + " published successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Publish failed: " + e.getMessage());
        }
        return "redirect:/admin/config/publish";
    }
}
