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
package com.teragrep.nbs_01.handlers;

import com.teragrep.nbs_01.endpoints.EndPoint;
import com.teragrep.nbs_01.requests.SimpleRequest;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;

// A Jetty Handler for HTTP connections to some Endpoint.
// Extracts the contents of the request and delegates it to it's EndPoint instance for processing, and then returns the response received from the EndPoint.
public class JettyHTTPConnection extends Handler.Abstract {

    private final EndPoint endPoint;

    public JettyHTTPConnection(EndPoint endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public boolean handle(Request jettyRequest, Response jettyResponse, Callback callback) throws Exception {
        try {
            com.teragrep.nbs_01.requests.Request request = new SimpleRequest(Content.Source.asString(jettyRequest));
            com.teragrep.nbs_01.responses.Response response = endPoint.createResponse(request);
            jettyResponse.setStatus(response.status());
            jettyResponse.write(true, ByteBuffer.wrap(response.parse().getBytes()), Callback.NOOP);
            callback.succeeded();
            return true;
        }
        catch (Exception exception) {
            callback.failed(exception);
            return false;
        }
    }
}
