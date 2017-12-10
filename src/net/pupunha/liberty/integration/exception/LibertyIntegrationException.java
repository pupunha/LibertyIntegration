package net.pupunha.liberty.integration.exception;

public abstract class LibertyIntegrationException extends Exception {

    public LibertyIntegrationException(String message) {
        super(message);
    }

    public LibertyIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

}
