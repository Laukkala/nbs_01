package com.teragrep.nbs_01;

import com.teragrep.nbs_01.endpoints.directory.FindDirectoryEndPoint;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    private final int serverPort = 8080;
    private final Path notebookDir = Paths.get("src/test/resources/notebook");
    @Test
    void serverPortTest() {
        Assertions.assertDoesNotThrow(()->{
            Configuration configuration = new Configuration(notebookDir,serverPort);
            assertEquals(serverPort,configuration.serverPort());
        });
    }

    @Test
    void notebookDirectoryTest() {
        Assertions.assertDoesNotThrow(()->{
            Configuration configuration = new Configuration(notebookDir,serverPort);
            assertEquals(notebookDir,configuration.notebookDirectory());
        });
    }


    @Test
    public void testContract() {
        EqualsVerifier.forClass(Configuration.class).verify();
    }
}