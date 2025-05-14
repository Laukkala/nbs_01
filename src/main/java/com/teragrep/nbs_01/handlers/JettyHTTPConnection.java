package com.teragrep.nbs_01.handlers;

import com.teragrep.nbs_01.endpoints.EndPoint;
import com.teragrep.nbs_01.requests.SimpleRequest;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;

// A Jetty Handler for HTTP connections to some Endpoint.
// Extracts the contents of the request and delegates it to it's EndPoint instance for processing, and then returns the response received from the EndPoint.
public class JettyHTTPConnection extends Handler.Abstract {
    private final EndPoint endPoint;

    public JettyHTTPConnection(EndPoint endPoint){
        this.endPoint = endPoint;
    }

    @Override
    public boolean handle(Request jettyRequest, Response jettyResponse, Callback callback) throws Exception {
        try{
            com.teragrep.nbs_01.requests.Request request = new SimpleRequest(Content.Source.asString(jettyRequest));
            com.teragrep.nbs_01.responses.Response response = endPoint.createResponse(request);
            jettyResponse.setStatus(response.status());
            jettyResponse.write(true, ByteBuffer.wrap(response.parse().getBytes()), Callback.NOOP);
            callback.succeeded();
            return true;
        }catch (Exception exception){
            callback.failed(exception);
            return false;
        }
    }
}
