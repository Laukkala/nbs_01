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
import com.teragrep.nbs_01.protocols.http.HTTPResponse;
import com.teragrep.nbs_01.repository.*;
import com.teragrep.nbs_01.protocols.http.BasicHTTPResponse;
import com.teragrep.nbs_01.repository.identifiers.Identifier;
import com.teragrep.nbs_01.repository.serialization.SerializedNotebook;
import com.teragrep.nbs_01.repository.storage.Storage;
import jakarta.json.JsonException;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

// Endpoint that finds a Paragraph with a given Id from a Notebook based on an Identifier and then creates a new paragraph to another existing Notebook identified with another Identifier, using a given paragraphID.
public final class CopyParagraphEndpoint implements HTTPEndPoint {

    private final Storage root;

    public CopyParagraphEndpoint(Storage root) {
        this.root = root;
    }

    public HTTPResponse createResponse(HTTPRequest request) {
        try {
            String sourceParagraphId = request.sourceParagraphId();
            String targetParagraphId = request.targetParagraphId();
            Identifier sourceIdentifier = request.sourceIdentifier();
            Identifier targetIdentifier = request.targetIdentifier();

            // Deserialize source notebook from Storage
            Notebook serializedSource = root.deserializeNotebook(sourceIdentifier);
            Map<String, Paragraph> sourceParagraphs = serializedSource.paragraphs();

            // Throw error if requested source paragraph doesn't exist
            if (!sourceParagraphs.containsKey(sourceParagraphId)) {
                throw new FileNotFoundException("No such paragraph: " + sourceParagraphId + "!");
            }

            // Create a copy of the paragraph from the source notebook
            Paragraph sourceParagraph = sourceParagraphs.get(sourceParagraphId);
            Paragraph copyParagraph = sourceParagraph.copy(targetParagraphId);

            // Deserialize destination notebook from Storage, and add the copied paragraph
            Notebook serializedDestination = root.deserializeNotebook(targetIdentifier);
            Map<String, Paragraph> destinationParagraphs = serializedDestination.paragraphs();
            // Throw error if requested destination paragraph already exists in destination notebook
            if (destinationParagraphs.containsKey(copyParagraph.id())) {
                throw new MalformedRequestException("Paragraph " + copyParagraph.id() + " already exists!");
            }
            destinationParagraphs.put(copyParagraph.id(), copyParagraph);

            // Serialize edited destination notebook to storage
            Notebook destinationNotebook = new Notebook(serializedDestination.title(), destinationParagraphs);
            SerializedNotebook serializedDestinationNotebook = root.serializeNotebook(destinationNotebook);
            root.writeFile(targetIdentifier, serializedDestinationNotebook.serialize());

            // Create response
            String paragraphContent = root.serializeParagraph(copyParagraph).serialize();
            ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", targetIdentifier.asLongString()));
            headers.add(new BasicHeader("Content-Type", "application/json"));
            return new BasicHTTPResponse(HttpStatus.CREATED_201, new StringBody(paragraphContent), headers);
        }
        catch (FileAlreadyExistsException | MalformedRequestException | JsonException badRequestException) {
            return new BasicHTTPResponse(HttpStatus.BAD_REQUEST_400, new ExceptionBody(badRequestException));
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
