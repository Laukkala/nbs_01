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

import com.teragrep.nbs_01.endpoints.HTTPEndPoint;
import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.http.body.ErrorBody;
import com.teragrep.nbs_01.ErrorEvent;
import com.teragrep.nbs_01.http.body.ExceptionBody;
import com.teragrep.nbs_01.http.body.StringBody;
import com.teragrep.nbs_01.http.requests.HTTPRequest;
import com.teragrep.nbs_01.http.responses.HTTPResponse;
import com.teragrep.nbs_01.repository.*;
import com.teragrep.nbs_01.http.responses.BasicHTTPResponse;
import com.teragrep.nbs_01.repository.serialization.SerializedNotebook;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

// Endpoint that creates a new Paragraph with a given ID into a given Notebook based on an Identifier.
public final class CreateParagraphEndpoint implements HTTPEndPoint {

    private final Storage root;

    public CreateParagraphEndpoint(Storage root) {
        this.root = root;
    }

    public HTTPResponse createResponse(HTTPRequest request) {
        try {
            Identifier targetIdentifier = request.targetIdentifier();
            String targetParagraphId = request.targetParagraphId();

            // Deserialize notebook
            Notebook notebook = root.deserializeNotebook(targetIdentifier);

            // Throw error if a paragraph with the ID already exists.
            if (notebook.paragraphs().containsKey(targetParagraphId)) {
                throw new MalformedRequestException("Paragraph " + targetParagraphId + " already exists!");
            }

            // Add a new empty paragraph to the notebook and serialize to Storage
            Paragraph newParagraph = new Paragraph(targetParagraphId, "", new Script(""));
            notebook.paragraphs().put(targetParagraphId, newParagraph);
            SerializedNotebook serializedNotebook = root.serializeNotebook(notebook);
            root.writeFile(targetIdentifier, serializedNotebook.serialize());

            // Create response
            String paragraphContent = root.serializeParagraph(newParagraph).serialize();
            ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", targetIdentifier.asLongString()));
            headers.add(new BasicHeader("Content-Type", "application/json"));
            return new BasicHTTPResponse(HttpStatus.CREATED_201, new StringBody(paragraphContent), headers);
        }
        catch (FileNotFoundException notFoundException) {
            return new BasicHTTPResponse(HttpStatus.NOT_FOUND_404, new ExceptionBody(notFoundException));
        }
        catch (IOException serverErrorException) {
            return new BasicHTTPResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR_500,
                    new ErrorBody(new ErrorEvent(serverErrorException))
            );
        }
        catch (MalformedRequestException badRequestException) {
            return new BasicHTTPResponse(HttpStatus.BAD_REQUEST_400, new ExceptionBody(badRequestException));
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
        CreateParagraphEndpoint that = (CreateParagraphEndpoint) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
