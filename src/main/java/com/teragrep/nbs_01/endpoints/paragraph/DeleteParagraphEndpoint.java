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
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.ExceptionResponse;
import com.teragrep.nbs_01.responses.JsonResponse;
import com.teragrep.nbs_01.responses.Response;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

// Deletes a Paragraph from a given Notebook. Should be provided with a path of the Notebook
public final class DeleteParagraphEndpoint implements EndPoint {

    private final FileTree root;

    public DeleteParagraphEndpoint(FileTree root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        try {
            validateRequestParameters(request);
            Path requestPath = request.path();
            Path notebookPath = requestPath.subpath(0, requestPath.getNameCount() - 2);

            String paragraphId = requestPath
                    .subpath(requestPath.getNameCount() - 1, requestPath.getNameCount())
                    .toString();

            List<Path> currentFiles = root.list();
            Path path = root.path().resolve(notebookPath);
            if (!currentFiles.contains(path)) {
                throw new FileNotFoundException("No such notebook: " + path + "!");
            }

            Notebook notebook = new Notebook(path).load();
            if (notebook.paragraphs().containsKey(paragraphId)) {
                notebook.paragraphs().remove(paragraphId);
                notebook.save();
                return new JsonResponse(HttpStatus.NO_CONTENT_204, "Deleted paragraph " + paragraphId);
            }
            else {
                return new JsonResponse(HttpStatus.NOT_FOUND_404, "Paragraph " + paragraphId + " doesn't exist!");
            }
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
