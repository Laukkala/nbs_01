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

import com.teragrep.nbs_01.protocols.http.path.HTTPRequestPath;
import com.teragrep.nbs_01.protocols.http.path.StubPath;
import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.exceptions.StubObjectException;
import com.teragrep.nbs_01.protocols.http.body.Body;
import com.teragrep.nbs_01.protocols.http.body.StubBody;
import com.teragrep.nbs_01.repository.identifiers.Identifier;
import com.teragrep.nbs_01.repository.identifiers.PathIdentifier;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic Request implementation
 */
public final class BasicHTTPRequest implements HTTPRequest {

    private final Body body;
    private final HTTPRequestPath path;
    private final List<Header> headers;

    public BasicHTTPRequest() {
        this(new StubBody(), new StubPath(), new ArrayList<>());
    }

    public BasicHTTPRequest(Body body) {
        this(body, new StubPath(), new ArrayList<>());
    }

    public BasicHTTPRequest(HTTPRequestPath path) {
        this(new StubBody(), path, new ArrayList<>());
    }

    public BasicHTTPRequest(HTTPRequestPath path, List<Header> headers) {
        this(new StubBody(), path, headers);
    }

    public BasicHTTPRequest(HTTPRequestPath path, Body body) {
        this(body, path, new ArrayList<>());
    }

    public BasicHTTPRequest(Body body, HTTPRequestPath path, List<Header> headers) {
        this.body = body;
        this.path = path;
        this.headers = headers;
    }

    public Body body() {
        return body;
    }

    public HTTPRequestPath path() {
        return path;
    }

    public List<Header> headers() {
        return headers;
    }

    @Override
    public String title() throws MalformedRequestException {
        try {
            return body().title();
        }
        catch (StubObjectException e) {
            return "";
        }
    }

    @Override
    public String targetParagraphId() throws MalformedRequestException {
        return path().paragraphId();
    }

    @Override
    public String sourceParagraphId() throws MalformedRequestException {
        try {
            return body().sourceParagraphId();
        }
        catch (StubObjectException e) {
            throw new MalformedRequestException("Request has a malformed sourceParagraph identifier!", e);
        }
    }

    @Override
    public String text() throws MalformedRequestException {
        try {
            return body().text();
        }
        catch (StubObjectException e) {
            throw new MalformedRequestException("Request has a malformed text!", e);
        }
    }

    @Override
    public Identifier targetIdentifier() throws MalformedRequestException {
        return new PathIdentifier(path.path());
    }

    @Override
    public Identifier sourceIdentifier() throws MalformedRequestException {
        try {
            String sourceString = body().sourceIdentifier();
            return new PathIdentifier(sourceString);
        }
        catch (StubObjectException exception) {
            throw new MalformedRequestException("Request has a malformed source identifier!", exception);
        }
    }
}
