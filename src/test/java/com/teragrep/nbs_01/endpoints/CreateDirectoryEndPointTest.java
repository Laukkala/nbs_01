package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.TestWebSocketClientEndpoint;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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

            URL serverURL = new URL("http://"+serverAddress()+"/notebook/newDirectory");
            HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            byte[] bytes = ("2A94M5J1D,created_directory").getBytes();
            System.out.println(new String(bytes,StandardCharsets.UTF_8));
            int length = bytes.length;

            connection.setFixedLengthStreamingMode(length);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            connection.connect();
            OutputStream output = connection.getOutputStream();
            output.write(bytes);

            int status = connection.getResponseCode();
            // Read the response received, and assert that we get the proper response indicating success.
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            Assertions.assertEquals(200,status);
            Assertions.assertTrue(sb.toString().contains("Created directory "));
            connection.disconnect();
            String newDirectoryId = sb.toString().split("Created directory ")[1];
            stopServer();
            Assertions.assertTrue(Files.exists(Paths.get(testParentDirectory.toString(),"created_directory_"+newDirectoryId)));
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketCreateDirectoryTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            URI serverURI = URI.create("ws://"+serverAddress()+"/notebook/newDirectory");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient,serverURI);
            Assertions.assertEquals(1,webSocketClient.getOpenSessions().size());
            client.sendText("2A94M5J1D,created_directory");
            // Read the WebSocket response and assert that we got the proper response indicating success.
            long startTime = System.currentTimeMillis();
            while (client.receivedMessages().size() == 0 && (System.currentTimeMillis()-startTime) < webSocketTimeoutMs){
                // Wait until a message is received or a timeout is reached.
            }
            ArrayList<String> receivedMessages = client.receivedMessages();
            Assertions.assertEquals(1,receivedMessages.size());
            Assertions.assertTrue(receivedMessages.get(0).contains("Created directory"));
            String newDirectoryId = receivedMessages.get(0).split("Created directory ")[1];
            stopServer();
            Assertions.assertTrue(Files.exists(Paths.get(testParentDirectory.toString(),"created_directory_"+newDirectoryId)));
        });
    }
}
