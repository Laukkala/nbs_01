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
package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.endpoints.paragraph.DeleteParagraphEndpoint;
import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.requests.JsonRequest;
import com.teragrep.nbs_01.responses.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeleteParagraphEndPointTest extends AbstractNotebookServerTest {

    private String notebookName = "/my_note3_2A94M5J3Z.zpln";
    private Path notebookPath = Paths.get(notebookDirectory().toString(), notebookName);
    private String paragraphId = "20150213-230428_1231780373";
    private String expectedFileContent = "{\"id\":\"2A94M5J3Z\",\"name\":\"my_note2\",\"config\":{},\"paragraphs\":[]}";

    public DeleteParagraphEndPointTest() {
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
    // Assert that a HTTP request to /notebook/new endpoint results in a new file being saved on disk.
    public void httpDeleteParagraphTest() {
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertTrue(Files.exists(notebookPath));
        Directory root = new Directory("root", notebookDirectory());
        DeleteParagraphEndpoint endPoint = new DeleteParagraphEndpoint(root);

        JsonRequest request = new JsonRequest(
                "{\"path\":\"" + notebookName + "\",\"paragraphId\":\"" + paragraphId + "\"}"
        );
        Response response = endPoint.createResponse(request);
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.NO_CONTENT_204, response.status());
        Assertions.assertTrue(response.body().getString("message").contains("Deleted paragraph"));
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
}
