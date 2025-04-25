package com.teragrep.nbs_01;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class TestWebSocketClientEndpoint implements Session.Listener {

    private final WebSocketClient webSocketClient;
    private final Session webSocketSession;
    private final URI serverURI;
    private final ArrayList<String> receivedMessages;

    public TestWebSocketClientEndpoint(WebSocketClient webSocketClient, URI serverURI){
        try{
            this.webSocketClient = webSocketClient;
            this.serverURI = serverURI;
            this.webSocketSession = this.webSocketClient.connect(this,this.serverURI).get();
            this.receivedMessages = new ArrayList<>();
        }catch (Exception exception){
            throw new RuntimeException("Failed to create client!");
        }
    }
    @Override
    public void onWebSocketOpen(Session session){
        System.out.println("Connected to server at "+session.getRemoteSocketAddress().toString());
    }
    @Override
    public void onWebSocketClose(int statusCode, String reason){
        System.out.println("Disconnected from server: "+ webSocketSession.getRemoteSocketAddress().toString() +", Reason: "+reason);
    }

    @Override
    public void onWebSocketBinary(ByteBuffer payload, Callback callback) {
        callback.succeed();
        webSocketSession.demand();
    }

    @Override
    public void onWebSocketText(String message){
        receivedMessages.add(message);
        System.out.println("Received message "+message+" from server at "+ webSocketSession.getRemoteSocketAddress().toString());
        webSocketSession.demand();
    }

    @Override
    public void onWebSocketError(Throwable cause){
        System.out.println("Error: "+cause.toString());
    }

    public void sendText(String message){
        webSocketSession.sendText(message,Callback.from(webSocketSession::demand, failure -> {
            webSocketSession.close(StatusCode.SERVER_ERROR, "Failure while sending message: "+ message, Callback.NOOP);
        }));
    }

    public ArrayList<String> receivedMessages(){
        return receivedMessages;
    }
}