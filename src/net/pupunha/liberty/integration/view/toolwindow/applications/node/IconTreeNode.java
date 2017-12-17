package net.pupunha.liberty.integration.view.toolwindow.applications.node;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.nio.file.Path;

public class IconTreeNode extends DefaultMutableTreeNode {

    @Getter
    @Setter
    private Icon icon;

    @Getter
    @Setter
    private Path location;

    @Getter
    @Setter
    private Type type;

    public enum Type {
        SERVER_XML,
        ENTERPRISE_APPLICATION_NAME,
        APPS,
        APPS_FILE,
        DROPPINS,
        DROPINS_FILE;
    }

    public IconTreeNode(Object userObject, Icon icon) {
        super(userObject);
        this.icon = icon;
    }

    public IconTreeNode(Object userObject) {
        super(userObject);
    }

    public IconTreeNode(Object userObject, Type type) {
        super(userObject);
        this.type = type;
    }

    public IconTreeNode() {
        super();
    }
}
