package com.teragrep.nbs_01.requests;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public final class JsonRequest implements Request{
    private final String body;
    private final JsonReader jsonReader;
    private final Map<String,String> parameters;
    public JsonRequest(String body){
        this.body = body;
        parameters = new HashMap<>();
        jsonReader = Json.createReader(new StringReader(body));
    }
    public String body(){
        return body;
    }
    public Map<String,String> parameters(){
        JsonObject object = jsonReader.readObject();
        for (Map.Entry<String, JsonValue> entry :object.entrySet()) {
            parameters.put(entry.getKey(),entry.getValue().toString());
        }
        return parameters;
    };
}
