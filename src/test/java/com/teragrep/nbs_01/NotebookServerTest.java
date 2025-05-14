package com.teragrep.nbs_01;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotebookServerTest extends AbstractNotebookServerTest
{
    public NotebookServerTest(){
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
    // Assert that a simple HTTP request to an existing endpoint results in return code 200 OK
    public void httpConnectTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            Map<Integer, List<String>> response = makeHttpGETRequest("http://"+serverAddress()+"/notebook/ping");
            Assertions.assertEquals("pong",response.get(200).get(0).toString());
            stopServer();
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketConnectTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            URI serverURI = URI.create("ws://"+serverAddress()+"/notebook/ping");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient,serverURI);
            Assertions.assertEquals(1,webSocketClient.getOpenSessions().size());
            webSocketClient.close();
            Assertions.assertEquals(0,webSocketClient.getOpenSessions().size());
            stopServer();
        });
    }
}
