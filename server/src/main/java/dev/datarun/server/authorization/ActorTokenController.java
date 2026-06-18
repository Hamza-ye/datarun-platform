package dev.datarun.server.authorization;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Development actor token admin API. Generate, list, and revoke tokens.
 */
@RestController
@RequestMapping("/api/actors")
public class ActorTokenController {

    private final ActorTokenRepository tokenRepository;
    private final AuthProperties authProperties;

    public ActorTokenController(ActorTokenRepository tokenRepository,
                                AuthProperties authProperties) {
        this.tokenRepository = tokenRepository;
        this.authProperties = authProperties;
    }

    @PostMapping("/{actorId}/tokens")
    public ResponseEntity<?> generateToken(@PathVariable UUID actorId) {
        if (!authProperties.isDevTokenMode()) {
            return devTokenAdminDisabled();
        }
        String token = tokenRepository.createToken(actorId);
        return ResponseEntity.ok(Map.of("token", token, "actor_id", actorId.toString()));
    }

    @GetMapping("/{actorId}/tokens")
    public ResponseEntity<?> listTokens(@PathVariable UUID actorId) {
        if (!authProperties.isDevTokenMode()) {
            return devTokenAdminDisabled();
        }
        List<String> tokens = tokenRepository.findActiveTokensForActor(actorId);
        return ResponseEntity.ok(Map.of("tokens", tokens));
    }

    @PostMapping("/{actorId}/tokens/revoke-all")
    public ResponseEntity<?> revokeAll(@PathVariable UUID actorId) {
        if (!authProperties.isDevTokenMode()) {
            return devTokenAdminDisabled();
        }
        tokenRepository.revokeAllForActor(actorId);
        return ResponseEntity.ok(Map.of("revoked", true));
    }

    @PostMapping("/tokens/revoke")
    public ResponseEntity<?> revokeToken(@RequestBody RevokeRequest request) {
        if (!authProperties.isDevTokenMode()) {
            return devTokenAdminDisabled();
        }
        if (request.token() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing token"));
        }
        tokenRepository.revoke(request.token());
        return ResponseEntity.ok(Map.of("revoked", true));
    }

    public record RevokeRequest(String token) {}

    private ResponseEntity<?> devTokenAdminDisabled() {
        return ResponseEntity.status(403)
                .body(Map.of("error", "dev_token_admin_disabled"));
    }
}
