package net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import lombok.Getter;
import lombok.extern.java.Log;
import net.pupunha.liberty.integration.configuration.LibertyConfiguration;
import net.pupunha.liberty.integration.configuration.LibertyConfigurationRepository;
import net.pupunha.liberty.integration.constants.GeneralConstants;
import net.pupunha.liberty.integration.exception.LibertyConfigurationException;
import net.pupunha.liberty.integration.exception.PathNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Map;

@Log
public class ComboBoxApplication extends ComboBoxAction {

    private LibertyConfigurationRepository repository;
    private LibertyConfiguration libertyConfiguration;
    private String appSelectedWhenInitializer;

    public ComboBoxApplication() throws LibertyConfigurationException {
        this.repository = new LibertyConfigurationRepository();
        this.libertyConfiguration = repository.load();
        this.appSelectedWhenInitializer = libertyConfiguration.getAppComboSelected();
    }

    @NotNull
    @Override
    protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
        DefaultActionGroup group = new DefaultActionGroup();
        final Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(jComponent));
        try {
            SelectApplicationComponent component = SelectApplicationComponent.getManager(project);
            //When initializing the plugin
            if (component.getApplications() == null) {
                this.libertyConfiguration = repository.load();
                if (this.libertyConfiguration.isValidConfiguration()) {
                    Map<String, File> applications = this.libertyConfiguration.getApplications();
                    component.setApplications(applications);
                    addApplicationItems(group, applications);
                }
            } else {
                addApplicationItems(group, component.getApplications());
            }
        } catch (LibertyConfigurationException ex) {
            Messages.showMessageDialog(project, ex.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
        } catch (PathNotFoundException ex) {
            log.warning(ex.getMessage());
        }
        return group;
    }

    private void addApplicationItems(DefaultActionGroup group, Map<String, File> applications) {
        if (applications != null) {
            for (String key : applications.keySet()) {
                File value = applications.get(key);
                group.addAction(new ApplicationItem(key, value,null, getIcon(key)));
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null || project.isDefault() || project.isDisposed()) {
            presentation.setEnabled(false);
            presentation.setText("");
            presentation.setIcon(null);
        } else {
            SelectApplicationComponent component = SelectApplicationComponent.getManager(project);
            presentation.setVisible(true);
            presentation.setEnabled(true);
            String appName = component.getSelectApplication().getAppName();
            if (appName != null) {
                presentation.setText(appName);
                presentation.setIcon(getIcon(appName));
            } else {
                if (StringUtils.isNotEmpty(appSelectedWhenInitializer)) {
                    component.getSelectApplication().setAppName(appSelectedWhenInitializer);
                    presentation.setText(appSelectedWhenInitializer);
                    presentation.setIcon(getIcon(appSelectedWhenInitializer));
                    appSelectedWhenInitializer = null;
                } else {
                    presentation.setText("");
                    presentation.setIcon(null);
                }
            }
        }
    }

    private Icon getIcon(String appName) {
        if (appName != null) {
            appName = appName.toLowerCase();
            if (appName.endsWith("ear.xml")) {
                return AllIcons.Javaee.JavaeeAppModule;
            } else if (appName.endsWith("war.xml")) {
                return AllIcons.Javaee.WebService;
            }
        }
        return null;
    }

    private class ApplicationItem extends AnAction {

        @Getter
        private String key;

        @Getter
        private File value;

        public ApplicationItem(@Nullable String text,
                               @Nullable File value,
                               @Nullable String description,
                               @Nullable Icon icon) {
            super(text, description, icon);
            this.key = text;
            this.value = value;
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            Project project = anActionEvent.getProject();
            if (project != null) {
                try {
                    LibertyConfigurationRepository repository = new LibertyConfigurationRepository();
                    LibertyConfiguration libertyConfiguration = repository.load();
                    SelectApplicationComponent component = SelectApplicationComponent.getManager(project);
                    SelectApplication selectApplication = component.getSelectApplication();
                    selectApplication.setAppName(getKey());
                    selectApplication.setApplication(getValue());
                    libertyConfiguration.setAppComboSelected(getKey());
                    repository.store(libertyConfiguration);
                } catch (LibertyConfigurationException ex) {
                    Messages.showMessageDialog(project, ex.getMessage(),
                            GeneralConstants.ERROR, Messages.getErrorIcon());
                }
            }
        }

    }

}
