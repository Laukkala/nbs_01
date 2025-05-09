package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.Response;

// EndPoints are objects that can be assigned to Jetty Handler objects, which define what procedures are executed when that endpoint is called by a client.
public interface EndPoint {
    // createResponse is where the functionality of the endpoint should be defined. The response should be created and returned.
    Response createResponse(Request request);
}
