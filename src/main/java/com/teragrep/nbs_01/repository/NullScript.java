package com.teragrep.nbs_01.repository;

import jakarta.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class NullScript{

    private static Logger LOGGER = LoggerFactory.getLogger(NullScript.class);

    public NullScript(){
    }

    public String text(){
        throw new UnsupportedOperationException("NullScript can't have a text!");
    }

    public JsonObject json(){
        throw new UnsupportedOperationException("Cannot turn NullScript into JSON!");
    }

    protected boolean jobAbort() {
        throw new UnsupportedOperationException("Cannot abort a NullScript!");
    }


    public Script load(JsonObject json){
        String text = json.getString("text");
        return new Script(text);
    }
}
