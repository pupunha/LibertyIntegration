package net.pupunha.liberty.integration.view.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import net.pupunha.liberty.integration.configuration.LibertyConfiguration;
import net.pupunha.liberty.integration.configuration.LibertyConfigurationRepository;
import net.pupunha.liberty.integration.constants.GeneralConstants;
import net.pupunha.liberty.integration.constants.GeneralConstants.TableConstants;
import net.pupunha.liberty.integration.exception.JMXLibertyException;
import net.pupunha.liberty.integration.exception.LibertyConfigurationException;
import net.pupunha.liberty.integration.exception.PathNotFoundException;
import net.pupunha.liberty.integration.jmx.Application;
import net.pupunha.liberty.integration.jmx.Application.Operation;
import net.pupunha.liberty.integration.jmx.JMXLibertyConnector;
import net.pupunha.liberty.integration.util.TableUtil;
import org.jetbrains.annotations.NotNull;

import javax.management.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;

import static net.pupunha.liberty.integration.constants.GeneralConstants.TableConstants.*;

public class ServerStatusToolWindow implements ToolWindowFactory {

    private static final String CONNECT_TEXT = "Connect";
    private static final String DISCONNECT_TEXT = "Disconnect";

    private static final String NOTIFICATION_APPLICATION_INSTALL_CALLED = "ApplicationsInstallCalled";

    private JPanel contentPane;
    private JButton startButton;
    private JButton stopButton;
    private JButton restartButton;

    private JTable table;
    private JScrollPane scrollPane;
    private JButton connectButton;

    private DefaultTableModel model;
    private Project project;

    private LibertyConfigurationRepository repository;
    private LibertyConfiguration libertyConfiguration;
    private JMXLibertyConnector libertyConnector;

//    private ObjectName runtimeUpdateNotificationMBean;
//    private MBeanServerConnection mBeanServerConnection;
    private ApplicationNotificationListener applicationNotificationListener;

