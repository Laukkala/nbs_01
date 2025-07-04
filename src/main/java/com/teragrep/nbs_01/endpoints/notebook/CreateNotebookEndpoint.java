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
import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.ExceptionResponse;
import com.teragrep.nbs_01.responses.SimpleResponse;
import com.teragrep.nbs_01.responses.JsonResponse;
import jakarta.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Creates a new Notebook. Should be provided with a path of the File
public class CreateNotebookEndpoint implements EndPoint {

    private final Directory root;

    public CreateNotebookEndpoint(Directory root) {
        this.root = root;
    }

    public JsonResponse createResponse(Request request) {
        try {
            Directory updatedDirectory = root.initializeDirectory(root.path(), new ConcurrentHashMap<>());
            JsonObject parameters = request.parameters();
            if (!parameters.containsKey("path")) {
                throw new MalformedRequestException("Request must contain a path!");
            }
            String pathString = parameters.getString("path");
            String title = parameters.containsKey("title") ? parameters.getString("title") : "";
            Path path = updatedDirectory.path().resolve(pathString);

            if (Files.exists(path)) {
                throw new FileAlreadyExistsException("Path at " + path + " is already in use!");
            }

            Notebook newFile = createNotebook(title, path);
            newFile.save();
            return new SimpleResponse(HttpStatus.CREATED_201, "Created new notebook " + newFile.id());
        }
        catch (FileNotFoundException fileNotFoundException) {
            return new ExceptionResponse(HttpStatus.NOT_FOUND_404, fileNotFoundException);
        }
        catch (FileAlreadyExistsException fileAlreadyExistsException) {
            return new ExceptionResponse(HttpStatus.BAD_REQUEST_400, fileAlreadyExistsException);
        }
        catch (IOException ioException) {
            return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, ioException);
        }
        catch (MalformedRequestException malformedRequestException) {
            return new ExceptionResponse(HttpStatus.BAD_REQUEST_400, malformedRequestException);
        }
    }

    private Notebook createNotebook(String title, Path path) {
        return new Notebook(title, UUID.randomUUID().toString(), path, new HashMap<>());
    }
}
