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
package com.teragrep.nbs_01.endpoints.paragraph;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.protocols.http.path.HTTPParagraphRequestPath;
import com.teragrep.nbs_01.repository.storage.LocalFilesystemStorage;
import com.teragrep.nbs_01.protocols.http.BasicHTTPRequest;
import com.teragrep.nbs_01.protocols.http.HTTPResponse;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DeleteParagraphEndPointTest extends AbstractNotebookServerTest {

    @Test
    // Assert that a request to DeleteParagraphEndpoint results in a file with edited content being saved on disk.
    public void httpDeleteParagraphTest() {
        // Assert that the file we are deleting from exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook3())));
        DeleteParagraphEndpoint endPoint = new DeleteParagraphEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        String paragraphId = "20150213-230428_1231780373";

        Path requestPath = Paths.get(notebook3().toString(), paragraphId);
        BasicHTTPRequest request = new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath));
        HTTPResponse response = endPoint.createResponse(request);
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.NO_CONTENT_204, response.status());
        Header expectedLocationHeader = new BasicHeader(
                "Location",
                requestPath.subpath(0, requestPath.getNameCount() - 1).toString()
        );
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());

        // Assert that the file was changed.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook3())));
        String expectedFileContent = "{\"name\":\"my_note2\",\"config\":{},\"paragraphs\":[]}";
        Assertions
                .assertEquals(
                        expectedFileContent,
                        Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook3())))
                );

    }

    @Test
    // Assert that a request to DeleteParagraphEndpoint with an invalid paragraphId results in an error.
    public void httpDeleteNonexistentParagraphTest() {
        // Assert that the file we are deleting already exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook3())));
        String nonExistentParagraphId = "nonExistentParagraphId";
        DeleteParagraphEndpoint endPoint = new DeleteParagraphEndpoint(new LocalFilesystemStorage(notebookDirectory()));

        Path requestPath = Paths.get(notebook3().toString(), nonExistentParagraphId);

        BasicHTTPRequest request = new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath));
        HTTPResponse response = endPoint.createResponse(request);

        // The endpoint should return a Response with the correct status and message.

        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "Paragraph " + nonExistentParagraphId + " doesn't exist!")
                .build();

        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    // Assert that a request to DeleteParagraphEndpoint a path to a nonexistent notebook results in an error.
    public void httpDeleteParagraphFromNonexistentNotebookTest() {
        // Assert that the file we are creating doesn't already exist.
        String nonExistentNotebookName = "nonExistentNotebook";
        Path nonExistentNotebookPath = Paths.get(notebookDirectory().toString(), nonExistentNotebookName);
        Assertions.assertFalse(Files.exists(nonExistentNotebookPath));
        DeleteParagraphEndpoint endPoint = new DeleteParagraphEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        String paragraphId = "20150213-230428_1231780373";

        Path requestPath = Paths.get(nonExistentNotebookName, paragraphId);
        BasicHTTPRequest request = new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath));
        HTTPResponse response = endPoint.createResponse(request);

        // The endpoint should return an JsonResponse with the correct status and specified cause.

        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such file: " + nonExistentNotebookName)
                .build();
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(DeleteParagraphEndpoint.class).verify();
    }
}
