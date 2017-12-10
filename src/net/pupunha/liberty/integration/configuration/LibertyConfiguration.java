package net.pupunha.liberty.integration.configuration;

import com.twelvemonkeys.util.LinkedMap;
import lombok.Data;
import net.pupunha.liberty.integration.annotations.Property;
import net.pupunha.liberty.integration.exception.LibertyConfigurationException;
import net.pupunha.liberty.integration.exception.PathNotFoundException;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static net.pupunha.liberty.integration.constants.ProfileConstants.*;

@Data
public class LibertyConfiguration {

    @Property("pack.current")
    private String packCurrent;

    @Property("wlp.user.dir")
    private String wlpUserDir;

    @Property("wlp.server.name")
    private String serverName;

    @Property("wlp.app.combo.selected")
    private String appComboSelected;

    public Path getAbsolutePathServerName() throws PathNotFoundException {
        Path path = Paths.get(wlpUserDir, SERVERS, serverName);
        if(!Files.exists(path)) {
            throw new PathNotFoundException("The file '" + path + " was not found");
        }
        return path;
    }

    public Path getAbsolutePathServers() throws PathNotFoundException {
        Path path = Paths.get(wlpUserDir, SERVERS);
        if(!Files.exists(path)) {
            throw new PathNotFoundException("The file '" + path + " was not found");
        }
        return path;
    }

    public String getAbsolutePathJmxLocalAddress() throws PathNotFoundException {
        final Path path = Paths.get(getAbsolutePathServerName().toString(), JMX_LOCAL_ADDRESS_PATH);
        try {
            if(!Files.exists(path)) {
                throw new PathNotFoundException("The file '" + path + " was not found");
            }
            List<String> lines = Files.readAllLines(path);
            return lines.get(0);
        } catch (IOException e) {
            throw new PathNotFoundException("Failed to read path: " + path);
        }
    }

    public Path getAbsolutePathApps() throws PathNotFoundException {
        Path path = Paths.get(getAbsolutePathServerName().toString(), APPS);
        if(!Files.exists(path)) {
            throw new PathNotFoundException("The file '" + path + " was not found");
        }
        return path;
    }

    public Path getAbsolutePathLogs() throws PathNotFoundException {
        Path path = Paths.get(getAbsolutePathServerName().toString(), LOGS);
        if(!Files.exists(path)) {
            throw new PathNotFoundException("The file '" + path + " was not found");
        }
        return path;
    }

    public Path getAbsolutePathServerXml() throws PathNotFoundException {
        Path path = Paths.get(getAbsolutePathServerName().toString(), SERVER_XML);
        if(!Files.exists(path)) {
            throw new PathNotFoundException("The file '" + path + " was not found");
        }
        return path;
    }

    public Map<String, File> getApplications() throws PathNotFoundException {
        final File fileApps = getAbsolutePathApps().toFile();
        if (fileApps.exists()) {
            final File[] apps = fileApps.listFiles();
            if (apps != null) {
                LinkedMap<String, File> appsMap = new LinkedMap<>();
                for (int i = 0; i < apps.length; i++) {
                    appsMap.put(apps[i].getName(), apps[i]);
                }
                return appsMap;
            }
        }
        return null;
    }

    public boolean isValidConfiguration() {
        return StringUtils.isNotEmpty(wlpUserDir) || StringUtils.isNotEmpty(serverName);
    }
}
