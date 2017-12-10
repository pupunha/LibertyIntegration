package net.pupunha.liberty.integration.jmx;



import net.pupunha.liberty.integration.exception.JMXLibertyException;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static net.pupunha.liberty.integration.constants.MBeanConstants.*;
import static net.pupunha.liberty.integration.exception.ErrorCode.CONNECTION_ALREADY_CLOSED;
import static net.pupunha.liberty.integration.exception.ErrorCode.CONNECTION_NOT_ESTABLISHED;
import static net.pupunha.liberty.integration.exception.ErrorCode.URL_NOT_FOUND;

public class JMXLibertyConnector {

    private JMXConnector jmxConnector;
    private String url;

    public JMXLibertyConnector(String url) {
        this.url = url;
    }

    public void connect() throws JMXLibertyException {
        try {
            JMXServiceURL serviceUrl = new JMXServiceURL(url);
            jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
        } catch (MalformedURLException e) {
            throw new JMXLibertyException(URL_NOT_FOUND, e);
        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        }
    }

    public void disconnect() {
        if (jmxConnector != null) {
            try {
                jmxConnector.close();
            } catch (IOException e) {
                System.err.println(CONNECTION_ALREADY_CLOSED.getDescription());
            }
        }
    }

    public boolean isConnected() throws JMXLibertyException {
        try {
            String id = jmxConnector.getConnectionId();
            if (id != null) {
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public Set<ObjectName> getApplications() throws JMXLibertyException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            ObjectName applicationsMonitorMBeanName = new ObjectName(MBEAN_APPLICATIONS);
            return mbean.queryNames(applicationsMonitorMBeanName, null);
        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to connect on MBean "+ MBEAN_APPLICATIONS, e);
        }
    }

    public Application getApplication(ObjectName appMonitorObjectName) throws JMXLibertyException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            if (!mbean.isRegistered(appMonitorObjectName)) {
                throw new JMXLibertyException("MBean invoke request failed " + appMonitorObjectName.getCanonicalName() + " is not registered.");
            }
            String name = appMonitorObjectName.getKeyProperty(NAME);
            String state = mbean.getAttribute(appMonitorObjectName, STATE).toString();
            String pid = mbean.getAttribute(appMonitorObjectName, PID).toString();
            Application application = new Application(pid, name, state, appMonitorObjectName);
            return application;
        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to connect on MBean "+ appMonitorObjectName.getCanonicalName(), e);
        }
    }

    public MBeanServerConnection getMBeanServerConnection(ObjectName appMonitorObjectName) throws JMXLibertyException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            if (!mbean.isRegistered(appMonitorObjectName)) {
                throw new JMXLibertyException("MBean invoke request failed " + appMonitorObjectName.getCanonicalName() + " is not registered.");
            }
            return mbean;
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to Get MBeanServerConnection", e);
        }
    }

    public boolean invokeOperationApplication(MBeanServerConnection mbean, ObjectName appMonitorObjectName, Application.Operation operation) throws JMXLibertyException {
        try {
            mbean.invoke(appMonitorObjectName, operation.getName(), null, null);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to invoke operation " + operation.getName() +" on MBean "+ appMonitorObjectName.getCanonicalName(), e);
        }
        return true;
    }

    public ObjectName getMBeanRuntimeUpdateNotification() throws JMXLibertyException {
        try {
            ObjectName runtimeUpdateNotificationMBean = new ObjectName(MBEAN_RUNTIME_UPDATE_NOTIFICATION);
            return runtimeUpdateNotificationMBean;
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to connect on MBean "+ MBEAN_RUNTIME_UPDATE_NOTIFICATION, e);
        }
    }

    public void addNotification(NotificationListener notificationListener) throws JMXLibertyException {
        try {
            ObjectName runtimeUpdateNotificationMBean = getMBeanRuntimeUpdateNotification();
            MBeanServerConnection mBeanServerConnection = getMBeanServerConnection(runtimeUpdateNotificationMBean);
            mBeanServerConnection.addNotificationListener(runtimeUpdateNotificationMBean, notificationListener, null, null);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to add notification on MBean "+ MBEAN_RUNTIME_UPDATE_NOTIFICATION, e);
        }
    }

    public void removeNotification(NotificationListener notificationListener) throws JMXLibertyException {
        try {
            ObjectName runtimeUpdateNotificationMBean = getMBeanRuntimeUpdateNotification();
            MBeanServerConnection mBeanServerConnection = getMBeanServerConnection(runtimeUpdateNotificationMBean);
            mBeanServerConnection.removeNotificationListener(runtimeUpdateNotificationMBean, notificationListener);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to remove notification on MBean "+ MBEAN_RUNTIME_UPDATE_NOTIFICATION, e);
        }
    }

    public void notifyFileChange(File application) throws JMXLibertyException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            // Invoke FileNotificationMBean
            ObjectName fileMonitorMBeanName = new ObjectName(MBEAN_FILE_NOTIFICATION);

            if (!mbean.isRegistered(fileMonitorMBeanName)) {
                throw new JMXLibertyException("MBean invoke request failed " + MBEAN_FILE_NOTIFICATION + " is not registered.");
            }

            // Create a list of absolute paths of each file to be checked
            application.setLastModified(new Date().getTime());
            List<String> modifiedFilePaths = new ArrayList<>();
            modifiedFilePaths.add(application.getAbsolutePath());

            // Set MBean method notifyFileChanges parameters (createdFiles, modifiedFiles, deletedFiles)
            Object[] params = new Object[]{null, modifiedFilePaths, null};

            // Invoke FileNotificationMBean method notifyFileChanges
            mbean.invoke(fileMonitorMBeanName, "notifyFileChanges", params,
                    MBEAN_FILE_NOTIFICATION_NOTIFYFILECHANGES_SIGNATURE);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to notify file change "+ MBEAN_FILE_NOTIFICATION, e);
        }
    }

    public JMXConnector getJmxConnector() {
        return jmxConnector;
    }
}
