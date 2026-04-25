package dev.datarun.ship1.sync;

import dev.datarun.ship1.scope.ScopeResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Bearer-token interceptor for coordinator-only endpoints (Ship-2 §6 commitment 1/2).
 *
 * <p>401 if the {@code Authorization: Bearer &lt;token&gt;} header is missing or unknown. 403 if
 * the resolved actor does not currently hold the {@code coordinator} role per the projection-derived
 * predicate {@link ScopeResolver#hasRoleAt(String, String, OffsetDateTime)} — no cache, no
 * {@code kind} discriminator on {@code actor_tokens}.
 *
 * <p>On success, the coordinator's actor UUID is stashed on the request as
 * attribute {@value #ATTR_COORDINATOR_ID}.
 *
 * <p>Registered on {@code /admin/subjects/**} only.
 */
@Component
public class CoordinatorAuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_COORDINATOR_ID = "ship1.coordinator_id";
    private static final String PREFIX = "Bearer ";
    private static final String ROLE = "coordinator";

    private final ActorTokenRepository tokens;
    private final ScopeResolver scopes;

    public CoordinatorAuthInterceptor(ActorTokenRepository tokens, ScopeResolver scopes) {
        this.tokens = tokens;
        this.scopes = scopes;
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
        if (!scopes.hasRoleAt(actorId.toString(), ROLE, OffsetDateTime.now())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        request.setAttribute(ATTR_COORDINATOR_ID, actorId);
        return true;
    }
}
