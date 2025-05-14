package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateDirectoryEndPointTest extends AbstractNotebookServerTest
{
    private Path testParentDirectory = Paths.get(notebookDirectory().toString(),"my_folder_2A94M5J1D");
    public CreateDirectoryEndPointTest(){
    }
    @BeforeAll
    private void setUp(){
        copyFileRecursively(notebookResources().toFile(),notebookDirectory().toFile());
    }
    @AfterAll
    private void tearDown(){
        deleteFileRecursively(notebookDirectory().toFile());
    }
    @Test
    // Assert that a simple HTTP request to /notebook/newDirectory endpoint results in a new directory being saved on disk.
    public void httpCreateDirectoryTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();
            Map<Integer, List<String>> response = makeHttpPOSTRequest("http://"+serverAddress()+"/notebook/newDirectory","2A94M5J1D,created_directory");
            Assertions.assertTrue(response.get(200).get(0).toString().contains("Created directory "));
            String newDirectoryId = response.get(200).get(0).toString().split("Created directory ")[1];
            stopServer();
            Assertions.assertTrue(Files.exists(Paths.get(testParentDirectory.toString(),"created_directory_"+newDirectoryId)));
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketCreateDirectoryTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            Map<Integer, List<String>> response = makeHttpPOSTRequest("http://"+serverAddress()+"/notebook/newDirectory","2A94M5J1D,created_directory");
            Assertions.assertTrue(response.get(200).get(0).contains("Created directory"));
            String newDirectoryId = response.get(200).get(0).split("Created directory ")[1];
            stopServer();
            Assertions.assertTrue(Files.exists(Paths.get(testParentDirectory.toString(),"created_directory_"+newDirectoryId)));
        });
    }
}
