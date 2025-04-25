package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;

import java.util.List;

// This endpoint lists all the saved notebooks the user has access to.
public class ListEndPoint implements EndPoint {

    private final Directory root;

    public ListEndPoint(Directory root){
        this.root = root;
    }

    @Override
    public String createResponse(String request) {
        System.out.println("Responding to request "+request);
        // Find all notebooks from Directory structure
        StringBuilder sb = new StringBuilder();
        List<ZeppelinFile> files = root.listAllChildren();
        for (ZeppelinFile file : files) {
            if (!file.isDirectory()) {
                sb.append(file.id());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
