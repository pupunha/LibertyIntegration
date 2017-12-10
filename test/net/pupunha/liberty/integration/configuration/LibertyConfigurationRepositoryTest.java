package net.pupunha.liberty.integration.configuration;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class LibertyConfigurationRepositoryTest {

    private static final String FILE_TEST_PROPERTIES = "fileTest.properties";
    private LibertyConfigurationRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = new LibertyConfigurationRepository();
        repository.setFileConfigurationName(FILE_TEST_PROPERTIES);
    }

    @Test
    public void load() throws Exception {
        LibertyConfiguration libertyConfiguration = repository.load();
        System.out.println(libertyConfiguration);
    }

    @Test
    public void store() throws Exception {

    }

}