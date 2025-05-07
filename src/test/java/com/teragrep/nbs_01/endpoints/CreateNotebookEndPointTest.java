package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateNotebookEndPointTest extends AbstractNotebookServerTest
{
    private Path testFileName = Paths.get("testFileName.zpln");
    public CreateNotebookEndPointTest(){
    }
    @AfterEach
    // Delete the notebook that was created by this test so that multiple tests can be run in succession.
    public void deleteTestNotebook(){
        Assertions.assertDoesNotThrow(()->{
            Path testFilePath = Paths.get(notebookDirectory().toString(), testFileName.toString());
            // Delete the test Notebook we create here if it already exists
            if(Files.exists(testFilePath)){
                Files.delete(testFilePath);
            }
        });
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
    // Assert that a simple HTTP request to /notebook/new endpoint results in a new file being saved on disk.
    public void httpCreateNotebookTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();
            Map<Integer, List<String>> response = makeHttpPOSTRequest("http://"+serverAddress()+"/notebook/new","TestTitle,"+testFileName);
            Assertions.assertTrue(response.get(200).get(0).toString().contains("Created notebook "));
            stopServer();
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketCreateNotebookTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            Map<Integer, List<String>> response = makeHttpPOSTRequest("http://"+serverAddress()+"/notebook/new","TestTitle,"+testFileName);
            Assertions.assertTrue(response.get(200).get(0).contains("Created notebook"));
            stopServer();
        });
    }
}
