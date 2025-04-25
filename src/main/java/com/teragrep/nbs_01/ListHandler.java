package com.teragrep.nbs_01;

import com.teragrep.nbs_01.endpoints.HTTPListEndPoint;
import com.teragrep.nbs_01.endpoints.WebSocketListEndPoint;
import com.teragrep.nbs_01.repository.Directory;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.websocket.server.ServerWebSocketContainer;

import java.nio.charset.Charset;

public class ListHandler extends Handler.Abstract {

    Directory root;

    public ListHandler(Directory root) {
        this.root = root;
    }


    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        String pathInContext = Request.getPathInContext(request);
        if (pathInContext.startsWith("/list") && request.getHeaders().contains("Upgrade", "websocket") && request.getHeaders().contains("Connection", "Upgrade"))
        {
            try
            {
                ServerWebSocketContainer container = ServerWebSocketContainer.get(request.getContext());
                // This is a WebSocket upgrade request, perform a direct upgrade.
                boolean upgraded = container.upgrade((rq, rs, cb) -> new WebSocketListEndPoint(root), request, response, callback);
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
            HTTPListEndPoint endPoint = new HTTPListEndPoint(root);
            endPoint.onRequest(request,response);
            callback.succeeded();
            return true;
        }
    }
}
