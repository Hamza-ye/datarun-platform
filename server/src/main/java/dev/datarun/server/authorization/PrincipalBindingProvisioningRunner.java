package dev.datarun.server.authorization;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PrincipalBindingProvisioningRunner implements ApplicationRunner {

    private final AuthProperties authProperties;
    private final PrincipalBindingManifestProvisioner provisioner;
    private final ResourceLoader resourceLoader;

    public PrincipalBindingProvisioningRunner(AuthProperties authProperties,
                                             PrincipalBindingManifestProvisioner provisioner,
                                             ResourceLoader resourceLoader) {
        this.authProperties = authProperties;
        this.provisioner = provisioner;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String manifestLocation = authProperties.principalBindingManifest();
        if (manifestLocation == null || manifestLocation.isBlank()) {
            return;
        }
        String manifestJson = readManifest(manifestLocation.trim());
        provisioner.applyManifestJson(manifestJson, authProperties.principalBindingAppliedBy());
    }

    private String readManifest(String location) throws Exception {
        Resource resource = resourceLoader.getResource(location);
        if (resource.exists()) {
            try (var input = resource.getInputStream()) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        Path path = Path.of(location);
        if (Files.exists(path)) {
            return Files.readString(path, StandardCharsets.UTF_8);
        }
        throw new PrincipalBindingProvisioningException(
                "principal binding manifest not found: " + location);
    }
}
