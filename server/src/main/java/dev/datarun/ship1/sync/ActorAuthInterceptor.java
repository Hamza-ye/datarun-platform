package dev.datarun.ship1.sync;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Bearer-token interceptor. Resolves {@code Authorization: Bearer &lt;token&gt;} to an actor UUID
 * and stashes it on the request as attribute {@value #ATTR_ACTOR_ID}. Unresolved → 401.
 *
 * Applies to {@code /api/sync/**}. Admin UI is currently unauthenticated for Ship-1 (dev-only).
 */
@Component
public class ActorAuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_ACTOR_ID = "ship1.actor_id";
    private static final String PREFIX = "Bearer ";

    private final ActorTokenRepository tokens;

    public ActorAuthInterceptor(ActorTokenRepository tokens) {
        this.tokens = tokens;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(PREFIX)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        String token = header.substring(PREFIX.length()).trim();
        UUID actorId = tokens.resolveToken(token).orElse(null);
        if (actorId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        request.setAttribute(ATTR_ACTOR_ID, actorId);
        return true;
    }
}
