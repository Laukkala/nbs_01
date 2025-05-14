package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.Response;
import com.teragrep.nbs_01.responses.StringResponse;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// Lists all the ID's of saved notebooks
public class ListEndPoint implements EndPoint {

    private final Directory root;

    public ListEndPoint(Directory root){
        this.root = root;
    }

    public Response createResponse(Request request) {
        // Find all notebooks from Directory structure
        StringBuilder sb = new StringBuilder();
        ZeppelinFile foundFile;
        Directory directoryToSearch;
        try{
            foundFile = root.findFile(request.body());
            directoryToSearch = (Directory) foundFile;
        }
        catch (FileNotFoundException fileNotFoundException){
            directoryToSearch = root;
        }
        try{
            Directory updatedDirectory = directoryToSearch.initializeDirectory(directoryToSearch.path(),new ConcurrentHashMap<>());
            List<ZeppelinFile> files = updatedDirectory.listAllChildren();
            for (ZeppelinFile file : files) {
                if (!file.isDirectory()) {
                    sb.append(file.id());
                    sb.append("\n");
                }
            }
            return new StringResponse(HttpStatus.OK_200,sb.toString());
        }catch (IOException ioException){
            return new StringResponse(HttpStatus.INTERNAL_SERVER_ERROR_500,"Failed to list notebooks");
        }
    }
}
