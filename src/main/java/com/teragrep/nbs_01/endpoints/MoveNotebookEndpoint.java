package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.Response;
import com.teragrep.nbs_01.responses.StringResponse;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

// Creates a new Directory. Should be provided with an ID of the parent directory where the new directory should be placed and a name for the Directory
public class MoveNotebookEndpoint implements EndPoint{
    private final Directory root;
    public MoveNotebookEndpoint(Directory root){
        this.root = root;
    }

    public Response createResponse(Request request) {
        try{
            Directory updatedDirectory = root.initializeDirectory(root.path(),new ConcurrentHashMap<>());
            String[] args = request.body().split(",");
            String notebookId = args[0];
            String directoryId = args[1];

            Notebook notebook = (Notebook) updatedDirectory.findFile(notebookId).load();
            Directory parent = (Directory) updatedDirectory.findFile(directoryId);

            notebook.move(parent);
            return new StringResponse(HttpStatus.OK_200,"Moved notebook "+notebook.id());
        }
        catch (FileNotFoundException fileNotFoundException){
            return new StringResponse(HttpStatus.BAD_REQUEST_400,"Failed to find a file");
        }
        catch (IOException ioException){
            return new StringResponse(HttpStatus.INTERNAL_SERVER_ERROR_500,"Failed to create directory, reason:\n"+ioException);
        }
    }
}
