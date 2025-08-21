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
package com.teragrep.nbs_01.endpoints.notebook;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.requests.JsonRequest;
import com.teragrep.nbs_01.responses.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class CreateNotebookEndpointTest extends AbstractNotebookServerTest {

    private String newNotebookName = "testNotebookName";
    private Path newNotebookPath = Paths.get(notebookDirectory().toString(), newNotebookName);
    private Path existingNotebookName = Paths.get("my_folder_2A94M5J1D");
    private Path existingNotebookPath = Paths.get(notebookDirectory().toString(), "my_folder_2A94M5J1D");

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterEach
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    // Assert that a proper request to CreateNotebookEndpoint results in a correct response and a file being saved to disk.
    public void httpCreateNotebookTest() {
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(newNotebookPath));
        CreateNotebookEndpoint endPoint = new CreateNotebookEndpoint(new Directory(notebookDirectory()));
        String body = "{}";
        Response response = endPoint.createResponse(new JsonRequest(body, Paths.get(newNotebookName)));
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Assertions.assertTrue(response.body().getString("message").contains("Created new notebook"));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(newNotebookPath));
    }

    @Test
    // Assert that a request to CreateNotebookEndpoint to a path that already contains a file results in an error
    public void httpCreateNotebookIntoUnavailablePathTest() {
        // Assert that the file we are creating already exists.
        Assertions.assertTrue(Files.exists(existingNotebookPath));
        CreateNotebookEndpoint endPoint = new CreateNotebookEndpoint(new Directory(notebookDirectory()));
        String body = "{}";
        Response response = endPoint.createResponse(new JsonRequest(body, existingNotebookName));
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        Assertions
                .assertEquals(
                        "java.nio.file.FileAlreadyExistsException: Path at " + existingNotebookPath
                                + " is already in use!",
                        response.body().getString("message")
                );
    }

    @Test
    // Assert that a request to CreateNotebookEndpoint to a path that already contains a file results in an error
    public void httpCreateNotebookIntoUnavailableDirectoryPathTest() {

        String parentDirectoryName = "newDirectory_123123";
        Path notebookName = Paths.get(parentDirectoryName, newNotebookName);
        Path notebookPath = Paths.get(notebookDirectory().toString(), notebookName.toString());
        // Assert that the file we are creating already exists.
        Assertions.assertFalse(Files.exists(notebookPath));
        CreateNotebookEndpoint endPoint = new CreateNotebookEndpoint(new Directory(notebookDirectory()));
        String body = "{\"path\":\"" + notebookName + "\"}";
        Response response = endPoint.createResponse(new JsonRequest(body, notebookName));
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Assertions.assertTrue(response.body().getString("message").contains("Created new notebook"));
        Assertions.assertTrue(Files.exists(notebookPath));
        Assertions.assertEquals(parentDirectoryName, notebookPath.getParent().getFileName().toString());
    }
}
