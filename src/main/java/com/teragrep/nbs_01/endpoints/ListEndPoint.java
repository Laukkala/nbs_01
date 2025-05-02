package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// Lists all the ID's of saved notebooks
public class ListEndPoint implements EndPoint {

    private final Directory root;

    public ListEndPoint(Directory root){
        this.root = root;
    }

    @Override
    public String createResponseBody(String request) {
        // Find all notebooks from Directory structure
        StringBuilder sb = new StringBuilder();
        ZeppelinFile foundFile;
        Directory directoryToSearch;
        try{
            foundFile = root.findFile(request);
            directoryToSearch = (Directory) foundFile;
        }
        catch (FileNotFoundException fileNotFoundException){
            directoryToSearch = root;
        }
        try{
            Directory updatedDirectory = directoryToSearch.initializeDirectory(directoryToSearch.path(),new ConcurrentHashMap<>());
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
