package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import org.junit.Test;

import static org.junit.Assert.*;

public class LooseApplicationGenerateTest {

    @Test
    public void findRepositoryLocal() throws Exception {

        String repositoryLocal = LooseApplicationGenerate.findRepositoryLocal();
        System.out.println(repositoryLocal);

    }

}