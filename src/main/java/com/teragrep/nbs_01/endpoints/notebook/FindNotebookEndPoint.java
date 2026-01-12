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

import com.teragrep.nbs_01.endpoints.HTTPEndPoint;
import com.teragrep.nbs_01.http.body.ErrorBody;
import com.teragrep.nbs_01.ErrorEvent;
import com.teragrep.nbs_01.http.body.ExceptionBody;
import com.teragrep.nbs_01.http.body.JSONBody;
import com.teragrep.nbs_01.http.requests.HTTPRequest;
import com.teragrep.nbs_01.http.responses.HTTPResponse;
import com.teragrep.nbs_01.repository.Identifier;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.serialization.JsonNotebook;
import com.teragrep.nbs_01.http.responses.BasicHTTPResponse;
import com.teragrep.nbs_01.repository.Storage;
import com.teragrep.nbs_01.repository.serialization.SerializedNotebook;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Objects;

// Endpoint that finds a Notebook with a given Identifier and returns its contents in JSON format.
public final class FindNotebookEndPoint implements HTTPEndPoint {

    private final Storage root;

    public FindNotebookEndPoint(Storage root) {
        this.root = root;
    }

    @Override
    public HTTPResponse createResponse(HTTPRequest request) {
        try {
            Identifier targetIdentifier = request.targetIdentifier();

            // Deserialize from Storage
            String jsonString = root.read(targetIdentifier);
            JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
            // We cannot simply return the file contents as is back to the UI, since it's possible that there are legacy Zeppelin files, which have a different structure.
            // Therefore, we must first create an in-memory Notebook object first, and call its .json() method, which will format the notebook properly whether it was sourced from a legacy file or not.

            SerializedNotebook serializedNotebook = new JsonNotebook(json);
            Notebook notebook = new Notebook(serializedNotebook.title(), serializedNotebook.paragraphs());

            // Create response
            ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", targetIdentifier.asLongString()));
            headers.add(new BasicHeader("Content-Type", "application/json"));
            return new BasicHTTPResponse(HttpStatus.OK_200, new JSONBody(notebook.json()), headers);
        }
        // If the file cannot be found from Storage, respond with a 404 not found.
        catch (FileNotFoundException notFoundException) {
            return new BasicHTTPResponse(HttpStatus.NOT_FOUND_404, new ExceptionBody(notFoundException));
        }
        // If Storage throws an IOException while accessing file contents, or the file contents retrieved from storage are not valid JSON, respond with a 500 internal server error.
        catch (IOException | JsonException serverErrorException) {
            return new BasicHTTPResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR_500,
                    new ErrorBody(new ErrorEvent(serverErrorException))
            );
        }
        catch (com.teragrep.nbs_01.exceptions.MalformedRequestException malformedRequestException) {
            throw new RuntimeException(malformedRequestException);
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
        FindNotebookEndPoint that = (FindNotebookEndPoint) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
