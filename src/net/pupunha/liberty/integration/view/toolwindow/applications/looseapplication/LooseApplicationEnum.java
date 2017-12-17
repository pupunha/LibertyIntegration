package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import lombok.Getter;
import lombok.Setter;

public enum LooseApplicationEnum {

    ARCHIVE("archive"),
    DIR("dir"),
    FILE("file")
    ;

    @Getter
    @Setter
    private String name;

    LooseApplicationEnum(String name) {
        this.name = name;
    }
}
