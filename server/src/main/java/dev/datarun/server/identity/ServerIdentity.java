package dev.datarun.server.identity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Provides stable server identity for server-generated events.
 * device_id: env var SERVER_DEVICE_ID primary, DB-stored fallback (auto-generated on first boot).
 * device_seq: PostgreSQL SEQUENCE (server_device_seq) — monotonic, gap-tolerant.
 */
@Component
public class ServerIdentity {

    private final UUID deviceId;
    private final JdbcTemplate jdbc;

    public ServerIdentity(JdbcTemplate jdbc,
                          @Value("${SERVER_DEVICE_ID:}") String envDeviceId) {
        this.jdbc = jdbc;
        this.deviceId = resolveDeviceId(envDeviceId);
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public long nextDeviceSeq() {
        return jdbc.queryForObject("SELECT nextval('server_device_seq')", Long.class);
    }

    private UUID resolveDeviceId(String envDeviceId) {
        // 1. Env var takes priority
        if (envDeviceId != null && !envDeviceId.isBlank()) {
            UUID id = UUID.fromString(envDeviceId);
            // Ensure it's in the DB too (idempotent)
            jdbc.update("INSERT INTO server_identity (device_id) VALUES (?::uuid) ON CONFLICT DO NOTHING",
                    id.toString());
            return id;
        }

        // 2. DB fallback — check for existing row
        List<UUID> existing = jdbc.query(
                "SELECT device_id FROM server_identity LIMIT 1",
                (rs, rowNum) -> UUID.fromString(rs.getString("device_id")));
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        // 3. First boot — generate and store
        UUID generated = UUID.randomUUID();
        jdbc.update("INSERT INTO server_identity (device_id) VALUES (?::uuid)", generated.toString());
        return generated;
    }
}
