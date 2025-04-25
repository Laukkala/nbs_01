package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.nio.ByteBuffer;
import java.util.List;

// This endpoint lists all the saved notebooks the user has access to.
public class WebSocketListEndPoint extends WebSocketEndPoint {

    private final Directory root;

    public WebSocketListEndPoint(Directory root){
        this.root = root;
    }

    @Override
    public String createResponse(String request) {
        System.out.println("User "+session().getRemoteSocketAddress()+" wants to list all notebooks!");

        // Find all notebooks from Directory structure
        StringBuilder sb = new StringBuilder();
        List<ZeppelinFile> files = root.listAllChildren();
        for (ZeppelinFile file:files) {
            if(!file.isDirectory()){
                sb.append(file.id());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
