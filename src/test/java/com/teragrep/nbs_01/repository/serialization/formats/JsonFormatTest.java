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
package com.teragrep.nbs_01.repository.serialization.formats;

import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.Paragraph;
import com.teragrep.nbs_01.repository.Script;
import com.teragrep.nbs_01.repository.serialization.JsonNotebook;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonFormatTest {

    @Test
    void formatTest() {
        String title = "testTitle";
        Map<String, Paragraph> paragraphs = new HashMap<>();

        String para1Id = "para1";
        String para2Id = "para2";

        String para1Text = "text1";
        String para2Text = "text2";

        Paragraph paragraph1 = new Paragraph(para1Id, new Script(para1Text));
        Paragraph paragraph2 = new Paragraph(para2Id, new Script(para2Text));

        paragraphs.put(para1Id, paragraph1);
        paragraphs.put(para2Id, paragraph2);

        Notebook notebook = new Notebook(title, paragraphs);
        JsonFormat format = new JsonFormat();
        JsonNotebook formatted = format.format(notebook);

        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("title", title)
                .add("config", Json.createObjectBuilder().build())
                .add("paragraphs", Json.createArrayBuilder().add(Json.createObjectBuilder().add("id", para1Id).add("title", para1Id).add("script", Json.createObjectBuilder().add("text", para1Text).build()).build()).add(Json.createObjectBuilder().add("id", para2Id).add("title", para2Id).add("script", Json.createObjectBuilder().add("text", para2Text).build()).build())).build();
        Assertions.assertEquals(expectedJson.toString(), formatted.serialize());
    }
}
