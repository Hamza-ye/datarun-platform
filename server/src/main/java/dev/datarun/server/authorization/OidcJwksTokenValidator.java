package dev.datarun.server.authorization;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Component
public class OidcJwksTokenValidator {

    private static final Set<JWSAlgorithm> ASYMMETRIC_ALGORITHMS = Set.of(
            JWSAlgorithm.RS256,
            JWSAlgorithm.RS384,
            JWSAlgorithm.RS512,
            JWSAlgorithm.PS256,
            JWSAlgorithm.PS384,
            JWSAlgorithm.PS512,
            JWSAlgorithm.ES256,
            JWSAlgorithm.ES384,
            JWSAlgorithm.ES512
    );
    private static final long CLOCK_SKEW_SECONDS = 60;

    private final AuthProperties authProperties;
    private final Clock clock;
    private volatile DefaultJWTProcessor<SecurityContext> processor;

    public OidcJwksTokenValidator(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.clock = Clock.systemUTC();
    }

    public JwtPrincipal validate(String token) {
        try {
            JWTClaimsSet claims = processor().process(token, null);
            return new JwtPrincipal(claims.getIssuer(), claims.getSubject());
        } catch (AuthResolutionException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthResolutionException("invalid_oidc_jwt");
        }
    }

    private DefaultJWTProcessor<SecurityContext> processor() {
        DefaultJWTProcessor<SecurityContext> current = processor;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (processor == null) {
                processor = buildProcessor();
            }
            return processor;
        }
    }

    private DefaultJWTProcessor<SecurityContext> buildProcessor() {
        try {
            DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            RemoteJWKSet<SecurityContext> jwkSet = new RemoteJWKSet<>(
                    URI.create(required(authProperties.oidcJwksUri(), "oidc_jwks_uri_not_configured"))
                            .toURL());
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(
                    ASYMMETRIC_ALGORITHMS, jwkSet));
            jwtProcessor.setJWTClaimsSetVerifier(this::verifyClaims);
            return jwtProcessor;
        } catch (AuthResolutionException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthResolutionException("invalid_oidc_jwks_configuration");
        }
    }

    private void verifyClaims(JWTClaimsSet claims, SecurityContext context) throws BadJWTException {
        String expectedIssuer = required(authProperties.oidcIssuer(), "oidc_issuer_not_configured");
        String expectedAudience = required(authProperties.oidcAudience(), "oidc_audience_not_configured");

        if (!expectedIssuer.equals(claims.getIssuer())) {
            throw new BadJWTException("invalid issuer");
        }
        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new BadJWTException("missing subject");
        }
        if (claims.getAudience() == null || !claims.getAudience().contains(expectedAudience)) {
            throw new BadJWTException("invalid audience");
        }

        Instant now = clock.instant();
        Date expiration = claims.getExpirationTime();
        if (expiration == null
                || !expiration.toInstant().isAfter(now.minusSeconds(CLOCK_SKEW_SECONDS))) {
            throw new BadJWTException("expired token");
        }

        Date notBefore = claims.getNotBeforeTime();
        if (notBefore != null
                && notBefore.toInstant().isAfter(now.plusSeconds(CLOCK_SKEW_SECONDS))) {
            throw new BadJWTException("token not yet valid");
        }
    }

    private String required(String value, String error) {
        if (value == null || value.isBlank()) {
            throw new AuthResolutionException(error);
        }
        return value;
    }
}
