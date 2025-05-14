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
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.websocket.server.ServerWebSocketContainer;

// A Jetty Handler for HTTP connections that can be upgraded to a WebSocket connection to some Endpoint.
// If the HTTP connection is an upgrade request, the connection is upgraded to a WebSocket connection and adds a WebSocketConnection object as a listener for incoming WebSocket events
// Otherwise the HTTP request will be handled normally by a HTTPConnection object.
// Creates either an HTTPConnection object or a WebSocketConnection object with the configured EndPoint.
public class JettyUpgradeableHTTPConnection extends Handler.Abstract {

    private final EndPoint endPoint;

    public JettyUpgradeableHTTPConnection(EndPoint endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        if (
            request.getHeaders().contains("Upgrade", "websocket")
                    && request.getHeaders().contains("Connection", "Upgrade")
        ) {
            // Handle a WebSocket connection
            ServerWebSocketContainer container = ServerWebSocketContainer.get(request.getContext());
            boolean upgraded = container
                    .upgrade((rq, rs, cb) -> new JettyWebSocketConnection(endPoint), request, response, callback);
            if (upgraded) {
                return true;
            }
            else {
                // This was supposed to be a WebSocket upgrade request, but something went wrong.
                Response.writeError(request, response, callback, HttpStatus.UPGRADE_REQUIRED_426);
                return true;
            }
        }
        else {
            // Handle a normal HTTP request.
            new JettyHTTPConnection(endPoint).handle(request, response, callback);
            callback.succeeded();
            return true;
        }
    }
}
