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
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 * A Body that takes a Throwable. Generates message body that contains only the highest level Exception message to be
 * shown to the end user. Should be used in cases where user has made a mistake, such as providing incorrect data.
 */

public final class ExceptionBody implements Body {

    private final JsonObject json;
    private final Throwable exception;
    private final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

    public ExceptionBody(final Throwable exception) {
        this.exception = exception;
        jsonObjectBuilder.add("message", exception.getMessage());
        this.json = jsonObjectBuilder.build();
    }

    public Throwable exception() {
        return exception;
    }

    @Override
    public String asString() {
        return json.toString();
    }

    @Override
    public String title() throws StubObjectException {
        throw new StubObjectException("ExceptionBody does not have a Title!");
    }

    @Override
    public String sourceParagraphId() throws StubObjectException {
        throw new StubObjectException("ExceptionBody does not have a SourceParagraphID!");
    }

    @Override
    public String sourceIdentifier() throws StubObjectException {
        throw new StubObjectException("ExceptionBody does not have a SourceIdentifier!!");
    }

    @Override
    public String text() throws StubObjectException {
        throw new StubObjectException("ExceptionBody does not have a Text!");
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
