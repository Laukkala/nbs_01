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
package com.teragrep.nbs_01.endpoints.directory;

import com.teragrep.nbs_01.endpoints.HTTPEndPoint;
import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.http.body.ErrorBody;
import com.teragrep.nbs_01.ErrorEvent;
import com.teragrep.nbs_01.http.body.ExceptionBody;
import com.teragrep.nbs_01.http.body.JSONBody;
import com.teragrep.nbs_01.http.requests.HTTPRequest;
import com.teragrep.nbs_01.http.responses.BasicHTTPResponse;
import com.teragrep.nbs_01.http.responses.HTTPResponse;
import com.teragrep.nbs_01.repository.Identifier;
import com.teragrep.nbs_01.repository.Storage;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Finds a given Directory and returns its own name and the names of its children in JSON format based on a given Identifier.
public final class FindDirectoryEndPoint implements HTTPEndPoint {

    private final Storage root;

    public FindDirectoryEndPoint(Storage root) {
        this.root = root;
    }

    public HTTPResponse createResponse(HTTPRequest request) {
        try {
            // Find a directory and get a list of its children
            Identifier targetIdentifier = request.targetIdentifier();
            List<Identifier> currentFiles = root.immediateChildren(targetIdentifier);

            // Create response
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Identifier currentFile : currentFiles) {
                arrayBuilder.add(currentFile.asShortString());
            }

            JsonObject json = Json
                    .createObjectBuilder()
                    .add("name", targetIdentifier.asShortString())
                    .add("children", arrayBuilder.build())
                    .build();

            ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", targetIdentifier.asLongString()));
            headers.add(new BasicHeader("Content-Type", "application/json"));
            return new BasicHTTPResponse(HttpStatus.OK_200, new JSONBody(json), headers);
        }
        catch (MalformedRequestException badRequestException) {
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
        FindDirectoryEndPoint that = (FindDirectoryEndPoint) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
