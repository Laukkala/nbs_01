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
package com.teragrep.nbs_01.http.responses;

import com.teragrep.nbs_01.http.ErrorBody;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Response object that represents an unrecoverable server-side error (Such as failure to write a file). Logs a Throwable with an event ID and generates a message body that prompts the user to check technical logs for error details.
// Should be used when an internal server error is encountered during a request
public final class ErrorResponse implements Response {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorResponse.class);
    private final UUID eventId;
    private final int status;
    private final ErrorBody body;
    private final List<Header> headers;

    public ErrorResponse(int status, Throwable throwable) {
        this(status, UUID.randomUUID(), new ArrayList<Header>(), throwable);
    }

    public ErrorResponse(int status, UUID eventId, List<Header> headers, Throwable throwable) {
        this(status, eventId, headers, new ErrorBody(throwable, eventId));
    }

    public ErrorResponse(int status, Throwable throwable, List<Header> headers) {
        this(status, UUID.randomUUID(), headers, throwable);
    }

    public ErrorResponse(int status, Throwable throwable, UUID eventId) {
        this(status, eventId, new ArrayList<Header>(), new ErrorBody(throwable, eventId));
    }

    public ErrorResponse(int status, UUID eventId, List<Header> headers, ErrorBody body) {
        this.status = status;
        this.eventId = eventId;
        this.headers = headers;
        this.body = body;
    }

    public int status() {
        return status;
    }

    public UUID eventId() {
        return eventId;
    }

    public ErrorBody body() {
        return body;
    }

    @Override
    public List<Header> headers() {
        return headers;
    }

}
