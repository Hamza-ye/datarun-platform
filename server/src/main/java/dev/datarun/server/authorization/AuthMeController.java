package dev.datarun.server.authorization;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthMeController {

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        UUID actorId = (UUID) request.getAttribute(ActorTokenInterceptor.ACTOR_ID_ATTR);
        AuthenticatedActor actor =
                (AuthenticatedActor) request.getAttribute(ActorTokenInterceptor.AUTHENTICATED_ACTOR_ATTR);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("actor_id", actorId.toString());
        if (actor != null) {
            body.put("auth_source", actor.source());
        }
        return ResponseEntity.ok(body);
    }
}
