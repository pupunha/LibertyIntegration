package net.pupunha.liberty.integration.view.toolwindow.applications;

import lombok.Getter;

import javax.swing.tree.DefaultMutableTreeNode;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApplicationLocationTreeNode extends DefaultMutableTreeNode {

    @Getter
    private String location;

    private Path dirApps;

    public ApplicationLocationTreeNode(Path dirApps, String location) {
        super();
        this.dirApps = dirApps;
        this.location = location;
    }

    public Path resolveFullLocation() {
        return dirApps.resolve(location.concat(".xml"));
    }

    @Override
    public String toString() {
        if (Files.exists(resolveFullLocation())) {
            return String.format("location: %s", location);
        } else {
            return String.format("location: %s [NOT EXIST]", location);
        }
    }

}
