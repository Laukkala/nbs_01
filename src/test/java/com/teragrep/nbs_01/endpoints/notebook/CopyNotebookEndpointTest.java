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
import com.teragrep.nbs_01.repository.FileTree;
import com.teragrep.nbs_01.requests.JsonRequest;
import com.teragrep.nbs_01.responses.ExceptionResponse;
import com.teragrep.nbs_01.responses.Response;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class CopyNotebookEndpointTest extends AbstractNotebookServerTest {

    private String sourceNotebookName = "my_note2_2A94M5J2Z.zpln";
    private Path sourceNotebookPath = Paths
            .get(notebookDirectory().toString(), "my_folder_2A94M5J1D", sourceNotebookName);
    private Path sourceNotebookParameter = Paths.get("my_folder_2A94M5J1D", sourceNotebookName);
    private String newNotebookName = "testNotebookName";
    private Path newNotebookPath = Paths.get(notebookDirectory().toString(), newNotebookName);
    private Path nonExistentNotebookPath = Paths.get("tillintallin", "tallintillin");
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
    // Assert that a proper request to CopyNotebookEndpoint results in a correct response and a file being saved to disk.
    public void httpCopyNotebookTest() {
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(newNotebookPath));
        Assertions.assertTrue(Files.exists(sourceNotebookPath));
        CopyNotebookEndpoint endPoint = new CopyNotebookEndpoint(new FileTree(notebookDirectory()));
        String body = "{\"sourcePath\":\"" + sourceNotebookParameter + "\"}";
        Response response = endPoint.createResponse(new JsonRequest(body, Paths.get(newNotebookName)));
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Assertions.assertTrue(response.body().getString("message").contains("Created new notebook"));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(newNotebookPath));
    }

    @Test
    // Assert that a request to CopyNotebookEndpoint with a source path that does not have a file results in an error
    public void httpCopyNonExistentNotebookTest() {
        // Assert that there is no file saved in the source path we are using
        Assertions.assertFalse(Files.exists(nonExistentNotebookPath));
        CopyNotebookEndpoint endPoint = new CopyNotebookEndpoint(new FileTree(notebookDirectory()));
        String body = "{\"sourcePath\":\"" + nonExistentNotebookPath + "\"}";
        Response response = endPoint.createResponse(new JsonRequest(body, Paths.get(newNotebookName)));

        // The endpoint should return an ExceptionResponse with the correct status and specified cause.
        Assertions.assertTrue(response.getClass().equals(ExceptionResponse.class));
        Response expectedResponse = new ExceptionResponse(
                HttpStatus.NOT_FOUND_404,
                new FileNotFoundException("Path at " + existingPath + " is already in use")
        );
        Assertions
                .assertEquals(
                        ((ExceptionResponse) expectedResponse).exception().getCause(),
                        ((ExceptionResponse) response).exception().getCause()
                );
        // Assert that the file was not created.
        Assertions.assertFalse(Files.exists(newNotebookPath));
    }

    @Test
    // Assert that a request to CopyNotebookEndpoint to a path that already contains a file results in an error
    public void httpCopyNotebookIntoUnavailablePathTest() {
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertTrue(Files.exists(existingPath));
        Assertions.assertTrue(Files.exists(sourceNotebookPath));
        CopyNotebookEndpoint endPoint = new CopyNotebookEndpoint(new FileTree(notebookDirectory()));
        String body = "{\"sourcePath\":\"" + sourceNotebookParameter + "\"}";
        Response response = endPoint.createResponse(new JsonRequest(body, existingPathEndpointParameter));

        // The endpoint should return an ExceptionResponse with the correct status and specified cause.
        Assertions.assertTrue(response.getClass().equals(ExceptionResponse.class));
        Response expectedResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST_400,
                new FileAlreadyExistsException("Path at " + existingPath + " is already in use")
        );
        Assertions
                .assertEquals(
                        ((ExceptionResponse) expectedResponse).exception().getCause(),
                        ((ExceptionResponse) response).exception().getCause()
                );
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(CopyNotebookEndpoint.class).verify();
    }

}
