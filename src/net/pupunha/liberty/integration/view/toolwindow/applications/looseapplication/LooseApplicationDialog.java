package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import lombok.Getter;
import net.pupunha.liberty.integration.util.ComboBoxItem;
import net.pupunha.liberty.integration.util.ListItem;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LooseApplicationDialog extends DialogWrapper {

    private Project project;

    @Getter
    private LooseApplicationPanel panel;

    protected LooseApplicationDialog(@Nullable Project project) {
        super(project, true, true);
        this.project = project;
        init();
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

    public LooseApplicationParameter getParameters() {
        LooseApplicationParameter params = new LooseApplicationParameter();
        ComboBoxItem selectedItem = (ComboBoxItem) panel.getComboEnterpriseApplication().getSelectedItem();
        String projectEAR = Optional.ofNullable(selectedItem).map(ComboBoxItem::getValue).orElse(null);
        params.setProjectEAR(Paths.get(projectEAR));

        List<Path> modules = new ArrayList<>();
        ListModel model = panel.getListProjectsRight().getModel();
        for(int i = 0; i< model.getSize(); i++) {
            ListItem item = (ListItem) model.getElementAt(i);
            modules.add(item.getValue());
        }
        params.setModules(modules);
        return params;
    }

}
