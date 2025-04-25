package com.teragrep.nbs_01.handlers;

import com.teragrep.nbs_01.endpoints.HTTPEndPoint;
import com.teragrep.nbs_01.endpoints.WebSocketEndPoint;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.websocket.server.ServerWebSocketContainer;

public class UpgradeableHTTPHandler extends Handler.Abstract {
    WebSocketEndPoint webSocketEndPoint;
    HTTPEndPoint httpEndPoint;

    public UpgradeableHTTPHandler(WebSocketEndPoint webSocketEndPoint, HTTPEndPoint httpEndPoint) {
        this.webSocketEndPoint = webSocketEndPoint;
        this.httpEndPoint = httpEndPoint;
    }


    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        if (request.getHeaders().contains("Upgrade", "websocket") && request.getHeaders().contains("Connection", "Upgrade"))
        {
            try
            {
                ServerWebSocketContainer container = ServerWebSocketContainer.get(request.getContext());
                // This is a WebSocket upgrade request, perform a direct upgrade.
                boolean upgraded = container.upgrade((rq, rs, cb) -> webSocketEndPoint, request, response, callback);
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
            HTTPEndPoint endPoint = httpEndPoint;
            endPoint.onRequest(request,response);
            callback.succeeded();
            return true;
        }
    }
}
