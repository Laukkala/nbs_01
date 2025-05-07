package com.teragrep.nbs_01.endpoints;

import com.google.common.io.Files;
import com.teragrep.nbs_01.AbstractNotebookServerTest;
import org.junit.jupiter.api.*;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UpdateParagraphEndpointTest extends AbstractNotebookServerTest {
    private final String testFileId = "2A94M5J1Z";
    private final Path testFilePath = Paths.get(notebookDirectory().toString(),"/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
    private final String testParagraphId = "20150703-133047_853701097";

    @BeforeEach
    private void setUp(){
        copyFileRecursively(notebookResources().toFile(),notebookDirectory().toFile());
    }
    @AfterAll
    private void tearDown(){
        deleteFileRecursively(notebookDirectory().toFile());
    }
    @Test
    public void httpUpdateNotebookTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();

            Map<Integer, List<String>> response = makeHttpPOSTRequest("http://"+serverAddress()+"/notebook/update",testFileId+","+testParagraphId+","+"testEditMessage");
            Assertions.assertTrue(response.get(200).get(0).toString().contains("Notebook edited successfully"));
            stopServer();
            // Assert that the message we wanted to edit can be found in the file.
            Assertions.assertTrue(Files.readLines(testFilePath.toFile(), Charset.defaultCharset()).get(0).contains("testEditMessage"));
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketUpdateTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();
            Map<Integer, List<String>> response = makeWebSocketRequest("ws://"+serverAddress()+"/notebook/update",testFileId+","+testParagraphId+","+"testEditMessage");
            Assertions.assertEquals("Notebook edited successfully",response.get(200).get(0));
            stopServer();
            // Assert that the message we wanted to edit can be found in the file.
            Assertions.assertTrue(Files.readLines(testFilePath.toFile(), Charset.defaultCharset()).get(0).contains("testEditMessage"));
        });
    }
}