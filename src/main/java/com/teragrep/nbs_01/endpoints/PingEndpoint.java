package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.Response;
import com.teragrep.nbs_01.responses.StringResponse;
import org.eclipse.jetty.http.HttpStatus;

public class PingEndpoint implements EndPoint{

    // Simply returns a "pong" response to any request. Can be used as a heartbeat function.
    public PingEndpoint(){

    }

    public Response createResponse(Request request) {
        return new StringResponse(HttpStatus.OK_200,"pong");
    }
}
