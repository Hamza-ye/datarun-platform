package dev.datarun.server.ops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionRuntimeValidatorTest {

    private static final String[] VALID_PROPERTIES = {
            "spring.profiles.active=production",
            "datarun.auth.mode=oidc-jwks",
            "datarun.auth.dev-token.allow-unauthenticated-push=false",
            "datarun.auth.oidc.issuer=https://identity.example.test/realms/datarun",
            "datarun.auth.oidc.audience=datarun-mobile",
            "datarun.auth.oidc.jwks-uri=https://identity.example.test/jwks",
            "datarun.web-admin.oidc.client-id=datarun-web-admin",
            "datarun.web-admin.oidc.client-secret=test-only-web-admin-client-secret",
            "datarun.web-admin.oidc.authorization-uri=https://identity.example.test/auth",
            "datarun.web-admin.oidc.token-uri=https://identity.example.test/token",
            "datarun.web-admin.oidc.redirect-uri=https://app.example.test/web-admin/oidc/callback",
            "datarun.auth.principal-bindings.applied-by=operator:release-123",
            "spring.datasource.url=jdbc:postgresql://database.internal/datarun_prod",
            "spring.datasource.username=datarun_runtime",
            "spring.datasource.password=not-the-development-password"
    };

    @Test
    void validProductionConfigurationStartsContext() {
        new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withInitializer(context ->
                        context.getEnvironment().setActiveProfiles("production"))
                .withUserConfiguration(ProductionRuntimeValidator.class)
                .withPropertyValues(VALID_PROPERTIES)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getEnvironment().getActiveProfiles())
                            .contains("production");
                    assertThat(context.getSourceApplicationContext().isRunning()).isTrue();
                });
    }

    @Test
    void productionRejectsDevelopmentAndMissingSecuritySettings() {
        Map<String, String> invalidOverrides = Map.ofEntries(
                Map.entry("datarun.auth.mode", "dev-token"),
                Map.entry(
                        "datarun.auth.dev-token.allow-unauthenticated-push",
                        "true"),
                Map.entry("datarun.auth.oidc.issuer", ""),
                Map.entry("datarun.auth.oidc.audience", ""),
                Map.entry("datarun.auth.oidc.jwks-uri", ""),
                Map.entry("datarun.web-admin.oidc.client-id", ""),
                Map.entry("datarun.web-admin.oidc.client-secret", ""),
                Map.entry("datarun.web-admin.oidc.authorization-uri", ""),
                Map.entry("datarun.web-admin.oidc.token-uri", ""),
                Map.entry("datarun.web-admin.oidc.redirect-uri", ""),
                Map.entry(
                        "datarun.auth.principal-bindings.applied-by",
                        ProductionRuntimeValidator.DEFAULT_BINDING_OPERATOR),
                Map.entry(
                        "spring.datasource.url",
                        "jdbc:postgresql://localhost:5432/datarun"),
                Map.entry("spring.datasource.username", "datarun"),
                Map.entry("spring.datasource.password", "datarun"));

        invalidOverrides.forEach((property, invalidValue) -> {
            MockEnvironment environment = validEnvironment()
                    .withProperty(property, invalidValue);
            ProductionRuntimeValidator validator =
                    new ProductionRuntimeValidator();
            validator.setEnvironment(environment);

            assertThatThrownBy(() -> validator.postProcessBeanFactory(
                    new DefaultListableBeanFactory()))
                    .as(property)
                    .isInstanceOf(BeanDefinitionStoreException.class)
                    .hasMessageContaining(property);
        });
    }

    @Test
    void nonProductionProfileRetainsDevelopmentDefaults() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("test");
        ProductionRuntimeValidator validator = new ProductionRuntimeValidator();
        validator.setEnvironment(environment);

        validator.postProcessBeanFactory(new DefaultListableBeanFactory());
    }

    @Test
    void productionDefaultProfileCannotBypassValidation() {
        MockEnvironment environment = new MockEnvironment();
        environment.setDefaultProfiles("production");
        ProductionRuntimeValidator validator = new ProductionRuntimeValidator();
        validator.setEnvironment(environment);

        assertThatThrownBy(() -> validator.postProcessBeanFactory(
                new DefaultListableBeanFactory()))
                .isInstanceOf(BeanDefinitionStoreException.class)
                .hasMessageContaining("datarun.auth.mode");
    }

    @Test
    void productionProfileEnablesBoundedGracefulShutdown() {
        new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withInitializer(context ->
                        context.getEnvironment().setActiveProfiles("production"))
                .withPropertyValues(VALID_PROPERTIES)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getEnvironment().getProperty("server.shutdown"))
                            .isEqualTo("graceful");
                    assertThat(context.getEnvironment().getProperty(
                            "spring.lifecycle.timeout-per-shutdown-phase"))
                            .isEqualTo("30s");
                });
    }

    @Test
    void productionProfileUsesLaxSecureHttpOnlyWebAdminCookie() {
        new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withInitializer(context ->
                        context.getEnvironment().setActiveProfiles("production"))
                .withPropertyValues(VALID_PROPERTIES)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getEnvironment().getProperty(
                            "server.servlet.session.cookie.name"))
                            .isEqualTo("DATARUN_ADMIN_SESSION");
                    assertThat(context.getEnvironment().getProperty(
                            "server.servlet.session.cookie.http-only"))
                            .isEqualTo("true");
                    assertThat(context.getEnvironment().getProperty(
                            "server.servlet.session.cookie.secure"))
                            .isEqualTo("true");
                    assertThat(context.getEnvironment().getProperty(
                            "server.servlet.session.cookie.same-site"))
                            .isEqualTo("lax");
                });
    }

    private MockEnvironment validEnvironment() {
        MockEnvironment environment =
                new MockEnvironment().withProperty("spring.profiles.active", "production");
        for (String property : VALID_PROPERTIES) {
            int separator = property.indexOf('=');
            environment.setProperty(
                    property.substring(0, separator),
                    property.substring(separator + 1));
        }
        environment.setActiveProfiles("production");
        return environment;
    }
}
