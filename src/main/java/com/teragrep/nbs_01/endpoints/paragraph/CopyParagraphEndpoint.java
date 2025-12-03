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
import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.http.body.ErrorBody;
import com.teragrep.nbs_01.ErrorEvent;
import com.teragrep.nbs_01.http.body.ExceptionBody;
import com.teragrep.nbs_01.http.body.JSONBody;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.Paragraph;
import com.teragrep.nbs_01.http.requests.Request;
import com.teragrep.nbs_01.http.responses.BasicResponse;
import com.teragrep.nbs_01.http.responses.Response;
import com.teragrep.nbs_01.repository.Storage;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

// Copies a Notebook. Should be provided with a path of the File and a path of the source notebook to be copied.
public final class CopyParagraphEndpoint implements EndPoint {

    private final Storage root;

    public CopyParagraphEndpoint(Storage root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        try {
            validateRequestParameters(request);
            JsonObject parameters = request.body().asJson().asJsonObject();
            String sourcePathString = parameters.getString("sourcePath");
            String sourceParagraphId = parameters.getString("sourceParagraphId");
            Path sourcePath = root.root().resolve(Paths.get(sourcePathString));

            Path destinationPath = root
                    .root()
                    .resolve(request.path())
                    .subpath(0, root.root().resolve(request.path()).getNameCount() - 2);
            String destinationParagraphId = request
                    .path()
                    .subpath(request.path().getNameCount() - 1, request.path().getNameCount())
                    .toString();

            JsonObject json = Json.createReader(new StringReader(root.read(sourcePath))).readObject();
            Notebook source = new Notebook().load(json);
            if (!source.paragraphs().containsKey(sourceParagraphId)) {
                throw new FileNotFoundException("No such paragraph: " + sourceParagraphId + "!");
            }
            Paragraph sourceParagraph = source.paragraphs().get(sourceParagraphId);
            Paragraph copyParagraph = sourceParagraph.copy(destinationParagraphId);

            Notebook destinationNotebook = new Notebook()
                    .load(Json.createReader(new StringReader(root.read(destinationPath))).readObject());
            Map<String, Paragraph> destinationParagraphs = destinationNotebook.paragraphs();
            if (destinationParagraphs.containsKey(copyParagraph.id())) {
                throw new MalformedBodyException("Paragraph " + destinationParagraphId + " already exists!");
            }
            destinationParagraphs.put(copyParagraph.id(), copyParagraph);

            Notebook editedNotebook = new Notebook(destinationNotebook.name(), destinationParagraphs);
            root.write(destinationPath, editedNotebook.json().toString());
            ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", request.path().toString()));
            headers.add(new BasicHeader("Content-Type", "application/json"));
            return new BasicResponse(HttpStatus.CREATED_201, new JSONBody(copyParagraph.json()), headers);
        }
        catch (
                MalformedBodyException | BodyNotFoundException | FileAlreadyExistsException
                | MalformedRequestException badRequestException
        ) {
            return new BasicResponse(HttpStatus.BAD_REQUEST_400, new ExceptionBody(badRequestException));
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
        if (!json.asJsonObject().containsKey("sourceParagraphId")) {
            throw new MalformedBodyException("Request does not contain a source paragraph id");
        }
        if (!json.asJsonObject().containsKey("sourcePath")) {
            throw new MalformedBodyException("Request does not contain a source path!");
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
