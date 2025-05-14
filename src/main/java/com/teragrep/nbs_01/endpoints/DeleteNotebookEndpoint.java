package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.Response;
import com.teragrep.nbs_01.responses.StringResponse;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

// Creates a new notebook. Should be provided with a title and a file path in a comma-separated string
public class DeleteNotebookEndpoint implements EndPoint{
    private final Directory root;
    public DeleteNotebookEndpoint(Directory root){
        this.root = root;
    }

    public Response createResponse(Request request) {
        try{
            Directory updatedDirectory = root.initializeDirectory(root.path(),new ConcurrentHashMap<>());
            ZeppelinFile file = updatedDirectory.findFile(request.body());
            file.delete();
            return new StringResponse(HttpStatus.OK_200,"Notebook deleted");
        } catch (IOException ioException){
            return new StringResponse(HttpStatus.INTERNAL_SERVER_ERROR_500,"Failed to delete notebook, reason:\n"+ioException);
        }
    }
}
