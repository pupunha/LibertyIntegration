package net.pupunha.liberty.integration.view.toolwindow.applications;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application.ToolBarComponent;
import org.jetbrains.annotations.NotNull;

public class ApplicationToolWindow implements ToolWindowFactory {

    private ApplicationToolWindowPanel panel;

    public ApplicationToolWindow() {
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        panel = new ApplicationToolWindowPanel(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
        ToolBarComponent.getManager(project).executeRefresh();
    }
}
