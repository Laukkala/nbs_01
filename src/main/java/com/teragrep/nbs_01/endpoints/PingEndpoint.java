package com.teragrep.nbs_01.endpoints;

public class PingEndpoint implements EndPoint{

    public PingEndpoint(){

    }

    public String createResponse(String request) {
        return "pong";
    }
}
