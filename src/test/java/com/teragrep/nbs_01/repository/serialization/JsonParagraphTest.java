/*
 * Notebook server for Teragrep Backend (nbs_01)
 * Copyright (C) 2025 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
package com.teragrep.nbs_01.repository.serialization;

import com.teragrep.nbs_01.repository.Script;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class JsonParagraphTest {

    private final JsonObject testJsonParagraph1 = Json
            .createObjectBuilder()
            .add("id", "paragraph1id")
            .add("title", "paragraph1Title")
            .add("script", Json.createObjectBuilder().add("text", "testText1").build())
            .build();
    private final JsonObject testJsonParagraph2 = Json
            .createObjectBuilder()
            .add("id", "paragraph2id")
            .add("title", "paragraph2Title")
            .add("script", Json.createObjectBuilder().add("text", "testText2").build())
            .build();

    @Test
    void id() {
        final JsonParagraph testParagraph1 = new JsonParagraph(testJsonParagraph1);
        final JsonParagraph testParagraph2 = new JsonParagraph(testJsonParagraph2);

        Assertions.assertEquals("paragraph1id", testParagraph1.id());
        Assertions.assertEquals("paragraph2id", testParagraph2.id());
    }

    @Test
    void title() {
        final JsonParagraph testParagraph1 = new JsonParagraph(testJsonParagraph1);
        final JsonParagraph testParagraph2 = new JsonParagraph(testJsonParagraph2);

        Assertions.assertEquals("paragraph1Title", testParagraph1.title());
        Assertions.assertEquals("paragraph2Title", testParagraph2.title());
    }

    @Test
    void script() {
        final JsonParagraph testParagraph1 = new JsonParagraph(testJsonParagraph1);
        final JsonParagraph testParagraph2 = new JsonParagraph(testJsonParagraph2);
        final Script script1 = new Script("testText1");
        final Script script2 = new Script("testText2");

        Assertions.assertEquals(script1, testParagraph1.script());
        Assertions.assertEquals(script2, testParagraph2.script());
    }

    @Test
    void serialize() {
        final JsonParagraph testParagraph1 = new JsonParagraph(testJsonParagraph1);
        final JsonParagraph testParagraph2 = new JsonParagraph(testJsonParagraph2);

        Assertions.assertEquals(testJsonParagraph1.toString(), testParagraph1.serialize());
        Assertions.assertEquals(testJsonParagraph2.toString(), testParagraph2.serialize());
    }
}
