package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import lombok.Getter;
import lombok.extern.java.Log;
import net.miginfocom.swing.MigLayout;
import net.pupunha.liberty.integration.configuration.LibertyConfiguration;
import net.pupunha.liberty.integration.configuration.LibertyConfigurationRepository;
import net.pupunha.liberty.integration.serverxml.EnterpriseApplication;
import net.pupunha.liberty.integration.serverxml.ManipulationLibertyServer;
import net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application.ConfigurationComponent;
import net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application.ToolBarComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log
public class LogLooseApplicationDialog extends DialogWrapper {

    private Project project;
    private LooseApplicationParameter parameter;
    private JTextPane textPane;

    @Getter
    private ExecutorService executorService;

    public LogLooseApplicationDialog(@Nullable Project project, LooseApplicationParameter parameter) {
        super(project, true, IdeModalityType.PROJECT);
        this.project = project;
        this.parameter = parameter;
        this.textPane = new JTextPane();
        this.textPane.setEditable(false);
        this.textPane.setFont(UIManager.getFont("Label.font"));
        this.myOKAction.setEnabled(false);

        this.init();

        execute();
    }

    public void execute() {
        //        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Runnable runnableTask = () -> {
            try {
                System.out.println(String.format("[%s]", Thread.currentThread()));
                LibertyConfigurationRepository repository = new LibertyConfigurationRepository();
                LibertyConfiguration libertyConfiguration = repository.load();
                LooseApplicationGenerate generate = new LooseApplicationGenerate(project, parameter, textPane);
                Path looseApplication = generate.createLooseApplication(true);
                Path serverXml = libertyConfiguration.getAbsolutePathServerXml();
                ManipulationLibertyServer manipulationLibertyServer = new ManipulationLibertyServer(serverXml.toString());
                String earName = looseApplication.getFileName().toString();

                EnterpriseApplication earFirst = manipulationLibertyServer.listApplications()
                        .stream()
                        .filter(ear -> ear.getId().equals(earName))
                        .findFirst().orElse(null);
                if (earFirst == null) {
                    EnterpriseApplication enterpriseApplication = new EnterpriseApplication();
                    enterpriseApplication.setId(earName);
                    enterpriseApplication.setName(earName);
                    enterpriseApplication.setLocation(earName);
                    manipulationLibertyServer.addApplication(enterpriseApplication);
                }
                parameter.setProjectEAR(null);
                parameter.setModules(null);
                parameter.setModulesInPackage(null);
                parameter = null;
                ToolBarComponent.getManager(project).executeRefresh();

                myOKAction.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

//        executorService = Executors.newSingleThreadExecutor();
//        executorService.execute(runnableTask);
//        SwingUtilities.invokeLater(runnableTask);

        ConfigurationComponent.getManager(project).getExecutor().execute(runnableTask);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new MigLayout("", "[]", "[][][]"));
        panel.setPreferredSize(new Dimension(700, 400));
        JScrollPane logScrollPane = new JBScrollPane(textPane);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(new JLabel("View Log:"), "grow, wrap");
        panel.add(logScrollPane, "grow, push, wrap");

        return panel;
    }

}
