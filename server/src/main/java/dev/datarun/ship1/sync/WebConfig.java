package dev.datarun.ship1.sync;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registers the bearer-token interceptor on sync endpoints. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ActorAuthInterceptor interceptor;

    public WebConfig(ActorAuthInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/sync/**");
    }
}
