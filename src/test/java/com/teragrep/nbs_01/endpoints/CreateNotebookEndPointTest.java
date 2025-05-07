package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.TestWebSocketClientEndpoint;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

            URL serverURL = new URL("http://"+serverAddress()+"/notebook/new");
            HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            byte[] bytes = ("TestTitle,"+ testFileName).getBytes();
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
                sb.append(line+"\n");
            }
            Assertions.assertEquals(200,status);
            Assertions.assertTrue(sb.toString().contains("Created notebook "));
            connection.disconnect();
            stopServer();
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketCreateNotebookTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            URI serverURI = URI.create("ws://"+serverAddress()+"/notebook/new");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient,serverURI);
            Assertions.assertEquals(1,webSocketClient.getOpenSessions().size());
            client.sendText("TestTitle,"+testFileName);
            // Read the WebSocket response and assert that we got the proper list of notebook IDs.
            long startTime = System.currentTimeMillis();
            while (client.receivedMessages().size() == 0 && (System.currentTimeMillis()-startTime) < webSocketTimeoutMs){
                // Wait until a message is received or a timeout is reached.
            }
            ArrayList<String> receivedMessages = client.receivedMessages();
            Assertions.assertEquals(1,receivedMessages.size());
            Assertions.assertTrue(receivedMessages.get(0).contains("Created notebook"));
            stopServer();
        });
    }
}
