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

import com.teragrep.nbs_01.exceptions.ErrorEvent;
import com.teragrep.nbs_01.exceptions.StubObjectException;
import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * A Body that takes an ErrorEvent. Provides access to the ErrorEvent and Generates a preset message body that does not
 * expose the inner workings of the program to the end user.
 */
public class ErrorBody implements Body {

    private final JsonObject message;
    private final ErrorEvent event;

    public ErrorBody(final ErrorEvent event) {
        this(
                event,
                Json
                        .createObjectBuilder()
                        .add(
                                "message",
                                "An error occurred while processing your Request. See event id " + event.id()
                                        + " in the technical log for details."
                        )
                        .build()
        );
    }

    public ErrorBody(final ErrorEvent event, final JsonObject message) {
        this.event = event;
        this.message = message;
    }

    public ErrorEvent event() {
        return event;
    }

    @Override
    public String asString() throws StubObjectException {
        return message.toString();
    }

    @Override
    public String title() throws StubObjectException {
        throw new StubObjectException("ErrorBody does not have a Title!");
    }

    @Override
    public String sourceParagraphId() throws StubObjectException {
        throw new StubObjectException("ErrorBody does not have a SourceParagraphID!");
    }

    @Override
    public String sourceIdentifier() throws StubObjectException {
        throw new StubObjectException("ErrorBody does not have a SourceIdentifier!!");
    }

    @Override
    public String text() throws StubObjectException {
        throw new StubObjectException("ErrorBody does not have a Text!");
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
