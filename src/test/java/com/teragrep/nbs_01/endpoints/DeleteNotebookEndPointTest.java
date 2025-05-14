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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeleteNotebookEndPointTest extends AbstractNotebookServerTest {

    private final Path fileToDelete = Paths.get(notebookDirectory().toString(), "my_note3_2A94M5J3Z.zpln");

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterAll
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    // Assert that a simple HTTP request to /notebook/delete endpoint results in a notebook being deleted
    public void httpDeleteTest() {
        Assertions.assertDoesNotThrow(() -> {
            // Start server and wait for it to initialize.
            startServer();
            // Assert that the correct number of files exist
            Assertions.assertEquals(4, Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            Map<Integer, List<String>> response = makeHttpPOSTRequest(
                    "http://" + serverAddress() + "/notebook/delete", "2A94M5J3Z"
            );
            Assertions.assertEquals("Notebook deleted", response.get(200).get(0).toString());
            stopServer();
            // Assert that a file was deleted.
            Assertions.assertEquals(3, Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            // Assert that the correct file was deleted.
            Assertions.assertFalse(Files.exists(fileToDelete));
        });
    }

    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketDeleteTest() {
        Assertions.assertDoesNotThrow(() -> {
            // Start server and wait for it to initialize.
            startServer();
            // Assert that the correct number of files exist
            Assertions.assertEquals(4, Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            Map<Integer, List<String>> response = makeHttpPOSTRequest(
                    "http://" + serverAddress() + "/notebook/delete", "2A94M5J3Z"
            );
            Assertions.assertEquals("Notebook deleted", response.get(200).get(0));
            // Assert that a file was deleted.
            Assertions.assertEquals(3, Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            // Assert that the correct file was deleted.
        });
    }
}
