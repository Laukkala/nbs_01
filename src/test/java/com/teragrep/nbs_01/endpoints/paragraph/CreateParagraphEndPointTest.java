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
import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.requests.JsonRequest;
import com.teragrep.nbs_01.responses.JsonResponse;
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
        Directory root = new Directory(notebookDirectory());
        CreateParagraphEndpoint endPoint = new CreateParagraphEndpoint(root);

        JsonRequest request = new JsonRequest(
                "{\"path\":\"" + notebookName + "\",\"paragraphId\":\"" + paragraphId + "\"}"
        );
        JsonResponse response = endPoint.createResponse(request);
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Assertions.assertTrue(response.body().getString("message").contains("Created new paragraph"));
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
        // Assert that the file we are creating doesn't exist.
        Assertions.assertFalse(Files.exists(nonexistentFilePath));
        Directory root = new Directory(notebookDirectory());
        CreateParagraphEndpoint endPoint = new CreateParagraphEndpoint(root);

        JsonRequest request = new JsonRequest(
                "{\"path\":\"" + nonexistentFileName + "\",\"paragraphId\":\"" + paragraphId + "\"}"
        );
        JsonResponse response = endPoint.createResponse(request);
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertEquals(
                        "java.io.FileNotFoundException: Notebook or directory with path " + nonexistentFilePath
                                + " not found!",
                        response.body().getString("message")
                );
    }
}
