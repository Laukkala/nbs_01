package com.teragrep.nbs_01.handlers;

import com.teragrep.nbs_01.endpoints.HTTPEndPoint;
import com.teragrep.nbs_01.endpoints.WebSocketEndPoint;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.websocket.server.ServerWebSocketContainer;

public class HTTPHandler extends Handler.Abstract {
    HTTPEndPoint httpEndPoint;

    public HTTPHandler(HTTPEndPoint httpEndPoint) {
        this.httpEndPoint = httpEndPoint;
    }


    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        {
            // Handle a normal HTTP request.
            httpEndPoint.onRequest(request,response);
            callback.succeeded();
            return true;
        }
    }
}
