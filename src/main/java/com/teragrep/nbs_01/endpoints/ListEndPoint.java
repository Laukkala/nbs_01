package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// This endpoint lists all the saved notebooks the user has access to.
public class ListEndPoint implements EndPoint {

    private final Directory root;

    public ListEndPoint(Directory root){
        this.root = root;
    }

    @Override
    public String createResponse(String request) {
        //System.out.println("Responding to request "+request);
        // Find all notebooks from Directory structure
        StringBuilder sb = new StringBuilder();
        try{
            Directory updatedDirectory = root.initializeDirectory(root.path(),new ConcurrentHashMap<>());
            List<ZeppelinFile> files = updatedDirectory.listAllChildren();
            for (ZeppelinFile file : files) {
                if (!file.isDirectory()) {
                    sb.append(file.id());
                    sb.append("\n");
                }
            }
            return sb.toString();
        }catch (IOException ioException){
            return "Failed to list notebooks";
        }
    }
}
