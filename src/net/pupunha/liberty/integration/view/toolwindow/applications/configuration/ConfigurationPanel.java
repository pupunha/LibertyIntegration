package net.pupunha.liberty.integration.view.toolwindow.applications.configuration;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.project.Project;
import net.miginfocom.swing.MigLayout;
import net.pupunha.liberty.integration.configuration.LibertyConfiguration;
import net.pupunha.liberty.integration.configuration.LibertyConfigurationRepository;
import net.pupunha.liberty.integration.constants.GeneralConstants;
import net.pupunha.liberty.integration.constants.ProfileConstants;
import net.pupunha.liberty.integration.exception.LibertyConfigurationException;
import net.pupunha.liberty.integration.exception.LibertyIntegrationException;
import net.pupunha.liberty.integration.exception.PathNotFoundException;
import net.pupunha.liberty.integration.util.ComboBoxItem;
import net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application.SelectApplicationComponent;
import net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application.ToolBarComponent;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class ConfigurationPanel extends JPanel {

    private JTextField txtPackCurrent = new JTextField(30);
    private JXButton buttonChoosePackage = new JXButton(GeneralConstants.ICON_OPEN);
    private JTextField txtWlpUserDirectory = new JTextField(30);
    private JXButton buttonUserDirectory = new JXButton(GeneralConstants.ICON_OPEN);
    private JComboBox comboServerName = new JXComboBox();

    private DefaultComboBoxModel<ComboBoxItem> comboBoxModel = new DefaultComboBoxModel<>();
    private LibertyConfigurationRepository repository;
    private LibertyConfiguration configuration;
    private Project project;

    public ConfigurationPanel(Project project) throws Exception {
        super(new MigLayout());
        this.project = project;

//        txtPackCurrent.setEditable(false);
//        txtWlpUserDirectory.setEditable(false);

        add(new JLabel("Package Current"));
        add(txtPackCurrent, "growx");
        add(buttonChoosePackage, "grow, wrap");
        add(new JLabel("WLP User Directory:"));
        add(txtWlpUserDirectory, "growx");
        add(buttonUserDirectory, "grow, wrap");
        add(new JLabel("Server Name:"));
        add(comboServerName, "growx, wrap");

        init();

        buttonChoosePackage.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            if (StringUtils.isNotEmpty(txtPackCurrent.getText())) {
                fileChooser.setCurrentDirectory(Paths.get(txtPackCurrent.getText()).toFile());
            }
            int returnVal = fileChooser.showOpenDialog(getParent());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                txtPackCurrent.setText(fileChooser.getSelectedFile().toString().replace("\\", "/"));
            }
        });

        buttonUserDirectory.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            if (StringUtils.isNotEmpty(txtWlpUserDirectory.getText())) {
                fileChooser.setCurrentDirectory(Paths.get(txtWlpUserDirectory.getText()).toFile());
            }
            int returnVal = fileChooser.showOpenDialog(getParent());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                txtWlpUserDirectory.setText(fileChooser.getSelectedFile().toString().replace("\\", "/"));

                ComboBoxItem selectedItem = (ComboBoxItem) comboServerName.getSelectedItem();
                String profileSelected = selectedItem != null ? selectedItem.getKey() : null;
                populateComboBox(txtWlpUserDirectory.getText(), profileSelected);
            }
        });

        txtWlpUserDirectory.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                ComboBoxItem selectedItem = (ComboBoxItem) comboServerName.getSelectedItem();
                String profileSelected = selectedItem != null ? selectedItem.getKey() : null;
                populateComboBox(txtWlpUserDirectory.getText(), profileSelected);
            }
        });

    }

    private void init() throws Exception {
        repository = new LibertyConfigurationRepository();
        configuration = repository.load();
        comboServerName.setModel(comboBoxModel);
        txtWlpUserDirectory.setText(configuration.getWlpUserDir());
        txtPackCurrent.setText(configuration.getPackCurrent());
        populateComboBox(configuration.getWlpUserDir(), configuration.getServerName());

    }

    private void refreshComboApplication(LibertyConfiguration configuration) throws PathNotFoundException, LibertyConfigurationException {
        SelectApplicationComponent applicationComponent = SelectApplicationComponent.getManager(project);
        if (StringUtils.isNotEmpty(configuration.getServerName())) {
            Map<String, File> applications = configuration.getApplications();
            applicationComponent.setApplications(applications);

            configuration.setAppComboSelected("");
            repository.store(configuration);
            applicationComponent.clearSelectApplication();

        } else {
            configuration.setAppComboSelected("");
            repository.store(configuration);
            applicationComponent.clearSelectApplication();
        }
    }

    public void saveConfiguration() throws LibertyIntegrationException {
        LibertyConfiguration configuration = new LibertyConfiguration();
        configuration.setWlpUserDir(txtWlpUserDirectory.getText());
        configuration.setPackCurrent(txtPackCurrent.getText());
        ComboBoxItem comboBoxItem = ((ComboBoxItem)comboServerName.getSelectedItem());
        if(comboBoxItem != null) {
            configuration.setServerName(comboBoxItem.getKey());
        }
        repository.store(configuration);
        refreshComboApplication(configuration);
        ToolBarComponent.getManager(project).executeRefresh();
    }

    private void populateComboBox(String wlpUserDirStr, String profileUseStr) {
        comboBoxModel.removeAllElements();
        if(StringUtils.isNotEmpty(wlpUserDirStr) && Files.exists(Paths.get(wlpUserDirStr))) {
            File wlpUserDir = new File(wlpUserDirStr);
            comboServerName.setEnabled(true);
            final String servers = wlpUserDir.getAbsolutePath().concat(ProfileConstants.SERVERS);
            File[] listServers = new File(servers).listFiles(pathname -> !pathname.getName().contains("."));

            if(listServers != null) {
                for(File server : listServers) {
                    ComboBoxItem comboBoxItem = new ComboBoxItem(server.getName(), server.getAbsolutePath());
                    comboBoxModel.addElement(comboBoxItem);
                }
                if(profileUseStr != null) {
                    ComboBoxItem comboBoxItem = new ComboBoxItem(profileUseStr);
                    comboServerName.setSelectedItem(comboBoxItem);
                }
            } else {
                comboServerName.setEnabled(false);
            }
        } else {
            comboServerName.setEnabled(false);
        }
    }

}
