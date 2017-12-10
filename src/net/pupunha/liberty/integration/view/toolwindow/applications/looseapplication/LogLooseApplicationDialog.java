package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LogLooseApplicationDialog extends DialogWrapper {

    private Project project;
    private LogLooseApplicationPanel panel;
    private Parameter parameter;

    public LogLooseApplicationDialog(@Nullable Project project, Parameter parameter) {
        super(project);
        this.project = project;
        this.parameter = parameter;
        this.init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        panel = new LogLooseApplicationPanel(project, parameter);
        return panel;
    }

}
