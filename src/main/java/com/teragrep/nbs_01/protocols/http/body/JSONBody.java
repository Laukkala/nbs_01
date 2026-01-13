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
package com.teragrep.nbs_01.protocols.http.body;

import com.teragrep.nbs_01.exceptions.StubObjectException;
import jakarta.json.*;

/**
 * A Body that encapsulates a JsonStructure
 */

public class JSONBody implements Body {

    private final JsonStructure json;

    public JSONBody(final JsonStructure json) {
        this.json = json;
    }

    public JsonStructure json() {
        return json;
    }

    @Override
    public String asString() throws StubObjectException {
        return json.toString();
    }

    @Override
    public String title() throws StubObjectException {
        if (
            json.asJsonObject().containsKey("title")
                    && json.asJsonObject().get("title").getValueType().equals(JsonValue.ValueType.STRING)
        ) {
            return json.asJsonObject().getString("title");
        }
        else {
            throw new StubObjectException("No such value: \"title\"");
        }
    }

    @Override
    public String sourceParagraphId() throws StubObjectException {
        if (
            json.asJsonObject().containsKey("sourceParagraphId")
                    && json.asJsonObject().get("sourceParagraphId").getValueType().equals(JsonValue.ValueType.STRING)
        ) {
            return json.asJsonObject().getString("sourceParagraphId");
        }
        else {
            throw new StubObjectException("No such value: \"sourceParagraphId\"");
        }
    }

    @Override
    public String sourceIdentifier() throws StubObjectException {
        if (
            json.asJsonObject().containsKey("sourcePath")
                    && json.asJsonObject().get("sourcePath").getValueType().equals(JsonValue.ValueType.STRING)
        ) {
            return json.asJsonObject().getString("sourcePath");
        }
        else {
            throw new StubObjectException("No such value: \"sourcePath\"");
        }
    }

    @Override
    public String text() throws StubObjectException {
        if (
            json.asJsonObject().containsKey("text")
                    && json.asJsonObject().get("text").getValueType().equals(JsonValue.ValueType.STRING)
        ) {
            return json.asJsonObject().getString("text");
        }
        else {
            throw new StubObjectException("No such value: \"text\"");
        }
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
