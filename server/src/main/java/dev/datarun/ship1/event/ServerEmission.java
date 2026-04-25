package dev.datarun.ship1.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Server-authored envelope fields shared by every component that emits a server-authored event
 * (Ship-1: ConflictDetector. Ship-2: AdminSubjectsController for merge/split per §6 commitment 7).
 *
 * <p>Single reserved server device UUID — never a second one. {@code device_seq} is drawn from the
 * Postgres sequence {@code server_device_seq} (durable across restart per ADR-002 §S4).
 */
@Component
public class ServerEmission {

    /** The one and only server-as-device UUID. Reserved at Ship-1; reused for all server-authored events. */
    public static final UUID SERVER_DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final JdbcTemplate jdbc;

    public ServerEmission(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public UUID serverDeviceId() {
        return SERVER_DEVICE_ID;
    }

    public synchronized long nextServerDeviceSeq() {
        Long v = jdbc.queryForObject("SELECT nextval('server_device_seq')", Long.class);
        return v == null ? 1L : v;
    }
}
