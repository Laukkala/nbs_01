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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MoveDirectoryEndPointTest extends AbstractNotebookServerTest {

    private final String directoryId = "2A94M5J2D";
    private final String parentId = "notebooks";
    private final Path originalDirectoryPath = Paths
            .get(notebookDirectory().toString(), "my_folder_2A94M5J1D", "my_second_folder_2A94M5J2D");
    private final Path originalChildNotebookPath = Paths
            .get(
                    notebookDirectory().toString(), "my_folder_2A94M5J1D", "my_second_folder_2A94M5J2D",
                    "my_note1_2A94M5J1Z.zpln"
            );
    private final Path expectedDirectoryPath = Paths.get(notebookDirectory().toString(), "my_second_folder_2A94M5J2D");
    private final Path expectedChildNotebookPath = Paths
            .get(notebookDirectory().toString(), "my_second_folder_2A94M5J2D", "my_note1_2A94M5J1Z.zpln");

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterEach
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    // Assert that a HTTP request to /notebook/moveDirectory endpoint results in the proper directory and their children being moved.
    public void httpMoveTest() {
        Assertions.assertDoesNotThrow(() -> {
            // Assert that the directory and its child files are in their proper places.
            Assertions.assertTrue(Files.exists(originalDirectoryPath));
            Assertions.assertTrue(Files.exists(originalChildNotebookPath));
            // Start server and wait for it to initialize.
            startServer();
            Response response = makeHttpPOSTRequest(
                    "http://" + serverAddress() + "/notebook/moveDirectory",
                    "{\"directoryId\":\"" + directoryId + "\",\"parentId\":\"" + parentId + "\"}"
            );
            stopServer();
            // Assert that we got the proper response.
            Assertions.assertEquals("Moved directory " + directoryId, response.body().getString("message").strip());
            // Assert that the directory and child have moved to the new place.
            Assertions.assertTrue(Files.exists(expectedDirectoryPath));
            Assertions.assertTrue(Files.exists(expectedChildNotebookPath));
        });
    }

    @Test
    // Assert that a WebSocket request to /notebook/moveDirectory endpoint results in the proper directory and their children being moved.
    public void webSocketMoveTest() {
        Assertions.assertDoesNotThrow(() -> {
            // Assert that the directory and its child files are in their proper places.
            Assertions.assertTrue(Files.exists(originalDirectoryPath));
            Assertions.assertTrue(Files.exists(originalChildNotebookPath));
            // Start server and wait for it to initialize.
            startServer();
            Response response = makeWebSocketRequest(
                    "ws://" + serverAddress() + "/notebook/moveDirectory",
                    "{\"directoryId\":\"" + directoryId + "\",\"parentId\":\"" + parentId + "\"}"
            );
            stopServer();
            // Assert that we got the proper response.
            Assertions.assertEquals("Moved directory " + directoryId, response.body().getString("message").strip());
            // Assert that the directory and child have moved to the new place.
            Assertions.assertTrue(Files.exists(expectedDirectoryPath));
            Assertions.assertTrue(Files.exists(expectedChildNotebookPath));
        });
    }
}
