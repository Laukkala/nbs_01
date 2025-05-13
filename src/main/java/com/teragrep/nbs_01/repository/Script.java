package com.teragrep.nbs_01.repository;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Script implements Stubable {

    private static Logger LOGGER = LoggerFactory.getLogger(Script.class);
    private final String text;

    public Script(String text){
        this.text = text;
    }

    public String text(){
        return text;
    }
    public JsonObject json(){
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("text",text);
        return builder.build();
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
