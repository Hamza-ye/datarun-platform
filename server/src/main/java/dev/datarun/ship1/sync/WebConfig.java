package dev.datarun.ship1.sync;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registers the bearer-token interceptor on sync endpoints. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ActorAuthInterceptor interceptor;
    private final CoordinatorAuthInterceptor coordinatorInterceptor;

    public WebConfig(ActorAuthInterceptor interceptor, CoordinatorAuthInterceptor coordinatorInterceptor) {
        this.interceptor = interceptor;
        this.coordinatorInterceptor = coordinatorInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/sync/**");
        // Ship-2 §6 commitment 2: coordinator-only path. Does NOT extend to /admin/events,
        // /admin/flags, or /api/sync/** — those keep their existing auth posture.
        registry.addInterceptor(coordinatorInterceptor)
                .addPathPatterns("/admin/subjects/**");
    }
}
