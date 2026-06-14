package dev.datarun.server.ops.provisioning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "datarun.ops.command")
public class OneShotProvisioningRunner
        implements ApplicationListener<ApplicationReadyEvent> {

    private static final long MAX_INPUT_BYTES = 10 * 1024 * 1024;

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final OneShotProvisioningService provisioningService;
    private final ConfigurableApplicationContext context;

    public OneShotProvisioningRunner(
            Environment environment,
            ObjectMapper objectMapper,
            OneShotProvisioningService provisioningService,
            ConfigurableApplicationContext context) {
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.provisioningService = provisioningService;
        this.context = context;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            requireNonWebMode();
            String command = requiredProperty("datarun.ops.command");
            UUID operatorId = UUID.fromString(requiredProperty("datarun.ops.operator-id"));
            String evidenceId = requiredProperty("datarun.ops.evidence-id");
            byte[] inputBytes = readInput(requiredProperty("datarun.ops.input"));
            ObjectNode result = provisioningService.execute(
                    command, inputBytes, operatorId, evidenceId);
            System.out.println(objectMapper.writeValueAsString(result));
            SpringApplication.exit(context);
        } catch (Exception exception) {
            ObjectNode failure = objectMapper.createObjectNode();
            failure.put("status", "failed");
            failure.put("error", safeError(exception));
            try {
                System.err.println(objectMapper.writeValueAsString(failure));
            } catch (Exception ignored) {
                System.err.println("{\"status\":\"failed\",\"error\":\"provisioning_failed\"}");
            }
            throw exception instanceof RuntimeException runtime
                    ? runtime
                    : new ProvisioningCommandException("provisioning command failed", exception);
        }
    }

    private void requireNonWebMode() {
        if (!"none".equalsIgnoreCase(
                environment.getProperty("spring.main.web-application-type", ""))) {
            throw new ProvisioningCommandException(
                    "spring.main.web-application-type must be none");
        }
    }

    private String requiredProperty(String name) {
        String value = environment.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new ProvisioningCommandException(name + " is required");
        }
        return value.trim();
    }

    private byte[] readInput(String inputLocation) throws Exception {
        Path path;
        if (inputLocation.startsWith("file:")) {
            path = Path.of(java.net.URI.create(inputLocation));
        } else {
            path = Path.of(inputLocation);
        }
        if (!path.isAbsolute() || !Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new ProvisioningCommandException(
                    "datarun.ops.input must be a readable absolute file");
        }
        long size = Files.size(path);
        if (size < 1 || size > MAX_INPUT_BYTES) {
            throw new ProvisioningCommandException(
                    "datarun.ops.input size is outside the allowed range");
        }
        return Files.readAllBytes(path);
    }

    private String safeError(Exception exception) {
        if (exception instanceof ProvisioningCommandException
                && exception.getMessage() != null) {
            return exception.getMessage().replaceAll("[\\r\\n]+", " ");
        }
        return "provisioning_failed";
    }
}
