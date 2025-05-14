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
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.requests.SimpleRequest;
import com.teragrep.nbs_01.responses.Response;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.nio.ByteBuffer;

// A Jetty listener for a WebSocket connection to some Endpoint.
// Creates a session between Client and Server once the connection has been opened, through which communication is routed
public class JettyWebSocketConnection implements Session.Listener {

    private Session session;
    private final EndPoint endPoint;

    public JettyWebSocketConnection(EndPoint endPoint) {
        this.endPoint = endPoint;
    }

    // Jetty creates a Session when onWebSocketOpen is called, so we have to assign the Session here instead of in the constructor.
    @Override
    public void onWebSocketOpen(Session session) {
        this.session = session;
        session.demand();
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
    }

    @Override
    public void onWebSocketBinary(ByteBuffer payload, Callback callback) {
        callback.succeed();
        session.demand();
    }

    @Override
    public void onWebSocketText(String message) {
        Request request = new SimpleRequest(message);
        Response response = endPoint.createResponse(request);
        session.sendText(response.parse(), Callback.from(() -> {
            session.demand();
        }, failure -> {
            session.close(StatusCode.SERVER_ERROR, "failure", Callback.NOOP);
        }));
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        System.err.println("Server error: " + cause.toString());
        session.close(StatusCode.SERVER_ERROR, "Websocket Error occurred", Callback.NOOP);
    }
}
