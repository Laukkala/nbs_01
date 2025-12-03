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
package com.teragrep.nbs_01.repository;

import jakarta.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Represents a single Notebook that can be added to a Directory. Is identified by a Path, and corresponds to a file
 * saved on the filesystem.
 */
public final class Notebook implements FilesystemEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(Notebook.class);
    private final Map<String, Paragraph> paragraphs;
    private final String name;

    // Constructor for a stub notebook that can be loaded from file.
    public Notebook() {
        this.name = "";
        this.paragraphs = new LinkedHashMap<>();
    }

    public Notebook(String name) {
        this.name = name;
        this.paragraphs = new LinkedHashMap<>();
    }

    public Notebook(String name, Map<String, Paragraph> paragraphs) {
        this.name = name;
        this.paragraphs = paragraphs;
    }

    public String name() {
        return name;
    }

    public Map<String, Paragraph> paragraphs() {
        return paragraphs;
    }

    public JsonObject json() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("name", name);
        //compatibility fields//
        builder.add("config", Json.createObjectBuilder(new HashMap<>()).build());
        // end //
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (Paragraph paragraph : paragraphs.values()) {
            arrayBuilder.add(paragraph.json());
        }
        JsonArray paragraphJsonArray = arrayBuilder.build();
        builder.add("paragraphs", paragraphJsonArray);
        return builder.build();
    }

    public Notebook load(JsonObject json) {
        String loadedTitle = json.containsKey("name") ? json.getString("name") : "";
        Map<String, Paragraph> loadedParagraphs = new HashMap<>();
        JsonArray paragraphArray = json.getJsonArray("paragraphs");
        for (JsonValue value : paragraphArray) {
            JsonObject paragraphJson = value.asJsonObject();
            String paragraphId = paragraphJson.getString("id");
            String paragraphTitle = paragraphJson.containsKey("title") ? paragraphJson.getString("title") : "";
            String text;
            if (paragraphJson.containsKey("script")) {
                JsonObject scriptJson = paragraphJson.getJsonObject("script");
                text = scriptJson.getString("text");
            }
            else {
                text = paragraphJson.containsKey("text") ? paragraphJson.getString("text") : "";
            }
            Script script = new Script(text);
            Paragraph paragraph = new Paragraph(paragraphId, paragraphTitle, script);
            loadedParagraphs.put(paragraph.id(), paragraph);
        }
        return new Notebook(loadedTitle, loadedParagraphs);
    }

    public Notebook copy() throws IOException {
        return copy(name);
    }

    public Notebook copy(String copyTitle) throws IOException {
        Map<String, Paragraph> copyParagraphs = new LinkedHashMap<String, Paragraph>();
        for (Paragraph paragraph : paragraphs.values()) {
            Paragraph copyParagraph = paragraph.copy();
            copyParagraphs.put(copyParagraph.id(), copyParagraph);
        }
        Notebook copyNotebook = new Notebook(copyTitle, copyParagraphs);
        return copyNotebook;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Notebook notebook = (Notebook) o;
        return Objects.equals(paragraphs, notebook.paragraphs) && Objects.equals(name, notebook.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paragraphs, name);
    }
}
