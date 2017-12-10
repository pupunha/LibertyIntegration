package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LooseApplicationDialog extends DialogWrapper {

    private Project project;
    private LooseApplicationPanel panel;

    protected LooseApplicationDialog(@Nullable Project project) {
        super(project);
        this.project = project;
        init();
    }

    @Override
    protected void init() {
        super.init();
    }

    protected JComponent createCenterPanel() {
        panel = new LooseApplicationPanel(project);
        return panel;
    }

    protected ValidationInfo doValidate() {
        if (panel.getComboEnterpriseApplication().getSelectedIndex() == 0) {
            return new ValidationInfo("No Enterprise Application (EAR) selected");
        }
        return null;
    }
}
