package com.teragrep.nbs_01.endpoints;

public class PingEndpoint implements EndPoint{

    // Simply returns a "pong" response to any request. Can be used as a heartbeat function.
    public PingEndpoint(){

    }

    public String createResponse(String request) {
        return "pong";
    }
}
