package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Creates a new notebook. Should be provided with a title and a file path in a comma-separated string
public class DeleteNotebookEndpoint implements EndPoint{
    private final Directory root;
    public DeleteNotebookEndpoint(Directory root){
        this.root = root;
    }

    public String createResponse(String request) {
        try{
            Directory updatedDirectory = root.initializeDirectory(root.path(),new ConcurrentHashMap<>());
            ZeppelinFile file = updatedDirectory.findFile(request);
            file.delete();
            return "Notebook deleted";
        } catch (IOException ioException){
            return "Failed to delete notebook, reason:\n"+ioException;
        }
    }
}
