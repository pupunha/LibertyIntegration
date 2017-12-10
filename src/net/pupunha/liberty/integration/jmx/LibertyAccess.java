package net.pupunha.liberty.integration.jmx;

import net.pupunha.liberty.integration.configuration.LibertyConfiguration;
import net.pupunha.liberty.integration.exception.JMXLibertyException;
import net.pupunha.liberty.integration.exception.LibertyAccessException;
import net.pupunha.liberty.integration.exception.LibertyConfigurationException;
import net.pupunha.liberty.integration.exception.PathNotFoundException;

import javax.management.ObjectName;
import java.io.File;
import java.util.*;

public class LibertyAccess {

    private JMXLibertyConnector connector;

    public List<Application> getApplications(LibertyConfiguration configuration) throws LibertyAccessException {
        try {
            String jmxLocalAddress = configuration.getAbsolutePathJmxLocalAddress();
            connector = new JMXLibertyConnector(jmxLocalAddress);
            connector.connect();
            Set<ObjectName> applicationsObjectName = connector.getApplications();
            List<Application> applications = new ArrayList<>();
            for (ObjectName objectName : applicationsObjectName) {
                applications.add(connector.getApplication(objectName));
            }
            connector.disconnect();
            return applications;
        } catch (JMXLibertyException | PathNotFoundException e) {
            throw new LibertyAccessException("Falha ao recuperar applications", e);
        }
    }

    public Map<String, File> getServersProfile(LibertyConfiguration configuration) throws PathNotFoundException {

        File[] listServers = configuration.getAbsolutePathServers().toFile().listFiles(pathname -> !pathname.getName().contains("."));
        Map<String, File> map = new LinkedHashMap<>();

        assert listServers != null;
        Arrays.stream(listServers).forEach(file -> {
            map.put(file.getName(), file);
        });

        return map;
    }

}
