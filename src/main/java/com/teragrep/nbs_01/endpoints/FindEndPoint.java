package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;
import org.eclipse.jetty.util.IO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

// This endpoint lists all the saved notebooks the user has access to.
public class FindEndPoint implements EndPoint {

    private final Directory root;

    public FindEndPoint(Directory root){
        this.root = root;
    }

    @Override
    public String createResponse(String request) {
        //System.out.println("Responding to request "+request);
        // Find a notebooks from Directory structure based on given ID
        try{
            ZeppelinFile file = root.findFile(request);
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
