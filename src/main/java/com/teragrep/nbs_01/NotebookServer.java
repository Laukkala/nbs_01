package com.teragrep.nbs_01;

import com.teragrep.nbs_01.endpoints.*;
import com.teragrep.nbs_01.handlers.HTTPConnection;
import com.teragrep.nbs_01.handlers.WebSocketConnection;
import com.teragrep.nbs_01.handlers.HTTPHandler;
import com.teragrep.nbs_01.handlers.UpgradeableHTTPHandler;
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
            // Endpoint that supports upgrading to WebSocket communication. Also responds to standard HTTP requests.
            pathMappingsHandler.addMapping(PathSpec.from("/list"),new UpgradeableHTTPHandler(new ListEndPoint(root())));
            // Endpoint that doesn't support upgrading to WebSocket communication. Takes only HTTP requests.
            pathMappingsHandler.addMapping(PathSpec.from("/hello"),new HTTPHandler(new HTTPConnection(new HelloEndpoint())));
            contextHandler.setHandler(pathMappingsHandler);
            ServerWebSocketContainer container = ServerWebSocketContainer.ensure(server, contextHandler);
            server.start();
            System.out.println("Server started!");
        } catch (Exception exception){
            System.err.println(exception);
        }
    }

    private static Directory initializeNotebooks(Path path, ConcurrentHashMap<String, ZeppelinFile> existingFiles) throws IOException {
        // Create a copy of existingFiles so that we don't make any direct edits to it.
        HashMap<String, ZeppelinFile> children = new HashMap<>();
        children.putAll(existingFiles);
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // walkFileTree visits the root directory it's called on.
                // This method operates on the given directory's children so we skip the processing of the root directory here.
                if(dir.equals(path)){
                    return FileVisitResult.CONTINUE;
                }
                // Ignore .git directory. This could be moved somewhere else.
                if(dir.startsWith(path+"/.git")){
                    return FileVisitResult.SKIP_SUBTREE;
                }
                // Read an ID from the file name
                String directoryId = extractIDFromFileName(dir);
                try {
                    // Here we create any child Directories by calling this function recursively.
                    // First we will check if we already have the files of the child Directory in existingFiles, and pass them to the recursive call so that we don't do any unnecessary operations in later recursions.
                    ConcurrentHashMap<String,ZeppelinFile> subtreeChildren = new ConcurrentHashMap<>();
                    if(children.containsKey(directoryId)){
                        subtreeChildren.putAll(children.get(directoryId).children());
                    }
                    Directory subtree = initializeNotebooks(dir,subtreeChildren);
                    children.put(subtree.id(),subtree);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String notebookId = extractIDFromFileName(file);
                if(!children.containsKey(notebookId)){
                    children.put(notebookId,new UnloadedNotebook(notebookId,file));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return super.visitFileFailed(file, exc);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return super.postVisitDirectory(dir, exc);
            }
        });
        Directory root = new Directory(extractIDFromFileName(path),path,children);
        return root;
    }

    private static String extractIDFromFileName(Path file) {
        return extractIDFromFileName(file,"_");
    }
    private static String extractIDFromFileName(Path file, String delimiter) {
        String fileName = file.getFileName().toString();
        if(fileName.contains(delimiter)){
            int idStartIndex = fileName.lastIndexOf(delimiter);
            if(fileName.endsWith(".zpln")){
                int idEndIndex = fileName.lastIndexOf(".zpln");
                return fileName.substring(idStartIndex+1,idEndIndex);
            }
            return fileName.substring(idStartIndex+1);
        }
        // If filename doesn't conform to naming conventions, return the filename iteself.
        else {
            return fileName;
        }
    }

    public Directory root() throws IOException {
        return initializeNotebooks(configuration.notebookDirectory(), new ConcurrentHashMap<>());
    }
}

