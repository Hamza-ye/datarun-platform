package dev.datarun.server.authorization;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the actor token interceptor for actor-scoped API endpoints.
 * The HTML admin console is development-only until production admin auth lands.
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
                .addPathPatterns(
                        "/api/sync/pull",
                        "/api/sync/subject-history",
                        "/api/sync/config",
                        "/api/assignments",
                        "/api/assignments/**",
                        "/api/conflicts",
                        "/api/conflicts/**");
    }
}
