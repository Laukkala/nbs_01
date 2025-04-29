package com.teragrep.nbs_01.handlers;

import com.teragrep.nbs_01.endpoints.EndPoint;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.nio.ByteBuffer;
// A Jetty listener for a WebSocket connection to some Endpoint.
// Creates a session between Client and Server once the connection has been opened, through which communication is routed
public class WebSocketConnection implements Session.Listener {
    private Session session;
    private final EndPoint endPoint;

    public WebSocketConnection(EndPoint endPoint){
        this.endPoint = endPoint;
    }
    // Jetty creates a Session when onWebSocketOpen is called, so we have to assign the Session here instead of in the constructor.
    @Override
    public void onWebSocketOpen(Session session){
        this.session = session;
        session.demand();
    }
    @Override
    public void onWebSocketClose(int statusCode, String reason){
        }

    @Override
    public void onWebSocketBinary(ByteBuffer payload, Callback callback) {
        callback.succeed();
        session.demand();
    }

    @Override
    public void onWebSocketText(String message){
        session.sendText(endPoint.createResponse(message), Callback.from(()->{
            session.demand();
        },failure -> {
            session.close(StatusCode.SERVER_ERROR, "failure", Callback.NOOP);
        }));
    }
    @Override
    public void onWebSocketError(Throwable cause){
        System.err.println("Server error: "+cause.toString());
        session.close(StatusCode.SERVER_ERROR,"Websocket Error occurred",Callback.NOOP);
    }
}
