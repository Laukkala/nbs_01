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

import com.google.common.base.Charsets;
import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.protocols.http.body.JSONBody;
import com.teragrep.nbs_01.protocols.http.path.HTTPParagraphRequestPath;
import com.teragrep.nbs_01.repository.storage.LocalFilesystemStorage;
import com.teragrep.nbs_01.protocols.http.BasicHTTPRequest;
import com.teragrep.nbs_01.protocols.http.HTTPResponse;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class CopyParagraphEndpointTest extends AbstractNotebookServerTest {

    @Test
    // Assert that a HTTP request to CopyParagraphEndpoint results in an updated file with the contents of the copied paragraph being saved on disk.
    public void httpCopyParagraphTest() {
        final Path destinationNotebookPath = notebookDirectory().resolve(notebook1());
        final String destinationParagraphId = "copyParagraph";
        final Path sourceNotebookPath = notebookDirectory().resolve(notebook3());
        final String sourceParagraphId = "20150213-230428_1231780373";
        // Source and Destination files should exist.
        Assertions.assertTrue(Files.exists(destinationNotebookPath));
        Assertions.assertTrue(Files.exists(sourceNotebookPath));

        // Assert that the paragraph text is contained in the sourceNotebook but not in the destinationNotebook
        final String expectedParagraphContent = "\"text\":\"%test\\n## Hello, I'm a new notebook. Totally different to the previous one, I have one less paragraphs, you see.\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\"";
        // Read file contents into a JSON object to handle unicode escaping that is present in the test files.
        final JsonObject sourceFileObject = readFileContents(sourceNotebookPath);
        final JsonObject originalDestinationFileObject = readFileContents(destinationNotebookPath);
        Assertions.assertTrue(sourceFileObject.toString().contains(expectedParagraphContent));
        Assertions.assertFalse(originalDestinationFileObject.toString().contains(expectedParagraphContent));

        final CopyParagraphEndpoint endPoint = new CopyParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );
        final Path requestPath = Paths.get(notebook1().toString(), destinationParagraphId);

        final JsonObject body = Json
                .createObjectBuilder()
                .add("sourcePath", notebook3().toString())
                .add("sourceParagraphId", sourceParagraphId)
                .build();
        final BasicHTTPRequest request = new BasicHTTPRequest(
                new HTTPParagraphRequestPath(requestPath),
                new JSONBody(body)
        );
        final HTTPResponse response = endPoint.createResponse(request);

        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        final Header expectedLocationHeader = new BasicHeader(
                "Location",
                requestPath.subpath(0, requestPath.getNameCount() - 1).toString()
        );
        final Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions
                                .assertTrue(
                                        response
                                                .body()
                                                .asString()
                                                .contains(
                                                        "\"script\":{\"text\":\"%test\\n## Hello, I'm a new notebook. Totally different to the previous one, I have one less paragraphs, you see.\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\"}"
                                                )
                                )
                );

        // Assert that the paragraph content exists in the destination file after the operation is complete.
        final JsonObject destinationFileObject = readFileContents(destinationNotebookPath);
        Assertions
                .assertTrue(
                        destinationFileObject
                                .toString()
                                .contains(new String(expectedParagraphContent.getBytes(), Charsets.UTF_8))
                );
    }

    @Test
    // Assert that a HTTP request to CopyParagraphEndpoint with an incorrect source paragraph id results in an error.
    public void httpCopyParagraphWithIncorrectSourceParagraphIdTest() {
        final Path destinationNotebookPath = notebookDirectory().resolve(notebook1());
        final String destinationParagraphId = "copyParagraph";
        final Path sourceNotebookPath = notebookDirectory().resolve(notebook3());
        final String sourceParagraphId = "I_DON'T_EXIST";

        // Source and Destination files must exist.
        Assertions.assertTrue(Files.exists(destinationNotebookPath));
        Assertions.assertTrue(Files.exists(sourceNotebookPath));

        // Assert that the paragraph ID we are looking for is not contained in the sourceNotebook
        final String expectedParagraphContent = "I_DON'T_EXIST";
        // Read file contents into a JSON object to handle unicode escaping that is present in the test files.
        final JsonObject sourceFileObject = readFileContents(sourceNotebookPath);
        Assertions.assertFalse(sourceFileObject.toString().contains(expectedParagraphContent));

        final CopyParagraphEndpoint endPoint = new CopyParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );
        final Path requestPath = Paths.get(notebook1().toString(), destinationParagraphId);
        final JsonObject body = Json
                .createObjectBuilder()
                .add("sourcePath", notebook3().toString())
                .add("sourceParagraphId", sourceParagraphId)
                .build();
        final BasicHTTPRequest request = new BasicHTTPRequest(
                new HTTPParagraphRequestPath(requestPath),
                new JSONBody(body)
        );
        final HTTPResponse response = endPoint.createResponse(request);

        // Assert that we receive the proper response.

        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such paragraph: " + sourceParagraphId + "!")
                .build();

        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));

        // Assert that the paragraph content does not exist in the destination file after the operation is complete.
        final JsonObject destinationFileObject = readFileContents(destinationNotebookPath);
        Assertions
                .assertFalse(
                        destinationFileObject
                                .toString()
                                .contains(new String(expectedParagraphContent.getBytes(), Charsets.UTF_8))
                );
    }

    @Test
    // Assert that a HTTP request to CopyParagraphEndpoint with no source paragraph id results in an error
    public void httpCopyParagraphWithNoSourceParagraphId() {
        final String destinationParagraphId = "copyParagraph";
        final Path sourceNotebookPath = notebookDirectory().resolve(notebook3());
        // Source path must exist
        Assertions.assertTrue(Files.exists(sourceNotebookPath));

        final CopyParagraphEndpoint endPoint = new CopyParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );
        final Path requestPath = Paths.get(notebook1().toString(), destinationParagraphId);
        final JsonObject body = Json.createObjectBuilder().add("sourcePath", notebook3().toString()).build();
        final BasicHTTPRequest request = new BasicHTTPRequest(
                new HTTPParagraphRequestPath(requestPath),
                new JSONBody(body)
        );
        final HTTPResponse response = endPoint.createResponse(request);

        // Assert that we receive the proper response.

        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "Request has a malformed sourceParagraph identifier!")
                .build();

        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    // Assert that a HTTP request to CopyParagraphEndpoint results in an updated file with the contents of the copied paragraph being saved on disk.
    public void httpCopyParagraphWithIncorrectSourcePathTest() {
        final String destinationParagraphId = "copyParagraph";
        final String sourceNotebookName = "I_DONT_EXIST";
        final Path sourceNotebookPath = Paths.get(sourceNotebookName);
        final String sourceParagraphId = "20150213-230428_1231780373";
        // Source path must not exist
        Assertions.assertFalse(Files.exists(sourceNotebookPath));

        final CopyParagraphEndpoint endPoint = new CopyParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );
        final Path requestPath = Paths.get(notebook1().toString(), destinationParagraphId);
        final JsonObject body = Json
                .createObjectBuilder()
                .add("sourcePath", sourceNotebookName)
                .add("sourceParagraphId", sourceParagraphId)
                .build();
        final BasicHTTPRequest request = new BasicHTTPRequest(
                new HTTPParagraphRequestPath(requestPath),
                new JSONBody(body)
        );
        final HTTPResponse response = endPoint.createResponse(request);

        // Assert that we receive the proper response.
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such file: " + sourceNotebookName)
                .build();

        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    // Assert that a HTTP request to CopyParagraphEndpoint results in an updated file with the contents of the copied paragraph being saved on disk.
    public void httpCopyParagraphWithNoSourcePathTest() {
        final String destinationParagraphId = "copyParagraph";
        final String sourceParagraphId = "20150213-230428_1231780373";

        final CopyParagraphEndpoint endPoint = new CopyParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );
        final Path requestPath = Paths.get(notebook1().toString(), destinationParagraphId);
        final JsonObject body = Json.createObjectBuilder().add("sourceParagraphId", sourceParagraphId).build();
        final BasicHTTPRequest request = new BasicHTTPRequest(
                new HTTPParagraphRequestPath(requestPath),
                new JSONBody(body)
        );
        final HTTPResponse response = endPoint.createResponse(request);

        // Assert that we receive the proper response.

        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "Request has a malformed source identifier!")
                .build();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    // Assert that a HTTP request to CopyParagraphEndpoint with a sourceId that already exists results in an error.
    public void httpCopyParagraphIntoExistingParagraphIdTest() {
        final Path destinationNotebookPath = notebookDirectory().resolve(notebook1());
        final String destinationParagraphId = "20150213-231621_168813393";
        final Path sourceNotebookPath = notebookDirectory().resolve(notebook3());
        final String sourceParagraphId = "20150213-230428_1231780373";
        // Source and destination paths must exist
        Assertions.assertTrue(Files.exists(destinationNotebookPath));
        Assertions.assertTrue(Files.exists(sourceNotebookPath));

        // Assert that the paragraph id is already contained in the destinationNotebook
        // Read file contents into a JSON object to handle unicode escaping that is present in the test files.
        final JsonObject originalDestinationFileObject = readFileContents(destinationNotebookPath);
        Assertions.assertTrue(originalDestinationFileObject.toString().contains(destinationParagraphId));

        final CopyParagraphEndpoint endPoint = new CopyParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );
        final Path requestPath = Paths.get(notebook1().toString(), destinationParagraphId);
        final JsonObject body = Json
                .createObjectBuilder()
                .add("sourcePath", notebook3().toString())
                .add("sourceParagraphId", sourceParagraphId)
                .build();
        final BasicHTTPRequest request = new BasicHTTPRequest(
                new HTTPParagraphRequestPath(requestPath),
                new JSONBody(body)
        );
        final HTTPResponse response = endPoint.createResponse(request);

        // Assert that we receive the proper response.

        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "Paragraph " + destinationParagraphId + " already exists!")
                .build();

        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    // Assert that a HTTP request to CopyParagraphEndpoint results in an updated file with the contents of the copied paragraph being saved on disk.
    public void httpCopyParagraphIntoIncorrectDestinationPathTest() {
        final Path destinationNotebookPath = Paths.get("I_DONT_EXIST");
        final String destinationParagraphId = "copyParagraph";
        final Path sourceNotebookPath = notebookDirectory().resolve(notebook3());
        final String sourceParagraphId = "20150213-230428_1231780373";
        // Source path must exist, but destination past must not
        Assertions.assertFalse(Files.exists(destinationNotebookPath));
        Assertions.assertTrue(Files.exists(sourceNotebookPath));

        final CopyParagraphEndpoint endPoint = new CopyParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );
        final Path requestPath = Paths.get(destinationNotebookPath.toString(), destinationParagraphId);
        final JsonObject body = Json
                .createObjectBuilder()
                .add("sourcePath", notebook3().toString())
                .add("sourceParagraphId", sourceParagraphId)
                .build();
        final BasicHTTPRequest request = new BasicHTTPRequest(
                new HTTPParagraphRequestPath(requestPath),
                new JSONBody(body)
        );
        final HTTPResponse response = endPoint.createResponse(request);

        // Assert that we receive the proper response.

        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such file: " + destinationNotebookPath)
                .build();

        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    private JsonObject readFileContents(final Path filePath) {
        final String fileContent = Assertions.assertDoesNotThrow(() -> Files.readString(filePath));
        final StringReader stringReader = new StringReader(fileContent);
        final JsonReader jsonReader = Json.createReader(stringReader);
        final JsonObject destinationFileObject = jsonReader.readObject();
        stringReader.close();
        jsonReader.close();
        return destinationFileObject;
    }
}
