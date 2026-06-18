package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Minimal local JWT validator for the accepted principal-binding foundation.
 *
 * This proves principal-to-actor semantics without a live Keycloak/JWKS
 * dependency. OIDC/JWKS provider integration can replace this validator behind
 * the same AuthenticatedActorResolver boundary.
 */
@Component
public class JwtPrincipalTokenValidator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;

    public JwtPrincipalTokenValidator(ObjectMapper objectMapper,
                                      AuthProperties authProperties) {
        this.objectMapper = objectMapper;
        this.authProperties = authProperties;
    }

    public JwtPrincipal validate(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            throw new AuthResolutionException("invalid_jwt");
        }
        try {
            JsonNode header = objectMapper.readTree(decode(parts[0]));
            JsonNode payload = objectMapper.readTree(decode(parts[1]));
            if (!"HS256".equals(header.path("alg").asText(null))) {
                throw new AuthResolutionException("unsupported_jwt_alg");
            }
            verifySignature(parts[0] + "." + parts[1], parts[2]);

            String issuer = payload.path("iss").asText(null);
            String subject = payload.path("sub").asText(null);
            if (issuer == null || issuer.isBlank() || subject == null || subject.isBlank()) {
                throw new AuthResolutionException("missing_principal_claim");
            }
            if (authProperties.jwtIssuer() != null
                    && !authProperties.jwtIssuer().equals(issuer)) {
                throw new AuthResolutionException("invalid_issuer");
            }
            if (authProperties.jwtAudience() != null
                    && !audienceMatches(payload.path("aud"), authProperties.jwtAudience())) {
                throw new AuthResolutionException("invalid_audience");
            }
            validateTemporalClaims(payload);
            return new JwtPrincipal(issuer, subject);
        } catch (AuthResolutionException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthResolutionException("invalid_jwt");
        }
    }

    private void verifySignature(String signingInput, String signaturePart) throws Exception {
        String secret = authProperties.jwtHmacSecret();
        if (secret == null) {
            throw new AuthResolutionException("jwt_secret_not_configured");
        }
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        byte[] expected = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        byte[] actual = decode(signaturePart);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new AuthResolutionException("invalid_signature");
        }
    }

    private void validateTemporalClaims(JsonNode payload) {
        long now = Instant.now().getEpochSecond();
        JsonNode exp = payload.path("exp");
        if (exp.isNumber() && exp.asLong() <= now) {
            throw new AuthResolutionException("jwt_expired");
        }
        JsonNode nbf = payload.path("nbf");
        if (nbf.isNumber() && nbf.asLong() > now) {
            throw new AuthResolutionException("jwt_not_yet_valid");
        }
    }

    private boolean audienceMatches(JsonNode aud, String expected) {
        if (aud.isTextual()) {
            return expected.equals(aud.asText());
        }
        if (aud.isArray()) {
            for (JsonNode item : aud) {
                if (item.isTextual() && expected.equals(item.asText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
