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

import com.teragrep.nbs_01.Request;
import com.teragrep.nbs_01.endpoints.HTTPEndPoint;
import com.teragrep.nbs_01.http.body.Body;
import com.teragrep.nbs_01.http.body.StringBody;
import com.teragrep.nbs_01.http.body.StubBody;
import com.teragrep.nbs_01.http.requests.BasicHTTPRequest;
import com.teragrep.nbs_01.http.requests.HTTPRequest;
import com.teragrep.nbs_01.http.responses.HTTPResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.Header;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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
            HTTPEndPoint getEndPoint,
            HTTPEndPoint postEndPoint,
            HTTPEndPoint putEndPoint,
            HTTPEndPoint deleteEndPoint
    ) {
        this(getEndPoint, postEndPoint, putEndPoint, deleteEndPoint, Charset.defaultCharset());
    }

    public FileSystemServlet(
            HTTPEndPoint getEndPoint,
            HTTPEndPoint postEndPoint,
            HTTPEndPoint putEndPoint,
            HTTPEndPoint deleteEndPoint,
            Charset charset
    ) {
        super();
        this.getEndPoint = getEndPoint;
        this.postEndPoint = postEndPoint;
        this.putEndPoint = putEndPoint;
        this.deleteEndPoint = deleteEndPoint;
        this.charset = charset;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleHTTPRequest(req, resp, getEndPoint);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleHTTPRequest(req, resp, postEndPoint);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleHTTPRequest(req, resp, putEndPoint);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleHTTPRequest(req, resp, deleteEndPoint);
    }

    private void handleHTTPRequest(HttpServletRequest req, HttpServletResponse resp, HTTPEndPoint endPoint)
            throws IOException {
        // Extract the path of the requested file from the URL of the received request. getPathInfo() removes the path to the endpoint automatically, leaving only the file path specified after /{ContextPath}/{ServLetPath}/.
        String pathString = req.getServletPath();
        Path path = Paths.get("/").relativize(Paths.get(pathString));

        Request.RequestType type;
        if (req.getContextPath().equals("/paragraph")) {
            type = Request.RequestType.PARAGRAPH;
        }
        else if (req.getContextPath().equals("/notebook")) {
            type = Request.RequestType.NOTEBOOK;
        }
        else if (req.getContextPath().equals("/directory")) {
            type = Request.RequestType.DIRECTORY;
        }
        else {
            type = Request.RequestType.GENERIC;
        }

        BufferedReader reader = req.getReader();
        String bodyString = reader.lines().collect(Collectors.joining());
        reader.close();
        Body body;
        if (!bodyString.isEmpty()) {
            body = new StringBody(bodyString);
        }
        else {
            body = new StubBody();
        }
        HTTPRequest endPointRequest = new BasicHTTPRequest(type, path, body);

        // Transfer the Request to an EndPoint and create an HTTP response using the Response object generated by the Endpoint
        HTTPResponse endPointResponse = endPoint.createResponse(endPointRequest);
        resp.setStatus(endPointResponse.status());
        resp.setCharacterEncoding(charset.name());
        for (Header header : endPointResponse.headers()) {
            resp.setHeader(header.getName(), header.getValue());
        }
        // If the endpoint's response has a body, write it to ServletResponse's PrintWriter
        if (!endPointResponse.body().isStub()) {
            PrintWriter writer = resp.getWriter();
            try {
                writer.write(endPointResponse.body().asString());
            }
            catch (com.teragrep.nbs_01.exceptions.StubObjectException e) {
                throw new RuntimeException(e);
            }
            writer.flush();
            writer.close();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileSystemServlet that = (FileSystemServlet) o;
        return Objects.equals(getEndPoint, that.getEndPoint) && Objects
                .equals(postEndPoint, that.postEndPoint) && Objects.equals(putEndPoint, that.putEndPoint)
                && Objects.equals(deleteEndPoint, that.deleteEndPoint) && Objects.equals(charset, that.charset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEndPoint, postEndPoint, putEndPoint, deleteEndPoint, charset);
    }
}
