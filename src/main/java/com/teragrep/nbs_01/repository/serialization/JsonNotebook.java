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

import com.teragrep.nbs_01.repository.Paragraph;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.util.HashMap;
import java.util.Map;

public final class JsonNotebook implements SerializedNotebook {

    private final JsonObject jsonObject;

    public JsonNotebook(final JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public String title() throws JsonException {
        final String title;
        if (!jsonObject.containsKey("title") && !jsonObject.containsKey("name")) {
            title = "";
        }
        else if (jsonObject.containsKey("title")) {
            final JsonValue.ValueType type = jsonObject.get("title").getValueType();
            if (type.equals(JsonValue.ValueType.STRING)) {
                title = jsonObject.getString("title");
            }
            else {
                throw new JsonException(
                        "Expected key 'title' to be of type " + JsonValue.ValueType.STRING + " but was: " + type
                );
            }
        }
        // Handling of legacy zeppelin files that use "name" instead of "title"
        else {
            final JsonValue.ValueType type = jsonObject.get("name").getValueType();
            if (type.equals(JsonValue.ValueType.STRING)) {
                title = jsonObject.getString("name");
            }
            else {
                throw new JsonException(
                        "Expected key 'name' to be of type " + JsonValue.ValueType.STRING + " but was: " + type
                );
            }
        }
        return title;
    }

    @Override
    public Map<String, Paragraph> paragraphs() throws JsonException {
        final Map<String, Paragraph> loadedParagraphs = new HashMap<>();
        final JsonValue.ValueType type = jsonObject.get("paragraphs").getValueType();
        final JsonArray paragraphArray = jsonObject.getJsonArray("paragraphs");
        if (!type.equals(JsonValue.ValueType.ARRAY)) {
            throw new JsonException(
                    "Expected key 'paragraphs' to be of type " + JsonValue.ValueType.ARRAY + " but was: " + type
            );
        }
        for (final JsonValue value : paragraphArray) {
            if (!value.getValueType().equals(JsonValue.ValueType.OBJECT)) {
                throw new JsonException(
                        "Expected array value to be of type " + JsonValue.ValueType.OBJECT + " but was: " + type
                );
            }
            final JsonObject paragraphJson = value.asJsonObject();
            final JsonParagraph jsonParagraph = new JsonParagraph(paragraphJson);
            final Paragraph paragraph = new Paragraph(jsonParagraph.title(), jsonParagraph.script());
            loadedParagraphs.put(paragraphJson.getString("id"), paragraph);
        }
        return loadedParagraphs;
    }

    @Override
    public String serialize() {
        return jsonObject.toString();
    }
}
