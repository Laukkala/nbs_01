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
import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.ZeppelinFile;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.ExceptionResponse;
import com.teragrep.nbs_01.responses.SimpleResponse;
import com.teragrep.nbs_01.responses.JsonResponse;
import jakarta.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

// Searches for a paragraph from a given Notebook based on a given ParagraphId, and returns its contents in JSON format.
public class FindParagraphEndPoint implements FileSystemEndPoint {

    private final Directory root;

    public FindParagraphEndPoint(Directory root) {
        this.root = root;
    }

    public JsonResponse createResponse(Request request) {
        // Find a notebooks from Directory structure based on given ID
        try {
            validateRequestParameters(request);
            JsonObject parameters = request.parameters();
            String pathString = parameters.getString("path");
            Path requestPath = Paths.get(pathString);
            Path notebookPath = requestPath.subpath(0, requestPath.getNameCount() - 2);

            Directory updatedDirectory = root.initializeDirectory(root.path(), root.children());
            Path path = updatedDirectory.path().resolve(notebookPath);
            ZeppelinFile file = updatedDirectory.findFile(path).load();
            return createResponse(file, parameters);
        }
        catch (FileNotFoundException fileNotFoundException) {
            return new ExceptionResponse(HttpStatus.NOT_FOUND_404, fileNotFoundException);
        }
        catch (IOException ioException) {
            return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, ioException);
        }
        catch (MalformedRequestException malformedRequestException) {
            return new ExceptionResponse(HttpStatus.BAD_REQUEST_400, malformedRequestException);
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
    }

    @Override
    public JsonResponse createResponse(ZeppelinFile file, JsonObject parameters) {
        try {
            if (!file.isDirectory()) {
                String pathString = parameters.getString("path");
                Path requestPath = Paths.get(pathString);
                String paragraphId = requestPath
                        .subpath(requestPath.getNameCount() - 1, requestPath.getNameCount())
                        .toString();

                Notebook notebook = (Notebook) file.load();
                if (notebook.paragraphs().containsKey(paragraphId)) {
                    return new SimpleResponse(
                            HttpStatus.OK_200,
                            notebook.paragraphs().get(paragraphId).json().toString()
                    );
                }
                else {
                    return new ExceptionResponse(
                            HttpStatus.BAD_REQUEST_400,
                            new MalformedRequestException("Paragraph not found!")
                    );
                }
            }
            else {
                return new ExceptionResponse(
                        HttpStatus.BAD_REQUEST_400,
                        new MalformedRequestException("Not a notebook")
                );
            }
        }
        catch (IOException ioException) {
            return new ExceptionResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR_500,
                    new MalformedRequestException("Failed to save notebook!")
            );
        }
    }

    @Override
    public Directory root() {
        return root;
    }
}
