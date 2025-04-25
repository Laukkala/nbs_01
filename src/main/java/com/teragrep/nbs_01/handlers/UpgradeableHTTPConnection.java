package com.teragrep.nbs_01.handlers;

import com.teragrep.nbs_01.endpoints.EndPoint;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.websocket.server.ServerWebSocketContainer;

public class UpgradeableHTTPConnection extends Handler.Abstract {
    EndPoint endPoint;

    public UpgradeableHTTPConnection(EndPoint endPoint){
        this.endPoint = endPoint;
    }


    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
            if (request.getHeaders().contains("Upgrade", "websocket") && request.getHeaders().contains("Connection", "Upgrade"))
            {
                // Handle a WebSocket connection
                try
                {
                    ServerWebSocketContainer container = ServerWebSocketContainer.get(request.getContext());
                    boolean upgraded = container.upgrade((rq, rs, cb) -> new WebSocketConnection(endPoint), request, response, callback);
                    if (upgraded){
                        return true;
                    }
                    else {
                        // This was supposed to be a WebSocket upgrade request, but something went wrong.
                        Response.writeError(request, response, callback, HttpStatus.UPGRADE_REQUIRED_426);
                        return true;
                    }
                }
                catch (Exception exception)
                {
                    Response.writeError(request, response, callback, HttpStatus.UPGRADE_REQUIRED_426, "failed to upgrade", exception);
                    return true;
                }
            }
            else
            {
                // Handle a normal HTTP request.
                new HTTPConnection(endPoint).handle(request,response,callback);
                callback.succeeded();
                return true;
            }
    }
}
