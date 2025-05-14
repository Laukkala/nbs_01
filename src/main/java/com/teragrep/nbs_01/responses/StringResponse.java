package com.teragrep.nbs_01.responses;

// A simple response object consisting of a string body and a status code.
public final class StringResponse implements Response{
    int status;
    String body;
    public StringResponse(int status, String body){
        this.status = status;
        this.body = body;
    }

    public String parse() {
        return body;
    }

    public int status(){
        return status;
    }
}
