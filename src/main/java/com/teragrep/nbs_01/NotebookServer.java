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

import java.util.concurrent.ConcurrentHashMap;

// A thread that registers all endpoints users can connect to and starts the Jetty server.
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
            Directory root = new Directory("root",configuration.notebookDirectory()).initializeDirectory(configuration.notebookDirectory(),new ConcurrentHashMap<>());
            // Endpoints that supports upgrading to WebSocket communication. Also responds to standard HTTP requests.
            pathMappingsHandler.addMapping(PathSpec.from("/list"),new JettyUpgradeableHTTPConnection(new ListEndPoint(root)));
            pathMappingsHandler.addMapping(PathSpec.from("/ping"),new JettyUpgradeableHTTPConnection(new PingEndpoint()));
            pathMappingsHandler.addMapping(PathSpec.from("/find"),new JettyUpgradeableHTTPConnection(new FindEndPoint(root)));
            pathMappingsHandler.addMapping(PathSpec.from("/new"),new JettyUpgradeableHTTPConnection(new CreateNotebookEndpoint(root)));
            pathMappingsHandler.addMapping(PathSpec.from("/move"),new JettyUpgradeableHTTPConnection(new MoveNotebookEndpoint(root)));
            pathMappingsHandler.addMapping(PathSpec.from("/newDirectory"),new JettyUpgradeableHTTPConnection(new CreateDirectoryEndpoint(root)));
            pathMappingsHandler.addMapping(PathSpec.from("/delete"),new JettyUpgradeableHTTPConnection(new DeleteNotebookEndpoint(root)));
            pathMappingsHandler.addMapping(PathSpec.from("/update"),new JettyUpgradeableHTTPConnection(new UpdateParagraphEndpoint(root)));
            // Endpoint that doesn't support upgrading to WebSocket communication. Takes only HTTP requests.
            pathMappingsHandler.addMapping(PathSpec.from("/hello"),new JettyHTTPConnection(new PingEndpoint()));
            contextHandler.setHandler(pathMappingsHandler);
            ServerWebSocketContainer container = ServerWebSocketContainer.ensure(server, contextHandler);
            server.start();
            System.out.println("Server started!");
        } catch (Exception exception){
            System.err.println(exception);
        }
    }
}

