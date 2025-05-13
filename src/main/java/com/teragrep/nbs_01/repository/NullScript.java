package com.teragrep.nbs_01.repository;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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


    public Script load(JsonValue json){
        if(json.equals(JsonValue.EMPTY_JSON_OBJECT)){
            return new Script("");
        }
        String text = json.toString();
        return new Script(text);
    }
}
