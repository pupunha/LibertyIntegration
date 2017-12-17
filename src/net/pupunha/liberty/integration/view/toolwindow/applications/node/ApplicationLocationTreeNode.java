package net.pupunha.liberty.integration.view.toolwindow.applications.node;

import lombok.Getter;
import lombok.Setter;
import net.pupunha.liberty.integration.serverxml.EnterpriseApplication;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationLocationTreeNode extends IconTreeNode {

    private EnterpriseApplication enterpriseApplication;

    @Getter
    @Setter
    private String status;

    public ApplicationLocationTreeNode(EnterpriseApplication enterpriseApplication) {
        super();
        this.enterpriseApplication = enterpriseApplication;
    }

    public ApplicationLocationTreeNode(EnterpriseApplication enterpriseApplication, String status) {
        super();
        this.enterpriseApplication = enterpriseApplication;
        this.status = status;
    }


    public Path resolveFullLocation() {
        return Paths.get(enterpriseApplication.getLocation().concat(".xml"));
    }

    @Override
    public String toString() {
        if (Files.exists(resolveFullLocation())) {
            if (StringUtils.isNotEmpty(status)) {
                return String.format("%s (location: %s [%s])", enterpriseApplication.getId(), resolveFullLocation().getFileName(), status);
            } else {
                return String.format("%s (location: %s)", enterpriseApplication.getId(), resolveFullLocation().getFileName());
            }
        } else {
            return String.format("%s (location: %s [NOT EXIST])", enterpriseApplication.getId(), resolveFullLocation().getFileName());
        }
    }

}
