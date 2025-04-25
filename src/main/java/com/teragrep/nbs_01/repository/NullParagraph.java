package com.teragrep.nbs_01.repository;

import jakarta.json.JsonObject;

public final class NullParagraph {
    private final NullScript script;

    public NullParagraph(){
        this.script = new NullScript();
    }

    public JsonObject json(){
        throw new UnsupportedOperationException("NullParagraph can't have an JsonObject!");
    }

    public Paragraph fromJson(JsonObject json){
        String id = json.getString("id");
        String title = json.containsKey("title") ? json.getString("title") : "";
        Script script = json.containsKey("script") ? this.script.load(json.getJsonObject("script")) : this.script.load(json.containsKey("text") ? json.getJsonObject("text") : JsonObject.EMPTY_JSON_OBJECT);
        return new Paragraph(id,title,script);
    }
    public String name(){
        throw new UnsupportedOperationException("NullParagraph can't have a name!");
    }
    public String id(){
        throw new UnsupportedOperationException("NullParagraph can't have an ID!");
    }

    public Script script(){
        throw new UnsupportedOperationException("NullParagraph can't have a Script!");
    }

}
