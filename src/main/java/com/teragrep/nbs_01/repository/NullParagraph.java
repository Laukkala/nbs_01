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

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public final class NullParagraph {

    private final NullScript script;

    public NullParagraph() {
        this.script = new NullScript();
    }

    public JsonObject json() {
        throw new UnsupportedOperationException("NullParagraph can't have an JsonObject!");
    }

    public Paragraph fromJson(JsonObject json) {
        String id = json.getString("id");
        String title = json.containsKey("title") ? json.getString("title") : "";
        boolean isLegacy = json.containsKey("text");
        Script script;
        if (isLegacy) {
            script = this.script.load(json.get("text"));
        }
        else {
            if (!json.containsKey("script")) {
                script = this.script.load(JsonValue.EMPTY_JSON_OBJECT);
            }
            else {
                script = this.script.load(json.get("script"));
            }
        }
        //Script script = json.containsKey("script") ? this.script.load(json.getJsonObject("script")) : this.script.load(json.containsKey("text") ? json.getJsonObject("text").asJsonObject() : JsonObject.EMPTY_JSON_OBJECT);
        return new Paragraph(id, title, script);
    }

    public String name() {
        throw new UnsupportedOperationException("NullParagraph can't have a name!");
    }

    public String id() {
        throw new UnsupportedOperationException("NullParagraph can't have an ID!");
    }

    public Script script() {
        throw new UnsupportedOperationException("NullParagraph can't have a Script!");
    }

}
