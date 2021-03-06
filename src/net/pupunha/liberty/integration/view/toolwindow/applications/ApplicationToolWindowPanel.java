package net.pupunha.liberty.integration.view.toolwindow.applications;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import lombok.Getter;
import lombok.extern.java.Log;
import net.pupunha.liberty.integration.configuration.LibertyConfiguration;
import net.pupunha.liberty.integration.configuration.LibertyConfigurationRepository;
import net.pupunha.liberty.integration.constants.GeneralConstants;
import net.pupunha.liberty.integration.exception.LibertyConfigurationException;
import net.pupunha.liberty.integration.exception.PathNotFoundException;
import net.pupunha.liberty.integration.serverxml.EnterpriseApplication;
import net.pupunha.liberty.integration.serverxml.ManipulationLibertyServer;
import net.pupunha.liberty.integration.view.toolwindow.applications.configuration.ConfigurationAction;
import net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication.LooseApplicationAction;
import net.pupunha.liberty.integration.view.toolwindow.applications.node.ApplicationLocationTreeNode;
import net.pupunha.liberty.integration.view.toolwindow.applications.node.IconTreeNode;
import net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application.ComboBoxApplication;
import net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application.RestartApplicationAction;
import net.pupunha.liberty.integration.view.toolwindow.applications.toolbar.application.ToolBarComponent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.tree.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

import static net.pupunha.liberty.integration.constants.ProfileConstants.LOGS_STATE;
import static net.pupunha.liberty.integration.constants.ProfileConstants.SERVER_XML;

@Log
public class ApplicationToolWindowPanel extends SimpleToolWindowPanel implements Disposable {

    public static final String REFRESH = "Refresh";
    public static final String CLEAR_LOGS = "Clear Logs";
    private static final String SETTINGS = "Settings";
    private static final String RESTART_APPLICATION = "Restart Application";

    public static final String APPLICATIONS_VIEW_TOOLBAR = "ApplicationsViewToolbar";
    private static final String TARGET_IN_ARCHIVE = "targetInArchive";
    private static final String LOOSE_APPLICATION = "Loose Application";

    public static final String ENTERPRISE_APPLICATION = "enterpriseApplication";
    public static final String DEPENDENCIES = "Dependencies";

    private Project project;

    private DefaultTreeModel model;
    private Tree tree;
    private LibertyConfigurationRepository repository;
    private ManipulationLibertyServer manipulationLibertyServer;
    private Path dirApps;
    private Path serverXml;

    @Getter
    private RefreshToolBar refreshToolBar;

