package com.teragrep.nbs_01.requests;

import java.util.HashMap;
import java.util.Map;

public final class SimpleRequest implements Request{
    private final String body;
    public SimpleRequest(String body){
        this.body = body;
    }
    public String body(){
        return body;
    }
}
