package dev.datarun.server.authorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Runtime authentication mode selection.
 *
 * dev-token preserves the Phase 2 actor token path. jwt resolves a validated
 * local HS256 JWT principal through auth_principal_bindings before exposing
 * actor_id. oidc-jwks resolves an asymmetric provider JWT the same way.
 */
@Component
public class AuthProperties {

    private final String mode;
    private final String jwtIssuer;
    private final String jwtAudience;
    private final String jwtHmacSecret;
    private final String oidcIssuer;
    private final String oidcAudience;
    private final String oidcJwksUri;
    private final boolean allowUnauthenticatedDevPush;

    public AuthProperties(
            @Value("${datarun.auth.mode:dev-token}") String mode,
            @Value("${datarun.auth.jwt.issuer:}") String jwtIssuer,
            @Value("${datarun.auth.jwt.audience:}") String jwtAudience,
            @Value("${datarun.auth.jwt.hmac-secret:}") String jwtHmacSecret,
            @Value("${datarun.auth.oidc.issuer:}") String oidcIssuer,
            @Value("${datarun.auth.oidc.audience:}") String oidcAudience,
            @Value("${datarun.auth.oidc.jwks-uri:}") String oidcJwksUri,
            @Value("${datarun.auth.dev-token.allow-unauthenticated-push:true}")
            boolean allowUnauthenticatedDevPush) {
        this.mode = normalize(mode);
        this.jwtIssuer = blankToNull(jwtIssuer);
        this.jwtAudience = blankToNull(jwtAudience);
        this.jwtHmacSecret = blankToNull(jwtHmacSecret);
        this.oidcIssuer = blankToNull(oidcIssuer);
        this.oidcAudience = blankToNull(oidcAudience);
        this.oidcJwksUri = blankToNull(oidcJwksUri);
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

    public boolean isOidcJwksMode() {
        return "oidc-jwks".equals(mode);
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

    public String oidcIssuer() {
        return oidcIssuer;
    }

    public String oidcAudience() {
        return oidcAudience;
    }

    public String oidcJwksUri() {
        return oidcJwksUri;
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