    public ApplicationToolWindowPanel(@NotNull Project project) {
        super(true, true);
        try {
            log.info("Construct Application Tool Window Panel");

            this.project = project;

            JPanel toolBarPanel = new JPanel(new GridLayout());
            log.info("Create Toolbar");
            DefaultActionGroup group = new DefaultActionGroup();
            this.refreshToolBar = new RefreshToolBar(REFRESH, REFRESH, AllIcons.Actions.Refresh);
            ToolBarComponent.getManager(project).setRefreshToolBar(refreshToolBar);
            group.add(this.refreshToolBar);
            group.addSeparator();
            group.add(new ClearLogsAction(CLEAR_LOGS, CLEAR_LOGS, GeneralConstants.ICON_CLEAR_LOGS));
            group.add(new LooseApplicationAction(LOOSE_APPLICATION, LOOSE_APPLICATION, AllIcons.RunConfigurations.Application));
            group.addSeparator();
            group.add(new ConfigurationAction(SETTINGS, SETTINGS, GeneralConstants.ICON_SETTINGS));
            group.addSeparator();
            group.add(new ComboBoxApplication());
            group.add(new RestartApplicationAction(RESTART_APPLICATION, RESTART_APPLICATION, GeneralConstants.ICON_ROCKET));

            toolBarPanel.add(ActionManager.getInstance().createActionToolbar(APPLICATIONS_VIEW_TOOLBAR, group, true).getComponent());

            this.setToolbar(toolBarPanel);

            log.info("Create Tree");
            this.model = new DefaultTreeModel(new DefaultMutableTreeNode());
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
            this.tree = new Tree(model);
            this.tree.setRootVisible(false);


            repository = new LibertyConfigurationRepository();
            log.info("Loading liberty configuration plugin...");
            LibertyConfiguration libertyConfiguration = repository.load();

            log.info("Server Name: "+libertyConfiguration.getServerName());
            if (StringUtils.isNotEmpty(libertyConfiguration.getServerName())) {
                this.dirApps = libertyConfiguration.getAbsolutePathApps();
                log.info("Path Apps: "+dirApps);
                this.tree.addMouseListener(new MouseAdapterTree(dirApps));
                this.tree.setCellRenderer(new TreeCellRenderer());

                this.serverXml = libertyConfiguration.getAbsolutePathServerXml();
                log.info("Path server.xml: "+serverXml);
                this.manipulationLibertyServer = new ManipulationLibertyServer(serverXml.toString());

                log.info("Loading Enterprise Applications in server.xml");
                loadTree(root, libertyConfiguration);

            } else {
                log.info("Create Root Tree Name when NOT CONFIGURATED");
                this.tree.setRootVisible(true);
                root.setUserObject("[SERVER NAME NOT CONFIGURATED]");
            }

            this.setContent(ScrollPaneFactory.createScrollPane(tree));

        } catch (Exception e) {
            Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
            e.printStackTrace();
        }
    }

    private void loadTree(DefaultMutableTreeNode root, LibertyConfiguration libertyConfiguration) throws Exception {
        loadDirectoryApps(root, libertyConfiguration);
        loadDirectoryDropins(root, libertyConfiguration);
        loadServerXml(root, libertyConfiguration);
    }

