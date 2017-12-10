package net.pupunha.liberty.integration.exception;

public class LibertyAccessException extends LibertyIntegrationException {

    public LibertyAccessException(String message) {
        super(message);
    }

    public LibertyAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
