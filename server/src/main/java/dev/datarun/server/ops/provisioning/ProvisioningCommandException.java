package dev.datarun.server.ops.provisioning;

public class ProvisioningCommandException extends RuntimeException {

    public ProvisioningCommandException(String message) {
        super(message);
    }

    public ProvisioningCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
