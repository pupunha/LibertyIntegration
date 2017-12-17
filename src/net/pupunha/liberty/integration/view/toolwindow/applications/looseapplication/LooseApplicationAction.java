package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application.ConfigurationComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class LooseApplicationAction extends AnAction {

    public LooseApplicationAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        try {
            LooseApplicationDialog dialog = new LooseApplicationDialog(project);
            dialog.setTitle("Create Loose Application EAR");
            dialog.setSize(700, 500);
            dialog.show();
            if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                LooseApplicationParameter parameter = dialog.getParameters();
                LogLooseApplicationDialog logDialog = new LogLooseApplicationDialog(project, parameter);
                logDialog.show();

                if (logDialog.getExitCode() == DialogWrapper.CANCEL_EXIT_CODE) {
//                    logDialog.getExecutorService().shutdownNow();
                    ConfigurationComponent.getManager(project).getExecutor().shutdownNow();
                    System.out.println("JA ENTROU");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
        }
    }

}
