package com.teragrep.nbs_01;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NotebookServerTest
{
    private final int serverPort = 8080;
    private final String serverAddress = "localhost:"+serverPort;
    private final Path notebookDirectory = Paths.get("target/notebook");
    private final Configuration testConfiguration = new Configuration(notebookDirectory,serverPort);

    public NotebookServerTest(){
        NotebookServer server = new NotebookServer(testConfiguration);
        server.start();
    }

    @Test
    public void connectTest(){
        Assertions.assertDoesNotThrow(()->{
            URI serverURI = URI.create("ws://"+serverAddress+"/notebook/list");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketConnection client = new TestWebSocketConnection(webSocketClient,serverURI);
            client.sendText("hello");
        });
    }
}
