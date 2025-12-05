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
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public final class JsonParagraph implements SerializedParagraph {

    private final JsonObject jsonObject;

    public JsonParagraph(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public String id() throws JsonException {
        String id;
        if (!jsonObject.containsKey("id")) {
            throw new JsonException("Json does not contain expected key 'id'");
        }
        JsonValue.ValueType type = jsonObject.get("id").getValueType();
        if (type.equals(JsonValue.ValueType.STRING)) {
            id = jsonObject.getString("id");
        }
        else {
            throw new JsonException(
                    "Expected key 'id' to be of type " + JsonValue.ValueType.STRING + " but was: " + type
            );
        }
        return id;
    }

    @Override
    public String title() throws JsonException {
        String title;
        if (!jsonObject.containsKey("title")) {
            title = "";
        }
        else {
            JsonValue.ValueType type = jsonObject.get("title").getValueType();
            if (type.equals(JsonValue.ValueType.STRING)) {
                title = jsonObject.getString("title");
            }
            else {
                throw new JsonException(
                        "Expected key 'title' to be of type " + JsonValue.ValueType.STRING + " but was: " + type
                );
            }
        }
        return title;
    }

    @Override
    public Script script() throws JsonException {
        Script script;
        if (jsonObject.containsKey("script")) {
            JsonValue.ValueType type = jsonObject.get("script").getValueType();
            if (type.equals(JsonValue.ValueType.OBJECT)) {
                JsonObject scriptJson = jsonObject.getJsonObject("script");
                script = new Script(new JsonScript(scriptJson).text());
            }
            else {
                throw new JsonException(
                        "Expected key 'script' to be of type " + JsonValue.ValueType.OBJECT + " but was: " + type
                );
            }
        }
        // To support legacy .zpln files, we must also check for 'text' key
        else if (jsonObject.containsKey("text")) {
            JsonValue.ValueType type = jsonObject.get("text").getValueType();
            if (type.equals(JsonValue.ValueType.STRING)) {
                String scriptText = jsonObject.getString("text");
                script = new Script(scriptText);
            }
            else {
                throw new JsonException(
                        "Expected key 'text' to be of type " + JsonValue.ValueType.STRING + " but was: " + type
                );
            }
        }
        else {
            return new Script();
        }

        return script;
    }

    @Override
    public String serialize() {
        return jsonObject.toString();
    }
}
