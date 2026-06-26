package dev.datarun.server.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

class WebAdminSecurityFoundationNonWebTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(WebAdminSecurityFoundationConfig.class);

    @Test
    void nonWebContextStartsWithoutHttpSecurityOrWebAdminFilterChain() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(HttpSecurity.class);
            assertThat(context).doesNotHaveBean(
                    WebAdminSecurityFoundationConfig.class);
            assertThat(context).doesNotHaveBean(SecurityFilterChain.class);
            assertThat(context).doesNotHaveBean("webAdminSecurityFoundation");
        });
    }
}
