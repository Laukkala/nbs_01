package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.nio.ByteBuffer;
import java.util.List;

// This endpoint lists all the saved notebooks the user has access to.
public class HTTPListEndPoint extends HTTPEndPoint {
    private final Directory root;
    public HTTPListEndPoint(Directory root){
        this.root = root;
    }

    public String createResponse(String request){
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
