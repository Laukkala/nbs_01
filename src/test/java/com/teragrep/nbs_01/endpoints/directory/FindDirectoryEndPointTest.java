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

public class FindDirectoryEndPointTest extends AbstractNotebookServerTest {

    @Test
    // Assert that a HTTP request to /directory/find endpoint results in a response with the expected file contents
    public void httpFindTest() {
        // Destination directory must exist
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory2())));

        final String expectedFileContent = "{\"title\":\"my_second_folder_2A94M5J2D\",\"children\":[\""
                + notebook1().getFileName() + "\"]}";

        final FindDirectoryEndPoint endPoint = new FindDirectoryEndPoint(
                new LocalFilesystemStorage(notebookDirectory())
        );
        final HTTPResponse response = endPoint
                .createResponse(new BasicHTTPRequest(new HTTPBasicRequestPath(directory2())));
        Assertions.assertEquals(HttpStatus.OK_200, response.status());
        final Header expectedLocationHeader = new BasicHeader("Location", directory2().toString());
        final Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());
        Assertions.assertDoesNotThrow(() -> Assertions.assertEquals(expectedFileContent, response.body().asString()));
    }

    @Test
    public void httpDirectoryNotFoundTest() {

        // Destination directory must not exist
        final Path nonExistentPath = Paths.get("nonExistentPath");
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(nonExistentPath)));

        final FindDirectoryEndPoint endPoint = new FindDirectoryEndPoint(
                new LocalFilesystemStorage(notebookDirectory())
        );
        final HTTPResponse response = endPoint
                .createResponse(new BasicHTTPRequest(new HTTPBasicRequestPath(nonExistentPath)));

        // The endpoint should return a response with the correct status and message

        final JsonObject expectedResponse = Json
                .createObjectBuilder()
                .add("message", "No such file: " + nonExistentPath)
                .build();

        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertEquals(expectedResponse.toString(), response.body().asString())
                );
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(FindDirectoryEndPoint.class).verify();
    }
}
