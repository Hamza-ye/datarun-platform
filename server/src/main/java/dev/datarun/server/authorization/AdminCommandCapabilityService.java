package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.config.AdminCommandCapabilityPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminCommandCapabilityService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AdminCommandCapabilityService.class);

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public AdminCommandCapabilityService(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public List<String> updatePolicy(JsonNode policy, UUID updatedBy) {
        List<String> violations = AdminCommandCapabilityPolicy.validate(policy);
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
                AdminCommandCapabilityPolicy.CONFIG_KEY,
                policy.toString(),
                updatedBy != null ? updatedBy.toString() : null);
        return List.of();
    }

    public boolean actorGrants(UUID actorId, String command) {
        if (!AdminCommandCapabilityPolicy.SUPPORTED_COMMANDS.contains(command)) {
            throw new IllegalArgumentException("Unknown admin command: " + command);
        }
        JsonNode stored = readStoredPolicyForEvaluation(actorId, command);
        if (stored == null) {
            return false;
        }
        return AdminCommandCapabilityPolicy.actorGrants(stored, actorId, command);
    }

    public ObjectNode getValidatedPolicy() {
        JsonNode stored = readStoredPolicy();
        List<String> violations = AdminCommandCapabilityPolicy.validate(stored);
        if (!violations.isEmpty()) {
            throw new IllegalStateException("Invalid admin_command_capabilities: "
                    + String.join("; ", violations));
        }
        return (ObjectNode) stored.deepCopy();
    }

    private JsonNode readStoredPolicyForEvaluation(UUID actorId, String command) {
        JsonNode stored;
        try {
            stored = readStoredPolicy();
        } catch (IllegalStateException e) {
            LOGGER.warn("event=admin_command_policy_invalid actor_id={} command={} reason={}",
                    actorId, command, e.getMessage());
            return null;
        }
        List<String> violations = AdminCommandCapabilityPolicy.validate(stored);
        if (!violations.isEmpty()) {
            LOGGER.warn("event=admin_command_policy_invalid actor_id={} command={} reason={}",
                    actorId, command, String.join("; ", violations));
            return null;
        }
        return stored;
    }

    private JsonNode readStoredPolicy() {
        List<String> rows = jdbc.queryForList("""
                SELECT config_json::text
                FROM deployment_config
                WHERE config_key = ?
                """, String.class, AdminCommandCapabilityPolicy.CONFIG_KEY);
        if (rows.isEmpty()) {
            return AdminCommandCapabilityPolicy.emptyPolicy(objectMapper);
        }
        try {
            return objectMapper.readTree(rows.get(0));
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse stored admin_command_capabilities", e);
        }
    }
}
