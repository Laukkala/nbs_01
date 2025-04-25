package com.teragrep.nbs_01.endpoints;

public class HelloEndpoint implements EndPoint{

    public HelloEndpoint(){

    }

    public String createResponse(String request) {
        return "Hello!";
    }
}
