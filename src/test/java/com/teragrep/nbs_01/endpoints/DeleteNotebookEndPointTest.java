package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.TestWebSocketClientEndpoint;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

            // Verify that correct number of files exist, and that the file to be deleted exists.
            Assertions.assertEquals(4,Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            Assertions.assertTrue(Files.exists(fileToDelete));

            URL serverURL = new URL("http://"+serverAddress()+"/notebook/delete");
            HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            byte[] bytes = URLEncoder.encode("2A94M5J3Z", StandardCharsets.UTF_8).getBytes();
            int length = bytes.length;

            connection.setFixedLengthStreamingMode(length);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            connection.connect();
            OutputStream output = connection.getOutputStream();
            output.write(bytes);

            int status = connection.getResponseCode();
            // Read the response received, and assert that we have a list of notebook IDs matching files saved in the notebook directory.
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            Assertions.assertEquals(200,status);
            Assertions.assertEquals("Notebook deleted",sb.toString());
            connection.disconnect();
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

            // Verify that correct number of files exist, and that the file to be deleted exists.
            Assertions.assertEquals(4,Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            Assertions.assertTrue(Files.exists(fileToDelete));

            URI serverURI = URI.create("ws://"+serverAddress()+"/notebook/delete");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient,serverURI);
            Assertions.assertEquals(1,webSocketClient.getOpenSessions().size());
            client.sendText("2A94M5J3Z");
            long startTime = System.currentTimeMillis();
            while (client.receivedMessages().size() == 0 && (System.currentTimeMillis()-startTime) < webSocketTimeoutMs){
                // Wait until a message is received or a timeout is reached.
            }
            // Read the WebSocket response and assert that we got the proper response.
            ArrayList<String> receivedMessages = client.receivedMessages();
            Assertions.assertFalse(Files.exists(fileToDelete));
            webSocketClient.close();
            stopServer();
            Assertions.assertEquals("Notebook deleted",receivedMessages.get(0));
            // Assert that a file was deleted.
            Assertions.assertEquals(3,Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            // Assert that the correct file was deleted.
        });
    }
}
