package net.pupunha.liberty.integration.jmx;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.management.ObjectName;

@Data
@AllArgsConstructor
public class Application {

    private String pid;
    private String name;
    private String state;
    private ObjectName objectName;

    public enum Operation {
        START("start"),
        STOP("stop"),
        RESTART("restart");
//        REFRESH("refresh");

        private String name;

        Operation(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
