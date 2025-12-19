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
package com.teragrep.nbs_01.endpoints.notebook;

import com.teragrep.nbs_01.endpoints.EndPoint;
import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.http.body.ErrorBody;
import com.teragrep.nbs_01.ErrorEvent;
import com.teragrep.nbs_01.http.body.ExceptionBody;
import com.teragrep.nbs_01.http.body.StringBody;
import com.teragrep.nbs_01.repository.*;
import com.teragrep.nbs_01.http.requests.Request;
import com.teragrep.nbs_01.http.responses.BasicResponse;
import com.teragrep.nbs_01.http.responses.Response;
import com.teragrep.nbs_01.repository.serialization.JsonNotebook;
import com.teragrep.nbs_01.repository.serialization.SerializedNotebook;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParsingException;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;

// Copies a Notebook. Should be provided with a path of the File and a path of the source notebook to be copied.
public final class CopyNotebookEndpoint implements EndPoint {

    private final Storage root;

    public CopyNotebookEndpoint(Storage root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        try {
            validateRequest(request);
            JsonObject body = parseBody(request);
            String sourcePathString = body.getString("sourcePath");
            Identifier sourceIdentifier = new Identifier(sourcePathString);
            Identifier destinationIdentifier = new Identifier(request.path().toString());
            // Deserialize from Storage
            JsonObject sourceJson = Json.createReader(new StringReader(root.read(sourceIdentifier))).readObject();
            SerializedNotebook serializedSource = new JsonNotebook(sourceJson);
            Notebook source = new Notebook(serializedSource.title(), serializedSource.paragraphs());
            // Create a copy with newly generated IDs
            Notebook copy = source.copy();
            // Serialize to storage
            SerializedNotebook serializedCopy = new JsonNotebook(copy.json());
            String serializedString = serializedCopy.serialize();
            root.write(destinationIdentifier, serializedString);
            // Generate response
            ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", request.path().toString()));
            headers.add(new BasicHeader("Content-Type", "application/json"));
            return new BasicResponse(HttpStatus.CREATED_201, new StringBody(serializedString), headers);
        }
        catch (FileNotFoundException notFoundException) {
            return new BasicResponse(HttpStatus.NOT_FOUND_404, new ExceptionBody(notFoundException));
        }
        catch (MalformedRequestException | JsonException | FileAlreadyExistsException badRequestException) {
            return new BasicResponse(HttpStatus.BAD_REQUEST_400, new ExceptionBody(badRequestException));
        }
        catch (IOException serverErrorException) {
            return new BasicResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR_500,
                    new ErrorBody(new ErrorEvent(serverErrorException))
            );
        }
    }

    private void validateRequest(Request request) throws MalformedRequestException {
        // Parse Request parameters
        if (request.body().isStub()) {
            throw new MalformedRequestException("Request must contain a Body!");
        }
    }

    private JsonObject parseBody(Request request) throws JsonException {
        String jsonString = request.body().asString();
        try {
            JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
            return json;
        }
        catch (JsonParsingException jsonParsingException) {
            throw new JsonException("Request body contains invalid JSON!", jsonParsingException);
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
        CopyNotebookEndpoint that = (CopyNotebookEndpoint) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
