package net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

public class SelectApplicationComponent implements ProjectComponent {

    @Getter @Setter
    private Map<String, File> applications;

    @Getter @Setter
    private SelectApplication selectApplication;

    public SelectApplicationComponent() {
        this.selectApplication = new SelectApplication();
    }

    public static SelectApplicationComponent getManager(@NotNull Project project) {
        return project.getComponent(SelectApplicationComponent.class);
    }

    public void clearSelectApplication() {
//        this.applications = null;
        this.selectApplication = new SelectApplication();
    }
}
