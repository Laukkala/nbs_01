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

public final class FindNotebookEndPointTest extends AbstractNotebookServerTest {

    @Test
    // Assert that a HTTP GET request to /notebook/{path/to/notebook} endpoint results in a response with the expected file contents
    public void httpFindTest() {
        // Destination notebook must exist
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook1())));
        final String expectedFileContent = "\"title\":\"my_note1\",\"config\":{}";

        final FindNotebookEndPoint endPoint = new FindNotebookEndPoint(new LocalFilesystemStorage(notebookDirectory()));
        final HTTPResponse response = endPoint
                .createResponse(new BasicHTTPRequest(new HTTPBasicRequestPath(notebook1())));
        final Header expectedLocationHeader = new BasicHeader("Location", notebook1().toString());
        final Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertTrue(response.body().asString().strip().contains(expectedFileContent))
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertTrue(response.body().asString().strip().contains(notebook1Paragraph1()))
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertTrue(response.body().asString().strip().contains(notebook1Paragraph2()))
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertTrue(response.body().asString().strip().contains(notebook1Paragraph3()))
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertTrue(response.body().asString().strip().contains(notebook1Paragraph4()))
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertTrue(response.body().asString().strip().contains(notebook1Paragraph5()))
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertTrue(response.body().asString().strip().contains(notebook1Paragraph6()))
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertTrue(response.body().asString().strip().contains(notebook1Paragraph7()))
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertTrue(response.body().asString().strip().contains(notebook1Paragraph8()))
                );
    }

    // Assert that a HTTP GET request to /notebook/{path/to/notebook} endpoint with a path not corresponding with any file results in an error
    @Test
    public void httpNotebookNotFoundTest() {
        final Path nonExistentNotebookPath = Paths.get("nonExistentNotebook");
        // Start server and wait for it to initialize.
        final FindNotebookEndPoint endPoint = new FindNotebookEndPoint(new LocalFilesystemStorage(notebookDirectory()));
        final HTTPResponse response = endPoint
                .createResponse(new BasicHTTPRequest(new HTTPBasicRequestPath(nonExistentNotebookPath)));

        // The endpoint should return the correct status and message.
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such file: " + nonExistentNotebookPath)
                .build();

        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    // Assert that a HTTP GET request to /notebook/{path/to/notebook} endpoint with a path to a malformed / corrupted file results in an error message directing users to check details from logs.
    @Test
    public void httpFindCorruptNotebookTest() {
        final Path nonExistentNotebookPath = Paths.get("junkfile");
        final FindNotebookEndPoint endPoint = new FindNotebookEndPoint(new LocalFilesystemStorage(notebookDirectory()));
        final HTTPResponse response = endPoint
                .createResponse(new BasicHTTPRequest(new HTTPBasicRequestPath(nonExistentNotebookPath)));

        // The endpoint should return the correct status and message.
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.status());
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions
                                .assertTrue(
                                        response
                                                .body()
                                                .asString()
                                                .contains(
                                                        "An error occurred while processing your Request. See event id "
                                                )
                                )
                );

        Assertions
                .assertDoesNotThrow(
                        () -> Assertions
                                .assertTrue(response.body().asString().contains(" in the technical log for details."))
                );
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(FindNotebookEndPoint.class).verify();
    }
}
