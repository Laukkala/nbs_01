package com.teragrep.nbs_01.responses;

import jakarta.json.JsonObject;

// Response object that contains a JsonObject as its body
public final class JsonResponse implements Response{
    private final int status;
    private final JsonObject body;
    public JsonResponse(int status,JsonObject body){
        this.status = status;
        this.body = body;
    }

    public int status(){
        return status;
    }

    public String parse(){
        return body.toString();
    }
}
