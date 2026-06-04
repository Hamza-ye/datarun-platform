package dev.datarun.server.authorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Runtime authentication mode selection.
 *
 * dev-token preserves the Phase 2 actor token path. jwt resolves a validated
 * JWT principal through auth_principal_bindings before exposing actor_id.
 */
@Component
public class AuthProperties {

    private final String mode;
    private final String jwtIssuer;
    private final String jwtAudience;
    private final String jwtHmacSecret;
    private final boolean allowUnauthenticatedDevPush;

    public AuthProperties(
            @Value("${datarun.auth.mode:dev-token}") String mode,
            @Value("${datarun.auth.jwt.issuer:}") String jwtIssuer,
            @Value("${datarun.auth.jwt.audience:}") String jwtAudience,
            @Value("${datarun.auth.jwt.hmac-secret:}") String jwtHmacSecret,
            @Value("${datarun.auth.dev-token.allow-unauthenticated-push:true}")
            boolean allowUnauthenticatedDevPush) {
        this.mode = normalize(mode);
        this.jwtIssuer = blankToNull(jwtIssuer);
        this.jwtAudience = blankToNull(jwtAudience);
        this.jwtHmacSecret = blankToNull(jwtHmacSecret);
        this.allowUnauthenticatedDevPush = allowUnauthenticatedDevPush;
    }

    public String mode() {
        return mode;
    }

    public boolean isDevTokenMode() {
        return "dev-token".equals(mode);
    }

    public boolean isJwtMode() {
        return "jwt".equals(mode);
    }

    public boolean allowUnauthenticatedDevPush() {
        return isDevTokenMode() && allowUnauthenticatedDevPush;
    }

    public String jwtIssuer() {
        return jwtIssuer;
    }

    public String jwtAudience() {
        return jwtAudience;
    }

    public String jwtHmacSecret() {
        return jwtHmacSecret;
    }

    private String normalize(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase();
        return normalized.isBlank() ? "dev-token" : normalized;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
