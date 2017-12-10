package net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application;

import com.intellij.ide.actions.RefreshAction;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import net.pupunha.liberty.integration.view.toolwindow.applications.ApplicationToolWindowPanel;
import org.jetbrains.annotations.NotNull;

public class ToolBarComponent implements ProjectComponent {

    @Getter @Setter
    private ApplicationToolWindowPanel.RefreshToolBar refreshToolBar;

    public static ToolBarComponent getManager(@NotNull Project project) {
        return project.getComponent(ToolBarComponent.class);
    }

    public void executeRefresh() {
        refreshToolBar.execute();
    }


}
