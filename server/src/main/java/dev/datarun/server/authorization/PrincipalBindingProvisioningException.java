package dev.datarun.server.authorization;

public class PrincipalBindingProvisioningException extends RuntimeException {

    public PrincipalBindingProvisioningException(String message) {
        super(message);
    }

    public PrincipalBindingProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
