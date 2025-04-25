package com.teragrep.nbs_01;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NotebookServerTest
{
    private final int serverPort = 8080;
    private final String serverAddress = "localhost:"+serverPort;
    private final Path notebookDirectory = Paths.get("target/notebooks");
    private final Configuration testConfiguration = new Configuration(notebookDirectory,serverPort);

    public NotebookServerTest(){
        NotebookServer server = new NotebookServer(testConfiguration);
        server.start();
    }
    @Test
    // Assert that a simple HTTP request to an existing endpoint results in return code 200 OK
    public void httpConnectTest(){
        Assertions.assertDoesNotThrow(()->{
            URL serverURL = new URL("http://"+serverAddress+"/notebook/hello");
            HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
            int status = connection.getResponseCode();
            connection.disconnect();
            Assertions.assertEquals(200,status);
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketConnectTest(){
        Assertions.assertDoesNotThrow(()->{
            URI serverURI = URI.create("ws://"+serverAddress+"/notebook/list");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketConnection client = new TestWebSocketConnection(webSocketClient,serverURI);
            Assertions.assertEquals(1,webSocketClient.getOpenSessions().size());
            webSocketClient.close();
            Assertions.assertEquals(0,webSocketClient.getOpenSessions().size());
        });
    }
}
