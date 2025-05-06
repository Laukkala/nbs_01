package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

// Updates the text of a given paragraph within a notebook. Should be provided with a notebook ID and a Paragraph ID as well as the updated content of the paragraph in a comma-separated string
public class UpdateParagraphEndpoint implements EndPoint{
    private final Directory root;
    public UpdateParagraphEndpoint(Directory root){
        this.root = root;
    }

    public String createResponseBody(String request) {
        try{
            root.initializeDirectory(root.path(),new ConcurrentHashMap<>(root.children()));
            String[] args = request.split(",");
            String notebookId = args[0];
            String paragraphId = args[1];
            String updatedParagraph = args[2];
            Notebook notebook = (Notebook) root.findFile(notebookId).load();
            LinkedHashMap<String,Paragraph> paragraphs = new LinkedHashMap<>(notebook.paragraphs());
            Paragraph paragraph = paragraphs.get(paragraphId);

            Script editedScript = new Script(updatedParagraph);
            paragraphs.put(paragraphId,new Paragraph(paragraphId,paragraph.title(),editedScript));
            Notebook newNotebook = new Notebook(notebook.title(),notebook.id(),notebook.path(),paragraphs);
            newNotebook.save();
            return "Notebook edited successfully";
        } catch (IOException ioException){
            return "Failed to edit notebook, reason:\n"+ioException;
        }
    }
}
