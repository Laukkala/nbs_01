package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.TestWebSocketClientEndpoint;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListEndPointTest extends AbstractNotebookServerTest
{
    private List<String> savedFileNames;
    public List<String> readFilesOnDisk(){
        try{
            return Files.list(notebookDirectory())
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }catch (IOException ioException){
            throw new RuntimeException("Failed to initialize test!",ioException);
        }
    }
    @BeforeAll
    private void setUp(){
        copyFileRecursively(notebookResources().toFile(),notebookDirectory().toFile());
        savedFileNames = readFilesOnDisk();
    }
    @AfterAll
    private void tearDown(){
        deleteFileRecursively(notebookDirectory().toFile());
    }
    @Test
    // Assert that a simple HTTP request to /notebook/list endpoint results in a list of notebook IDs
    public void httpListAllTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();

            URL serverURL = new URL("http://"+serverAddress()+"/notebook/list");
            HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
            int status = connection.getResponseCode();

            // Read the response received, and assert that we have a list of notebook IDs matching files saved in the notebook directory.
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            ArrayList<String> receivedIds = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                receivedIds.add(line);
            }
            Assertions.assertEquals(200,status);
            for (String filename :savedFileNames) {
                Assertions.assertTrue(receivedIds.stream().anyMatch(filename::contains));
            }
            connection.disconnect();
            stopServer();
        });
    }
    @Test
    // Assert that a simple HTTP request to /notebook/list endpoint results in a list of notebook IDs
    public void httpListWithinFolderTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();

            URL serverURL = new URL("http://"+serverAddress()+"/notebook/list");
            HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            byte[] bytes = URLEncoder.encode("2A94M5J1D", StandardCharsets.UTF_8).getBytes();
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
            ArrayList<String> receivedIds = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                receivedIds.add(line);
            }

            ArrayList<String> savedIdsInFolder = new ArrayList<>();
            savedFileNames.add("2A94M5J1Z");
            savedFileNames.add("2A94M5J2Z");
            Assertions.assertEquals(200,status);
            for (String filename :savedIdsInFolder) {
                Assertions.assertTrue(receivedIds.stream().anyMatch(filename::equals));
            }
            connection.disconnect();
            stopServer();
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketListAllTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            URI serverURI = URI.create("ws://"+serverAddress()+"/notebook/list");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient,serverURI);
            Assertions.assertEquals(1,webSocketClient.getOpenSessions().size());
            client.sendText("hello");
            long startTime = System.currentTimeMillis();
            while (client.receivedMessages().size() == 0 && (System.currentTimeMillis()-startTime) < webSocketTimeoutMs){
                // Wait until a message is received or a timeout is reached.
            }
            // Read the WebSocket response and assert that we got the proper list of notebook IDs.
            ArrayList<String> receivedMessages = client.receivedMessages();
            Assertions.assertEquals(1,receivedMessages.size());
            List<String> receivedIds = Arrays.stream(receivedMessages.get(0).split("\n")).toList();
            for (String filename :savedFileNames) {
                Assertions.assertTrue(receivedIds.stream().anyMatch(filename::contains));
            }
            webSocketClient.close();
            Assertions.assertEquals(0,webSocketClient.getOpenSessions().size());
            stopServer();
        });
    }

    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketListWithinFolderTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            URI serverURI = URI.create("ws://"+serverAddress()+"/notebook/list");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient,serverURI);
            Assertions.assertEquals(1,webSocketClient.getOpenSessions().size());
            client.sendText("2A94M5J1D");


            ArrayList<String> savedIdsInFolder = new ArrayList<>();
            savedFileNames.add("2A94M5J1Z");
            savedFileNames.add("2A94M5J2Z");

            long startTime = System.currentTimeMillis();
            while (client.receivedMessages().size() == 0 && (System.currentTimeMillis()-startTime) < webSocketTimeoutMs){
                // Wait until a message is received or a timeout is reached.
            }
            // Read the WebSocket response and assert that we got the proper list of notebook IDs.
            ArrayList<String> receivedMessages = client.receivedMessages();
            Assertions.assertEquals(1,receivedMessages.size());
            List<String> receivedIds = Arrays.stream(receivedMessages.get(0).split("\n")).toList();
            for (String filename :savedIdsInFolder) {
                Assertions.assertTrue(receivedIds.stream().anyMatch(filename::equals));
            }
            webSocketClient.close();
            Assertions.assertEquals(0,webSocketClient.getOpenSessions().size());
            stopServer();
        });
    }
}
