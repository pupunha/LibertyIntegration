package net.pupunha.liberty.integration.constants;

import com.intellij.openapi.util.IconLoader;
import lombok.experimental.UtilityClass;

import javax.swing.*;

public class GeneralConstants {

    public static final Icon ICON_ROCKET = IconLoader.getIcon("/rocket-16.png");
    public static final Icon ICON_APPLICATION = IconLoader.getIcon("/application-16.png");
    public static final Icon ICON_CLEAR_LOGS = IconLoader.getIcon("/trash-16.png");
    public static final Icon ICON_OPEN = IconLoader.getIcon("/openProject.png");

    public static final String ERROR = "Error";
    public static final String INFORMATION = "Information";

    public static class TableConstants {

        public static final String ATTRIBUTE_PID = "Pid";
        public static final String ATTRIBUTE_APPLICATION = "Application Name";
        public static final String ATTRIBUTE_STATE = "State";
        public static final String ATTRIBUTE_OBJECT_NAME = "ObjectName";

    }

}
