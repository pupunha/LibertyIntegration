package net.pupunha.liberty.integration.configuration;

import com.intellij.openapi.ui.Messages;
import lombok.Getter;
import lombok.Setter;
import net.pupunha.liberty.integration.annotations.Property;
import net.pupunha.liberty.integration.exception.LibertyConfigurationException;
import net.pupunha.liberty.integration.exception.PluginNotConfiguratedException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public class LibertyConfigurationRepository {

    private final static String FILE_CONFIGURATION_NAME_DEFAULT = ".libertyConfig";

    @Getter
    @Setter
    private String fileConfigurationName;

    private Properties properties;

    public LibertyConfigurationRepository() {
        properties = new Properties();
    }

    public LibertyConfiguration load() throws LibertyConfigurationException {
        LibertyConfiguration libertyConfiguration = new LibertyConfiguration();
        try {
            Path pathConfiguration = pathConfiguration();
            if (!Files.exists(pathConfiguration)) {
                Files.createFile(pathConfiguration);
            }
            properties.load(new FileInputStream(pathConfiguration.toFile()));
            Field[] declaredFields = libertyConfiguration.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                Property propertyAnnotation = field.getAnnotation(Property.class);
                String property = properties.getProperty(propertyAnnotation.value());
                BeanUtils.setProperty(libertyConfiguration, field.getName(), property);
            }
        } catch (IOException | IllegalAccessException | InvocationTargetException e) {
            throw new LibertyConfigurationException(e.getMessage(), e);
        }
        return libertyConfiguration;
    }

    public void store(LibertyConfiguration libertyConfiguration) throws LibertyConfigurationException {
        try {
            Field[] declaredFields = libertyConfiguration.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                Property annotation = field.getAnnotation(Property.class);
                String key = annotation.value();
                String value = BeanUtils.getProperty(libertyConfiguration, field.getName());
                properties.setProperty(key, value != null ? value : "");
            }
            properties.store(new FileOutputStream(pathConfiguration().toFile()), null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | IOException e) {
            throw new LibertyConfigurationException(e.getMessage(), e);
        }
    }

    private Path pathConfiguration() {
        String userHome = System.getProperties().getProperty("user.home");
        String file = Optional.ofNullable(fileConfigurationName).orElse(FILE_CONFIGURATION_NAME_DEFAULT);
        return Paths.get(userHome, file);
    }

}
