package com.teragrep.nbs_01.endpoints;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.nio.charset.Charset;

public abstract class HTTPEndPoint implements EndPoint{
    public Response onRequest(Request request, Response response){
        System.out.println("Received an HTTP request!");
        response.setStatus(HttpStatus.OK_200);
        response.write(true, Charset.defaultCharset().encode(createResponse(request.toString())), Callback.NOOP);
        return response;
    }
}
