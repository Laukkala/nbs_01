package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MoveNotebookEndPointTest extends AbstractNotebookServerTest
{
    private final String notebookId = "2A94M5J4Z";
    private final String directoryId = "2A94M5J2D";
    @BeforeEach
    private void setUp(){
        copyFileRecursively(notebookResources().toFile(),notebookDirectory().toFile());
    }
    @AfterAll
    private void tearDown(){
        deleteFileRecursively(notebookDirectory().toFile());
    }
    @Test
    // Assert that a simple HTTP request to /notebook/list endpoint results in a list of notebook IDs
    public void httpMoveTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();
            Map<Integer, List<String>> response = makeHttpPOSTRequest("http://"+serverAddress()+"/notebook/move",notebookId+","+directoryId);
            Assertions.assertEquals("Moved notebook "+notebookId,response.get(200).get(0).toString());
            stopServer();
            Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(),"my_folder_2A94M5J1D","my_second_folder_"+directoryId,"my_note4_"+notebookId+".zpln")));
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketMoveTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();
            Map<Integer,List<String>> response = makeWebSocketRequest("ws://"+serverAddress()+"/notebook/move",notebookId+","+directoryId);
            Assertions.assertEquals("Moved notebook "+notebookId,response.get(200).get(0));
            stopServer();
            Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(),"my_folder_2A94M5J1D","my_second_folder_"+directoryId,"my_note4_"+notebookId+".zpln")));
        });
    }
}
