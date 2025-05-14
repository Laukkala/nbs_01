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
package com.teragrep.nbs_01;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class TestWebSocketClientEndpoint implements Session.Listener {

    private final WebSocketClient webSocketClient;
    private final Session webSocketSession;
    private final URI serverURI;
    private final ArrayList<String> receivedMessages;

    public TestWebSocketClientEndpoint(WebSocketClient webSocketClient, URI serverURI) {
        try {
            this.webSocketClient = webSocketClient;
            this.serverURI = serverURI;
            this.webSocketSession = this.webSocketClient.connect(this, this.serverURI).get();
            this.receivedMessages = new ArrayList<>();
        }
        catch (Exception exception) {
            throw new RuntimeException("Failed to create client!");
        }
    }

    @Override
    public void onWebSocketOpen(Session session) {
        System.out.println("Connected to server at " + session.getRemoteSocketAddress().toString());
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        System.out
                .println(
                        "Disconnected from server: " + webSocketSession.getRemoteSocketAddress().toString()
                                + ", Reason: " + reason
                );
    }

    @Override
    public void onWebSocketBinary(ByteBuffer payload, Callback callback) {
        callback.succeed();
        webSocketSession.demand();
    }

    @Override
    public void onWebSocketText(String message) {
        receivedMessages.add(message);
        System.out
                .println(
                        "Received message " + message + " from server at "
                                + webSocketSession.getRemoteSocketAddress().toString()
                );
        webSocketSession.demand();
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        System.out.println("Error: " + cause.toString());
    }

    public void sendText(String message) {
        webSocketSession.sendText(message, Callback.from(webSocketSession::demand, failure -> {
            webSocketSession.close(StatusCode.SERVER_ERROR, "Failure while sending message: " + message, Callback.NOOP);
        }));
    }

    public ArrayList<String> receivedMessages() {
        return receivedMessages;
    }
}
