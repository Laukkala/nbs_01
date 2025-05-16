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
package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.repository.ZeppelinFile;
import com.teragrep.nbs_01.requests.Request;
import com.teragrep.nbs_01.responses.JsonResponse;
import com.teragrep.nbs_01.responses.Response;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

// Returns the JSON content of a Notebook if a matching ID is provided.
public class FindEndPoint implements EndPoint {

    private final Directory root;

    public FindEndPoint(Directory root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        // Find a notebooks from Directory structure based on given ID
        try {
            JsonObject parameters = request.parameters();
            String id = parameters.getString("notebookId");
            Directory updatedDirectory = root.initializeDirectory(root.path(), new ConcurrentHashMap<>());
            ZeppelinFile file = updatedDirectory.findFile(id);
            if (!file.isDirectory()) {
                return new JsonResponse(HttpStatus.OK_200, file.load().json().toString());
            }
            else {
                return new JsonResponse(HttpStatus.BAD_REQUEST_400, "Notebook not found");
            }
        }
        catch (FileNotFoundException fileNotFoundException) {
            return new JsonResponse(HttpStatus.BAD_REQUEST_400, "Notebook not found!");
        }
        catch (IOException ioException) {
            return new JsonResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, "An error occurred");
        }
        catch (JsonException jsonException) {
            return new JsonResponse(HttpStatus.BAD_REQUEST_400, "Malformed JSON :\n" + jsonException);
        }
    }
}
