package dev.datarun.server.authorization;

public record JwtPrincipal(
        String issuer,
        String subject
) {}
