package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side deployment policy for platform-owned web/config admin commands.
 * This actor-bound policy is intentionally separate from assignment roles.
 */
public final class AdminCommandCapabilityPolicy {

    public static final String CONFIG_KEY = "admin_command_capabilities";
    public static final String WEB_ADMIN_ACCESS = "web_admin.access";
    public static final String WEB_ADMIN_READ_SCOPED = "web_admin.read_scoped";
    public static final String CONFIG_ADMIN_AUTHOR = "config_admin.author";
    public static final String CONFIG_ADMIN_VALIDATE = "config_admin.validate";
    public static final String CONFIG_ADMIN_READINESS_REVIEW = "config_admin.readiness_review";
    public static final String CONFIG_ADMIN_APPROVE = "config_admin.approve";
    public static final String CONFIG_ADMIN_PUBLISH = "config_admin.publish";
    public static final Set<String> SUPPORTED_COMMANDS = Set.of(
            WEB_ADMIN_ACCESS,
            WEB_ADMIN_READ_SCOPED,
            CONFIG_ADMIN_AUTHOR,
            CONFIG_ADMIN_VALIDATE,
            CONFIG_ADMIN_READINESS_REVIEW,
            CONFIG_ADMIN_APPROVE,
            CONFIG_ADMIN_PUBLISH);

    private AdminCommandCapabilityPolicy() {
    }

    public static ObjectNode emptyPolicy(ObjectMapper objectMapper) {
        ObjectNode policy = objectMapper.createObjectNode();
        policy.put("schema_version", 1);
        policy.set("actors", objectMapper.createObjectNode());
        return policy;
    }

    public static List<String> validate(JsonNode policyNode) {
        List<String> violations = new ArrayList<>();
        if (policyNode == null || !policyNode.isObject()) {
            violations.add("admin_command_capabilities must be an object");
            return violations;
        }

        policyNode.fieldNames().forEachRemaining(field -> {
            if (!"schema_version".equals(field) && !"actors".equals(field)) {
                violations.add("admin_command_capabilities key '" + field + "' is not supported");
            }
        });

        JsonNode schemaVersion = policyNode.get("schema_version");
        if (schemaVersion == null || !schemaVersion.isIntegralNumber() || schemaVersion.asInt() != 1) {
            violations.add("admin_command_capabilities.schema_version must be 1");
        }

        JsonNode actorsNode = policyNode.get("actors");
        if (actorsNode == null || !actorsNode.isObject()) {
            violations.add("admin_command_capabilities.actors must be an object");
            return violations;
        }

        actorsNode.fields().forEachRemaining(entry -> {
            String actorId = entry.getKey();
            JsonNode commandsNode = entry.getValue();
            if (actorId == null || actorId.isBlank()) {
                violations.add("admin_command_capabilities actor id must be non-empty");
            } else {
                try {
                    UUID.fromString(actorId);
                } catch (IllegalArgumentException e) {
                    violations.add("admin_command_capabilities actor id '" + actorId
                            + "' must be a UUID");
                }
            }
            if (commandsNode == null || !commandsNode.isArray()) {
                violations.add("admin_command_capabilities actor '" + actorId
                        + "' must map to a command array");
                return;
            }
            for (JsonNode commandNode : commandsNode) {
                if (!commandNode.isTextual() || commandNode.asText().isBlank()) {
                    violations.add("admin_command_capabilities actor '" + actorId
                            + "' command must be a non-empty string");
                    continue;
                }
                String command = commandNode.asText();
                if (!SUPPORTED_COMMANDS.contains(command)) {
                    violations.add("Unknown admin command '" + command
                            + "' for actor '" + actorId + "'");
                }
            }
        });

        return violations;
    }

    public static boolean actorGrants(JsonNode policyNode, UUID actorId, String command) {
        if (actorId == null || !SUPPORTED_COMMANDS.contains(command)) {
            return false;
        }
        JsonNode actorsNode = policyNode == null ? null : policyNode.get("actors");
        if (actorsNode == null || !actorsNode.isObject()) {
            return false;
        }
        JsonNode commandsNode = actorsNode.get(actorId.toString());
        if (commandsNode == null || !commandsNode.isArray()) {
            return false;
        }
        for (JsonNode commandNode : commandsNode) {
            if (commandNode.isTextual() && command.equals(commandNode.asText())) {
                return true;
            }
        }
        return false;
    }
}