    private void loadDirectoryApps(DefaultMutableTreeNode root, LibertyConfiguration libertyConfiguration) {
        try {
            Path absolutePathApps = libertyConfiguration.getAbsolutePathApps();
            DefaultMutableTreeNode appsTreeNode = new IconTreeNode("apps", GeneralConstants.ICON_FOLDER);
            root.add(appsTreeNode);
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(absolutePathApps)) {
                for (Path path : directoryStream) {
                    IconTreeNode treeNode = new IconTreeNode(path.getFileName(), IconTreeNode.Type.APPS_FILE);
                    treeNode.setLocation(path);
                    appsTreeNode.add(treeNode);
                }
                directoryStream.close();
            } catch (IOException ex) {}
        } catch (PathNotFoundException e) {
            DefaultMutableTreeNode appsTreeNode = new IconTreeNode(String.format("apps [%s]", e.getMessage()));
            root.add(appsTreeNode);
        }
    }

    private void loadDirectoryDropins(DefaultMutableTreeNode root, LibertyConfiguration libertyConfiguration) {
        try {
            Path absolutePathDropins = libertyConfiguration.getAbsolutePathDropins();
            DefaultMutableTreeNode appsTreeNode = new IconTreeNode("dropins", GeneralConstants.ICON_FOLDER);
            root.add(appsTreeNode);
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(absolutePathDropins)) {
                for (Path path : directoryStream) {
                    appsTreeNode.add(new IconTreeNode(path.getFileName(), IconTreeNode.Type.DROPINS_FILE));
                }
                directoryStream.close();
            } catch (IOException ex) {}
        } catch (PathNotFoundException e) {
            DefaultMutableTreeNode appsTreeNode = new IconTreeNode(String.format("dropins [%s]", e.getMessage()));
            root.add(appsTreeNode);
        }
    }

    private void loadServerXml(DefaultMutableTreeNode root, LibertyConfiguration libertyConfiguration) throws Exception {
        log.info("Create Root Tree Name...");
        String serverXMLTreeNodeValue = String.format("%s [%s]", SERVER_XML, libertyConfiguration.getServerName());
        DefaultMutableTreeNode serverXMLTreeNode = new IconTreeNode(serverXMLTreeNodeValue, GeneralConstants.ICON_XML);
        root.add(serverXMLTreeNode);

        List<EnterpriseApplication> enterpriseApplications = manipulationLibertyServer.listApplications();
        if (CollectionUtils.isNotEmpty(enterpriseApplications)) {

            DefaultMutableTreeNode enterpriseApplicationTreeNode = new IconTreeNode(ENTERPRISE_APPLICATION, GeneralConstants.ICON_TAG);
            serverXMLTreeNode.add(enterpriseApplicationTreeNode);

            for (EnterpriseApplication application : enterpriseApplications) {
                application.setLocation(Paths.get(dirApps.toString(), application.getLocation()).toString());
                ApplicationLocationTreeNode applicationTreeNode = new ApplicationLocationTreeNode(application);
                applicationTreeNode.setIcon(GeneralConstants.ICON_TAG);

                IconTreeNode treeNode = new IconTreeNode(String.format("Name (%s)", application.getName()), AllIcons.Javaee.JavaeeAppModule);
                treeNode.setType(IconTreeNode.Type.ENTERPRISE_APPLICATION_NAME);
                applicationTreeNode.add(treeNode);

                if (Files.exists(applicationTreeNode.resolveFullLocation())) {
                    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = builderFactory.newDocumentBuilder();

                    try {
                        Document xmlDocument = builder.parse(applicationTreeNode.resolveFullLocation().toFile());
                        XPath xPath = XPathFactory.newInstance().newXPath();
                        String expression = "/archive/archive/archive";
                        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

                        if (nodeList.getLength() > 0) {
                            DefaultMutableTreeNode dependenciesTreeNode = new IconTreeNode(DEPENDENCIES, GeneralConstants.ICON_DEPENDENCIES_FOLDER);
                            applicationTreeNode.add(dependenciesTreeNode);

                            for (int i = 0; i < nodeList.getLength(); i++) {
                                Node item = nodeList.item(i);
                                String targetInArchive = item.getAttributes().getNamedItem(TARGET_IN_ARCHIVE).getNodeValue();
                                Path fileName = Paths.get(targetInArchive).getFileName();
                                dependenciesTreeNode.add(new IconTreeNode(fileName, GeneralConstants.ICON_LIBRARY));
                            }

                        } else {
                            applicationTreeNode.add(new IconTreeNode(DEPENDENCIES + " [DOESN'T HAVE]", GeneralConstants.ICON_DEPENDENCIES_FOLDER));
                        }

                    } catch (SAXException e) {
                        applicationTreeNode.setStatus("failure to read file");
                    }

                }
                enterpriseApplicationTreeNode.add(applicationTreeNode);
            }
        }
    }

    @Override
    public void dispose() {

    }

    public class RefreshToolBar extends AnAction {

        private static final String PLUGIN_CONFIGURATION_IS_NOT_VALID = "PLUGIN SETTINGS IS NOT VALID";

        public RefreshToolBar(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
            super(text, description, icon);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            log.info("Clicked button refresh");
            execute();
        }

        public void execute() {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
            root.removeAllChildren();
            try {
                LibertyConfiguration libertyConfiguration = repository.load();
                if (libertyConfiguration.isValidConfiguration()) {
                    dirApps = libertyConfiguration.getAbsolutePathApps();
                    tree.addMouseListener(new MouseAdapterTree(dirApps));
                    Path serverXml = libertyConfiguration.getAbsolutePathServerXml();

                    manipulationLibertyServer = new ManipulationLibertyServer(serverXml.toString());
                    loadTree(root, libertyConfiguration);
                    model.reload((TreeNode) model.getRoot());
                } else {
                    root.setUserObject(PLUGIN_CONFIGURATION_IS_NOT_VALID);
                    model.reload((TreeNode) model.getRoot());
                }
            } catch (PathNotFoundException e) {
                root.setUserObject(PLUGIN_CONFIGURATION_IS_NOT_VALID);
                model.reload((TreeNode) model.getRoot());
            } catch (Exception e) {
                Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
            }
        }
    }

    private class ClearLogsAction extends AnAction {

        private JTextPane textPane;
        private StyledDocument doc;

        public ClearLogsAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
            super(text, description, icon);
        }

        public void insertText(File file) {
            try {
                doc.insertString(doc.getLength(), file.toString().concat("\n"), null);
                textPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
            }
        }

        public DialogBuilder createDialog(Project project) {
            DialogBuilder builder = new DialogBuilder(project);
            textPane = new JTextPane();
            textPane.setEditable(false);
            textPane.setFont(UIManager.getFont("Label.font"));
            doc = textPane.getStyledDocument();

            JScrollPane editorScrollPane = new JBScrollPane(textPane);
            editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(600, 345));
            editorScrollPane.setMinimumSize(new Dimension(10, 10));

            builder.setTitle("Information delete");
            builder.setCenterPanel(editorScrollPane);
            return builder;
        }

        @Override
        public void actionPerformed(AnActionEvent event) {
            try {
                int confirmation = Messages.showOkCancelDialog(event.getProject(), "Do you want to confirm log cleanup?", "Confirmation", Messages.getQuestionIcon());
                if (confirmation == Messages.OK) {
                    LibertyConfiguration libertyConfiguration = repository.load();
                    Path absolutePathLogs = libertyConfiguration.getAbsolutePathLogs();

                    DialogBuilder dialog = createDialog(event.getProject());
                    dialog.showNotModal();

                    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(absolutePathLogs)) {
                        for (Path path : directoryStream) {
                            Files.walk(path, FileVisitOption.FOLLOW_LINKS)
                                    .sorted(Comparator.reverseOrder())
                                    .filter(p -> !p.toString().endsWith(LOGS_STATE))
                                    .map(Path::toFile)
                                    .peek(this::insertText)
                                    .forEach(File::delete);
                        }
                        directoryStream.close();
                    }
                }
            } catch (LibertyConfigurationException | IOException | PathNotFoundException e) {
                Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
            }
        }
    }

    private class MouseAdapterTree extends MouseAdapter {

        private Path dirApps;

        public MouseAdapterTree(Path dirApps) {
            super();
            this.dirApps = dirApps;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            if(selRow != -1) {
                if (e.getClickCount() == 2) {
                    if (selPath != null && selPath.getLastPathComponent() instanceof IconTreeNode) {
                        IconTreeNode treeNode = (IconTreeNode) selPath.getLastPathComponent();
                        if (treeNode != null && IconTreeNode.Type.ENTERPRISE_APPLICATION_NAME.equals(treeNode.getType())) {
                            ApplicationLocationTreeNode locationTreeNode = (ApplicationLocationTreeNode) treeNode.getParent();
                            if (locationTreeNode != null && Files.exists(locationTreeNode.resolveFullLocation())) {
                                VirtualFile file = VfsUtil.findFileByIoFile(locationTreeNode.resolveFullLocation().toFile(), true);
                                if (file != null) {
                                    new OpenFileDescriptor(project, file).navigate(true);
                                }
                            }
                        } else if (treeNode != null && IconTreeNode.Type.APPS_FILE.equals(treeNode.getType())) {
                            if (treeNode.getLocation() != null && Files.exists(treeNode.getLocation())) {
                                VirtualFile file = VfsUtil.findFileByIoFile(treeNode.getLocation().toFile(), true);
                                if (file != null) {
                                    new OpenFileDescriptor(project, file).navigate(true);
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    public class TreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof IconTreeNode) {
                IconTreeNode treeNode = (IconTreeNode) value;
                if (treeNode.getIcon() != null) {
                    setIcon(treeNode.getIcon());
                }
            }
            return this;
        }

    }

}
