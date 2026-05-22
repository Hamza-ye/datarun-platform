package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.integrity.FlagCatalog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Deployment-wide L0 flag severity configuration.
 *
 * <p>The stored value is only the deployer override map. Platform defaults live
 * in {@link FlagCatalog} so severity can be interpreted without changing event
 * payloads or flag resolvability.
 */
@Service
public class FlagSeverityConfigService {

    static final String CONFIG_KEY = "flag_severity_overrides";

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public FlagSeverityConfigService(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public List<String> updateOverrides(JsonNode overrides, UUID updatedBy) {
        List<String> violations = DeployTimeValidator.validateFlagSeverityOverrides(overrides);
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
                CONFIG_KEY,
                overrides.toString(),
                updatedBy != null ? updatedBy.toString() : null);
        return List.of();
    }

    public ObjectNode getValidatedOverrides() {
        JsonNode stored = readStoredOverrides();
        List<String> violations = DeployTimeValidator.validateFlagSeverityOverrides(stored);
        if (!violations.isEmpty()) {
            throw new IllegalStateException("Invalid flag_severity_overrides: "
                    + String.join("; ", violations));
        }
        return (ObjectNode) stored.deepCopy();
    }

    public String effectiveSeverity(String flagCategory) {
        return FlagCatalog.effectiveSeverityFor(flagCategory, getValidatedOverrides());
    }

    private JsonNode readStoredOverrides() {
        List<String> rows = jdbc.queryForList("""
                SELECT config_json::text
                FROM deployment_config
                WHERE config_key = ?
                """, String.class, CONFIG_KEY);
        if (rows.isEmpty()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(rows.get(0));
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse stored flag_severity_overrides", e);
        }
    }
}
