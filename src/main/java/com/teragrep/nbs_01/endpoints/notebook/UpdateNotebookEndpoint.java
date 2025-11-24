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
import com.teragrep.nbs_01.exceptions.BodyNotFoundException;
import com.teragrep.nbs_01.exceptions.MalformedBodyException;
import com.teragrep.nbs_01.http.ErrorBody;
import com.teragrep.nbs_01.http.ExceptionBody;
import com.teragrep.nbs_01.http.JSONBody;
import com.teragrep.nbs_01.repository.*;
import com.teragrep.nbs_01.http.requests.Request;
import com.teragrep.nbs_01.http.responses.JsonResponse;
import com.teragrep.nbs_01.http.responses.Response;
import jakarta.json.JsonObject;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// Updates the title of a Notebook.
public final class UpdateNotebookEndpoint implements EndPoint {

    private final FileTree root;

    public UpdateNotebookEndpoint(FileTree root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        try {
            JsonObject parameters = request.body().asJson().asJsonObject();
            Path path = root.path().resolve(request.path());

            if (Files.isDirectory(path)) {
                throw new MalformedBodyException(request.path() + " is not a Notebook!");
            }

            if (!parameters.containsKey("title")) {
                throw new MalformedBodyException("Request does not contain a title!");
            }

            List<Path> currentFiles = root.list();
            if (!currentFiles.contains(path)) {
                throw new FileNotFoundException("Notebook at path " + request.path() + " does not exist!");
            }
            Notebook notebook = new Notebook(path).load();

            // Create a copy of the current paragraphs
            Map<String, Paragraph> paragraphs = new LinkedHashMap<>(notebook.paragraphs());

            // Add a modified title
            String title = parameters.getString("title");
            Notebook newNotebook = new Notebook(title, notebook.path(), paragraphs);
            newNotebook.save();
            ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", request.path().toString()));
            headers.add(new BasicHeader("Content-Type", "application/json"));
            return new JsonResponse(HttpStatus.OK_200, new JSONBody(newNotebook.json()), headers);
        }
        catch (FileNotFoundException fileNotFoundException) {
            return new JsonResponse(HttpStatus.NOT_FOUND_404, new ExceptionBody(fileNotFoundException));
        }
        catch (BodyNotFoundException bodyNotFoundException) {
            return new JsonResponse(HttpStatus.BAD_REQUEST_400, new ExceptionBody(bodyNotFoundException));
        }
        catch (IOException ioException) {
            return new JsonResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, new ErrorBody(ioException));
        }
        catch (MalformedBodyException malformedBodyException) {
            return new JsonResponse(HttpStatus.BAD_REQUEST_400, new ExceptionBody(malformedBodyException));
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
        UpdateNotebookEndpoint that = (UpdateNotebookEndpoint) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
