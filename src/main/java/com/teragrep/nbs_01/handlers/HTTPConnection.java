package com.teragrep.nbs_01.handlers;

import com.teragrep.nbs_01.endpoints.EndPoint;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;

public class HTTPConnection extends Handler.Abstract {
    private EndPoint endPoint;

    public HTTPConnection(EndPoint endPoint){
        this.endPoint = endPoint;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        //System.out.println("Received an HTTP request!");
        response.setStatus(HttpStatus.OK_200);
        response.write(true, ByteBuffer.wrap(endPoint.createResponse(Content.Source.asString(request)).getBytes()), Callback.NOOP);
        callback.succeeded();
        return true;
    }
}
