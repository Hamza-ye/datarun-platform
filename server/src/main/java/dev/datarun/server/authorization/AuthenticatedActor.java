package dev.datarun.server.authorization;

import java.util.UUID;

public record AuthenticatedActor(
        UUID actorId,
        String source,
        String issuer,
        String subject
) {}
