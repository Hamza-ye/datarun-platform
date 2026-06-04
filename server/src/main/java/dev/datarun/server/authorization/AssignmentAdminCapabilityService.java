package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.config.AssignmentAdminCapabilityPolicy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AssignmentAdminCapabilityService {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public AssignmentAdminCapabilityService(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public List<String> updatePolicy(JsonNode policy, UUID updatedBy) {
        List<String> violations = AssignmentAdminCapabilityPolicy.validate(policy);
        if (!violations.isEmpty()) {
            return violations;
        }

        jdbc.update("""
                INSERT INTO deployment_config (config_key, config_json, updated_by, updated_at)
                VALUES (?, ?::jsonb, ?::uuid, NOW())
                ON CONFLICT (config_key) DO UPDATE
                SET config_json = EXCLUDED.config_json,
                    updated_by = EXCLUDED.updated_by,
                    updated_at = NOW()
                """,
                AssignmentAdminCapabilityPolicy.CONFIG_KEY,
                policy.toString(),
                updatedBy != null ? updatedBy.toString() : null);
        return List.of();
    }

    public boolean roleGrants(String role, String command) {
        if (!AssignmentAdminCapabilityPolicy.SUPPORTED_COMMANDS.contains(command)) {
            throw new IllegalArgumentException("Unknown assignment-admin command: " + command);
        }
        ObjectNode policy = getValidatedPolicy();
        return AssignmentAdminCapabilityPolicy.roleGrants(policy, role, command);
    }

    public ObjectNode getValidatedPolicy() {
        JsonNode stored = readStoredPolicy();
        List<String> violations = AssignmentAdminCapabilityPolicy.validate(stored);
        if (!violations.isEmpty()) {
            throw new IllegalStateException("Invalid assignment_admin_capabilities: "
                    + String.join("; ", violations));
        }
        return (ObjectNode) stored.deepCopy();
    }

    private JsonNode readStoredPolicy() {
        List<String> rows = jdbc.queryForList("""
                SELECT config_json::text
                FROM deployment_config
                WHERE config_key = ?
                """, String.class, AssignmentAdminCapabilityPolicy.CONFIG_KEY);
        if (rows.isEmpty()) {
            return AssignmentAdminCapabilityPolicy.emptyPolicy(objectMapper);
        }
        try {
            return objectMapper.readTree(rows.get(0));
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse stored assignment_admin_capabilities", e);
        }
    }
}
