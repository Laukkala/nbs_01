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
import com.teragrep.nbs_01.repository.FileTree;
import com.teragrep.nbs_01.requests.JsonRequest;
import com.teragrep.nbs_01.responses.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateParagraphEndPointTest extends AbstractNotebookServerTest {

    private String notebookName = "my_note3_2A94M5J3Z.zpln";
    private Path notebookPath = Paths.get(notebookDirectory().toString(), notebookName);
    private String paragraphId = "testParagraphId";
    private String expectedFileContent = "{\"name\":\"my_note2\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"%test\\n## Hello, I'm a new notebook. Totally different to the previous one, I have one less paragraphs, you see.\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\"}},{\"id\":\"testParagraphId\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";

    public CreateParagraphEndPointTest() {
    }

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterEach
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    // Assert that a HTTP request to CreateParagraphEndpoint results in a new file being saved on disk.
    public void httpCreateParagraphTest() {
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertTrue(Files.exists(notebookPath));
        CreateParagraphEndpoint endPoint = new CreateParagraphEndpoint(new FileTree(notebookDirectory()));

        Path requestPath = Paths.get(notebookName, "/paragraph/" + paragraphId);
        JsonRequest request = new JsonRequest("{}", requestPath);
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
        Assertions.assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body()));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(notebookPath));
        Assertions
                .assertEquals(
                        expectedFileContent,
                        Assertions
                                .assertDoesNotThrow(
                                        () -> com.google.common.io.Files.readLines(Paths.get(notebookPath.toString()).toFile(), Charset.defaultCharset()).stream().collect(Collectors.joining())
                                )
                );
    }

    @Test
    // Assert that a HTTP request to /notebook/new endpoint with a path that alreday doesn't contain a file results in an error.
    public void httpCreateParagraphInNonExistentPathTest() {

        String nonexistentFileName = "NonExistentFile";
        Path nonexistentFilePath = Paths.get(notebookDirectory().toString(), nonexistentFileName);
        // Assert that the file we are trying to add a paragraph to doesn't exist.
        Assertions.assertFalse(Files.exists(nonexistentFilePath));
        CreateParagraphEndpoint endPoint = new CreateParagraphEndpoint(new FileTree(notebookDirectory()));

        Path requestPath = Paths.get(nonexistentFileName, "/paragraph/" + paragraphId);
        JsonRequest request = new JsonRequest("{\"paragraphId\":\"" + paragraphId + "\"}", requestPath);
        Response response = endPoint.createResponse(request);

        // The endpoint should return an ExceptionResponse with the correct status and specified cause.

        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such notebook: " + nonexistentFileName + " !")
                .build();
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions.assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body()));
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(CreateParagraphEndpoint.class).verify();
    }
}
