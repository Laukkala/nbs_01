package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.Paragraph;
import com.teragrep.nbs_01.repository.Script;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Creates a new notebook. Should be provided with a title and a file path in a comma-separated string
public class CreateNotebookEndpoint implements EndPoint{
    private Directory root;
    public CreateNotebookEndpoint(Directory root){
        this.root = root;
    }

    public String createResponse(String request) {
        try{
            root = root.initializeDirectory(root.path(),new ConcurrentHashMap<>(root.children()));
            String[] args = request.split(",");
            String title = args[0];
            Path path = Paths.get(root.path().toString(),args[1]);
            if(root.contains(path)){
                return "Notebook already exists!";
            }
            Paragraph paragraph = new Paragraph(UUID.randomUUID().toString(),"",new Script(""));
            LinkedHashMap paragraphs = new LinkedHashMap();
            paragraphs.put(paragraph.id(),paragraph);
            Notebook newNotebook = new Notebook(title, UUID.randomUUID().toString(),path,paragraphs);
            newNotebook.save();
            return "Created notebook "+newNotebook.id();
        } catch (IOException ioException){
            return "Failed to create notebook, reason:\n"+ioException;
        }
    }
}
