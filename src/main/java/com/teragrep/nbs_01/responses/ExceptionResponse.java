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
package com.teragrep.nbs_01.responses;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Collection;

// Response object that takes a Throwable and returns a given response back to the user.
// Should be used when a Request cannot be fulfilled, but the error is not unrecoverable (such as a malformed request being received)
public final class ExceptionResponse implements Response {

    private final int status;
    private final Throwable throwable;
    private final Collection<Header> headers;

    public ExceptionResponse(int status, Throwable throwable) {
        this(status, throwable, new ArrayList<>());
    }

    public ExceptionResponse(int status, Throwable throwable, Collection<Header> headers) {
        this.status = status;
        this.throwable = throwable;
        this.headers = headers;
    }

    public int status() {
        return status;
    }

    public Throwable exception() {
        return throwable;
    }

    public String body() {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("message", throwable.getMessage());
        JsonObject json = jsonObjectBuilder.build();
        return json.toString();
    }

    @Override
    public Collection<Header> headers() {
        return headers;
    }
}
