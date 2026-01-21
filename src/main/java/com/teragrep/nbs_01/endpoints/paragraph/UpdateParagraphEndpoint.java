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

import com.teragrep.nbs_01.protocols.http.HTTPEndPoint;
import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.protocols.http.body.ErrorBody;
import com.teragrep.nbs_01.exceptions.ErrorEvent;
import com.teragrep.nbs_01.protocols.http.body.ExceptionBody;
import com.teragrep.nbs_01.protocols.http.body.StringBody;
import com.teragrep.nbs_01.protocols.http.HTTPRequest;
import com.teragrep.nbs_01.protocols.http.BasicHTTPResponse;
import com.teragrep.nbs_01.protocols.http.HTTPResponse;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.Paragraph;
import com.teragrep.nbs_01.repository.Script;
import com.teragrep.nbs_01.repository.identifiers.Identifier;
import com.teragrep.nbs_01.repository.serialization.SerializedNotebook;
import com.teragrep.nbs_01.repository.storage.Storage;
import jakarta.json.JsonException;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

// Endpoint that updates the text and/or title of a paragraph with a given ID within a Notebook based on an Identifier.
public final class UpdateParagraphEndpoint implements HTTPEndPoint {

    private final Storage root;

    public UpdateParagraphEndpoint(final Storage root) {
        this.root = root;
    }

    public HTTPResponse createResponse(final HTTPRequest request) {
        HTTPResponse response;
        try {
            final Identifier targetIdentifier = request.targetIdentifier();
            final String paragraphId = request.targetParagraphId();

            // Deserialize notebook from Storage
            final Notebook notebook = root.deserializeNotebook(targetIdentifier);
            final Map<String, Paragraph> paragraphs = notebook.paragraphs();

            // Throw an error if the paragraph doesn't exist
            if (!paragraphs.containsKey(paragraphId)) {
                throw new MalformedRequestException("Paragraph with Id " + paragraphId + " not found!");
            }
            final Paragraph originalParagraph = paragraphs.get(paragraphId);

            // Get modified script text and / or title from request.
            String scriptText;
            try {
                scriptText = request.text();
            }
            catch (final MalformedRequestException exception) {
                scriptText = originalParagraph.script().text();
            }
            // Retrieve the title from the request. If the request does not have a title, use the existing title of the paragraph.
            String title;
            try {
                title = request.title();
            }
            catch (final MalformedRequestException exception) {
                title = originalParagraph.title();
            }
            final Script newScript = new Script(scriptText);

            // Overwrite the old paragraph with the edited paragraph, and serialize the notebook to Storage
            final Paragraph newParagraph = new Paragraph(originalParagraph.id(), title, newScript);
            paragraphs.put(newParagraph.id(), newParagraph);
            final Notebook newNotebook = new Notebook(notebook.title(), paragraphs);
            final SerializedNotebook serializedNotebook = root.serializeNotebook(newNotebook);
            root.writeFile(targetIdentifier, serializedNotebook.serialize());

            // Create response
            final String paragraphContent = root.serializeParagraph(newParagraph).serialize();
            final ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", targetIdentifier.name()));
            headers.add(new BasicHeader("Content-Type", "application/json"));
            response = new BasicHTTPResponse(HttpStatus.OK_200, new StringBody(paragraphContent), headers);
        }
        catch (final MalformedRequestException | JsonException badRequestException) {
            response = new BasicHTTPResponse(HttpStatus.BAD_REQUEST_400, new ExceptionBody(badRequestException));
        }
        catch (final FileNotFoundException notFoundException) {
            response = new BasicHTTPResponse(HttpStatus.NOT_FOUND_404, new ExceptionBody(notFoundException));
        }
        catch (final IOException serverErrorException) {
            response = new BasicHTTPResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR_500,
                    new ErrorBody(new ErrorEvent(serverErrorException))
            );
        }
        return response;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UpdateParagraphEndpoint that = (UpdateParagraphEndpoint) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
