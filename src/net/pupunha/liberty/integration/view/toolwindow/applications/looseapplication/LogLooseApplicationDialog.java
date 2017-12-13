package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import net.miginfocom.swing.MigLayout;
import net.pupunha.liberty.integration.configuration.LibertyConfiguration;
import net.pupunha.liberty.integration.configuration.LibertyConfigurationRepository;
import net.pupunha.liberty.integration.serverxml.EnterpriseApplication;
import net.pupunha.liberty.integration.serverxml.ManipulationLibertyServer;
import net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application.ToolBarComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class LogLooseApplicationDialog extends DialogWrapper {

    private Project project;
    private LooseApplicationParameter parameter;
    private JTextPane textPane = new JTextPane();
    private SwingWorker worker;

    public LogLooseApplicationDialog(@Nullable Project project, LooseApplicationParameter parameter) {
        super(project, true, IdeModalityType.PROJECT);
        this.project = project;
        this.parameter = parameter;
        this.textPane.setEditable(false);
        this.textPane.setFont(UIManager.getFont("Label.font"));

        this.init();

        this.worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    LibertyConfigurationRepository repository = new LibertyConfigurationRepository();
                    LibertyConfiguration libertyConfiguration = repository.load();
                    Path absolutePathApps = libertyConfiguration.getAbsolutePathApps();

                    LooseApplicationGenerate generate = new LooseApplicationGenerate(project, parameter, textPane);
                    Path pathFileLooseApplication = generate.getPathFileLooseApplication(absolutePathApps);
                    FileOutputStream outputStream = new FileOutputStream(pathFileLooseApplication.toFile());
                    Path looseApplication = generate.createLooseApplication(outputStream);

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

                    ToolBarComponent.getManager(project).executeRefresh();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        };
        worker.execute();

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
