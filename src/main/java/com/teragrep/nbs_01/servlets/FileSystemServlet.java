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
package com.teragrep.nbs_01.servlets;

import com.teragrep.nbs_01.protocols.http.HTTPEndPoint;
import com.teragrep.nbs_01.protocols.http.body.Body;
import com.teragrep.nbs_01.protocols.http.body.JSONBody;
import com.teragrep.nbs_01.protocols.http.body.StringBody;
import com.teragrep.nbs_01.protocols.http.body.StubBody;
import com.teragrep.nbs_01.protocols.http.BasicHTTPRequest;
import com.teragrep.nbs_01.protocols.http.HTTPRequest;
import com.teragrep.nbs_01.protocols.http.HTTPResponse;
import com.teragrep.nbs_01.protocols.http.path.HTTPBasicRequestPath;
import com.teragrep.nbs_01.protocols.http.path.HTTPParagraphRequestPath;
import com.teragrep.nbs_01.protocols.http.path.HTTPRequestPath;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.Header;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

// HTTPServlet that acts on the Filesystem to Find, Create, Delete and Update Notebooks, Directories and Paragraphs.
// Request to a FileSystemServlet must have a Path, which is used to identify the resource the user wants to operate on.
// Delegates each different HTTP request type to a different endpoint.
public final class FileSystemServlet extends jakarta.servlet.http.HttpServlet {

    private final HTTPEndPoint getEndPoint;
    private final HTTPEndPoint postEndPoint;
    private final HTTPEndPoint putEndPoint;
    private final HTTPEndPoint deleteEndPoint;
    private final Charset charset;

    // Servlet that assigns an endpoint for each of the supported HTTP request types (GET,POST,PUT,DELETE)
    public FileSystemServlet(
            final HTTPEndPoint getEndPoint,
            final HTTPEndPoint postEndPoint,
            final HTTPEndPoint putEndPoint,
            final HTTPEndPoint deleteEndPoint
    ) {
        this(getEndPoint, postEndPoint, putEndPoint, deleteEndPoint, Charset.defaultCharset());
    }

    public FileSystemServlet(
            final HTTPEndPoint getEndPoint,
            final HTTPEndPoint postEndPoint,
            final HTTPEndPoint putEndPoint,
            final HTTPEndPoint deleteEndPoint,
            final Charset charset
    ) {
        this.getEndPoint = getEndPoint;
        this.postEndPoint = postEndPoint;
        this.putEndPoint = putEndPoint;
        this.deleteEndPoint = deleteEndPoint;
        this.charset = charset;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        handleHTTPRequest(req, resp, getEndPoint);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        handleHTTPRequest(req, resp, postEndPoint);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        handleHTTPRequest(req, resp, putEndPoint);
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        handleHTTPRequest(req, resp, deleteEndPoint);
    }

    private void handleHTTPRequest(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HTTPEndPoint endPoint
    ) throws IOException {
        // Extract the path of the requested file from the URL of the received request. getPathInfo() removes the path to the endpoint automatically, leaving only the file path specified after /{ContextPath}/{ServLetPath}/.
        final String pathString = req.getServletPath();
        final Path path = Paths.get("/").relativize(Paths.get(pathString));
        final HTTPRequestPath requestPath;

        if (req.getContextPath().equals("/paragraph")) {
            requestPath = new HTTPParagraphRequestPath(path);
        }
        else if (req.getContextPath().equals("/notebook")) {
            requestPath = new HTTPBasicRequestPath(path);
        }
        else if (req.getContextPath().equals("/directory")) {
            requestPath = new HTTPBasicRequestPath(path);
        }
        else {
            requestPath = new HTTPBasicRequestPath(path);
        }

        final BufferedReader reader = req.getReader();
        final String bodyString = reader.lines().collect(Collectors.joining());
        reader.close();
        Body body;
        if (!bodyString.isEmpty()) {
            try {
                final JsonObject json = Json.createReader(new StringReader(bodyString)).readObject();
                body = new JSONBody(json);
            }
            catch (final JsonException e) {
                body = new StringBody(bodyString);
            }
        }
        else {
            body = new StubBody();
        }
        final HTTPRequest endPointRequest = new BasicHTTPRequest(requestPath, body);

        // Transfer the Request to an EndPoint and create an HTTP response using the Response object generated by the Endpoint
        final HTTPResponse endPointResponse = endPoint.createResponse(endPointRequest);
        resp.setStatus(endPointResponse.status());
        resp.setCharacterEncoding(charset.name());
        for (final Header header : endPointResponse.headers()) {
            resp.setHeader(header.getName(), header.getValue());
        }
        // If the endpoint's response has a body, write it to ServletResponse's PrintWriter
        if (!endPointResponse.body().isStub()) {
            final PrintWriter writer = resp.getWriter();
            try {
                writer.write(endPointResponse.body().asString());
            }
            catch (final com.teragrep.nbs_01.exceptions.StubObjectException e) {
                throw new RuntimeException(e);
            }
            writer.flush();
            writer.close();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FileSystemServlet that = (FileSystemServlet) o;
        return Objects.equals(getEndPoint, that.getEndPoint) && Objects
                .equals(postEndPoint, that.postEndPoint) && Objects.equals(putEndPoint, that.putEndPoint)
                && Objects.equals(deleteEndPoint, that.deleteEndPoint) && Objects.equals(charset, that.charset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEndPoint, postEndPoint, putEndPoint, deleteEndPoint, charset);
    }
}
