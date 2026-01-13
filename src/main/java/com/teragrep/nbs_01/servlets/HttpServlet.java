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

import com.teragrep.nbs_01.protocols.http.path.StubPath;
import com.teragrep.nbs_01.protocols.http.HTTPEndPoint;
import com.teragrep.nbs_01.protocols.http.body.Body;
import com.teragrep.nbs_01.protocols.http.body.StringBody;
import com.teragrep.nbs_01.protocols.http.body.StubBody;
import com.teragrep.nbs_01.protocols.http.BasicHTTPRequest;
import com.teragrep.nbs_01.protocols.http.HTTPRequest;
import com.teragrep.nbs_01.protocols.http.HTTPResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.Header;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.stream.Collectors;

// Generic HTTPServlet that delegates received HTTP requests to a single Endpoint, and generates an HTTP response based on output from the Endpoint.
public final class HttpServlet extends jakarta.servlet.http.HttpServlet {

    private final HTTPEndPoint endPoint;
    private final Charset charset;

    public HttpServlet(final HTTPEndPoint endPoint) {
        this(endPoint, Charset.defaultCharset());
    }

    public HttpServlet(final HTTPEndPoint endPoint, final Charset charset) {
        super();
        this.endPoint = endPoint;
        this.charset = charset;
    }

    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        handleHttpRequest(req, resp, endPoint);
    }

    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        handleHttpRequest(req, resp, endPoint);
    }

    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        handleHttpRequest(req, resp, endPoint);
    }

    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        handleHttpRequest(req, resp, endPoint);
    }

    private void handleHttpRequest(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HTTPEndPoint requestEndPoint
    ) throws IOException {
        // Read body of request
        final BufferedReader reader = req.getReader();
        final String bodyString = reader.lines().collect(Collectors.joining());
        reader.close();
        final Body body;
        if (!bodyString.isEmpty()) {
            body = new StringBody(bodyString);
        }
        else {
            body = new StubBody();
        }
        final HTTPRequest endPointRequest = new BasicHTTPRequest(new StubPath(), body);

        // Transfer the Request to an EndPoint and create an HTTP response using the generated response object
        final HTTPResponse endPointResponse = requestEndPoint.createResponse(endPointRequest);
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
        final HttpServlet that = (HttpServlet) o;
        return Objects.equals(endPoint, that.endPoint) && Objects.equals(charset, that.charset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endPoint, charset);
    }
}
