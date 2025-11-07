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

import com.teragrep.nbs_01.endpoints.EndPoint;
import com.teragrep.nbs_01.http.Body;
import com.teragrep.nbs_01.http.JSONBody;
import com.teragrep.nbs_01.http.StubBody;
import com.teragrep.nbs_01.http.requests.JsonRequest;
import com.teragrep.nbs_01.http.requests.Request;
import com.teragrep.nbs_01.http.responses.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletException;
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
// Delegates each different HTTP request type to a different endpoint.
public final class FileSystemServlet extends jakarta.servlet.http.HttpServlet {

    private final EndPoint getEndPoint;
    private final EndPoint postEndPoint;
    private final EndPoint putEndPoint;
    private final EndPoint deleteEndPoint;
    private final Charset charset;

    // Servlet that assigns an endpoint for each of the supported HTTP request types (GET,POST,PUT,DELETE)
    public FileSystemServlet(
            EndPoint getEndPoint,
            EndPoint postEndPoint,
            EndPoint putEndPoint,
            EndPoint deleteEndPoint
    ) {
        this(getEndPoint, postEndPoint, putEndPoint, deleteEndPoint, Charset.defaultCharset());
    }

    public FileSystemServlet(
            EndPoint getEndPoint,
            EndPoint postEndPoint,
            EndPoint putEndPoint,
            EndPoint deleteEndPoint,
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
        // Extract the path of the requested file from the URL of the received request. getPathInfo() removes the path to the endpoint automatically, leaving only the file path specified after /{ContextPath}/{ServletPath}/.
        String pathString = req.getServletPath();
        Path path = Paths.get("/").relativize(Paths.get(pathString));

        // GET requests should not have a body, but it is possible to include one.
        BufferedReader reader = req.getReader();
        String bodyString = reader.lines().collect(Collectors.joining());
        reader.close();
        Body body;
        if (!bodyString.isEmpty()) {
            JsonReader jsonReader = Json.createReader(new StringReader(bodyString));
            JsonObject bodyJson = jsonReader.readObject();
            body = new JSONBody(bodyJson);
        }
        else {
            body = new StubBody();
        }
        Request endPointRequest = new JsonRequest(path, body);

        // Transfer the Request to an EndPoint and create an HTTP response using the Response object generated by the Endpoint
        Response endPointResponse = getEndPoint.createResponse(endPointRequest);
        resp.setStatus(endPointResponse.status());
        resp.setCharacterEncoding(charset.name());
        for (Header header : endPointResponse.headers()) {
            resp.setHeader(header.getName(), header.getValue());
        }
        // If the endpoint's response has a body, write it to ServletResponse's PrintWriter
        try {
            String responseBody = endPointResponse.body().asString();
            PrintWriter writer = resp.getWriter();
            writer.write(responseBody);
            writer.flush();
            writer.close();
        }
        catch (IllegalStateException malformedBodyException) {
            // Request does not have a body, or it is malformed
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Extract the path of the requested file from the URL of the received request. getPathInfo() removes the path to the endpoint automatically, leaving only the file path specified after /{ContextPath}/{ServLetPath}/.
        String pathString = req.getServletPath();
        Path path = Paths.get("/").relativize(Paths.get(pathString));

        // Read the body of the POST request. Request is expected to be in JSON.
        BufferedReader reader = req.getReader();
        String bodyString = reader.lines().collect(Collectors.joining());
        reader.close();
        Body body;
        if (!bodyString.isEmpty()) {
            JsonReader jsonReader = Json.createReader(new StringReader(bodyString));
            JsonObject bodyJson = jsonReader.readObject();
            body = new JSONBody(bodyJson);
        }
        else {
            body = new StubBody();
        }
        Request endPointRequest = new JsonRequest(path, body);

        // Transfer the Request to an EndPoint and create an HTTP response using the Response object generated by the Endpoint
        Response endPointResponse = postEndPoint.createResponse(endPointRequest);

        resp.setStatus(endPointResponse.status());
        resp.setCharacterEncoding(charset.name());
        for (Header header : endPointResponse.headers()) {
            resp.setHeader(header.getName(), header.getValue());
        }
        // If the endpoint's response has a body, write it to ServletResponse's PrintWriter
        try {
            String responseBody = endPointResponse.body().asString();
            PrintWriter writer = resp.getWriter();
            writer.write(responseBody);
            writer.flush();
            writer.close();
        }
        catch (IllegalStateException malformedBodyException) {
            // Request does not have a body.
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Extract the path of the requested file from the URL of the received request. getPathInfo() removes the path to the endpoint automatically, leaving only the file path specified after /{ContextPath}/{ServLetPath}/.
        String pathString = req.getServletPath();
        Path path = Paths.get("/").relativize(Paths.get(pathString));

        // Read the body of the PUT request
        BufferedReader reader = req.getReader();
        String bodyString = reader.lines().collect(Collectors.joining());
        reader.close();
        Body body;
        if (!bodyString.isEmpty()) {
            JsonReader jsonReader = Json.createReader(new StringReader(bodyString));
            JsonObject bodyJson = jsonReader.readObject();
            body = new JSONBody(bodyJson);
        }
        else {
            body = new StubBody();
        }

        // Create an endPointRequest based on the body. Body should contain JSON key-value pairs containing information about the resource.
        Request endPointRequest = new JsonRequest(path, body);

        // Transfer the Request to an EndPoint and create an HTTP response using the Response object generated by the Endpoint
        Response endPointResponse = putEndPoint.createResponse(endPointRequest);
        resp.setStatus(endPointResponse.status());
        resp.setCharacterEncoding(charset.name());
        for (Header header : endPointResponse.headers()) {
            resp.setHeader(header.getName(), header.getValue());
        }
        // If the endpoint's response has a body, write it to ServletResponse's PrintWriter
        try {
            String responseBody = endPointResponse.body().asString();
            PrintWriter writer = resp.getWriter();
            writer.write(responseBody);
            writer.flush();
            writer.close();
        }
        catch (IllegalStateException malformedBodyException) {
            // Request does not have a body.
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Extract the path of the requested file from the URL of the received request. getPathInfo() removes the path to the endpoint automatically, leaving only the file path specified after /{ContextPath}/{ServLetPath}/.
        String pathString = req.getServletPath();
        Path path = Paths.get("/").relativize(Paths.get(pathString));

        // DELETE requests should not have a body, but it is possible to send one.
        BufferedReader reader = req.getReader();
        String bodyString = reader.lines().collect(Collectors.joining());
        reader.close();
        Body body;
        if (!bodyString.isEmpty()) {
            JsonReader jsonReader = Json.createReader(new StringReader(bodyString));
            JsonObject bodyJson = jsonReader.readObject();
            body = new JSONBody(bodyJson);
        }
        else {
            body = new StubBody();
        }
        Request endPointRequest = new JsonRequest(path, body);

        // Transfer the Request to an EndPoint and create an HTTP response using the Response object generated by the Endpoint
        Response endPointResponse = deleteEndPoint.createResponse(endPointRequest);
        resp.setStatus(endPointResponse.status());
        resp.setCharacterEncoding(charset.name());
        for (Header header : endPointResponse.headers()) {
            resp.setHeader(header.getName(), header.getValue());
        }
        // If the endpoint's response has a body, write it to ServletResponse's PrintWriter
        try {
            String responseBody = endPointResponse.body().asString();
            PrintWriter writer = resp.getWriter();
            writer.write(responseBody);
            writer.flush();
            writer.close();
        }
        catch (IllegalStateException malformedBodyException) {
            // Request does not have a body.
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
