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

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.endpoints.general.ListEndPoint;
import com.teragrep.nbs_01.http.body.JSONBody;
import com.teragrep.nbs_01.repository.LocalFilesystemStorage;
import com.teragrep.nbs_01.http.requests.BasicRequest;
import com.teragrep.nbs_01.http.responses.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

public class ListEndPointTest extends AbstractNotebookServerTest {

    private final List<String> allFileIds = Arrays
            .asList("2A94M5J1Z", "2A94M5J2Z", "2A94M5J3Z", "2A94M5J4Z", "junkfile");
    private final List<String> allFileIdsWithinDirectory = Arrays.asList("2A94M5J1Z", "2A94M5J2Z");

    @Test
    // Assert that a HTTP request to /notebook/list endpoint results in a list of notebook IDs
    public void httpListAllTest() {
        ListEndPoint listEndPoint = new ListEndPoint(new LocalFilesystemStorage(notebookDirectory()));
        Response response = listEndPoint.createResponse(new BasicRequest());
        for (String filename : allFileIds) {
            Assertions.assertDoesNotThrow(() -> Assertions.assertTrue(response.body().asString().contains(filename)));
        }
    }

    @Test
    // Assert that a HTTP request with a defined DirectoryId to /notebook/list endpoint results in a list of notebook IDs contained in that directory
    public void httpListWithinFolderTest() {
        ListEndPoint listEndPoint = new ListEndPoint(new LocalFilesystemStorage(notebookDirectory()));
        JsonObject requestBody = Json.createObjectBuilder().add("directoryPath", directory1().toString()).build();
        Response response = listEndPoint.createResponse(new BasicRequest(new JSONBody(requestBody)));
        for (String filename : allFileIdsWithinDirectory) {
            Assertions.assertDoesNotThrow(() -> Assertions.assertTrue(response.body().asString().contains(filename)));
        }
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(ListEndPoint.class).verify();
    }
}
