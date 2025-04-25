package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.TestWebSocketClientEndpoint;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListEndPointTest extends AbstractNotebookServerTest
{
    private List<String> savedFileNames;
    public ListEndPointTest(){
        Assertions.assertDoesNotThrow(()->{
            savedFileNames = Files.list(notebookDirectory())
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        });
    }
    @Test
    // Assert that a simple HTTP request to /notebook/list endpoint results in a list of notebook IDs
    public void httpListTest(){
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
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketConnectTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            URI serverURI = URI.create("ws://"+serverAddress()+"/notebook/list");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient,serverURI);
            Assertions.assertEquals(1,webSocketClient.getOpenSessions().size());
            client.sendText("hello");
            Thread.sleep(500);
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
}
