package com.teragrep.nbs_01.handlers;

import com.teragrep.nbs_01.endpoints.EndPoint;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.nio.ByteBuffer;

public class WebSocketConnection implements Session.Listener {
    private Session session;
    private EndPoint endPoint;

    public WebSocketConnection(EndPoint endPoint){
        this.endPoint = endPoint;
    }
    @Override
    public void onWebSocketOpen(Session session){
        this.session = session;
        System.out.println("New WebSocket session created! "+session.getRemoteSocketAddress().toString());
        session.demand();
    }
    @Override
    public void onWebSocketClose(int statusCode, String reason){
        System.out.println("Client disconnected from WebSocket: "+ session.getRemoteSocketAddress().toString() +", Reason: "+reason);
    }

    @Override
    public void onWebSocketBinary(ByteBuffer payload, Callback callback) {
        callback.succeed();
        session.demand();
    }

    @Override
    public void onWebSocketText(String message){
        session.sendText(endPoint.createResponse(message), Callback.from(()->{
            System.out.println("Sent response to " + session.getRemoteSocketAddress());
            session.demand();
        },failure -> {
            session.close(StatusCode.SERVER_ERROR, "failure", Callback.NOOP);
        }));
    }
    @Override
    public void onWebSocketError(Throwable cause){
        System.out.println("Server error: "+cause.toString());
        session.close(StatusCode.SERVER_ERROR,"Websocket Error occurred",Callback.NOOP);
    }

    public Session session(){
        return session;
    }
}
