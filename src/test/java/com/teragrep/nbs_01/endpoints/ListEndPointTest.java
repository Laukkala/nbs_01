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
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListEndPointTest extends AbstractNotebookServerTest {

    private List<String> savedFileNames;

    public List<String> readFilesOnDisk() {
        try {
            return Files
                    .list(notebookDirectory())
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
        catch (IOException ioException) {
            throw new RuntimeException("Failed to initialize test!", ioException);
        }
    }

    @BeforeAll
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
        savedFileNames = readFilesOnDisk();
    }

    @AfterAll
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    // Assert that a simple HTTP request to /notebook/list endpoint results in a list of notebook IDs
    public void httpListAllTest() {
        Assertions.assertDoesNotThrow(() -> {
            startServer();
            Map<Integer, List<String>> response = makeHttpPOSTRequest(
                    "http://" + serverAddress() + "/notebook/list", ""
            );
            for (String filename : savedFileNames) {
                Assertions.assertTrue(response.get(200).stream().anyMatch(filename::contains));
            }
            stopServer();
        });
    }

    @Test
    // Assert that a simple HTTP request to /notebook/list endpoint results in a list of notebook IDs
    public void httpListWithinFolderTest() {
        Assertions.assertDoesNotThrow(() -> {
            startServer();
            Map<Integer, List<String>> response = makeHttpPOSTRequest(
                    "http://" + serverAddress() + "/notebook/list", "2A94M5J1D"
            );
            ArrayList<String> savedIdsInFolder = new ArrayList<>();
            savedFileNames.add("2A94M5J1Z");
            savedFileNames.add("2A94M5J2Z");
            for (String filename : savedIdsInFolder) {
                Assertions.assertTrue(response.get(200).stream().anyMatch(filename::equals));
            }
            stopServer();
        });
    }

    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketListAllTest() {
        Assertions.assertDoesNotThrow(() -> {
            startServer();
            Map<Integer, List<String>> response = makeWebSocketRequest(
                    "ws://" + serverAddress() + "/notebook/list", ""
            );
            List<String> ids = Arrays.stream(response.get(200).get(0).split("\n")).toList();
            ids.stream().anyMatch(savedFileNames::contains);
            stopServer();
        });
    }

    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketListWithinFolderTest() {
        Assertions.assertDoesNotThrow(() -> {
            startServer();
            Map<Integer, List<String>> response = makeWebSocketRequest(
                    "ws://" + serverAddress() + "/notebook/list", "2A94M5J1D"
            );
            ArrayList<String> savedIdsInFolder = new ArrayList<>();
            savedFileNames.add("2A94M5J1Z");
            savedFileNames.add("2A94M5J2Z");

            List<String> receivedIds = Arrays.stream(response.get(200).get(0).split("\n")).toList();
            for (String filename : savedIdsInFolder) {
                Assertions.assertTrue(receivedIds.stream().anyMatch(filename::equals));
            }
            stopServer();
        });
    }
}
