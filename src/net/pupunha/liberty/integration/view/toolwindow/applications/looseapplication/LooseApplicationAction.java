package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.AsyncResult;
import lombok.Data;
import org.jdesktop.swingx.JXComboBox;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;
import java.util.List;

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
            dialog.show();

            if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                Parameter parameter = dialog.getParameters();
                LogLooseApplicationDialog logDialog = new LogLooseApplicationDialog(project, parameter);
                logDialog.show();


            }
        } catch (Exception e) {
            Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
        }
    }

}
