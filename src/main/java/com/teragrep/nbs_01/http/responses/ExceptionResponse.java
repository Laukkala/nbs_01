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

import com.teragrep.nbs_01.http.ExceptionBody;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

// Response object that takes a Throwable and returns a given response back to the user.
// Should be used when a Request cannot be fulfilled, but the error is not unrecoverable (such as a malformed request being received)
public final class ExceptionResponse implements Response {

    private final int status;
    private final List<Header> headers;
    private final ExceptionBody body;

    public ExceptionResponse(int status, Throwable throwable) {
        this(status, new ArrayList<>(), new ExceptionBody(throwable));
    }

    public ExceptionResponse(int status, List<Header> headers, ExceptionBody body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public int status() {
        return status;
    }

    public ExceptionBody body() {
        return body;
    }

    @Override
    public List<Header> headers() {
        return headers;
    }
}
