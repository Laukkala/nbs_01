package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeleteNotebookEndPointTest extends AbstractNotebookServerTest
{
    private final Path fileToDelete = Paths.get(notebookDirectory().toString(),"my_note3_2A94M5J3Z.zpln");
    @BeforeEach
    private void setUp(){
        copyFileRecursively(notebookResources().toFile(),notebookDirectory().toFile());
    }
    @AfterAll
    private void tearDown(){
        deleteFileRecursively(notebookDirectory().toFile());
    }
    @Test
    // Assert that a simple HTTP request to /notebook/delete endpoint results in a notebook being deleted
    public void httpDeleteTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();
            // Assert that the correct number of files exist
            Assertions.assertEquals(4,Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            Map<Integer, List<String>> response = makeHttpPOSTRequest("http://"+serverAddress()+"/notebook/delete","2A94M5J3Z");
            Assertions.assertEquals("Notebook deleted",response.get(200).get(0).toString());
            stopServer();
            // Assert that a file was deleted.
            Assertions.assertEquals(3,Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            // Assert that the correct file was deleted.
            Assertions.assertFalse(Files.exists(fileToDelete));
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketDeleteTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();
            // Assert that the correct number of files exist
            Assertions.assertEquals(4,Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            Map<Integer, List<String>> response = makeHttpPOSTRequest("http://"+serverAddress()+"/notebook/delete","2A94M5J3Z");
            Assertions.assertEquals("Notebook deleted",response.get(200).get(0));
            // Assert that a file was deleted.
            Assertions.assertEquals(3,Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            // Assert that the correct file was deleted.
        });
    }
}
