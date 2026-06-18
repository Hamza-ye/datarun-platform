package dev.datarun.server.authorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class WebAdminSessionProperties {

    private final Duration sessionTimeout;
    private final Duration loginStateTtl;
    private final String oidcClientId;
    private final String oidcClientSecret;
    private final String oidcAuthorizationUri;
    private final String oidcTokenUri;
    private final String oidcRedirectUri;

    public WebAdminSessionProperties(
            @Value("${datarun.web-admin.session.timeout:PT30M}") Duration sessionTimeout,
            @Value("${datarun.web-admin.login.state-ttl:PT5M}") Duration loginStateTtl,
            @Value("${datarun.web-admin.oidc.client-id:datarun-web-admin}") String oidcClientId,
            @Value("${datarun.web-admin.oidc.client-secret:}") String oidcClientSecret,
            @Value("${datarun.web-admin.oidc.authorization-uri:}") String oidcAuthorizationUri,
            @Value("${datarun.web-admin.oidc.token-uri:}") String oidcTokenUri,
            @Value("${datarun.web-admin.oidc.redirect-uri:}") String oidcRedirectUri) {
        this.sessionTimeout = sessionTimeout;
        this.loginStateTtl = loginStateTtl;
        this.oidcClientId = requireNonBlank(oidcClientId, "oidc client id");
        this.oidcClientSecret = blankToNull(oidcClientSecret);
        this.oidcAuthorizationUri = blankToNull(oidcAuthorizationUri);
        this.oidcTokenUri = blankToNull(oidcTokenUri);
        this.oidcRedirectUri = blankToNull(oidcRedirectUri);
    }

    public Duration sessionTimeout() {
        return sessionTimeout;
    }

    public Duration loginStateTtl() {
        return loginStateTtl;
    }

    public String oidcClientId() {
        return oidcClientId;
    }

    public String oidcClientSecret() {
        return oidcClientSecret;
    }

    public String oidcAuthorizationUri(String issuer) {
        if (oidcAuthorizationUri != null) {
            return oidcAuthorizationUri;
        }
        if (issuer == null || issuer.isBlank()) {
            throw new AuthResolutionException("oidc_issuer_not_configured");
        }
        return issuer.replaceAll("/+$", "") + "/protocol/openid-connect/auth";
    }

    public String oidcTokenUri(String issuer) {
        if (oidcTokenUri != null) {
            return oidcTokenUri;
        }
        if (issuer == null || issuer.isBlank()) {
            throw new AuthResolutionException("oidc_issuer_not_configured");
        }
        return issuer.replaceAll("/+$", "") + "/protocol/openid-connect/token";
    }

    public String oidcRedirectUri(String fallbackRedirectUri) {
        return oidcRedirectUri == null ? fallbackRedirectUri : oidcRedirectUri;
    }

    private String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing " + name);
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
