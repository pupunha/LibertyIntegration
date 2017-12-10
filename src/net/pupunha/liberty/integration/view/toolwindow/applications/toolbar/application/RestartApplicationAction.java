package net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import net.pupunha.liberty.integration.configuration.LibertyConfiguration;
import net.pupunha.liberty.integration.configuration.LibertyConfigurationRepository;
import net.pupunha.liberty.integration.constants.GeneralConstants;
import net.pupunha.liberty.integration.exception.PathNotFoundException;
import net.pupunha.liberty.integration.jmx.JMXLibertyConnector;
import org.apache.commons.collections.MapUtils;
import org.jetbrains.annotations.Nullable;

import javax.management.remote.JMXConnector;
import javax.swing.*;
import java.io.File;
import java.util.Map;

public class RestartApplicationAction extends AnAction {

    public RestartApplicationAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {

        ApplicationManager.getApplication().invokeLater(() -> {
            final Project project = event.getData(PlatformDataKeys.PROJECT);
            if (project != null) {
                try {
                    LibertyConfigurationRepository repository = new LibertyConfigurationRepository();
                    LibertyConfiguration libertyConfiguration = repository.load();
                    Map<String, File> applications = libertyConfiguration.getApplications();
                    if (MapUtils.isEmpty(applications)) {
                        Messages.showMessageDialog(project,
                                "The server name '" + libertyConfiguration.getServerName() + "' don't have loose applications (EAR/WAR) in the 'apps' directory.",
                                GeneralConstants.ERROR,
                                Messages.getErrorIcon());
                        return;
                    }

                    SelectApplicationComponent component = SelectApplicationComponent.getManager(project);
                    SelectApplication selectApplication = component.getSelectApplication();

                    String appName = selectApplication.getAppName();
                    if(appName == null) {
                        Messages.showMessageDialog(project, "Select the application to restart", GeneralConstants.ERROR, Messages.getErrorIcon());
                        return;
                    }

                    File fileAppName = new File(applications.get(appName).getAbsolutePath());

                    selectApplication.setAppName(appName);
                    selectApplication.setApplication(fileAppName);

                    libertyConfiguration.setAppComboSelected(appName);
                    repository.store(libertyConfiguration);

                    String jmxLocalAddress = libertyConfiguration.getAbsolutePathJmxLocalAddress();
                    JMXLibertyConnector connector = new JMXLibertyConnector(jmxLocalAddress);
                    connector.connect();
                    connector.notifyFileChange(selectApplication.getApplication());
                    connector.disconnect();

                } catch (PathNotFoundException ex) {
                    Messages.showMessageDialog(project, "Plugin Configuration Invalid", GeneralConstants.ERROR, Messages.getErrorIcon());
                } catch (Exception ex) {
                    if (ex.getMessage().trim().equals("")) {
                        Messages.showMessageDialog(project, "Unknown error", GeneralConstants.ERROR, Messages.getErrorIcon());
                    } else {
                        Messages.showMessageDialog(project, ex.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
                    }
                }
            }
        });
    }
}
