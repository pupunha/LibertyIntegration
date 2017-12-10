package net.pupunha.liberty.integration.exception;

public class PluginNotConfiguratedException extends LibertyIntegrationException {

    public PluginNotConfiguratedException() {
        super("Plugin is not configured. Menu > DevTools > Configuration.");
    }

}
