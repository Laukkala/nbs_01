package com.teragrep.nbs_01;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Paths;

public class NotebookServerTest
{
    private final String serverPort = "8080";
    private final String serverAddress = "localhost:"+serverPort;

    public NotebookServerTest(){
        NotebookServer server = new NotebookServer(Paths.get("target"));
        server.start();
    }

    @Test
    public void connectTest(){
        Assertions.assertDoesNotThrow(()->{
            URI serverURI = URI.create("ws://"+serverAddress+"/notebook/list");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            WebSocketConnection client = new WebSocketConnection(webSocketClient,serverURI);
            client.sendText("hello");
        });
    }
}
