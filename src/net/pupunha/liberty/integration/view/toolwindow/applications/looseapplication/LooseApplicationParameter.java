package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import lombok.Data;

import java.nio.file.Path;
import java.util.List;

@Data
public class LooseApplicationParameter {

    private Path projectEAR;
    private List<Path> modules;

}
