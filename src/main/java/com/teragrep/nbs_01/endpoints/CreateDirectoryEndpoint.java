package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Creates a new Directory. Should be provided with an ID of the parent directory where the new directory should be placed and a name for the Directory
public class CreateDirectoryEndpoint implements EndPoint{
    private final Directory root;
    public CreateDirectoryEndpoint(Directory root){
        this.root = root;
    }

    public String createResponseBody(String request) {
        try{
            Directory updatedDirectory = root.initializeDirectory(root.path(),new ConcurrentHashMap<>());
            String[] args = request.split(",");
            String parentId = args[0];
            String name = args[1];

            Directory parent = (Directory) root.findFile(parentId);
            String id = UUID.randomUUID().toString();
            Path path = Paths.get(parent.path().toString(),name+"_"+id);
            Directory newDirectory = new Directory(id,path);
            newDirectory.save();
            return "Created directory "+newDirectory.id();
        }
        catch (FileNotFoundException fileNotFoundException){
            return "Directory doesn't exist!";
        }
        catch (IOException ioException){
            return "Failed to create directory, reason:\n"+ioException;
        }

    }
}
