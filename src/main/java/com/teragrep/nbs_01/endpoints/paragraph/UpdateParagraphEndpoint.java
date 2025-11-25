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

import com.teragrep.nbs_01.endpoints.EndPoint;
import com.teragrep.nbs_01.exceptions.BodyNotFoundException;
import com.teragrep.nbs_01.exceptions.MalformedBodyException;
import com.teragrep.nbs_01.http.ErrorBody;
import com.teragrep.nbs_01.http.ErrorEvent;
import com.teragrep.nbs_01.http.ExceptionBody;
import com.teragrep.nbs_01.http.JSONBody;
import com.teragrep.nbs_01.repository.*;
import com.teragrep.nbs_01.http.requests.Request;
import com.teragrep.nbs_01.http.responses.BasicResponse;
import com.teragrep.nbs_01.http.responses.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

// Updates the text and optionally the title of a given paragraph within a Notebook. Should be provided with the path to the Notebook, the ID of the  Paragraph as well as the updated content of the Paragraph.
public final class UpdateParagraphEndpoint implements EndPoint {

    private final FileTree root;

    public UpdateParagraphEndpoint(FileTree root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        try {
            validateRequestParameters(request);
            Path requestPath = request.path();
            String paragraphId = requestPath
                    .subpath(requestPath.getNameCount() - 1, requestPath.getNameCount())
                    .toString();
            Path notebookPath = root.path().resolve(requestPath.subpath(0, requestPath.getNameCount() - 2));
            JsonObject parameters = Json
                    .createObjectBuilder(request.body().asJson().asJsonObject())
                    .add("paragraphId", paragraphId)
                    .build();
            List<Path> currentFiles = root.list();
            if (!currentFiles.contains(notebookPath)) {
                throw new FileNotFoundException(
                        "Notebook with path " + root.path().relativize(notebookPath) + " not found!"
                );
            }
            Notebook notebook = new Notebook(notebookPath).load();

            // Copy the paragraphs from the notebook into a new map
            Map<String, Paragraph> paragraphs = new HashMap<>(notebook.paragraphs());

            // Find the paragraph to be edited
            if (!paragraphs.containsKey(paragraphId)) {
                throw new MalformedBodyException("Paragraph with Id " + paragraphId + " not found!");
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
            ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", request.path().toString()));
            headers.add(new BasicHeader("Content-Type", "application/json"));
            return new BasicResponse(HttpStatus.OK_200, new JSONBody(newParagraph.json()), headers);
        }
        catch (BodyNotFoundException bodyNotFoundException) {
            return new BasicResponse(HttpStatus.BAD_REQUEST_400, new ExceptionBody(bodyNotFoundException));
        }
        catch (MalformedBodyException malformedBodyException) {
            return new BasicResponse(HttpStatus.BAD_REQUEST_400, new ExceptionBody(malformedBodyException));
        }
        catch (FileNotFoundException fileNotFoundException) {
            return new BasicResponse(HttpStatus.NOT_FOUND_404, new ExceptionBody(fileNotFoundException));
        }
        catch (IOException ioException) {
            return new BasicResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, new ErrorBody(new ErrorEvent(ioException)));
        }

    }

    private void validateRequestParameters(Request request) throws MalformedBodyException, BodyNotFoundException {
        Path requestPath = request.path();
        JsonStructure json = request.body().asJson();
        if (requestPath.getNameCount() < 3) {
            throw new MalformedBodyException(
                    "Request path must be in format  \"{path/to/notebook}/paragraph/{paragraphId}\""
            );
        }
        if (!requestPath.getName(requestPath.getNameCount() - 2).toString().equals("paragraph")) {
            throw new MalformedBodyException(
                    "Request path must be in format  \"{path/to/notebook}/paragraph/{paragraphId}\""
            );
        }
        if (!json.asJsonObject().containsKey("text") && !json.asJsonObject().containsKey("title")) {
            throw new MalformedBodyException("Request does not contain either a text or a title field!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateParagraphEndpoint that = (UpdateParagraphEndpoint) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
