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

import com.teragrep.nbs_01.endpoints.*;
import com.teragrep.nbs_01.handlers.JettyHTTPConnection;
import com.teragrep.nbs_01.handlers.JettyUpgradeableHTTPConnection;
import com.teragrep.nbs_01.repository.Directory;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.eclipse.jetty.websocket.server.ServerWebSocketContainer;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

// A thread that registers all endpoints users can connect to and starts the Jetty server.
public class NotebookServer extends Thread {

    private final Configuration configuration;

    public NotebookServer(Configuration configuration) {
        this.configuration = configuration;
    }

    public void run() {
        // Start jetty server
        try {
            // Jetty setup
            Server server = new Server(configuration.serverPort());
            ContextHandler contextHandler = new ContextHandler("/notebook");
            server.setHandler(contextHandler);
            PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();
            Directory root = new Directory("root", configuration.notebookDirectory())
                    .initializeDirectory(configuration.notebookDirectory(), new ConcurrentHashMap<>());
            // Endpoints that supports upgrading to WebSocket communication. Also responds to standard HTTP requests.
            pathMappingsHandler
                    .addMapping(PathSpec.from("/list"), new JettyUpgradeableHTTPConnection(new ListEndPoint(root)));
            pathMappingsHandler
                    .addMapping(PathSpec.from("/ping"), new JettyUpgradeableHTTPConnection(new PingEndpoint()));
            pathMappingsHandler
                    .addMapping(PathSpec.from("/find"), new JettyUpgradeableHTTPConnection(new FindEndPoint(root)));
            pathMappingsHandler
                    .addMapping(PathSpec.from("/new"), new JettyUpgradeableHTTPConnection(new CreateNotebookEndpoint(root)));
            pathMappingsHandler
                    .addMapping(PathSpec.from("/move"), new JettyUpgradeableHTTPConnection(new MoveNotebookEndpoint(root)));
            pathMappingsHandler
                    .addMapping(PathSpec.from("/newDirectory"), new JettyUpgradeableHTTPConnection(new CreateDirectoryEndpoint(root)));
            pathMappingsHandler
                    .addMapping(PathSpec.from("/moveDirectory"), new JettyUpgradeableHTTPConnection(new MoveDirectoryEndpoint(root)));
            pathMappingsHandler
                    .addMapping(PathSpec.from("/delete"), new JettyUpgradeableHTTPConnection(new DeleteNotebookEndpoint(root)));
            pathMappingsHandler
                    .addMapping(PathSpec.from("/update"), new JettyUpgradeableHTTPConnection(new UpdateParagraphEndpoint(root)));
            // Endpoint that doesn't support upgrading to WebSocket communication. Takes only HTTP requests.
            pathMappingsHandler.addMapping(PathSpec.from("/hello"), new JettyHTTPConnection(new PingEndpoint()));
            contextHandler.setHandler(pathMappingsHandler);
            ServerWebSocketContainer container = ServerWebSocketContainer.ensure(server, contextHandler);
            server.start();
            System.out.println("Server started!");
        }
        catch (IOException exception) {
            System.err.println("An error occurred while configuring server:");
            System.err.println(exception);
        }
        catch (Exception exception) {
            System.err.println("An error occurred while starting server:");
            System.err.println(exception);
        }
    }
}
