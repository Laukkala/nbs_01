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
import com.teragrep.nbs_01.responses.JsonResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class CopyDirectoryEndpointTest extends AbstractNotebookServerTest {

    private String sourceDirectoryName = "my_second_folder_2A94M5J2D/";
    private Path sourceDirectoryPath = Paths
            .get(notebookDirectory().toString(), "my_folder_2A94M5J1D", sourceDirectoryName);
    private Path sourceDirectoryParameter = Paths.get("my_folder_2A94M5J1D", sourceDirectoryName);
    private String newDirectoryName = "testDirectoryName/";
    private Path copiedDirectoryPath = Paths.get(notebookDirectory().toString(), newDirectoryName);
    private Path faultyEndpointParameter = Paths.get("tillintallin", "tallintillin");
    private Path nonExistentSourcePath = Paths.get(notebookDirectory().toString(), "tillintallin", "tallintillin");
    private Path existingPathEndpointParameter = Paths.get("my_folder_2A94M5J1D");
    private Path existingPath = Paths.get(notebookDirectory().toString(), "my_folder_2A94M5J1D");

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterEach
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    // Assert that a proper request to CopyDirectoryEndpoint results in a correct response and a file being saved to disk.
    public void httpCopyDirectoryTest() {
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(copiedDirectoryPath));
        Assertions.assertTrue(Files.exists(sourceDirectoryPath));
        CopyDirectoryEndpoint endPoint = new CopyDirectoryEndpoint(new Directory("root", notebookDirectory()));
        String body = "{\"path\":\"" + newDirectoryName + "\",\"sourcePath\":\"" + sourceDirectoryParameter + "\"}";
        JsonResponse response = endPoint.createResponse(new JsonRequest(body));
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Assertions.assertTrue(response.body().getString("message").contains("Created new directory"));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(copiedDirectoryPath));
    }

    @Test
    // Assert that a request to CopyDirectoryEndpoint with a source path that does not have a file results in an error
    public void httpCopyNonExistentDirectoryTest() {
        // Assert that there is no file saved in the source path we are using
        Assertions.assertFalse(Files.exists(faultyEndpointParameter));
        CopyDirectoryEndpoint endPoint = new CopyDirectoryEndpoint(new Directory("root", notebookDirectory()));
        String body = "{\"path\":\"" + newDirectoryName + "\",\"sourcePath\":\"" + faultyEndpointParameter + "\"}";
        JsonResponse response = endPoint.createResponse(new JsonRequest(body));
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertEquals(
                        "java.io.FileNotFoundException: Notebook or directory with path " + nonExistentSourcePath
                                + " not found!",
                        response.body().getString("message")
                );
        // Assert that the file was not created.
        Assertions.assertFalse(Files.exists(copiedDirectoryPath));
    }

    @Test
    // Assert that a request to CopyDirectoryEndpoint to a path that already contains a file results in an error
    public void httpCopyDirectoryIntoUnavailablePathTest() {
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertTrue(Files.exists(existingPath));
        Assertions.assertTrue(Files.exists(sourceDirectoryPath));
        CopyDirectoryEndpoint endPoint = new CopyDirectoryEndpoint(new Directory("root", notebookDirectory()));
        String body = "{\"path\":\"" + existingPathEndpointParameter + "\",\"sourcePath\":\"" + sourceDirectoryParameter
                + "\"}";
        JsonResponse response = endPoint.createResponse(new JsonRequest(body));
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertEquals(
                        "java.nio.file.FileAlreadyExistsException: Path at " + existingPath + " is already in use!",
                        response.body().getString("message")
                );
    }

}
