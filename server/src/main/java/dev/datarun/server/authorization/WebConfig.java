package dev.datarun.server.authorization;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the actor token interceptor for sync endpoints.
 * Admin and other endpoints remain unauthenticated for Phase 2a.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ActorTokenInterceptor actorTokenInterceptor;

    public WebConfig(ActorTokenInterceptor actorTokenInterceptor) {
        this.actorTokenInterceptor = actorTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(actorTokenInterceptor)
                .addPathPatterns("/api/sync/pull", "/api/sync/config");
    }
}
