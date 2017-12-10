package net.pupunha.liberty.integration.constants;

import java.util.Collection;

public final class MBeanConstants {

    /** ApplicationMBean name */
    public final static String MBEAN_APPLICATIONS = "WebSphere:service=com.ibm.websphere.application.ApplicationMBean,name=*";

    /** RuntimeUpdateNotificationMBean name */
    public final static String MBEAN_RUNTIME_UPDATE_NOTIFICATION = "WebSphere:name=com.ibm.websphere.runtime.update.RuntimeUpdateNotificationMBean";

    /** FileNotificationMBean name */
    public final static String MBEAN_FILE_NOTIFICATION = "WebSphere:service=com.ibm.ws.kernel.filemonitor.FileNotificationMBean";

    /** FileNotificationMBean method notifyFileChanges signature */
    public final static String[] MBEAN_FILE_NOTIFICATION_NOTIFYFILECHANGES_SIGNATURE = new String[] {
            Collection.class.getName(), Collection.class.getName(), Collection.class.getName() };

    /** ApplicationMBean name attributes */
    public static final String PID = "Pid";
    public static final String NAME = "name";
    public static final String STATE = "State";

    public static final Object[] ATTRIBUTES_APPLICATIONS = {
            PID.toUpperCase(),
            NAME.toUpperCase(),
            STATE.toUpperCase()
    };


}
