package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.Response;
import com.teragrep.nbs_01.responses.StringResponse;
import org.eclipse.jetty.http.HttpStatus;

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

    public Response createResponse(Request request) {
        try{
            Directory updatedDirectory = root.initializeDirectory(root.path(),new ConcurrentHashMap<>());
            String[] args = request.body().split(",");
            String parentId = args[0];
            String name = args[1];

            Directory parent = (Directory) updatedDirectory.findFile(parentId);
            String id = UUID.randomUUID().toString();
            Path path = Paths.get(parent.path().toString(),name+"_"+id);
            Directory newDirectory = new Directory(id,path);
            newDirectory.save();
            return new StringResponse(HttpStatus.OK_200,"Created directory "+newDirectory.id());
        }
        catch (FileNotFoundException fileNotFoundException){
            return new StringResponse(HttpStatus.BAD_REQUEST_400,"Directory doesn't exist!");
        }
        catch (IOException ioException){
            return new StringResponse(HttpStatus.INTERNAL_SERVER_ERROR_500,"Failed to create directory, reason:\n"+ioException);
        }
    }
}
