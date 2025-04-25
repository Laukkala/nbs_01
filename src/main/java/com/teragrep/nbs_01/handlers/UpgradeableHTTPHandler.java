package com.teragrep.nbs_01.handlers;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.websocket.server.ServerWebSocketContainer;

public class UpgradeableHTTPHandler extends Handler.Abstract {
    WebSocketConnection webSocketConnection;
    HTTPConnection httpConnection;

    public UpgradeableHTTPHandler(WebSocketConnection webSocketConnection, HTTPConnection httpConnection) {
        this.webSocketConnection = webSocketConnection;
        this.httpConnection = httpConnection;
    }


    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        if (request.getHeaders().contains("Upgrade", "websocket") && request.getHeaders().contains("Connection", "Upgrade"))
        {
            try
            {
                ServerWebSocketContainer container = ServerWebSocketContainer.get(request.getContext());
                // This is a WebSocket upgrade request, perform a direct upgrade.
                boolean upgraded = container.upgrade((rq, rs, cb) -> webSocketConnection, request, response, callback);
                if (upgraded){
                    return true;
                }
                else {
                    // This was supposed to be a WebSocket upgrade request, but something went wrong.
                    Response.writeError(request, response, callback, HttpStatus.UPGRADE_REQUIRED_426);
                    return true;
                }
            }
            catch (Exception x)
            {
                Response.writeError(request, response, callback, HttpStatus.UPGRADE_REQUIRED_426, "failed to upgrade", x);
                return true;
            }
        }
        else
        {
            // Handle a normal HTTP request.
            httpConnection.onRequest(request,response);
            callback.succeeded();
            return true;
        }
    }
}
