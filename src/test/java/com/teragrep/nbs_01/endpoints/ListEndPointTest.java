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
import com.teragrep.nbs_01.responses.Response;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListEndPointTest extends AbstractNotebookServerTest {

    private final List<String> allFileIds = Arrays
            .asList("2A94M5J1Z", "2A94M5J2Z", "2A94M5J3Z", "2A94M5J4Z", "junkfile");
    private final List<String> allFileIdsWithinDirectory = Arrays.asList("2A94M5J1Z", "2A94M5J2Z");

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterEach
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    // Assert that a HTTP request to /notebook/list endpoint results in a list of notebook IDs
    public void httpListAllTest() {
        Assertions.assertDoesNotThrow(() -> {
            startServer();
            Response response = makeHttpPOSTRequest("http://" + serverAddress() + "/notebook/list", "{}");
            stopServer();
            for (String filename : allFileIds) {
                Assertions.assertTrue(response.body().getString("message").contains(filename));
            }

        });
    }

    @Test
    // Assert that a HTTP request with a defined DirectoryId to /notebook/list endpoint results in a list of notebook IDs contained in that directory
    public void httpListWithinFolderTest() {
        Assertions.assertDoesNotThrow(() -> {
            startServer();
            Response response = makeHttpPOSTRequest(
                    "http://" + serverAddress() + "/notebook/list", "{\"directoryId\":\"2A94M5J1D\"}"
            );
            stopServer();
            for (String filename : allFileIdsWithinDirectory) {
                Assertions.assertTrue(response.body().getString("message").contains(filename));
            }
        });
    }

    @Test
    // Assert that a WebSocket request to /notebook/list endpoint results in a list of notebook IDs
    public void webSocketListAllTest() {
        Assertions.assertDoesNotThrow(() -> {
            startServer();
            Response response = makeWebSocketRequest("ws://" + serverAddress() + "/notebook/list", "{}");
            stopServer();
            List<String> ids = Arrays.stream(response.body().getString("message").split("\n")).toList();
            ids.stream().anyMatch(allFileIds::contains);

        });
    }

    @Test
    // Assert that a WebSocket request with a defined DirectoryId to /notebook/list endpoint results in a list of notebook IDs contained in that directory
    public void webSocketListWithinFolderTest() {
        Assertions.assertDoesNotThrow(() -> {
            startServer();
            Response response = makeWebSocketRequest(
                    "ws://" + serverAddress() + "/notebook/list", "{\"directoryId\":\"2A94M5J1D\"}"
            );

            List<String> receivedIds = Arrays.stream(response.body().getString("message").split("\n")).toList();
            for (String filename : allFileIdsWithinDirectory) {
                Assertions.assertTrue(receivedIds.stream().anyMatch(filename::equals));
            }
            stopServer();
        });
    }
}
