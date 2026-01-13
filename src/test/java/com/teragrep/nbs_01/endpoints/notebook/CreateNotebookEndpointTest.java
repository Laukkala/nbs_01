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
import com.teragrep.nbs_01.protocols.http.body.JSONBody;
import com.teragrep.nbs_01.protocols.http.path.HTTPBasicRequestPath;
import com.teragrep.nbs_01.repository.storage.LocalFilesystemStorage;
import com.teragrep.nbs_01.protocols.http.BasicHTTPRequest;
import com.teragrep.nbs_01.protocols.http.HTTPResponse;
import jakarta.json.Json;
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

class CreateNotebookEndpointTest extends AbstractNotebookServerTest {

    @Test
    // Assert that a proper request to CreateNotebookEndpoint results in a correct response and a file being saved to disk.
    public void httpCreateNotebookTest() {
        Path newNotebookPath = Paths.get("testNotebook");
        // Destination notebook must not exist
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(newNotebookPath)));

        CreateNotebookEndpoint endPoint = new CreateNotebookEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        HTTPResponse response = endPoint
                .createResponse(new BasicHTTPRequest(new HTTPBasicRequestPath(newNotebookPath)));
        // Assert that we receive the proper response.

        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("title", "")
                .add("config", Json.createObjectBuilder().build())
                .add("paragraphs", Json.createArrayBuilder())
                .build();

        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Header expectedLocationHeader = new BasicHeader("Location", newNotebookPath.toString());
        Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        // Destination notebook must exist
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(newNotebookPath)));
    }

    @Test
    // Assert that a proper request to CreateNotebookEndpoint with a specified title in request body results in a correct response and a file being saved to disk.
    public void httpCreateNotebookWithTitleTest() {
        Path newNotebookPath = Paths.get("testNotebook");
        // Destination notebook must not exist
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(newNotebookPath)));

        String title = "newNotebook";
        CreateNotebookEndpoint endPoint = new CreateNotebookEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        JsonObject body = Json.createObjectBuilder().add("title", title).build();
        HTTPResponse response = endPoint
                .createResponse(new BasicHTTPRequest(new HTTPBasicRequestPath(newNotebookPath), new JSONBody(body)));
        // Assert that we receive the proper response.

        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("title", title)
                .add("config", Json.createObjectBuilder().build())
                .add("paragraphs", Json.createArrayBuilder())
                .build();

        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Header expectedLocationHeader = new BasicHeader("Location", newNotebookPath.toString());
        Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(newNotebookPath)));
    }

    @Test
    // Assert that a request to CreateNotebookEndpoint to a path that already contains a Directory results in an error
    public void httpCreateAndOverwriteNotebookTest() {
        // Destination notebook must exist
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook2())));
        String originalFileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2())));
        CreateNotebookEndpoint endPoint = new CreateNotebookEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        HTTPResponse response = endPoint.createResponse(new BasicHTTPRequest(new HTTPBasicRequestPath(notebook2())));
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        String editedFileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2())));
        // Destination file must have changed.
        Assertions.assertNotEquals(originalFileContent, editedFileContent);

    }

    @Test
    // Assert that a request to CreateNotebookEndpoint to a path that already contains a Directory results in an error
    public void httpCreateNotebookIntoUnavailablePathTest() {
        // Destination notebook must exist
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory1())));
        Assertions.assertTrue(Files.isDirectory(notebookDirectory().resolve(directory1())));
        CreateNotebookEndpoint endPoint = new CreateNotebookEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        HTTPResponse response = endPoint.createResponse(new BasicHTTPRequest(new HTTPBasicRequestPath(directory1())));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(CreateNotebookEndpoint.class).verify();
    }
}
