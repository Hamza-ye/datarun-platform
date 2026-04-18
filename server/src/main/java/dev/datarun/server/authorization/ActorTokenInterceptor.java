package dev.datarun.server.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Resolves Bearer token to actor_id for sync endpoints.
 * Sets resolved actor_id as request attribute "actorId".
 * Returns 401 if token missing, malformed, or invalid.
 */
@Component
public class ActorTokenInterceptor implements HandlerInterceptor {

    public static final String ACTOR_ID_ATTR = "actorId";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ActorTokenRepository tokenRepository;

    public ActorTokenInterceptor(ActorTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"missing_token\"}");
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"empty_token\"}");
            return false;
        }

        UUID actorId = tokenRepository.resolveToken(token);
        if (actorId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"invalid_token\"}");
            return false;
        }

        request.setAttribute(ACTOR_ID_ATTR, actorId);
        return true;
    }
}
