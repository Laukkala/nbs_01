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
import com.teragrep.nbs_01.http.body.ErrorBody;
import com.teragrep.nbs_01.ErrorEvent;
import com.teragrep.nbs_01.http.body.ExceptionBody;
import com.teragrep.nbs_01.repository.serialization.JsonNotebook;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.http.requests.Request;
import com.teragrep.nbs_01.http.responses.BasicResponse;
import com.teragrep.nbs_01.http.responses.Response;
import com.teragrep.nbs_01.repository.Storage;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

// Deletes a Paragraph from a given Notebook. Should be provided with a path of the Notebook
public final class DeleteParagraphEndpoint implements EndPoint {

    private final Storage root;

    public DeleteParagraphEndpoint(Storage root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        try {
            validateRequest(request);
            Path requestPath = request.path();
            Path notebookPath = requestPath.subpath(0, requestPath.getNameCount() - 2);

            String paragraphId = requestPath
                    .subpath(requestPath.getNameCount() - 1, requestPath.getNameCount())
                    .toString();

            Path path = root.root().resolve(notebookPath);

            JsonObject json = Json.createReader(new StringReader(root.read(path))).readObject();
            JsonNotebook jsonNotebook = new JsonNotebook(json);
            Notebook notebook = new Notebook(jsonNotebook.title(), jsonNotebook.paragraphs());
            if (notebook.paragraphs().containsKey(paragraphId)) {
                notebook.paragraphs().remove(paragraphId);
                root.write(path, notebook.json().toString());

                ArrayList<Header> headers = new ArrayList<>();
                headers.add(new BasicHeader("Location", request.path().toString()));
                return new BasicResponse(HttpStatus.NO_CONTENT_204, headers);
            }
            else {
                throw new MalformedRequestException("Paragraph " + paragraphId + " doesn't exist!");
            }
        }
        catch (FileNotFoundException notFoundException) {
            return new BasicResponse(HttpStatus.NOT_FOUND_404, new ExceptionBody(notFoundException));
        }
        catch (IOException serverErrorException) {
            return new BasicResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR_500,
                    new ErrorBody(new ErrorEvent(serverErrorException))
            );
        }
        catch (MalformedRequestException badRequestException) {
            return new BasicResponse(HttpStatus.BAD_REQUEST_400, new ExceptionBody(badRequestException));
        }
    }

    private void validateRequest(Request request) throws MalformedRequestException {
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
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeleteParagraphEndpoint that = (DeleteParagraphEndpoint) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
