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
import com.teragrep.nbs_01.repository.Directory;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

// A thread that registers all endpoints users can connect to and starts the Jetty server.
public class NotebookServer implements Callable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotebookServer.class);
    private final Configuration configuration;
    private final Server server;

    public NotebookServer(Configuration configuration) {
        this.configuration = configuration;
        server = new Server(configuration.serverPort());
        Connector connector = new ServerConnector(server);
        server.addConnector(connector);
    }

    public Object call() throws Exception {
        // Start jetty server
        try {

            // Initialize filesystem
            Directory root = new Directory("root", configuration.notebookDirectory())
                    .initializeDirectory(configuration.notebookDirectory(), new ConcurrentHashMap<>());

            // Jetty setup
            ServletContextHandler contextHandler = new ServletContextHandler();
            contextHandler.setContextPath("/notebook");
            server.setHandler(contextHandler);

            // Servlets mapped to paths. NBS_01 Servlets are defined with the help of Endpoints, but any Servlet implementation can be used.
            FilesystemServlet fileSystemServlet = new FilesystemServlet(
                    new FindEndPoint(root),
                    new UpdateNotebookEndpoint(root),
                    new CreateFileEndpoint(root),
                    new DeleteFileEndpoint(root)
            );
            contextHandler.addServlet(fileSystemServlet, "/filesystem/*");

            HttpServlet pingServlet = new HttpServlet(new PingEndpoint());
            contextHandler.addServlet(pingServlet, "/ping");

            HttpServlet listServlet = new HttpServlet(new ListEndPoint(root));
            contextHandler.addServlet(listServlet, "/list");

            HttpServlet findServlet = new HttpServlet(new FindEndPoint(root));
            contextHandler.addServlet(findServlet, "/find");

            HttpServlet moveDirServlet = new HttpServlet(new MoveDirectoryEndpoint(root));
            contextHandler.addServlet(moveDirServlet, "/moveDirectory");

            HttpServlet moveNotebookServlet = new HttpServlet(new MoveNotebookEndpoint(root));
            contextHandler.addServlet(moveNotebookServlet, "/moveNotebook");

            server.start();
            LOGGER.info("Server started!");
        }
        catch (IOException ioException) {
            LOGGER.error("An error occurred while configuring server", ioException);
            throw ioException;
        }
        catch (Exception exception) {
            LOGGER.error("An error occurred while starting server", exception);
            throw exception;
        }
        // Callable.call() must return an object on a successful invocation
        return true;
    }

    public void stop() throws Exception {
        try {
            server.stop();
        }
        catch (Exception exception) {
            LOGGER.error("Failed to stop server", exception);
            throw exception;
        }
    }
}
