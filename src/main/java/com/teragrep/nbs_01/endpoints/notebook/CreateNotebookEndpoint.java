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
import com.teragrep.nbs_01.http.ErrorBody;
import com.teragrep.nbs_01.http.ErrorEvent;
import com.teragrep.nbs_01.http.ExceptionBody;
import com.teragrep.nbs_01.http.JSONBody;
import com.teragrep.nbs_01.repository.FileTree;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.http.requests.Request;
import com.teragrep.nbs_01.http.responses.BasicResponse;
import com.teragrep.nbs_01.http.responses.Response;
import jakarta.json.JsonStructure;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Creates a new Notebook. Should be provided with a path of the File
public final class CreateNotebookEndpoint implements EndPoint {

    private final FileTree root;

    public CreateNotebookEndpoint(FileTree root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        Path path = request.path();
        String title;
        try {
            JsonStructure json = request.body().asJson();
            title = json.asJsonObject().getString("title");
        }
        catch (BodyNotFoundException bodyNotFoundException) {
            title = "";
        }
        return createResponse(path, title);
    }

    private Response createResponse(Path path, String title) {
        try {
            List<Path> currentFiles = root.list();
            Path filePath = root.path().resolve(path);
            if (currentFiles.contains(filePath)) {
                throw new FileAlreadyExistsException("Path at " + path + " is already in use!");
            }
            Notebook newFile = new Notebook(title, filePath);
            newFile.save();
            ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", path.toString()));
            headers.add(new BasicHeader("Content-Type", "application/json"));
            return new BasicResponse(HttpStatus.CREATED_201, new JSONBody(newFile.json()), headers);
        }
        catch (FileNotFoundException fileNotFoundException) {
            return new BasicResponse(HttpStatus.NOT_FOUND_404, new ExceptionBody(fileNotFoundException));
        }
        catch (FileAlreadyExistsException fileAlreadyExistsException) {
            return new BasicResponse(HttpStatus.BAD_REQUEST_400, new ExceptionBody(fileAlreadyExistsException));
        }
        catch (IOException ioException) {
            return new BasicResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, new ErrorBody(new ErrorEvent(ioException)));
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
        CreateNotebookEndpoint that = (CreateNotebookEndpoint) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
