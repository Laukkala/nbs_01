package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.NotebookServer;
import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

// This endpoint lists all the saved notebooks the user has access to.
public class ListEndPoint implements Session.Listener {
    private Session session;
    private final NotebookServer server;

    public ListEndPoint(NotebookServer server){
        this.server = server;
    }
    @Override
    public void onWebSocketOpen(Session session){
        this.session = session;
        System.out.println("New session on ListEndPoint created! "+session.getRemoteSocketAddress().toString());
        session.demand();
    }
    @Override
    public void onWebSocketClose(int statusCode, String reason){
        System.out.println("Client disconnected from ListEndPoint: "+ session.getRemoteSocketAddress().toString() +", Reason: "+reason);
    }

    @Override
    public void onWebSocketBinary(ByteBuffer payload, Callback callback) {
        callback.succeed();
        session.demand();
    }

    @Override
    public void onWebSocketText(String message){
        // Check permissions of the user requesting notebook list, only return authorized notebooks
        String userID = message;
        System.out.println("User "+session.getRemoteSocketAddress()+" wants to list all notebooks!");

        String ret = "";
        // Find all notebooks from Directory structure
        try {
            StringBuilder sb = new StringBuilder();
            List<ZeppelinFile> files = server.root().listAllChildren();
            for (ZeppelinFile file:files) {
                if(!file.isDirectory()){
                    sb.append(file.id());
                    sb.append("\n");
                }
            }
            ret = sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        session.sendText(ret, Callback.from(()->{
            System.out.println("Sent notebook list to user at" + session.getRemoteSocketAddress());
            session.demand();
        },failure -> {
            session.close(StatusCode.SERVER_ERROR, "failure", Callback.NOOP);
        }));
    }
    @Override
    public void onWebSocketError(Throwable cause){
        System.out.println("Server error: "+cause.toString());
    }
}
