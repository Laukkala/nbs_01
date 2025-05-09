package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.Response;
import com.teragrep.nbs_01.responses.StringResponse;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

// Returns the JSON content of a Notebook if a matching ID is provided.
public class FindEndPoint implements EndPoint {

    private final Directory root;

    public FindEndPoint(Directory root){
        this.root = root;
    }

    public Response createResponse(Request request) {
        // Find a notebooks from Directory structure based on given ID
        try{
            Directory updatedDirectory = root.initializeDirectory(root.path(),new ConcurrentHashMap<>());
            ZeppelinFile file = updatedDirectory.findFile(request.body());
            if (!file.isDirectory()){
                return new StringResponse(HttpStatus.OK_200,file.load().json().toString());
            }
            else {
                return new StringResponse(HttpStatus.BAD_REQUEST_400,"Notebook not found");
            }
        }catch (FileNotFoundException fileNotFoundException){
            return new StringResponse(HttpStatus.BAD_REQUEST_400,"Notebook not found!");
        }catch (IOException ioException){
            return new StringResponse(HttpStatus.INTERNAL_SERVER_ERROR_500,"An error occurred");
        }
    }
}
