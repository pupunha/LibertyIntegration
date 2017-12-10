package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import net.pupunha.liberty.integration.constants.MavenConstants;
import net.pupunha.liberty.integration.util.ComboBoxItem;
import net.pupunha.liberty.integration.util.ReadPom;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class LooseApplicationPanel extends JPanel {

    private static final String SELECT = "-- SELECT --";

    @Getter private JXComboBox comboEnterpriseApplication = new JXComboBox();
    @Getter private DefaultComboBoxModel<ComboBoxItem> comboBoxModel = new DefaultComboBoxModel<>();
    @Getter private JList listProjectsLeft = new JXList(new DefaultListModel());
    @Getter private JList listProjectsRight = new JXList(new DefaultListModel());
    @Getter private JButton buttonAdd = new JButton(">");
    @Getter private JButton buttonAddAll = new JButton(">>");
    @Getter private JButton buttonRemove = new JButton("<");
    @Getter private JButton buttonRemoveAll = new JButton("<<");
//    @Getter private JButton buttonCancel = new JButton("Cancel");
//    @Getter private JButton buttonOK = new JButton("OK");

    private Project project;

    public LooseApplicationPanel(Project project) {
        super(new MigLayout("", "", "[][][][]"));
        this.project = project;

        this.add(new JLabel("Enterprise Application:"), "growx, wrap");
        this.add(comboEnterpriseApplication, "pushx, growx, span, wrap");
        this.add(new JLabel("Projects to Add:"), "gap 1px");
        this.add(new JLabel("Added Projects:"), "skip 1, wrap");

        this.add(new JBScrollPane(listProjectsLeft), "sg a, grow, push");

        JPanel panelButtons = new JPanel(new MigLayout("", "[]", "[][][][]"));
        panelButtons.add(buttonAdd, "sg b, wrap");
        panelButtons.add(buttonAddAll, "sg b, wrap");
        panelButtons.add(buttonRemove, "sg b, wrap");
        panelButtons.add(buttonRemoveAll, "sg b, wrap");
        this.add(panelButtons);

        this.add(new JBScrollPane(listProjectsRight), "sg a, grow, push, wrap");

//        BUTTONS
//        this.add(buttonCancel, "newline 20px, span, split 2, right");
//        this.add(buttonOK, "wrap");

        setEnableButtons(false);

        comboEnterpriseApplication.setModel(comboBoxModel);
        comboBoxModel.addElement(new ComboBoxItem(SELECT));
        if (project != null) {
            List<Path> paths = listModulesEAR();
            for (Path path : paths) {
                comboBoxModel.addElement(new ComboBoxItem(path.getFileName().toString(), path.toString()));
            }
        }

        comboEnterpriseApplication.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ComboBoxItem selectedItem = (ComboBoxItem) comboEnterpriseApplication.getSelectedItem();
                assert selectedItem != null;
                if (StringUtils.isNotEmpty(selectedItem.getValue())) {

                    DefaultListModel listModelLeft = (DefaultListModel) listProjectsLeft.getModel();
                    listModelLeft.removeAllElements();

                    List<Path> modulesInPackage = listModulesInPackage();
                    if (modulesInPackage.size() > 0) {
                        setEnableButtons(true);
                        modulesInPackage.forEach(p -> listModelLeft.addElement(p.getFileName()));
                    }

                    DefaultListModel listModelRight = (DefaultListModel) listProjectsRight.getModel();
                    listModelRight.removeAllElements();

                } else {
                    DefaultListModel listModelLeft = (DefaultListModel) listProjectsLeft.getModel();
                    listModelLeft.removeAllElements();
                    DefaultListModel listModelRight = (DefaultListModel) listProjectsRight.getModel();
                    listModelRight.removeAllElements();
                    setEnableButtons(false);
                }
            }
        });

        buttonAdd.addActionListener(e -> {
            DefaultListModel listModelLeft = (DefaultListModel) listProjectsLeft.getModel();
            DefaultListModel listModelRight = (DefaultListModel) listProjectsRight.getModel();
            Object selectedValue = listProjectsLeft.getSelectedValue();
            listModelRight.addElement(selectedValue);
            listModelLeft.removeElement(selectedValue);
        });

        buttonAddAll.addActionListener(e -> {
            DefaultListModel listModelLeft = (DefaultListModel) listProjectsLeft.getModel();
            DefaultListModel listModelRight = (DefaultListModel) listProjectsRight.getModel();
            for(int i = 0; i<listProjectsLeft.getModel().getSize(); i++) {
                Object elementAt = listProjectsLeft.getModel().getElementAt(i);
                listModelRight.addElement(elementAt);
            }
            listModelLeft.removeAllElements();
        });

        buttonRemove.addActionListener(e -> {
            DefaultListModel listModelLeft = (DefaultListModel) listProjectsLeft.getModel();
            DefaultListModel listModelRight = (DefaultListModel) listProjectsRight.getModel();
            Object selectedValue = listProjectsRight.getSelectedValue();
            listModelLeft.addElement(selectedValue);
            listModelRight.removeElement(selectedValue);
        });

        buttonRemoveAll.addActionListener(e -> {
            DefaultListModel listModelLeft = (DefaultListModel) listProjectsLeft.getModel();
            DefaultListModel listModelRight = (DefaultListModel) listProjectsRight.getModel();
            for(int i = 0; i<listProjectsRight.getModel().getSize(); i++) {
                Object elementAt = listProjectsRight.getModel().getElementAt(i);
                listModelLeft.addElement(elementAt);
            }
            listModelRight.removeAllElements();
        });


    }

    private void setEnableButtons(boolean enable) {
        buttonAdd.setEnabled(enable);
        buttonAddAll.setEnabled(enable);
        buttonRemove.setEnabled(enable);
        buttonRemoveAll.setEnabled(enable);
    }

    public List<Path> listModulesInPackage() {
        Collection<Module> modulesOfType = ModuleUtil.getModulesOfType(project, StdModuleTypes.JAVA);

        List<Path> collect = modulesOfType.stream()
                .map(this::convertToPath)
                .map(Path::getParent)
                .filter(path -> Files.exists(Paths.get(path.toString(), MavenConstants.POM_XML)))
                .map(p -> p.resolve(MavenConstants.POM_XML))
                .filter(p -> ReadPom.getPackaging(p).equals(MavenConstants.JAR))
                .map(Path::getParent)
                .collect(Collectors.toList());

        collect.add(Paths.get("other-jar"));

        return collect;
    }

    public List<Path> listModulesEAR() {
        Collection<Module> modulesOfType = ModuleUtil.getModulesOfType(project, StdModuleTypes.JAVA);
        return modulesOfType.stream()
                .map(this::convertToPath)
                .map(Path::getParent)
                .filter(path -> Files.exists(Paths.get(path.toString(), MavenConstants.POM_XML)))
                .map(p -> p.resolve(MavenConstants.POM_XML))
                .filter(p -> ReadPom.getPackaging(p).equals(MavenConstants.EAR))
                .map(Path::getParent)
                .collect(Collectors.toList());
    }

    public Path convertToPath(Module module) {
        return Paths.get(module.getModuleFilePath());
    }

    public static void main(String args[]) {
//        DialogBuilder builder = new DialogBuilder();
        JFrame frame = new JFrame("Teste");
        frame.setContentPane(new LooseApplicationPanel(null));
        frame.setSize(new Dimension(500, 400));
//        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
//        builder.show();
    }

}
