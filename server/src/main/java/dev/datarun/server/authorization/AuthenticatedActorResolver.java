package dev.datarun.server.authorization;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticatedActorResolver {

    private final AuthProperties authProperties;
    private final ActorTokenRepository tokenRepository;
    private final JwtPrincipalTokenValidator jwtValidator;
    private final AuthPrincipalBindingRepository bindingRepository;

    public AuthenticatedActorResolver(AuthProperties authProperties,
                                      ActorTokenRepository tokenRepository,
                                      JwtPrincipalTokenValidator jwtValidator,
                                      AuthPrincipalBindingRepository bindingRepository) {
        this.authProperties = authProperties;
        this.tokenRepository = tokenRepository;
        this.jwtValidator = jwtValidator;
        this.bindingRepository = bindingRepository;
    }

    public AuthenticatedActor resolve(String bearerToken) {
        if (authProperties.isDevTokenMode()) {
            UUID actorId = tokenRepository.resolveToken(bearerToken);
            if (actorId == null) {
                throw new AuthResolutionException("invalid_token");
            }
            return new AuthenticatedActor(actorId, "dev-token", null, null);
        }

        if (authProperties.isJwtMode()) {
            JwtPrincipal principal = jwtValidator.validate(bearerToken);
            UUID actorId = bindingRepository.resolveActor(principal.issuer(), principal.subject());
            if (actorId == null) {
                throw new AuthResolutionException("unmapped_principal");
            }
            return new AuthenticatedActor(
                    actorId, "jwt-principal", principal.issuer(), principal.subject());
        }

        throw new AuthResolutionException("unsupported_auth_mode");
    }
}
