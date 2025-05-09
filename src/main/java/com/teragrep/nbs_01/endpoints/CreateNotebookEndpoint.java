package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.Paragraph;
import com.teragrep.nbs_01.repository.Script;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.Response;
import com.teragrep.nbs_01.responses.StringResponse;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Creates a new notebook. Should be provided with a title and a file path in a comma-separated string
public class CreateNotebookEndpoint implements EndPoint{
    private final Directory root;
    public CreateNotebookEndpoint(Directory root){
        this.root = root;
    }

    public Response createResponse(Request request) {
        try{
            Directory updatedDirectory = root.initializeDirectory(root.path(),new ConcurrentHashMap<>());
            String[] args = request.body().split(",");
            String title = args[0];
            Path path = Paths.get(updatedDirectory.path().toString(),args[1]);
            if(updatedDirectory.contains(path)){
                return new StringResponse(HttpStatus.BAD_REQUEST_400,"Notebook already exists!");
            }
            Paragraph paragraph = new Paragraph(UUID.randomUUID().toString(),"",new Script(""));
            LinkedHashMap paragraphs = new LinkedHashMap();
            paragraphs.put(paragraph.id(),paragraph);
            Notebook newNotebook = new Notebook(title, UUID.randomUUID().toString(),path,paragraphs);
            newNotebook.save();
            return new StringResponse(HttpStatus.OK_200,"Created notebook "+newNotebook.id());
        } catch (IOException ioException){
            return new StringResponse(HttpStatus.INTERNAL_SERVER_ERROR_500,"Failed to create notebook, reason:\n"+ioException);
        }
    }
}
