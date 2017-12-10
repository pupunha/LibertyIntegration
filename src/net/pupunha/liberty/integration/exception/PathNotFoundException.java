package net.pupunha.liberty.integration.exception;

public class PathNotFoundException extends LibertyIntegrationException {

    public PathNotFoundException(String message) {
        super(message);
    }

    public PathNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