    public ServerStatusToolWindow() {
        applicationNotificationListener = new ApplicationNotificationListener();

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                execute(Operation.START);
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                execute(Operation.STOP);
            }
        });
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                execute(Operation.RESTART);
            }
        });

        connectButton.setPreferredSize(new Dimension(120, (int)connectButton.getPreferredSize().getHeight()));
        connectButton.addActionListener(e -> {
            try {
                if(CONNECT_TEXT.equals(connectButton.getText())) {
                    libertyConnector = new JMXLibertyConnector(repository.load().getAbsolutePathJmxLocalAddress());
                    libertyConnector.connect();
                    applicationNotificationListener = new ApplicationNotificationListener();
                    libertyConnector.addNotification(applicationNotificationListener);
                    refreshTable();
                    connectBehavior();
                } else {

//                    disconnectBehavior();

                }
            } catch (JMXLibertyException | LibertyConfigurationException | PathNotFoundException ex) {
                Messages.showMessageDialog(project, ex.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
            }
        });
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;
        this.repository = new LibertyConfigurationRepository();

        setupTable();
        disconnectBehavior();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(contentPane, "", false);
        toolWindow.getContentManager().addContent(content);

        try {
            if (libertyConnector != null && libertyConnector.isConnected()) {
                libertyConnector.disconnect();
            }
        } catch (Exception e) {
            Messages.showMessageDialog(project, e.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
        }

    }

    private void setupTable() {
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn(ATTRIBUTE_PID);
        model.addColumn(ATTRIBUTE_APPLICATION);
        model.addColumn(ATTRIBUTE_STATE);
        model.addColumn(ATTRIBUTE_OBJECT_NAME);
        table.setModel(model);

        TableUtil.hideTableColumn(table, 3);

        table.setRowHeight(22);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPane.setViewportView(table);
    }

    private void connect() {
        try {
            LibertyConfiguration libertyConfiguration = repository.load();
            libertyConnector = new JMXLibertyConnector(libertyConfiguration.getAbsolutePathJmxLocalAddress());
            if (!libertyConnector.isConnected()) {
                libertyConnector.connect();
            }
        } catch (Exception ex) {
            Messages.showMessageDialog(project, ex.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
        }
    }

    private void connectBehavior() {
//        if(execute(project, Operation.REFRESH)) {
            connectButton.setText(DISCONNECT_TEXT);
            startButton.setEnabled(true);
            stopButton.setEnabled(true);
            restartButton.setEnabled(true);

//            try {
//                runtimeUpdateNotificationMBean = libertyConnector.getMBeanRuntimeUpdateNotification();
//                mBeanServerConnection = libertyConnector.getMBeanServerConnection(runtimeUpdateNotificationMBean);
//                applicationNotificationListener = new ApplicationNotificationListener();
//                mBeanServerConnection.addNotificationListener(runtimeUpdateNotificationMBean, applicationNotificationListener, null, null);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

//        }
    }

    private void disconnectBehavior() {
//        try {
//            if (mBeanServerConnection != null) {
//                mBeanServerConnection.removeNotificationListener(
//                        runtimeUpdateNotificationMBean, applicationNotificationListener);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        libertyConnector.disconnect();
        connectButton.setText(CONNECT_TEXT);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        restartButton.setEnabled(false);
        model.setRowCount(0);
    }


    private void listApplications() {
        try {

            LibertyConfiguration libertyConfiguration = repository.load();
            libertyConnector = new JMXLibertyConnector(libertyConfiguration.getAbsolutePathJmxLocalAddress());

        } catch (Exception e) {

        }
    }

    private boolean execute(Operation operation) {
        try {
            LibertyConfiguration libertyConfiguration = repository.load();
            libertyConnector = new JMXLibertyConnector(libertyConfiguration.getAbsolutePathJmxLocalAddress());
            libertyConnector.connect();

//            if (!Operation.REFRESH.equals(operation)) {
            int row = table.getSelectedRow();
            if (row > -1) {
                int column = table.getColumn(ATTRIBUTE_OBJECT_NAME).getModelIndex();
                ObjectName applicationObjectName = (ObjectName) model.getValueAt(row, column);
                SwingWorker<Boolean, Void> worker = invokeOperation(project, row, operation, libertyConnector, applicationObjectName);
                worker.execute();
            } else {
                Messages.showMessageDialog(project, "Select the application", GeneralConstants.INFORMATION, Messages.getInformationIcon());
                return false;
            }
//            }

            applicationNotificationListener = new ApplicationNotificationListener();
            libertyConnector.addNotification(applicationNotificationListener);

            refreshTable();

        } catch (Exception e) {
            Messages.showMessageDialog(project, e.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
            try {
                if (libertyConnector != null && libertyConnector.isConnected()) {
                    disconnectBehavior();
                }
            } catch (JMXLibertyException e1) {
                e1.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private void refreshTable() throws JMXLibertyException {
        Set<ObjectName> applications = libertyConnector.getApplications();
        model.setRowCount(0);
        for(ObjectName objectName : applications) {
            Application application = libertyConnector.getApplication(objectName);
            model.addRow(new Object[]{application.getPid(), application.getName(), application.getState(), application.getObjectName()});
        }
    }

    @NotNull
    private SwingWorker<Boolean, Void> invokeOperation(@NotNull Project project, int row, Operation operation, JMXLibertyConnector jmxLibertyConnector, ObjectName objectName) {
        return new SwingWorker<Boolean, Void>() {

            private MBeanServerConnection mBeanServerConnection;
            private ApplicationStateNotificationListener listener = new ApplicationStateNotificationListener(row);

            @Override
            public Boolean doInBackground() {
                try {
                    mBeanServerConnection = jmxLibertyConnector.getMBeanServerConnection(objectName);
                    AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
                    filter.enableAttribute(TableConstants.ATTRIBUTE_STATE);
                    mBeanServerConnection.addNotificationListener(objectName, listener, filter, null);

                    jmxLibertyConnector.invokeOperationApplication(mBeanServerConnection, objectName, operation);
                    return true;
                } catch (Exception e) {
                    Messages.showMessageDialog(project, e.getMessage(),
                            GeneralConstants.ERROR, Messages.getErrorIcon());
                    return false;
                }
            }
            @Override
            public void done() {
                try {
                    if (get()) {
                        mBeanServerConnection.removeNotificationListener(objectName, listener);
                    }
                } catch (Exception ex) {
                    Messages.showMessageDialog(project, ex.getMessage(),
                            GeneralConstants.ERROR, Messages.getErrorIcon());
                }
            }
        };
    }

    protected class ApplicationStateNotificationListener implements NotificationListener {

        private int rowEffected;

        public ApplicationStateNotificationListener(int rowEffected) {
            this.rowEffected = rowEffected;
        }

        public int getRowEffected() {
            return rowEffected;
        }

        public void handleNotification(Notification notification, Object obj) {
            if(notification instanceof AttributeChangeNotification) {
                AttributeChangeNotification attributeChange =
                        (AttributeChangeNotification) notification;
                int columnEffected = table.getColumn(attributeChange.getAttributeName()).getModelIndex();
                model.setValueAt(attributeChange.getNewValue(), getRowEffected(), columnEffected);
            }
        }
    }

    protected class ApplicationNotificationListener implements NotificationListener {
        @Override
        public void handleNotification(Notification notification, Object handback) {
            if (notification.getUserData() instanceof Map) {
                Map userData = (Map) notification.getUserData();
                if (NOTIFICATION_APPLICATION_INSTALL_CALLED.equals(userData.get("name"))) {
                    try {
                        refreshTable();
                    } catch (JMXLibertyException e) {
                        Messages.showMessageDialog(project, e.getMessage(),
                                GeneralConstants.ERROR, Messages.getErrorIcon());
                    }
                }
            }
        }
    }

}
