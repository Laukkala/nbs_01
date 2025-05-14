package com.teragrep.nbs_01.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ParagraphTest {
    // Paragraph should be in the proper JSON format when json() is called.
    @Test
    void json() {
        Assertions.assertDoesNotThrow(()->{
            Script testScript = new Script("%spark\nprintln(\"Hello Spark\")");
            Paragraph paragraph = new Paragraph("testParagraph","testTitle",testScript);
            Assertions.assertEquals("{\"id\":\"testParagraph\",\"title\":\"testTitle\",\"script\":{\"text\":\"%spark\\nprintln(\\\"Hello Spark\\\")\"}}",paragraph.json().toString());
        });
    }
}