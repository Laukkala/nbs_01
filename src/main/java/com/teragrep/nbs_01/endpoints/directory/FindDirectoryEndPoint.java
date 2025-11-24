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

import com.teragrep.nbs_01.endpoints.EndPoint;
import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.http.ExceptionBody;
import com.teragrep.nbs_01.http.JSONBody;
import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.FileTree;
import com.teragrep.nbs_01.http.requests.Request;
import com.teragrep.nbs_01.http.responses.ErrorResponse;
import com.teragrep.nbs_01.http.responses.JsonResponse;
import com.teragrep.nbs_01.http.responses.JsonResponse;
import com.teragrep.nbs_01.http.responses.Response;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Finds a given Directory and returns the names of its children in JSON format.
public final class FindDirectoryEndPoint implements EndPoint {

    private final FileTree root;

    public FindDirectoryEndPoint(FileTree root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        // Find a notebooks from Directory structure based on given ID
        try {
            Path path = root.path().resolve(request.path());
            List<Path> currentFiles = root.list();
            if (!currentFiles.contains(path)) {
                return new JsonResponse(
                        HttpStatus.NOT_FOUND_404,
                        new ExceptionBody(new FileNotFoundException("No such directory!"))
                );
            }
            if (!path.toFile().isDirectory()) {
                return new JsonResponse(
                        HttpStatus.BAD_REQUEST_400,
                        new ExceptionBody(
                                new MalformedRequestException("File at path " + request.path() + " is not a directory!")
                        )
                );
            }
            Directory directory = new Directory(path).load();
            ArrayList<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Location", request.path().toString()));
            headers.add(new BasicHeader("Content-Type", "application/json"));
            return new JsonResponse(HttpStatus.OK_200, new JSONBody(directory.json()), headers);
        }
        catch (IOException ioException) {
            return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, ioException);
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
