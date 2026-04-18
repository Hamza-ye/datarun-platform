package dev.datarun.server.authorization;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Actor token admin API. Generate, list, and revoke tokens (IDR-016).
 */
@RestController
@RequestMapping("/api/actors")
public class ActorTokenController {

    private final ActorTokenRepository tokenRepository;

    public ActorTokenController(ActorTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @PostMapping("/{actorId}/tokens")
    public ResponseEntity<?> generateToken(@PathVariable UUID actorId) {
        String token = tokenRepository.createToken(actorId);
        return ResponseEntity.ok(Map.of("token", token, "actor_id", actorId.toString()));
    }

    @GetMapping("/{actorId}/tokens")
    public ResponseEntity<?> listTokens(@PathVariable UUID actorId) {
        List<String> tokens = tokenRepository.findActiveTokensForActor(actorId);
        return ResponseEntity.ok(Map.of("tokens", tokens));
    }

    @PostMapping("/{actorId}/tokens/revoke-all")
    public ResponseEntity<?> revokeAll(@PathVariable UUID actorId) {
        tokenRepository.revokeAllForActor(actorId);
        return ResponseEntity.ok(Map.of("revoked", true));
    }

    @PostMapping("/tokens/revoke")
    public ResponseEntity<?> revokeToken(@RequestBody RevokeRequest request) {
        if (request.token() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing token"));
        }
        tokenRepository.revoke(request.token());
        return ResponseEntity.ok(Map.of("revoked", true));
    }

    public record RevokeRequest(String token) {}
}
