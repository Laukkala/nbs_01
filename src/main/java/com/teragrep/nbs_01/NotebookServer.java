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

import com.teragrep.nbs_01.endpoints.directory.CopyDirectoryEndpoint;
import com.teragrep.nbs_01.endpoints.directory.CreateDirectoryEndpoint;
import com.teragrep.nbs_01.endpoints.directory.DeleteDirectoryEndpoint;
import com.teragrep.nbs_01.endpoints.directory.FindDirectoryEndPoint;
import com.teragrep.nbs_01.endpoints.general.DelegatingEndpoint;
import com.teragrep.nbs_01.endpoints.general.ListEndPoint;
import com.teragrep.nbs_01.endpoints.general.PingEndpoint;
import com.teragrep.nbs_01.endpoints.general.StubEndpoint;
import com.teragrep.nbs_01.endpoints.notebook.*;
import com.teragrep.nbs_01.endpoints.paragraph.*;
import com.teragrep.nbs_01.repository.Storage;
import com.teragrep.nbs_01.servlets.FileSystemServlet;
import com.teragrep.nbs_01.servlets.HttpServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

// A thread that registers all endpoints users can connect to and starts the Jetty server.
public class NotebookServer implements Callable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotebookServer.class);
    private final Storage root;
    private final Server jettyServer;

    public NotebookServer(Server jettyServer, Storage root) {
        this.jettyServer = jettyServer;
        this.root = root;
    }

    public Object call() throws Exception {
        // Start jetty server
        try {
            // Jetty setup
            ServletContextHandler notebookContextHandler = new ServletContextHandler();
            notebookContextHandler.setContextPath("/notebook");

            // Servlets mapped to paths. NBS_01 Servlets are defined with the help of Endpoints, but any Servlet implementation can be used.
            FileSystemServlet notebookServlet = new FileSystemServlet(
                    new FindNotebookEndPoint(root), // Endpoint to call on a GET Request
                    new UpdateNotebookEndpoint(root), // Endpoint to call on a POST Request
                    new DelegatingEndpoint(new CopyNotebookEndpoint(root), new CreateNotebookEndpoint(root), new DoAllKeysExistDelegate("sourcePath")), // Endpoint to call on a PUT Request
                    new DeleteNotebookEndpoint(root) // Endpoint to call on a DELETE Request
            );
            notebookContextHandler.addServlet(notebookServlet, "/");

            ServletContextHandler directoryContextHandler = new ServletContextHandler();
            directoryContextHandler.setContextPath("/directory");

            // Servlets mapped to paths. NBS_01 Servlets are defined with the help of Endpoints, but any Servlet implementation can be used.
            FileSystemServlet directoryServlet = new FileSystemServlet(
                    new FindDirectoryEndPoint(root), // Endpoint to call on a GET Request
                    new StubEndpoint(), // Endpoint to call on a POST Request
                    new DelegatingEndpoint(new CopyDirectoryEndpoint(root), new CreateDirectoryEndpoint(root), new DoAllKeysExistDelegate("sourcePath")), // Endpoint to call on a PUT Request
                    new DeleteDirectoryEndpoint(root) // Endpoint to call on a DELETE Request
            );
            directoryContextHandler.addServlet(directoryServlet, "/");

            ServletContextHandler paragraphContextHandler = new RegexServletContext(".*/paragraph/[^/]*/?$");
            paragraphContextHandler.setContextPath("/notebook");

            FileSystemServlet paragraphServlet = new FileSystemServlet(
                    new FindParagraphEndPoint(root), // Endpoint to call on a GET Request
                    new UpdateParagraphEndpoint(root), // Endpoint to call on a POST Request
                    new DelegatingEndpoint(new CopyParagraphEndpoint(root), new CreateParagraphEndpoint(root), new DoAllKeysExistDelegate(Arrays.asList("sourcePath", "sourceParagraph"))), // Endpoint to call on a PUT Request
                    new DeleteParagraphEndpoint(root) // Endpoint to call on a DELETE Request
            );
            paragraphContextHandler.addServlet(paragraphServlet, "/");

            HttpServlet pingServlet = new HttpServlet(new PingEndpoint());
            notebookContextHandler.addServlet(pingServlet, "/ping");

            HttpServlet listServlet = new HttpServlet(new ListEndPoint(root));
            notebookContextHandler.addServlet(listServlet, "/list");

            ContextHandlerCollection collection = new ContextHandlerCollection();
            collection.addHandler(paragraphContextHandler);
            collection.addHandler(notebookContextHandler);
            collection.addHandler(directoryContextHandler);

            jettyServer.setHandler(collection);

            jettyServer.start();
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
            jettyServer.stop();
        }
        catch (Exception exception) {
            LOGGER.error("Failed to stop server", exception);
            throw exception;
        }
    }
}
