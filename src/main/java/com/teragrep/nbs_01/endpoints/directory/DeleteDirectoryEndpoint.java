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
import com.teragrep.nbs_01.repository.FileTree;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.ExceptionResponse;
import com.teragrep.nbs_01.responses.JsonResponse;
import com.teragrep.nbs_01.responses.Response;
import org.eclipse.jetty.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

// Endpoint that deletes a Directory or a Notebook. Should be provided with a path of the File
public final class DeleteDirectoryEndpoint implements EndPoint {

    private final FileTree root;

    public DeleteDirectoryEndpoint(FileTree root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        try {
            Path path = root.path().resolve(request.path());
            if (!Files.exists(path)) {
                throw new NoSuchFileException(request.path().toString());
            }
            // Files.delete() throws an exception if trying to delete a non-empty directory, so we must clear the directory first.
            if (!Files.isDirectory(path)) {
                return new JsonResponse(HttpStatus.BAD_REQUEST_400, request.path() + " is not a directory!");
            }
            else {
                Stream<Path> files = Files.walk(path);
                files.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
            return new JsonResponse(HttpStatus.NO_CONTENT_204, "");
        }
        // DELETE requests should return 404 NOT FOUND if the requested file doesn't exist in the first place
        catch (NoSuchFileException noSuchFileException) {
            return new JsonResponse(HttpStatus.NOT_FOUND_404, "No such file: " + request.path());
        }
        // Any other IOException indicates that a more critical error happened, and should be logged.
        catch (IOException ioException) {
            return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, ioException);
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
        DeleteDirectoryEndpoint that = (DeleteDirectoryEndpoint) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
