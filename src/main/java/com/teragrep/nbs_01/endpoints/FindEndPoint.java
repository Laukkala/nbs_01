package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

// Returns the JSON content of a Notebook if a matching ID is provided.
public class FindEndPoint implements EndPoint {

    private final Directory root;

    public FindEndPoint(Directory root){
        this.root = root;
    }

    @Override
    public String createResponseBody(String request) {
        // Find a notebooks from Directory structure based on given ID
        try{
            Directory updatedDirectory = root.initializeDirectory(root.path(),new ConcurrentHashMap<>());
            ZeppelinFile file = updatedDirectory.findFile(request);
            if (!file.isDirectory()){
                return file.readFile();
            }
            else {
                return "Notebook not found";
            }
        }catch (FileNotFoundException fileNotFoundException){
            return "Notebook not found!";
        }catch (IOException ioException){
            return "An error occurred";
        }
    }
}
