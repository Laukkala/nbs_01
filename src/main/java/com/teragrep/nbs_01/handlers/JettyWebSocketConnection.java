package com.teragrep.nbs_01.handlers;

import com.teragrep.nbs_01.endpoints.EndPoint;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.requests.SimpleRequest;
import com.teragrep.nbs_01.responses.Response;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.nio.ByteBuffer;
// A Jetty listener for a WebSocket connection to some Endpoint.
// Creates a session between Client and Server once the connection has been opened, through which communication is routed
public class JettyWebSocketConnection implements Session.Listener {
    private Session session;
    private final EndPoint endPoint;

    public JettyWebSocketConnection(EndPoint endPoint){
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
        Request request = new SimpleRequest(message);
        Response response = endPoint.createResponse(request);
        session.sendText(response.parse(), Callback.from(()->{
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
