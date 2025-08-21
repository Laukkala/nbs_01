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
import com.teragrep.nbs_01.requests.JsonRequest;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.Response;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

// Generic HTTPServlet that delegates received HTTP requests to a single Endpoint, and generates an HTTP response based on output from the Endpoint.
public final class HttpServlet extends jakarta.servlet.http.HttpServlet {

    private final EndPoint endPoint;
    private final Charset charset;

    public HttpServlet(EndPoint endPoint) {
        this(endPoint, Charset.defaultCharset());
    }

    public HttpServlet(EndPoint endPoint, Charset charset) {
        super();
        this.endPoint = endPoint;
        this.charset = charset;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // GET requests shouldn't have a body, so we generate a JsonRequest with an Empty JSON Object, and ignore any body that might be present.
        Request endPointRequest = new JsonRequest("");

        // Transfer the Request to an EndPoint and create an HTTP response using the generated response object
        Response endPointResponse = endPoint.createResponse(endPointRequest);
        resp.setStatus(endPointResponse.status());
        resp.setContentType(endPointResponse.contentType());
        resp.setCharacterEncoding(charset.name());
        PrintWriter printWriter = resp.getWriter();
        printWriter.print(endPointResponse.body());
        printWriter.flush();

        // Close the writer to avoid resource leaks
        printWriter.close();
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Read the body of the POST request
        BufferedReader reader = req.getReader();
        String body = reader.lines().collect(Collectors.joining());
        // Close the reader to avoid resource leaks.
        reader.close();
        // Create an endPointRequest based on the body.
        Request endPointRequest = new JsonRequest(body);

        // Transfer the Request to an EndPoint and create an HTTP response using the generated response object
        Response endPointResponse = endPoint.createResponse(endPointRequest);
        resp.setStatus(endPointResponse.status());
        resp.setContentType(endPointResponse.contentType());
        resp.setCharacterEncoding(charset.name());
        PrintWriter writer = resp.getWriter();
        writer.write(endPointResponse.body().toString());
        writer.flush();

        // Close the writer to avoid resource leaks
        writer.close();
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Read the body of the PUT request
        BufferedReader reader = req.getReader();
        String body = reader.lines().collect(Collectors.joining());
        // Close the reader to avoid resource leaks.
        reader.close();
        // Create an endPointRequest based on the body.
        Request endPointRequest = new JsonRequest(body);

        // Transfer the Request to an EndPoint and create an HTTP response using the generated response object
        Response endPointResponse = endPoint.createResponse(endPointRequest);
        resp.setStatus(endPointResponse.status());
        resp.setContentType(endPointResponse.contentType());
        resp.setCharacterEncoding(charset.name());
        PrintWriter writer = resp.getWriter();
        writer.write(endPointResponse.body().toString());
        writer.flush();

        // Close the writer to avoid resource leaks
        writer.close();
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Read the body of the DELETE request
        BufferedReader reader = req.getReader();
        String body = reader.lines().collect(Collectors.joining());
        // Close the reader to avoid resource leaks.
        reader.close();
        // Create an endPointRequest based on the body.
        Request endPointRequest = new JsonRequest(body);

        // Transfer the Request to an EndPoint and create an HTTP response using the generated response object
        Response endPointResponse = endPoint.createResponse(endPointRequest);
        resp.setStatus(endPointResponse.status());
        resp.setContentType(endPointResponse.contentType());
        resp.setCharacterEncoding(charset.name());
        PrintWriter writer = resp.getWriter();
        writer.write(endPointResponse.body().toString());
        writer.flush();

        // Close the writer to avoid resource leaks
        writer.close();
    }
}
