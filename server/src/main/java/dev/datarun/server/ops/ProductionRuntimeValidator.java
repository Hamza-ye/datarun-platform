package dev.datarun.server.ops;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Rejects unsafe production configuration before ordinary application beans
 * initialize. It reports property names and reasons, never configured values.
 */
@Component
public final class ProductionRuntimeValidator
        implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {

    static final String DEFAULT_BINDING_OPERATOR =
            "system:auth-principal-binding-provisioner";

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        if (!isProductionProfileActive()) {
            return;
        }

        List<String> violations = validateProductionConfiguration();
        if (!violations.isEmpty()) {
            throw new BeanDefinitionStoreException(
                    "Unsafe production runtime configuration: "
                            + String.join("; ", violations));
        }
    }

    List<String> validateProductionConfiguration() {
        List<String> violations = new ArrayList<>();

        String authMode = property("datarun.auth.mode");
        if (!"oidc-jwks".equals(normalize(authMode))) {
            violations.add("datarun.auth.mode must be oidc-jwks");
        }
        if (environment.getProperty(
                "datarun.auth.dev-token.allow-unauthenticated-push",
                Boolean.class,
                true)) {
            violations.add(
                    "datarun.auth.dev-token.allow-unauthenticated-push must be false");
        }

        requireHttpsUri("datarun.auth.oidc.issuer", violations);
        requirePresent("datarun.auth.oidc.audience", violations);
        requireHttpsUri("datarun.auth.oidc.jwks-uri", violations);

        String operator = property("datarun.auth.principal-bindings.applied-by");
        if (operator == null) {
            violations.add(
                    "datarun.auth.principal-bindings.applied-by is required");
        } else if (DEFAULT_BINDING_OPERATOR.equals(operator)) {
            violations.add(
                    "datarun.auth.principal-bindings.applied-by must identify the operator");
        }

        String databaseUrl = property("spring.datasource.url");
        if (databaseUrl == null) {
            violations.add("spring.datasource.url is required");
        } else {
            String normalizedUrl = databaseUrl.toLowerCase(Locale.ROOT);
            if (!normalizedUrl.startsWith("jdbc:postgresql://")) {
                violations.add("spring.datasource.url must use PostgreSQL JDBC");
            }
            if (normalizedUrl.equals("jdbc:postgresql://localhost:5432/datarun")) {
                violations.add("spring.datasource.url must not use the development default");
            }
        }

        rejectMissingOrDefault(
                "spring.datasource.username", "datarun", violations);
        rejectMissingOrDefault(
                "spring.datasource.password", "datarun", violations);

        return violations;
    }

    private boolean isProductionProfileActive() {
        return environment != null
                && environment.acceptsProfiles(Profiles.of("production"));
    }

    private void requirePresent(String propertyName, List<String> violations) {
        if (property(propertyName) == null) {
            violations.add(propertyName + " is required");
        }
    }

    private void requireHttpsUri(String propertyName, List<String> violations) {
        String value = property(propertyName);
        if (value == null) {
            violations.add(propertyName + " is required");
            return;
        }
        try {
            URI uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                violations.add(propertyName + " must be an absolute HTTPS URI");
            }
        } catch (IllegalArgumentException exception) {
            violations.add(propertyName + " must be an absolute HTTPS URI");
        }
    }

    private void rejectMissingOrDefault(
            String propertyName,
            String developmentDefault,
            List<String> violations) {
        String value = property(propertyName);
        if (value == null) {
            violations.add(propertyName + " is required");
        } else if (developmentDefault.equals(value)) {
            violations.add(propertyName + " must not use the development default");
        }
    }

    private String property(String name) {
        if (environment == null) {
            return null;
        }
        String value = environment.getProperty(name);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
