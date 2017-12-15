package net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigurationComponent implements ProjectComponent {

    @Getter
    private ExecutorService executor;

    public ConfigurationComponent() {
        this.executor = Executors.newFixedThreadPool(10);
    }

    public static ConfigurationComponent getManager(@NotNull Project project) {
        return project.getComponent(ConfigurationComponent.class);
    }

}
