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
package com.teragrep.nbs_01.servlets;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.responses.JsonResponse;
import jakarta.json.Json;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DirectoryServletTest extends AbstractNotebookServerTest {

    private final String directoryName = "my_folder_2A94M5J1D";
    private final Path directoryPath = Paths.get(directoryName);
    private final String childNotebookName = "my_note2_2A94M5J2Z.zpln";
    private final Path childNotebookPath = Paths.get(directoryPath.toString(), childNotebookName);

    private final String childDirectoryName = "my_second_folder_2A94M5J2D";
    private final Path childDirectoryPath = Paths.get(directoryPath.toString(), childDirectoryName);

    public DirectoryServletTest() {
    }

    @Test
    // Assert that a HTTP PUT request to /directory/{path/to/directory} endpoint results in a new file being saved on disk.
    public void httpCreateDirectoryTest() {

        String newDirectoryName = "testFolderName/";
        Path newDirectoryPath = Paths.get(newDirectoryName);
        String requestBody = Json.createObjectBuilder().build().toString();

        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(newDirectoryPath)));
        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/directory/" + newDirectoryName, requestBody
                        )
                );
        // Assert that we receive the proper response.
        Assertions.assertTrue(response.body().getString("message").contains("Created new directory "));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(newDirectoryPath)));
    }

    @Test
    // Copying a directory
    public void httpCopyDirectoryTest() {
        String copyDirectoryName = "testCopyFolderName/";
        Path copyDirectoryPath = Paths.get(copyDirectoryName);
        String requestBody = Json.createObjectBuilder().add("sourcePath", directoryPath.toString()).build().toString();

        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(copyDirectoryPath)));
        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/directory/" + copyDirectoryName, requestBody
                        )
                );
        // Assert that we receive the proper response.
        Assertions.assertTrue((response.body().getString("message").contains("Created new directory ")));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(copyDirectoryPath)));
        // Assert that the original file still exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
    }

    @Test
    // Assert that a HTTP PUT request to /directory/{path/to/directory} endpoint where something already exists in the path results in an error.
    public void httpCopyDirectoryToExistingPathTest() {
        String copyDirectoryName = "testCopyFolderName/";
        Path copyDirectoryPath = Paths.get(copyDirectoryName);
        String requestBody = Json.createObjectBuilder().add("sourcePath", directoryPath.toString()).build().toString();

        // Assert that there is a file in the path where we plan to copy our directory to.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryName)));
        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/directory/" + directoryName, requestBody
                        )
                );
        // Assert that we receive the proper response.
        Assertions
                .assertTrue((response.body().getString("message").contains("Path at " + notebookDirectory().toString() + "/" + directoryName + " is already in use!")));
        // Assert that the file was not created.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(copyDirectoryPath)));
        // Assert that the original file still exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
    }

    @Test
    // Assert that a HTTP DELETE request to /directory/{path/to/directory} endpoint results in a directory and its children being deleted
    public void httpDeleteDirectoryTest() {

        // Assert that the correct number of files exist
        Assertions
                .assertEquals(4, Assertions.assertDoesNotThrow(() -> Files.list(notebookDirectory()).collect(Collectors.toList()).size()));

        // Assert that the file to be deleted and its children exist.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(childNotebookPath)));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(childDirectoryPath)));

        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest("http://" + serverAddress() + "/directory/" + directoryName, "{}")
                );
        Assertions.assertEquals(204, response.status());
        // Assert that a file was deleted.
        Assertions
                .assertEquals(3, Assertions.assertDoesNotThrow(() -> Files.list(notebookDirectory()).collect(Collectors.toList()).size()));
        // Assert that the correct file was deleted.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(directoryPath)));
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(childNotebookPath)));
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(childDirectoryPath)));
    }

    @Test
    // Assert that a HTTP GET request to /directory/{path/to/directory} endpoint results in a response with the expected contents
    public void httpFindDirectoryTest() {
        String expectedJson = "{\"name\":\"my_folder_2A94M5J1D\",\"children\":\"[" + directory2() + ", " + notebook2()
                + "]\"}";
        // Assert that the file exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
        JsonResponse response = Assertions
                .assertDoesNotThrow(() -> makeHttpGETRequest("http://" + serverAddress() + "/directory/" + directoryName));
        Assertions.assertEquals(expectedJson, response.body().toString());
    }

    @Test
    // Assert that a HTTP GET request to /directory/{path/to/directory} endpoint with nonexistent path results in a response with the expected contents
    public void httpFindNonexistentDirectoryTest() {
        String nonexistentDirectoryName = "nonexistent_directory";
        String expectedJson = "{\"message\":\"java.io.FileNotFoundException: Notebook or directory with path target/notebooks/nonexistent_directory not found!\"}";
        // Assert that the file does not exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(nonexistentDirectoryName)));
        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest("http://" + serverAddress() + "/directory/" + nonexistentDirectoryName)
                );
        Assertions.assertEquals(expectedJson, response.body().toString());
    }

    @Test
    // Assert that a HTTP GET request to /directory/{path/to/directory} endpoint with a path corresponding to a notebook results in a response with the expected contents
    public void httpFindDirectoryWithNotebookNameTest() {
        String notebookName = "my_note4_2A94M5J4Z.zpln";
        String expectedJson = "{\"message\":\"java.io.FileNotFoundException: Not a directory!\"}";
        // Assert that the path we are looking for exists, even though it's not a directory.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebookName)));
        JsonResponse response = Assertions
                .assertDoesNotThrow(() -> makeHttpGETRequest("http://" + serverAddress() + "/directory/" + notebookName));
        Assertions.assertEquals(expectedJson, response.body().toString());
    }
}
