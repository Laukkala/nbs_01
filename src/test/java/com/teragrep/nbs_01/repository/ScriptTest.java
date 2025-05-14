package com.teragrep.nbs_01.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScriptTest {
    // Script should be in the proper JSON format when json() is called.

    @Test
    void json() {
        Assertions.assertDoesNotThrow(()->{
            Script testScript = new Script("%spark\nprintln(\"Hello Spark\")");
            Assertions.assertEquals("{\"text\":\"%spark\\nprintln(\\\"Hello Spark\\\")\"}",testScript.json().toString());
        });
    }
}