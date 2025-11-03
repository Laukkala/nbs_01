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
import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.repository.FileTree;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.Paragraph;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.ErrorResponse;
import com.teragrep.nbs_01.responses.ExceptionResponse;
import com.teragrep.nbs_01.responses.JsonResponse;
import com.teragrep.nbs_01.responses.Response;
import jakarta.json.JsonObject;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Copies a Notebook. Should be provided with a path of the File and a path of the source notebook to be copied.
public final class CopyParagraphEndpoint implements EndPoint {

    private final FileTree root;

    public CopyParagraphEndpoint(FileTree root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        try {
            validateRequestParameters(request);
            JsonObject parameters = request.parameters();
            String sourcePathString = parameters.getString("sourcePath");
            String sourceParagraphId = parameters.getString("sourceParagraphId");
            Path sourcePath = root.path().resolve(Paths.get(sourcePathString));

            Path destinationPath = root
                    .path()
                    .resolve(request.path())
                    .subpath(0, root.path().resolve(request.path()).getNameCount() - 2);
            String destinationParagraphId = request
                    .path()
                    .subpath(request.path().getNameCount() - 1, request.path().getNameCount())
                    .toString();

            List<Path> currentFiles = root.list();
            if (!currentFiles.contains(sourcePath)) {
                throw new FileNotFoundException("No such file: " + parameters.getString("sourcePath") + "!");
            }
            Notebook source = new Notebook(sourcePath).load();
            if (!source.paragraphs().containsKey(sourceParagraphId)) {
                throw new FileNotFoundException("No such paragraph: " + sourceParagraphId + "!");
            }
            Paragraph sourceParagraph = source.paragraphs().get(sourceParagraphId);
            Paragraph copyParagraph = sourceParagraph.copy(destinationParagraphId);

            if (!Files.exists(destinationPath)) {
                throw new FileNotFoundException("No such file: " + root.path().relativize(destinationPath) + "!");
            }
            Notebook destinationNotebook = new Notebook(destinationPath).load();
            Map<String, Paragraph> destinationParagraphs = destinationNotebook.paragraphs();
            if (destinationParagraphs.containsKey(copyParagraph.id())) {
                throw new MalformedRequestException("Paragraph " + destinationParagraphId + " already exists!");
            }
            destinationParagraphs.put(copyParagraph.id(), copyParagraph);

            Notebook editedNotebook = new Notebook(
                    destinationNotebook.title(),
                    destinationNotebook.path(),
                    destinationParagraphs
            );
            editedNotebook.save();
            ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", request.path().toString()));
            return new JsonResponse(HttpStatus.CREATED_201, copyParagraph.json(), headers);
        }
        catch (FileNotFoundException fileNotFoundException) {
            return new ExceptionResponse(HttpStatus.NOT_FOUND_404, fileNotFoundException);
        }
        catch (FileAlreadyExistsException fileAlreadyExistsException) {
            return new ExceptionResponse(HttpStatus.BAD_REQUEST_400, fileAlreadyExistsException);
        }
        catch (IOException ioException) {
            return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, ioException);
        }
        catch (MalformedRequestException malformedRequestException) {
            return new ExceptionResponse(HttpStatus.BAD_REQUEST_400, malformedRequestException);
        }
    }

    private void validateRequestParameters(Request request) throws MalformedRequestException {
        Path requestPath = request.path();
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
        if (!request.parameters().containsKey("sourceParagraphId")) {
            throw new MalformedRequestException("Request does not contain a source paragraph id");
        }
        if (!request.parameters().containsKey("sourcePath")) {
            throw new MalformedRequestException("Request does not contain a source path!");
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
        CopyParagraphEndpoint that = (CopyParagraphEndpoint) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
