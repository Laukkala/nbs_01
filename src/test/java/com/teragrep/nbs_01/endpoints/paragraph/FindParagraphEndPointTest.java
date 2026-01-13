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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FindParagraphEndPointTest extends AbstractNotebookServerTest {

    @Test
    // Assert that a HTTP request to /notebook/{path/to/notebook}/paragraph/{paragraphId} endpoint results in a response with the expected file contents
    public void httpFindTest() {
        // Assert that the file exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook1())));
        final String paragraphId = "20150210-015259_1403135953";

        final Path requestPath = Paths.get(notebook1().toString(), paragraphId);
        final FindParagraphEndPoint endPoint = new FindParagraphEndPoint(
                new LocalFilesystemStorage(notebookDirectory())
        );
        final HTTPResponse response = endPoint
                .createResponse(new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath)));
        final Header expectedLocationHeader = new BasicHeader(
                "Location",
                requestPath.subpath(0, requestPath.getNameCount() - 1).toString()
        );
        final Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());
        final String expectedFileContent = "{\"id\":\"20150210-015259_1403135953\",\"title\":\"Load data into table\",\"script\":{\"text\":\"%test import org.apache.commons.io.IOUtils\\nimport java.net.URL\\nimport java.nio.charset.Charset\\n\\n// Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)\\n// So you don't need create them manually\\n\\n// load bank data\\nval bankText = sc.parallelize(\\n    IOUtils.toString(\\n        new URL(\\\"https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv\\\"),\\n        Charset.forName(\\\"utf8\\\")).split(\\\"\\\\n\\\"))\\n\\ncase class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)\\n\\nval bank = bankText.map(s => s.split(\\\";\\\")).filter(s => s(0) != \\\"\\\\\\\"age\\\\\\\"\\\").map(\\n    s => Bank(s(0).toInt, \\n            s(1).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(2).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(3).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(5).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\").toInt\\n        )\\n).toDF()\\nbank.registerTempTable(\\\"bank\\\")\"}}";
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertEquals(expectedFileContent, response.body().asString().strip())
                );
    }

    @Test
    // Assert that a HTTP request to /notebook/{path/to/notebook}/paragraph/{paragraphId} endpoint with a nonexistent Notebook results in an error
    public void httpFindParagraphFromNonExistentNotebookTest() {
        final String nonExistentNotebookName = "NonExistentNotebook";
        final FindParagraphEndPoint endPoint = new FindParagraphEndPoint(
                new LocalFilesystemStorage(notebookDirectory())
        );
        final String paragraphId = "20150210-015259_1403135953";

        final Path requestPath = Paths.get(nonExistentNotebookName, paragraphId);
        final HTTPResponse response = endPoint
                .createResponse(new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath)));

        // The endpoint should return a Response with the correct status and messagsse.
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such file: " + nonExistentNotebookName)
                .build();
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    // Assert that a HTTP request to /notebook/{path/to/notebook}/paragraph/{paragraphId} endpoint with a nonexistent paragraphId results in an error
    public void httpFindNonexistentParagraph() {
        final String nonExistentParagraphId = "nonExistentParagraphId";
        final FindParagraphEndPoint endPoint = new FindParagraphEndPoint(
                new LocalFilesystemStorage(notebookDirectory())
        );

        final Path requestPath = Paths.get(notebook1().toString(), nonExistentParagraphId);
        final HTTPResponse response = endPoint
                .createResponse(new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath)));

        // The endpoint should return an JsonResponse with the correct status and specified cause.
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "Paragraph with id " + nonExistentParagraphId + " not found!")
                .build();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(FindParagraphEndPoint.class).verify();
    }
}
