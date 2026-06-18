package dev.datarun.server.authorization;

public class WebAdminSessionException extends RuntimeException {

    private final String reason;
    private final boolean loginRedirectAllowed;

    public WebAdminSessionException(String reason, boolean loginRedirectAllowed) {
        super(reason);
        this.reason = reason;
        this.loginRedirectAllowed = loginRedirectAllowed;
    }

    public String reason() {
        return reason;
    }

    public boolean loginRedirectAllowed() {
        return loginRedirectAllowed;
    }
}
