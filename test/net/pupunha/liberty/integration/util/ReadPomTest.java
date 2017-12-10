package net.pupunha.liberty.integration.util;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ReadPomTest {

    @Test
    public void getPackaging() throws Exception {
        Path path = Paths.get("/Users/willlobato/Projects/java/test/test-ejb/pom.xml");
        String packaging = ReadPom.getPackaging(path);
        System.out.println(packaging);
    }

}