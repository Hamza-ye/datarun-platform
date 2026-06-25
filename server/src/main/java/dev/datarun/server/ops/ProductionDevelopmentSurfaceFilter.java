package dev.datarun.server.ops;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Production uses deployment-managed one-shot tooling, not development admin
 * controllers or development token administration.
 */
@Component
public final class ProductionDevelopmentSurfaceFilter extends OncePerRequestFilter {

    private final boolean production;

    public ProductionDevelopmentSurfaceFilter(Environment environment) {
        this.production = environment.acceptsProfiles(Profiles.of("production"));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!production) {
            return true;
        }
        String path = request.getRequestURI();
        return !(path.equals("/admin")
                || path.startsWith("/admin/")
                || path.equals("/api/actors")
                || path.startsWith("/api/actors/")
                || path.equals("/api/subjects")
                || path.startsWith("/api/subjects/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
