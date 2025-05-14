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

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

public final class TestClient implements Session.Listener {

    private static String[] selections = {
            "ping", "find", "new", "list", "delete", "update"
    };

    public static void main(String[] args) throws IOException {
        try {
            System.out
                    .print(
                            "Which endpoint to connect to?\n" + "1) " + selections[0] + "\n" + "2) " + selections[1]
                                    + "\n" + "3) " + selections[2] + "\n" + "4) " + selections[3] + "\n" + "5) "
                                    + selections[4] + "\n" + "6) " + selections[5] + "\n" + "Selection: "
                    );
            BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
            int selection = Integer.parseInt(br1.readLine());
            if (0 > selection || selection > selections.length) {
                System.out.println("Invalid selection!");
                System.exit(1);
            }

            URI serverURI = URI.create("ws://localhost:8080/notebook/" + selections[selection - 1]);
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient, serverURI);

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String message = "";
            while ((message = br.readLine()) != null) {
                client.sendText(message);
            }
            br1.close();
            br.close();
        }
        catch (Exception exception) {
            System.err.println(exception);
        }
    }
}
