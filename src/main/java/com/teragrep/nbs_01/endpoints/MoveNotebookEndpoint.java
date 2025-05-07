package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.Notebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Creates a new Directory. Should be provided with an ID of the parent directory where the new directory should be placed and a name for the Directory
public class MoveNotebookEndpoint implements EndPoint{
    private final Directory root;
    public MoveNotebookEndpoint(Directory root){
        this.root = root;
    }

    public String createResponseBody(String request) {
        try{
            Directory updatedDirectory = root.initializeDirectory(root.path(),new ConcurrentHashMap<>());
            String[] args = request.split(",");
            String notebookId = args[0];
            String directoryId = args[1];

            Notebook notebook = (Notebook) updatedDirectory.findFile(notebookId).load();
            Directory parent = (Directory) updatedDirectory.findFile(directoryId);

            notebook.move(parent);
            return "Moved notebook "+notebook.id();
        }
        catch (FileNotFoundException fileNotFoundException){
            return "Failed to find a file";
        }
        catch (IOException ioException){
            return "Failed to create directory, reason:\n"+ioException;
        }
    }
}
