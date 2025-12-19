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
import com.teragrep.nbs_01.http.body.JSONBody;
import com.teragrep.nbs_01.repository.LocalFilesystemStorage;
import com.teragrep.nbs_01.http.requests.BasicRequest;
import com.teragrep.nbs_01.http.responses.Response;
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

public class CreateParagraphEndPointTest extends AbstractNotebookServerTest {

    @Test
    // Assert that a HTTP request to CreateParagraphEndpoint results in a new file being saved on disk.
    public void httpCreateParagraphTest() {
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook3())));
        CreateParagraphEndpoint endPoint = new CreateParagraphEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        String paragraphId = "testParagraphId";

        Path requestPath = Paths.get(notebook3().toString(), paragraphId);
        BasicRequest request = new BasicRequest(requestPath);
        Response response = endPoint.createResponse(request);
        // Assert that we receive the proper response.

        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("id", paragraphId)
                .add("title", "")
                .add("script", Json.createObjectBuilder().add("text", "").build())
                .build();

        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Header expectedLocationHeader = new BasicHeader("Location", requestPath.toString());
        Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook3())));
        String expectedFileContent = "{\"id\":\"" + paragraphId + "\",\"title\":\"\",\"script\":{\"text\":\"\"}}";
        Assertions
                .assertTrue(Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook3())).contains(expectedFileContent)));
    }

    @Test
    // Assert that a HTTP request to /notebook/new endpoint with a path that alreday doesn't contain a file results in an error.
    public void httpCreateParagraphInNonExistentPathTest() {

        String nonexistentFileName = "NonExistentFile";
        Path nonexistentFilePath = Paths.get(notebookDirectory().toString(), nonexistentFileName);
        // Assert that the file we are trying to add a paragraph to doesn't exist.
        Assertions.assertFalse(Files.exists(nonexistentFilePath));
        CreateParagraphEndpoint endPoint = new CreateParagraphEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        String paragraphId = "testParagraphId";

        Path requestPath = Paths.get(nonexistentFileName, paragraphId);
        JsonObject body = Json.createObjectBuilder().add("paragraphId", paragraphId).build();
        BasicRequest request = new BasicRequest(requestPath, new JSONBody(body));
        Response response = endPoint.createResponse(request);

        // The endpoint should return an JsonResponse with the correct status and specified cause.

        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such file: " + nonexistentFileName)
                .build();
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(CreateParagraphEndpoint.class).verify();
    }
}
