package dev.datarun.server.authorization;

import java.time.Instant;
import java.util.UUID;

public record WebAdminSessionAuditEvent(
        String eventType,
        UUID actorId,
        String issuer,
        String subject,
        String sessionCorrelationId,
        String reason,
        Instant occurredAt) {}
