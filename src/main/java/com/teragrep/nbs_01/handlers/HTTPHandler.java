package com.teragrep.nbs_01.handlers;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class HTTPHandler extends Handler.Abstract {
    HTTPConnection httpEndPoint;

    public HTTPHandler(HTTPConnection httpEndPoint) {
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
