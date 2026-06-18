package dev.datarun.server.authorization;

import java.time.Instant;
import java.util.UUID;

public record WebAdminSessionContext(
        UUID actorId,
        String issuer,
        String subject,
        String authSource,
        String sessionCorrelationId,
        Instant loginTime,
        Instant lastSeenTime,
        Instant expiresAt) {}
