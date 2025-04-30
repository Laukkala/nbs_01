package com.teragrep.nbs_01.handlers;

import com.teragrep.nbs_01.endpoints.EndPoint;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;
// A Jetty Handler for HTTP connections to some Endpoint.
// Extracts the contents of the request and delegates it to it's EndPoint instance for processing, and then returns the response received from the EndPoint.
public class HTTPConnection extends Handler.Abstract {
    private final EndPoint endPoint;

    public HTTPConnection(EndPoint endPoint){
        this.endPoint = endPoint;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        response.setStatus(HttpStatus.OK_200);
        String body = new String(Content.Source.asString(request));
        response.write(true, ByteBuffer.wrap(endPoint.createResponse(body).getBytes()), Callback.NOOP);
        callback.succeeded();
        return true;
    }
}
