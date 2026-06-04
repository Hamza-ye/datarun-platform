package dev.datarun.server.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Resolves Bearer credentials to actor_id for actor-scoped API endpoints.
 * Sets resolved actor_id as request attribute "actorId".
 * Returns 401 if token missing, malformed, or invalid.
 */
@Component
public class ActorTokenInterceptor implements HandlerInterceptor {

    public static final String ACTOR_ID_ATTR = "actorId";
    public static final String AUTHENTICATED_ACTOR_ATTR = "authenticatedActor";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticatedActorResolver actorResolver;
    private final AuthProperties authProperties;

    public ActorTokenInterceptor(AuthenticatedActorResolver actorResolver,
                                 AuthProperties authProperties) {
        this.actorResolver = actorResolver;
        this.authProperties = authProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            if (isPush(request) && authProperties.allowUnauthenticatedDevPush()) {
                return true;
            }
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

        AuthenticatedActor actor;
        try {
            actor = actorResolver.resolve(token);
        } catch (AuthResolutionException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"invalid_token\"}");
            return false;
        }

        request.setAttribute(ACTOR_ID_ATTR, actor.actorId());
        request.setAttribute(AUTHENTICATED_ACTOR_ATTR, actor);
        return true;
    }

    private boolean isPush(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().endsWith("/api/sync/push");
    }
}
