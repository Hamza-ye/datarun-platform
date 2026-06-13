package dev.datarun.server.ops;

import dev.datarun.server.authorization.ActorTokenInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public final class StructuredRequestLoggingFilter extends OncePerRequestFilter {

    static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final Pattern SAFE_REQUEST_ID =
            Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final Logger log =
            LoggerFactory.getLogger(StructuredRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = requestId(request);
        long started = System.nanoTime();
        MDC.put("request_id", requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            Object actorId = request.getAttribute(ActorTokenInterceptor.ACTOR_ID_ATTR);
            var event = response.getStatus() >= 500 ? log.atWarn() : log.atInfo();
            event.addKeyValue("http_method", request.getMethod())
                    .addKeyValue("http_path", request.getRequestURI())
                    .addKeyValue("http_status", response.getStatus())
                    .addKeyValue("duration_ms", durationMs);
            if (actorId != null) {
                event.addKeyValue("actor_id", actorId);
            }
            event.log("http_request");
            MDC.remove("request_id");
        }
    }

    private String requestId(HttpServletRequest request) {
        String supplied = request.getHeader(REQUEST_ID_HEADER);
        if (supplied != null && SAFE_REQUEST_ID.matcher(supplied).matches()) {
            return supplied;
        }
        return UUID.randomUUID().toString();
    }
}
