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
package com.teragrep.nbs_01.endpoints.directory;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.http.body.JSONBody;
import com.teragrep.nbs_01.repository.LocalFilesystemStorage;
import com.teragrep.nbs_01.http.requests.BasicHTTPRequest;
import com.teragrep.nbs_01.http.responses.HTTPResponse;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

class CopyDirectoryEndpointTest extends AbstractNotebookServerTest {

    @Test
    // Assert that a proper request to CopyDirectoryEndpoint results in a correct response and a directory being saved to disk.
    public void httpCopyDirectoryTest() {
        // Source directory must exist
        Path sourceDirectory = notebookDirectory().resolve(directory1());
        Assertions.assertTrue(Files.exists(sourceDirectory));
        List<Path> sourceDirectoryChildren = Assertions
                .assertDoesNotThrow(() -> Files.list(sourceDirectory))
                .map(path -> path.getFileName())
                .collect(Collectors.toList());

        // Destination directory must not exist
        Path destinationDirectory = Paths.get("testDirectory");
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(destinationDirectory)));

        CopyDirectoryEndpoint endPoint = new CopyDirectoryEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        JsonObject body = Json.createObjectBuilder().add("sourcePath", directory1().toString()).build();
        HTTPResponse response = endPoint
                .createResponse(new BasicHTTPRequest(Paths.get(destinationDirectory.toString()), new JSONBody(body)));

        // Assert that we receive the proper response.
        JsonArrayBuilder expectedChildren = Json.createArrayBuilder();
        expectedChildren.add(notebook1().getFileName().toString());
        JsonObject expectedJson = Json.createObjectBuilder().build();
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Header expectedLocationHeader = new BasicHeader("Location", destinationDirectory.toString());
        Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(response.body().asString(), expectedJson.toString()));

        // Destination directory must exist
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(destinationDirectory)));
        // Destination directory must contain same list of files as Source directory
        List<Path> destinationDirectoryChildren = Assertions
                .assertDoesNotThrow(() -> Files.list(notebookDirectory().resolve(destinationDirectory)))
                .map(path -> path.getFileName())
                .collect(Collectors.toList());
        Assertions.assertEquals(sourceDirectoryChildren, destinationDirectoryChildren);
        // Source directory must exist
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory1())));
    }

    @Test
    // Assert that a request to CopyDirectoryEndpoint with a source path that does not have any saved file results in an error
    public void httpCopyNonExistentSourceDirectoryTest() {
        // Source directory not must exist
        Path sourceDirectory = Paths.get("I_DONT_EXIST");
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(sourceDirectory)));

        // Destination directory must not exist
        Path destinationDirectory = Paths.get("testDirectory");
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(destinationDirectory)));

        CopyDirectoryEndpoint endPoint = new CopyDirectoryEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        JsonObject body = Json.createObjectBuilder().add("sourcePath", sourceDirectory.toString()).build();
        HTTPResponse response = endPoint.createResponse(new BasicHTTPRequest(destinationDirectory, new JSONBody(body)));
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());

        // Assert that the file was not created.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(destinationDirectory)));
    }

    @Test
    // Assert that a request to CopyDirectoryEndpoint to a path that already contains a directory results in an error
    public void httpCopyDirectoryIntoUnavailablePathTest() {
        // Source directory must exist
        Path sourceDirectory = directory2();
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(sourceDirectory)));

        // Destination directory must exist
        Path destinationDirectoryName = directory1();
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(destinationDirectoryName)));

        CopyDirectoryEndpoint endPoint = new CopyDirectoryEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        JsonObject body = Json.createObjectBuilder().add("sourcePath", directory2().toString()).build();
        HTTPResponse response = endPoint.createResponse(new BasicHTTPRequest(directory1(), new JSONBody(body)));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(CopyDirectoryEndpoint.class).verify();
    }

}
