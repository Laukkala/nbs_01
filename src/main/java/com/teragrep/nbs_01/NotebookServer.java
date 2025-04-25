package com.teragrep.nbs_01;

import com.teragrep.nbs_01.endpoints.*;
import com.teragrep.nbs_01.handlers.HTTPConnection;
import com.teragrep.nbs_01.handlers.UpgradeableHTTPConnection;
import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.UnloadedNotebook;
import com.teragrep.nbs_01.repository.ZeppelinFile;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.eclipse.jetty.websocket.server.ServerWebSocketContainer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class NotebookServer extends Thread
{
    private final Configuration configuration;
    public NotebookServer(Configuration configuration){
        this.configuration = configuration;
    }

    public void run(){
        // Start jetty server
        try{
            // Jetty setup
            Server server = new Server(configuration.serverPort());
            ContextHandler contextHandler = new ContextHandler("/notebook");
            server.setHandler(contextHandler);
            PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();
            Directory root = new Directory("root",configuration.notebookDirectory());
            // Endpoints that supports upgrading to WebSocket communication. Also responds to standard HTTP requests.
            pathMappingsHandler.addMapping(PathSpec.from("/list"),new UpgradeableHTTPConnection(new ListEndPoint(root)));
            pathMappingsHandler.addMapping(PathSpec.from("/ping"),new UpgradeableHTTPConnection(new PingEndpoint()));
            pathMappingsHandler.addMapping(PathSpec.from("/find"),new UpgradeableHTTPConnection(new FindEndPoint(root)));
            // Endpoint that doesn't support upgrading to WebSocket communication. Takes only HTTP requests.
            pathMappingsHandler.addMapping(PathSpec.from("/hello"),new HTTPConnection(new PingEndpoint()));
            contextHandler.setHandler(pathMappingsHandler);
            ServerWebSocketContainer container = ServerWebSocketContainer.ensure(server, contextHandler);
            server.start();
            System.out.println("Server started!");
        } catch (Exception exception){
            System.err.println(exception);
        }
    }
}

