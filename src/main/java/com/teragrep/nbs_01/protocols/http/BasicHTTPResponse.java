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
package com.teragrep.nbs_01.protocols.http;

import com.teragrep.nbs_01.exceptions.StubObjectException;
import com.teragrep.nbs_01.protocols.http.body.Body;
import com.teragrep.nbs_01.protocols.http.body.StubBody;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic Response implementation
 */
public final class BasicHTTPResponse implements HTTPResponse {

    private final int status;
    private final Body body;
    private final List<Header> headers;

    public BasicHTTPResponse(int status) {
        this(status, new StubBody(), new ArrayList<>());
    }

    public BasicHTTPResponse(int status, List<Header> headers) {
        this(status, new StubBody(), headers);
    }

    public BasicHTTPResponse(int status, Body body) {
        this(status, body, new ArrayList<>());
    }

    public BasicHTTPResponse(int status, Body body, List<Header> headers) {
        this.status = status;
        this.body = body;
        this.headers = headers;
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public Body body() {
        return body;
    }

    @Override
    public List<Header> headers() {
        return headers;
    }

    @Override
    public String message() {
        try {
            return body.asString();
        }
        catch (StubObjectException exception) {
            return "";
        }
    }

    @Override
    public boolean success() {
        if (status >= 200 && status < 300) {
            return true;
        }
        else {
            return false;
        }
    }
}
