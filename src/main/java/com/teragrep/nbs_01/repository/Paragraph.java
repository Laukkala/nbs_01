package com.teragrep.nbs_01.repository;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public final class Paragraph {
    private final String id;
    private final String title;
    private final Script script;

    public Paragraph(String id, String title, Script script){
        this.id = id;
        this.title = title;
        this.script = script;
    }

    public JsonObject json(){
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("id",id);
        builder.add("title", title != null? title : "");
        builder.add("script",script.json());
        return builder.build();
    }
    public String title(){
        return title;
    }
    public String id(){
        return id;
    }
    public Script script(){
        return script;
    }
}
