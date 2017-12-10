package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import lombok.Data;

import java.nio.file.Path;
import java.util.List;

@Data
public class Parameter {

    private Path projectEAR;
    private List<Path> modules;

}
