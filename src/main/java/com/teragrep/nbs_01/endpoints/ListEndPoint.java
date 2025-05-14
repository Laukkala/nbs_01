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
import com.teragrep.nbs_01.responses.Response;
import com.teragrep.nbs_01.responses.StringResponse;
import org.eclipse.jetty.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// Lists all the ID's of saved notebooks
public class ListEndPoint implements EndPoint {

    private final Directory root;

    public ListEndPoint(Directory root) {
        this.root = root;
    }

    public Response createResponse(Request request) {
        // Find all notebooks from Directory structure
        StringBuilder sb = new StringBuilder();
        ZeppelinFile foundFile;
        Directory directoryToSearch;
        try {
            foundFile = root.findFile(request.body());
            directoryToSearch = (Directory) foundFile;
        }
        catch (FileNotFoundException fileNotFoundException) {
            directoryToSearch = root;
        }
        try {
            Directory updatedDirectory = directoryToSearch
                    .initializeDirectory(directoryToSearch.path(), new ConcurrentHashMap<>());
            List<ZeppelinFile> files = updatedDirectory.listAllChildren();
            for (ZeppelinFile file : files) {
                if (!file.isDirectory()) {
                    sb.append(file.id());
                    sb.append("\n");
                }
            }
            return new StringResponse(HttpStatus.OK_200, sb.toString());
        }
        catch (IOException ioException) {
            return new StringResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, "Failed to list notebooks");
        }
    }
}
