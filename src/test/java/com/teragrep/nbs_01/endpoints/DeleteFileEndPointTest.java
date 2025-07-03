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
import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.requests.JsonRequest;
import com.teragrep.nbs_01.responses.JsonResponse;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeleteFileEndPointTest extends AbstractNotebookServerTest {

    private String deletedNotebookName = "my_note3_2A94M5J3Z.zpln";
    private String deletedDirectoryName = "my_folder_2A94M5J1D/";
    private Path deletedNotebookPath = Paths.get(notebookDirectory().toString(), deletedNotebookName);
    private Path deletedDirectoryPath = Paths.get(notebookDirectory().toString(), deletedDirectoryName);

    public DeleteFileEndPointTest() {
    }

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterEach
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    // Assert that a HTTP request to /notebook/new endpoint results in a new file being saved on disk.
    public void httpDeleteNotebookTest() {
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertTrue(Files.exists(deletedNotebookPath));
        DeleteFileEndpoint endPoint = new DeleteFileEndpoint(new Directory("root", notebookDirectory()));
        String body = "{\"path\":\"" + deletedNotebookName + "\"}";
        JsonResponse response = endPoint.createResponse(new JsonRequest(body));
        // Assert that we receive the proper response.
        Assertions.assertEquals(204, response.status());
        // Assert that the file was created.
        Assertions.assertFalse(Files.exists(deletedNotebookPath));
    }

    @Test
    // Assert that a HTTP request to /notebook/new endpoint results in new directory being saved on disk.
    public void httpDeleteDirectoryTest() {
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertTrue(Files.exists(deletedDirectoryPath));
        DeleteFileEndpoint endPoint = new DeleteFileEndpoint(new Directory("root", notebookDirectory()));
        String body = "{\"path\":\"" + deletedDirectoryName + "\"}";
        JsonResponse response = endPoint.createResponse(new JsonRequest(body));
        // Assert that we receive the proper response.
        Assertions.assertEquals(204, response.status());
        // Assert that the file was created.
        Assertions.assertFalse(Files.exists(deletedDirectoryPath));
    }
}
