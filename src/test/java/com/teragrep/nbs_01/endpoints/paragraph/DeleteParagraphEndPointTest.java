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
import com.teragrep.nbs_01.repository.FileTree;
import com.teragrep.nbs_01.requests.JsonRequest;
import com.teragrep.nbs_01.responses.ExceptionResponse;
import com.teragrep.nbs_01.responses.Response;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeleteParagraphEndPointTest extends AbstractNotebookServerTest {

    private final String notebookName = "my_note3_2A94M5J3Z.zpln";
    private final Path notebookPath = Paths.get(notebookDirectory().toString(), notebookName);
    private final String paragraphId = "20150213-230428_1231780373";
    private final String expectedFileContent = "{\"name\":\"my_note2\",\"config\":{},\"paragraphs\":[]}";

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
    // Assert that a request to DeleteParagraphEndpoint results in a file with edited content being saved on disk.
    public void httpDeleteParagraphTest() {
        // Assert that the file we are deleting from exists.
        Assertions.assertTrue(Files.exists(notebookPath));
        DeleteParagraphEndpoint endPoint = new DeleteParagraphEndpoint(new FileTree(notebookDirectory()));

        Path requestPath = Paths.get(notebookName, "/paragraph/" + paragraphId);
        JsonRequest request = new JsonRequest("{}", requestPath);
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

    @Test
    // Assert that a request to DeleteParagraphEndpoint with an invalid paragraphId results in an error.
    public void httpDeleteNonexistentParagraphTest() {
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertTrue(Files.exists(notebookPath));
        String nonExistentParagraphId = "nonExistentParagraphId";
        Directory root = new Directory(notebookDirectory());
        DeleteParagraphEndpoint endPoint = new DeleteParagraphEndpoint(new FileTree(notebookDirectory()));

        Path requestPath = Paths.get(notebookName, "/paragraph/" + nonExistentParagraphId);

        JsonRequest request = new JsonRequest("{}", requestPath);
        Response response = endPoint.createResponse(request);

        // The endpoint should return an ExceptionResponse with the correct status and specified cause.
        Assertions.assertTrue(response.getClass().equals(ExceptionResponse.class));
        Response expectedResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST_400,
                new FileNotFoundException("Paragraph " + nonExistentParagraphId + " doesn't exist!")
        );
        Assertions
                .assertEquals(
                        ((ExceptionResponse) expectedResponse).exception().getCause(),
                        ((ExceptionResponse) response).exception().getCause()
                );
        Assertions.assertEquals(expectedResponse.status(), response.status());
    }

    @Test
    // Assert that a request to DeleteParagraphEndpoint a path to a nonexistent notebook results in an error.
    public void httpDeleteParagraphFromNonexistentNotebookTest() {
        // Assert that the file we are creating doesn't already exist.
        String nonExistentNotebookName = "nonExistentNotebook";
        Path nonExistentNotebookPath = Paths.get(notebookDirectory().toString(), nonExistentNotebookName);
        Assertions.assertFalse(Files.exists(nonExistentNotebookPath));
        Directory root = new Directory(notebookDirectory());
        DeleteParagraphEndpoint endPoint = new DeleteParagraphEndpoint(new FileTree(notebookDirectory()));

        Path requestPath = Paths.get(nonExistentNotebookName, "/paragraph/" + paragraphId);
        JsonRequest request = new JsonRequest("{}", requestPath);
        Response response = endPoint.createResponse(request);

        // The endpoint should return an ExceptionResponse with the correct status and specified cause.
        Assertions.assertTrue(response.getClass().equals(ExceptionResponse.class));
        Response expectedResponse = new ExceptionResponse(
                HttpStatus.NOT_FOUND_404,
                new FileNotFoundException("Notebook or directory with path " + nonExistentNotebookPath + " not found!")
        );
        Assertions
                .assertEquals(
                        ((ExceptionResponse) expectedResponse).exception().getCause(),
                        ((ExceptionResponse) response).exception().getCause()
                );
        Assertions.assertEquals(expectedResponse.status(), response.status());
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(DeleteParagraphEndpoint.class).verify();
    }
}
