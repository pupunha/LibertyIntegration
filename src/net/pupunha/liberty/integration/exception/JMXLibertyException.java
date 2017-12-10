package net.pupunha.liberty.integration.exception;

public class JMXLibertyException extends LibertyIntegrationException {

    public JMXLibertyException(ErrorCode errorCode) {
        super(errorCode.toString());
    }

    public JMXLibertyException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.toString(), cause);
    }

    public JMXLibertyException(String message) {
        super(message);
    }

    public JMXLibertyException(String message, Throwable cause) {
        super(message, cause);
    }
}
