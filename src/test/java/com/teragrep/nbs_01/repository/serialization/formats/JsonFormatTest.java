package com.teragrep.nbs_01.repository.serialization.formats;

import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.Paragraph;
import com.teragrep.nbs_01.repository.Script;
import com.teragrep.nbs_01.repository.serialization.JsonNotebook;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonFormatTest {

    @Test
    void formatTest() {
        String title = "testTitle";
        Map<String,Paragraph> paragraphs = new HashMap<>();

        String para1Id = "para1";
        String para2Id = "para2";

        String para1Text = "text1";
        String para2Text = "text2";

        Paragraph paragraph1 = new Paragraph(para1Id,new Script(para1Text));
        Paragraph paragraph2 = new Paragraph(para2Id,new Script(para2Text));

        paragraphs.put(para1Id,paragraph1);
        paragraphs.put(para2Id,paragraph2);

        Notebook notebook = new Notebook(title,paragraphs);
        JsonFormat format = new JsonFormat();
        JsonNotebook formatted = format.format(notebook);

        JsonObject expectedJson = Json.createObjectBuilder()
                .add("title",title)
                .add("config",Json.createObjectBuilder().build())
                .add("paragraphs",Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id",para1Id)
                                .add("title",para1Id)
                                .add("script",Json.createObjectBuilder()
                                        .add("text",para1Text).build())
                                .build())
                        .add(Json.createObjectBuilder()
                                .add("id",para2Id)
                                .add("title",para2Id)
                                .add("script",Json.createObjectBuilder()
                                        .add("text",para2Text).build())
                                .build()))
                .build();
        Assertions.assertEquals(expectedJson.toString(),formatted.serialize());
    }
}