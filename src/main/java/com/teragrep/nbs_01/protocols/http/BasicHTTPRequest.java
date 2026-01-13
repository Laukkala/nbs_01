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

import com.teragrep.nbs_01.StubPath;
import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.exceptions.StubObjectException;
import com.teragrep.nbs_01.protocols.http.body.Body;
import com.teragrep.nbs_01.protocols.http.body.StubBody;
import com.teragrep.nbs_01.repository.identifiers.Identifier;
import com.teragrep.nbs_01.repository.identifiers.PathIdentifier;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import org.apache.http.Header;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic Request implementation
 */
public final class BasicHTTPRequest implements HTTPRequest {

    private final Body body;
    private final Path path;
    private final List<Header> headers;
    private final RequestType type;

    public BasicHTTPRequest() {
        this(RequestType.GENERIC, new StubBody(), new StubPath(), new ArrayList<>());
    }

    public BasicHTTPRequest(Body body) {
        this(RequestType.GENERIC, body, new StubPath(), new ArrayList<>());
    }

    public BasicHTTPRequest(Path path) {
        this(RequestType.GENERIC, new StubBody(), path, new ArrayList<>());
    }

    public BasicHTTPRequest(RequestType type, Path path) {
        this(type, new StubBody(), path, new ArrayList<>());
    }

    public BasicHTTPRequest(Path path, List<Header> headers) {
        this(RequestType.GENERIC, new StubBody(), path, headers);
    }

    public BasicHTTPRequest(Path path, Body body) {
        this(RequestType.GENERIC, body, path, new ArrayList<>());
    }

    public BasicHTTPRequest(RequestType type, Path path, Body body) {
        this(type, body, path, new ArrayList<>());
    }

    public BasicHTTPRequest(RequestType type, Body body, Path path, List<Header> headers) {
        this.type = type;
        this.body = body;
        this.path = path;
        this.headers = headers;
    }

    public Body body() {
        return body;
    }

    public Path path() {
        return path;
    }

    public List<Header> headers() {
        return headers;
    }

    @Override
    public RequestType type() {
        return type;
    }

    @Override
    public String title() throws MalformedRequestException {
        try {
            JsonObject json = Json.createReader(new StringReader(body.asString())).readObject();
            if (json.containsKey("title")) {
                return json.getString("title");
            }
            else {
                throw new JsonException("Json does not contain title!");
            }
        }
        catch (IllegalStateException | JsonException exception) {
            throw new MalformedRequestException("Request has a malformed title!", exception);
        }
        catch (StubObjectException e) {
            return "";
        }
    }

    @Override
    public String targetParagraphId() throws MalformedRequestException {
        final int nameCount = path.getNameCount();
        return path.subpath(nameCount - 1, nameCount).toString();
    }

    @Override
    public String sourceParagraphId() throws MalformedRequestException {
        try {
            JsonObject json = Json.createReader(new StringReader(body.asString())).readObject();
            if (json.containsKey("sourceParagraphId")) {
                return json.getString("sourceParagraphId");
            }
            else {
                throw new JsonException("Json does not contain sourceParagraphId");
            }
        }
        catch (IllegalStateException | JsonException exception) {
            throw new MalformedRequestException("Request has a malformed sourceParagraph identifier!", exception);
        }
        catch (StubObjectException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String text() throws MalformedRequestException {
        try {
            JsonObject json = Json.createReader(new StringReader(body.asString())).readObject();
            if (json.containsKey("text")) {
                return json.getString("text");
            }
            else {
                throw new JsonException("Json does not contain text!");
            }
        }
        catch (IllegalStateException | JsonException exception) {
            throw new MalformedRequestException("Request has a malformed text!", exception);
        }
        catch (StubObjectException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Identifier targetIdentifier() throws MalformedRequestException {
        if (type.equals(RequestType.PARAGRAPH)) {
            if (path.getNameCount() < 2) {
                throw new MalformedRequestException("Request has a malformed identifier!");
            }
            return new PathIdentifier(path.subpath(0, path.getNameCount() - 1));
        }
        else {
            return new PathIdentifier(path);
        }
    }

    @Override
    public Identifier sourceIdentifier() throws MalformedRequestException {
        try {
            JsonObject json = Json.createReader(new StringReader(body.asString())).readObject();
            if (json.containsKey("sourcePath")) {

                return new PathIdentifier(json.getString("sourcePath"));
            }
            else {
                throw new JsonException("Json does not contain sourcePath");
            }
        }
        catch (StubObjectException | JsonException exception) {
            throw new MalformedRequestException("Request has a malformed source identifier!", exception);
        }
    }
}
