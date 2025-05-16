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

import com.google.common.io.Files;
import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.responses.Response;
import org.junit.jupiter.api.*;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UpdateParagraphEndpointTest extends AbstractNotebookServerTest {

    private final String testFileId = "2A94M5J1Z";
    private final Path testFilePath = Paths
            .get(
                    notebookDirectory().toString(),
                    "/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln"
            );
    private final String testParagraphId = "20150703-133047_853701097";

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterAll
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    public void httpUpdateNotebookTest() {
        Assertions.assertDoesNotThrow(() -> {
            // Start server and wait for it to initialize.
            startServer();

            Response response = makeHttpPOSTRequest(
                    "http://" + serverAddress() + "/notebook/update",
                    "{\"notebookId\":\"" + testFileId + "\",\"paragraphId\":\"" + testParagraphId
                            + "\",\"paragraphText\":\"" + "testEditMessage\"}"
            );
            Assertions
                    .assertTrue(response.body().getString("message").strip().contains("Notebook edited successfully"));
            stopServer();
            // Assert that the message we wanted to edit can be found in the file.
            Assertions
                    .assertTrue(Files.readLines(testFilePath.toFile(), Charset.defaultCharset()).get(0).contains("testEditMessage"));
        });
    }

    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketUpdateTest() {
        Assertions.assertDoesNotThrow(() -> {
            // Start server and wait for it to initialize.
            startServer();
            Response response = makeWebSocketRequest(
                    "ws://" + serverAddress() + "/notebook/update",
                    "{\"notebookId\":\"" + testFileId + "\",\"paragraphId\":\"" + testParagraphId
                            + "\",\"paragraphText\":\"" + "testEditMessage\"}"
            );
            Assertions.assertEquals("Notebook edited successfully", response.body().getString("message").strip());
            stopServer();
            // Assert that the message we wanted to edit can be found in the file.
            Assertions
                    .assertTrue(Files.readLines(testFilePath.toFile(), Charset.defaultCharset()).get(0).contains("testEditMessage"));
        });
    }
}
