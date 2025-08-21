/*
 * Notebook server for Teragrep Backend (nbs_01)
 * Copyright (C) 2025 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
package com.teragrep.nbs_01.endpoints.paragraph;

import com.teragrep.nbs_01.endpoints.FileSystemEndPoint;
import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.repository.*;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.ExceptionResponse;
import com.teragrep.nbs_01.responses.SimpleResponse;
import com.teragrep.nbs_01.responses.JsonResponse;
import jakarta.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Updates the text and optionally the title of a given paragraph within a Notebook. Should be provided with the path to the Notebook, the ID of the  Paragraph as well as the updated content of the Paragraph.
public class UpdateParagraphEndpoint implements FileSystemEndPoint {

    private final Directory root;

    public UpdateParagraphEndpoint(Directory root) {
        this.root = root;
    }

    public JsonResponse createResponse(Request request) {
        try {
            validateRequestParameters(request);
            JsonObject parameters = request.parameters();
            Path requestPath = Paths.get(parameters.getString("path"));
            Path notebookPath = root.path().resolve(requestPath.subpath(0, requestPath.getNameCount() - 2));

            if (!Files.exists(notebookPath)) {
                throw new FileNotFoundException("Notebook with path " + notebookPath + " not found!");
            }
            Directory updatedDirectory = root
                    .initializeDirectory(root.path(), new ConcurrentHashMap<>(root.children()));
            Notebook notebook = (Notebook) updatedDirectory.findFile(notebookPath).load();
            return createResponse(notebook, parameters);
        }
        catch (MalformedRequestException malformedRequestException) {
            return new ExceptionResponse(HttpStatus.BAD_REQUEST_400, malformedRequestException);
        }
        catch (FileNotFoundException fileNotFoundException) {
            return new ExceptionResponse(HttpStatus.NOT_FOUND_404, fileNotFoundException);
        }
        catch (IOException ioException) {
            return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, ioException);
        }

    }

    private void validateRequestParameters(Request request) throws MalformedRequestException {
        String pathString = request.parameters().getString("path");
        Path requestPath = Paths.get(pathString);
        if (requestPath.getNameCount() < 3) {
            throw new MalformedRequestException(
                    "Request path must be in format  \"{path/to/notebook}/paragraph/{paragraphId}\""
            );
        }
        if (!requestPath.getName(requestPath.getNameCount() - 2).toString().equals("paragraph")) {
            throw new MalformedRequestException(
                    "Request path must be in format  \"{path/to/notebook}/paragraph/{paragraphId}\""
            );
        }
        if (!request.parameters().containsKey("text") && !request.parameters().containsKey("title")) {
            throw new MalformedRequestException("Request does not contain either a text or a title field!");
        }
    }

    @Override
    public JsonResponse createResponse(ZeppelinFile file, JsonObject parameters) {
        try {
            Path requestPath = Paths.get(parameters.getString("path"));
            String paragraphId = requestPath
                    .subpath(requestPath.getNameCount() - 1, requestPath.getNameCount())
                    .toString();
            Notebook notebook = (Notebook) file;

            // Copy the paragraphs from the notebook into a new map
            Map<String, Paragraph> paragraphs = new HashMap<>(notebook.paragraphs());

            // Find the paragraph to be edited
            if (!paragraphs.containsKey(paragraphId)) {
                return new ExceptionResponse(
                        HttpStatus.BAD_REQUEST_400,
                        new MalformedRequestException("Paragraph with Id " + paragraphId + " not found!")
                );
            }
            Paragraph originalParagraph = paragraphs.get(paragraphId);
            String scriptText = parameters.containsKey("text") ? parameters
                    .getString("text") : originalParagraph.script().text();
            String title = parameters.containsKey("title") ? parameters.getString("title") : originalParagraph.title();
            Script newScript = new Script(scriptText);

            Paragraph newParagraph = new Paragraph(originalParagraph.id(), title, newScript);
            paragraphs.put(newParagraph.id(), newParagraph);
            Notebook newNotebook = new Notebook(notebook.title(), notebook.path(), paragraphs);
            newNotebook.save();
            return new SimpleResponse(HttpStatus.OK_200, "Paragraph edited successfully");
        }
        catch (IOException ioException) {
            return new ExceptionResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR_500,
                    new IOException("Failed to save edited notebook!", ioException)
            );
        }
    }

    @Override
    public Directory root() {
        return root;
    }
}
