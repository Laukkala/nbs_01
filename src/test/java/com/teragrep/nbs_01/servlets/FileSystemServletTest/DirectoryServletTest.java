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
package com.teragrep.nbs_01.servlets.FileSystemServletTest;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.http.responses.Response;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DirectoryServletTest extends AbstractNotebookServerTest {

    public DirectoryServletTest() {
    }

    @Test
    // Assert that a HTTP PUT request to /directory/{path/to/directory} endpoint results in a new file being saved on disk.
    public void httpCreateDirectoryTest() {

        Path directoryPath = Paths.get("testFolderName/");
        String requestBody = Json.createObjectBuilder().build().toString();

        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(directoryPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/directory/" + directoryPath, requestBody
                        )
                );
        // Assert that we receive the proper response.

        JsonObject expectedJson = Json.createObjectBuilder().build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));

        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
    }

    @Test
    // Assert that a HTTP PUT request to /directory/{path/to/existing/directory} endpoint results in an error.
    public void httpCreateDirectoryToExistingPathTest() {

        Path directoryPath = directory1();
        String requestBody = Json.createObjectBuilder().build().toString();

        // Assert that the existing directory path already has a saved file.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/directory/" + directoryPath, requestBody
                        )
                );
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "Path at " + directoryPath + " is already in use!")
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    // Copying a directory
    public void httpCopyDirectoryTest() {
        Path directoryPath = Paths.get("testCopyFolderName/");
        Path sourceDirectoryPath = directory1();
        String requestBody = Json
                .createObjectBuilder()
                .add("sourcePath", sourceDirectoryPath.toString())
                .build()
                .toString();

        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(directoryPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/directory/" + directoryPath, requestBody
                        )
                );
        // Assert that we receive the proper response.
        JsonArrayBuilder expectedChildren = Json.createArrayBuilder();
        expectedChildren.add(directory2().getFileName().toString());
        expectedChildren.add(notebook2().getFileName().toString());
        JsonObject expectedJson = Json.createObjectBuilder().build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
        // Assert that the original file still exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
    }

    @Test
    // Assert that a HTTP PUT request to /directory/{path/to/directory} endpoint with a path to a nonexistent source directory results in an error.
    public void httpCopyNonexistentDirectoryTest() {
        Path directoryPath = Paths.get("new_directory");
        Path sourceDirectoryPath = Paths.get("I_DONT_EXIST");
        String requestBody = Json
                .createObjectBuilder()
                .add("sourcePath", sourceDirectoryPath.toString())
                .build()
                .toString();

        // Assert that there is a file in the path where we plan to copy our directory to.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory1())));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/directory/" + directoryPath, requestBody
                        )
                );
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
    }

    @Test
    // Assert that a HTTP PUT request to /directory/{path/to/directory} endpoint where something already exists in the path results in an error.
    public void httpCopyDirectoryToExistingPathTest() {
        Path directoryPath = directory1();
        String requestBody = Json.createObjectBuilder().add("sourcePath", directory2().toString()).build().toString();

        // Assert that there is a file in the path where we plan to copy our directory to.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory1())));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/directory/" + directoryPath, requestBody
                        )
                );
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "Destination " + directoryPath + " is already in use!")
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    // Assert that a HTTP DELETE request to /directory/{path/to/directory} endpoint results in a directory and its children being deleted
    public void httpDeleteDirectoryTest() {

        Path directoryPath = directory1();
        // Assert that the correct number of files exist
        Assertions
                .assertEquals(4, Assertions.assertDoesNotThrow(() -> Files.list(notebookDirectory()).collect(Collectors.toList()).size()));

        // Assert that the file to be deleted and its children exist.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory1())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook2())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory2())));

        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest("http://" + serverAddress() + "/directory/" + directoryPath, "{}")
                );
        Assertions.assertEquals(204, response.status());
        // Assert that a file was deleted.
        Assertions
                .assertEquals(3, Assertions.assertDoesNotThrow(() -> Files.list(notebookDirectory()).collect(Collectors.toList()).size()));
        // Assert that the correct file was deleted.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(directory1())));
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(notebook2())));
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(directory2())));
    }

    @Test
    // Assert that a HTTP DELETE request to /directory/{path/to/nonexistent_directory} endpoint results in a 404 NOT FOUND response, as the directory to be deleted does not exist.
    public void httpDeleteNonexistentDirectoryTest() {
        Path nonexistentDirectoryPath = Paths.get("thisAintItChief");

        // Assert that the correct number of files exist
        Assertions
                .assertEquals(4, Assertions.assertDoesNotThrow(() -> Files.list(notebookDirectory()).collect(Collectors.toList()).size()));

        // Assert that the file to be deleted does not exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(nonexistentDirectoryPath)));

        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest(
                                "http://" + serverAddress() + "/directory/" + nonexistentDirectoryPath, "{}"
                        )
                );
        Assertions.assertEquals(404, response.status());
        // Assert that no files were deleted.
        Assertions
                .assertEquals(4, Assertions.assertDoesNotThrow(() -> Files.list(notebookDirectory()).collect(Collectors.toList()).size()));
    }

    @Test
    // Assert that a HTTP DELETE request to /directory/{path/to/directory} endpoint with a path corresponding to a notebook results in a response with the expected contents
    public void httpDeleteDirectoryWithNotebookPathTest() {
        Path directoryPath = notebook4();
        // Assert that the path we are looking for exists, even though it's not a directory.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest("http://" + serverAddress() + "/directory/" + directoryPath, "{}")
                );
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", directoryPath + " is not a Directory!")
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    // Assert that a HTTP GET request to /directory/{path/to/directory} endpoint results in a response with the expected contents
    public void httpFindDirectoryTest() {
        Path directoryPath = directory1();
        // Assert that the file exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory1())));
        Response response = Assertions
                .assertDoesNotThrow(() -> makeHttpGETRequest("http://" + serverAddress() + "/directory/" + directoryPath));

        JsonArrayBuilder expectedChildren = Json.createArrayBuilder();
        expectedChildren.add(notebook2().getFileName().toString());
        expectedChildren.add(directory2().getFileName().toString());
        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("name", directoryPath.toString())
                .add("children", expectedChildren)
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    // Assert that a HTTP GET request to /directory/{path/to/directory} endpoint with nonexistent path results in a response with the expected contents
    public void httpFindNonexistentDirectoryTest() {
        String directoryPath = "I_DONT_EXIST";
        // Assert that the file does not exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(directoryPath)));
        Response response = Assertions
                .assertDoesNotThrow(() -> makeHttpGETRequest("http://" + serverAddress() + "/directory/" + directoryPath));
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
    }

    @Test
    // Assert that a HTTP GET request to /directory/{path/to/notebook} endpoint with a path corresponding to a notebook results in a response with the expected contents
    public void httpFindDirectoryWithNotebookPathTest() {
        Path directoryPath = notebook1();
        // Assert that the path we are looking for exists and that it's a directory
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
        Assertions.assertFalse(Files.isDirectory(notebookDirectory().resolve(directoryPath)));
        Response response = Assertions
                .assertDoesNotThrow(() -> makeHttpGETRequest("http://" + serverAddress() + "/directory/" + directoryPath));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "File at path " + directoryPath + " is not a directory!")
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }
}
