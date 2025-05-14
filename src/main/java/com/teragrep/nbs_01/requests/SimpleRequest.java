package com.teragrep.nbs_01.requests;

import java.util.HashMap;
import java.util.Map;

public final class SimpleRequest implements Request{
    private final String body;
    private final Map<String,String> parameters;
    public SimpleRequest(String body){
        this.body = body;
        parameters = new HashMap<>();
    }
    public String body(){
        return body;
    }
    public Map<String,String> parameters(){
        int index = 0;
        for (String part:body.split(",")) {
            parameters.put(Integer.toString(index),part);
            index++;
        }
        return parameters;
    }
}
