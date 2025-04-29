package com.teragrep.nbs_01.endpoints;

// EndPoints are objects that can be assigned to Jetty Handler objects, which define what procedures are executed when that endpoint is called by a client.
public interface EndPoint {
    // createResponse is where the functionality of the endpoint should be defined. The response should be created and returned.
    String createResponse(String request);
}
