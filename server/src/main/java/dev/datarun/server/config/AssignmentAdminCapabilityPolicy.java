package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Server-side deployment policy for platform-owned assignment administration
 * commands. This is intentionally separate from activity role-action config.
 */
public final class AssignmentAdminCapabilityPolicy {

    public static final String CONFIG_KEY = "assignment_admin_capabilities";
    public static final String CREATE_COMMAND = "assignment_admin.create";
    public static final String END_COMMAND = "assignment_admin.end";
    public static final Set<String> SUPPORTED_COMMANDS = Set.of(CREATE_COMMAND, END_COMMAND);

    private AssignmentAdminCapabilityPolicy() {
    }

    public static ObjectNode emptyPolicy(ObjectMapper objectMapper) {
        ObjectNode policy = objectMapper.createObjectNode();
        policy.put("schema_version", 1);
        policy.set("roles", objectMapper.createObjectNode());
        return policy;
    }

    public static List<String> validate(JsonNode policyNode) {
        List<String> violations = new ArrayList<>();
        if (policyNode == null || !policyNode.isObject()) {
            violations.add("assignment_admin_capabilities must be an object");
            return violations;
        }

        policyNode.fieldNames().forEachRemaining(field -> {
            if (!"schema_version".equals(field) && !"roles".equals(field)) {
                violations.add("assignment_admin_capabilities key '" + field + "' is not supported");
            }
        });

        JsonNode schemaVersion = policyNode.get("schema_version");
        if (schemaVersion == null || !schemaVersion.isIntegralNumber() || schemaVersion.asInt() != 1) {
            violations.add("assignment_admin_capabilities.schema_version must be 1");
        }

        JsonNode rolesNode = policyNode.get("roles");
        if (rolesNode == null || !rolesNode.isObject()) {
            violations.add("assignment_admin_capabilities.roles must be an object");
            return violations;
        }

        rolesNode.fields().forEachRemaining(entry -> {
            String role = entry.getKey();
            JsonNode commandsNode = entry.getValue();
            if (role == null || role.isBlank()) {
                violations.add("assignment_admin_capabilities role name must be non-empty");
            }
            if (commandsNode == null || !commandsNode.isArray()) {
                violations.add("assignment_admin_capabilities role '" + role
                        + "' must map to a command array");
                return;
            }
            for (JsonNode commandNode : commandsNode) {
                if (!commandNode.isTextual() || commandNode.asText().isBlank()) {
                    violations.add("assignment_admin_capabilities role '" + role
                            + "' command must be a non-empty string");
                    continue;
                }
                String command = commandNode.asText();
                if (!SUPPORTED_COMMANDS.contains(command)) {
                    violations.add("Unknown assignment-admin command '" + command
                            + "' for role '" + role + "'");
                }
            }
        });

        return violations;
    }

    public static boolean roleGrants(JsonNode policyNode, String role, String command) {
        if (role == null || role.isBlank() || !SUPPORTED_COMMANDS.contains(command)) {
            return false;
        }
        JsonNode rolesNode = policyNode == null ? null : policyNode.get("roles");
        if (rolesNode == null || !rolesNode.isObject()) {
            return false;
        }
        JsonNode commandsNode = rolesNode.get(role);
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
