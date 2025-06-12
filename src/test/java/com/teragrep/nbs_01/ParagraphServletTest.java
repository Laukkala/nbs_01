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
package com.teragrep.nbs_01;

import com.teragrep.nbs_01.responses.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParagraphServletTest extends AbstractNotebookServerTest {

    private final String notebookName = "/my_note3_2A94M5J3Z.zpln";
    private final String paragraphId = "testParagraphId";
    private final Path notebookPath = Paths.get(notebookDirectory().toString(), notebookName);
    private String expectedFileContent = "{\"id\":\"2A94M5J3Z\",\"name\":\"my_note2\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Hello, I'm a new notebook. Totally different to the previous one, I have one less paragraphs, you see.\\\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\\\"\"}},{\"id\":\""
            + paragraphId + "\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";

    public ParagraphServletTest() {
    }

    @BeforeEach
    private void setUp() throws Exception {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
        startServer();
    }

    @AfterEach
    private void tearDown() throws Exception {
        deleteFileRecursively(notebookDirectory().toFile());
        stopServer();
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/notebook} endpoint results in a new file being saved on disk.
    public void httpCreateParagraphTest() {
        Assertions.assertDoesNotThrow(() -> {

            // Assert that the file we are creating a paragraph into exists.
            Assertions.assertTrue(Files.exists(notebookPath));
            Response response = makeHttpPUTRequest(
                    "http://" + serverAddress() + "/notebook" + notebookName + "/paragraph/" + paragraphId, "{}"
            );
            // Assert that we receive the proper response.
            Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
            Assertions.assertTrue(response.body().getString("message").contains("Created new paragraph "));
            // Assert that the paragraph was created into the file.
            Assertions
                    .assertEquals(
                            expectedFileContent, com.google.common.io.Files.readLines(Paths.get(notebookPath.toString()).toFile(), Charset.defaultCharset()).stream().collect(Collectors.joining())
                    );
        });
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/directory} endpoint results in a new file being saved on disk.
    public void httpCreateDirectoryTest() {
        Assertions.assertDoesNotThrow(() -> {

            String newNotebookName = "testFolderName/";
            Path newNotebookPath = Paths.get(newNotebookName);

            // Assert that the file we are creating doesn't already exist.
            Assertions.assertFalse(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
            Response response = makeHttpPUTRequest(
                    "http://" + serverAddress() + "/notebook/" + newNotebookName, "{\"title\":\"newTitle\"}"
            );
            // Assert that we receive the proper response.
            Assertions.assertTrue(response.body().getString("message").contains("Created new directory "));
            // Assert that the file was created.
            Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
        });
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/directory} endpoint results in a new file being saved on disk.
    public void httpCopyDirectoryTest() {
        Assertions.assertDoesNotThrow(() -> {

            String newNotebookName = "testCopyFolderName/";
            Path newNotebookPath = Paths.get(newNotebookName);

            // Assert that the file we are creating doesn't already exist.
            Assertions.assertFalse(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
            Response response = makeHttpPUTRequest(
                    "http://" + serverAddress() + "/notebook/" + newNotebookName,
                    "{\"sourcePath\":\"/my_folder_2A94M5J1D/\",\"title\":\"copyDirectory\"}"
            );
            // Assert that we receive the proper response.
            Assertions.assertTrue(response.body().getString("message").contains("Created new directory "));
            // Assert that the file was created.
            Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
        });
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/notebook} endpoint results in a copied file being saved on disk.
    public void httpCopyNotebookTest() {
        Assertions.assertDoesNotThrow(() -> {
        });
    }

    @Test
    // Assert that a HTTP DELETE request to /notebook/{path/to/notebook} endpoint results in a notebook being deleted
    public void httpDeleteTest() {
        Assertions.assertDoesNotThrow(() -> {
        });
    }

    @Test
    // Assert that a HTTP GET request to /notebook/{path/to/notebook}/paragraph/{paragraph_id} endpoint results in a response with the expected file contents
    public void httpFindParagraphTest() {
        Assertions.assertDoesNotThrow(() -> {
            Path notebookPath = Paths
                    .get("src/test/resources/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
            String expectedFileContent = "{\"id\":\"20150213-231621_168813393\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Welcome to Zeppelin.\\\\n##### This is a live tutorial, you can run the code yourself. (Shift-Enter to Run)\\\"\"}}";
            // Assert that the file exists.
            Assertions.assertTrue(Files.exists(notebookPath));
            Response response = makeHttpGETRequest(
                    "http://" + serverAddress()
                            + "/notebook/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln/paragraph/20150213-231621_168813393"
            );
            Assertions.assertEquals(expectedFileContent, response.body().getString("message").strip().toString());
        });
    }

    // Assert that searching for a nonexistent paragraph results in a message saying that the paragraph was not found
    @Test
    public void httpFindNonexistentParagraphTest() {
        Assertions.assertDoesNotThrow(() -> {
            Path notebookPath = Paths
                    .get("src/test/resources/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
            // Assert that the file exists, even if the paragraph doesn't
            Assertions.assertTrue(Files.exists(notebookPath));
            Response response = makeHttpGETRequest(
                    "http://" + serverAddress()
                            + "/notebook/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln/paragraph/nonexistent_id"
            );
            Assertions
                    .assertTrue(response.body().getString("message").strip().toString().contains("Paragraph not found"));
        });
    }

    // Assert that searching for a paragraph from a notebook that doesn't exist results in a message saying that the notebook was not found
    @Test
    public void httpFindParagraphFromNonexistentNotebookTest() {
        Assertions.assertDoesNotThrow(() -> {
            Path notebookPath = Paths
                    .get("src/test/resources/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/nonexistent_paragraph.zpln");
            // Assert that the file doesn't exist
            Assertions.assertFalse(Files.exists(notebookPath));
            Response response = makeHttpGETRequest(
                    "http://" + serverAddress()
                            + "/notebook/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/nonexistent_paragraph.zpln/paragraph/"
                            + paragraphId
            );
            Assertions
                    .assertTrue(response.body().getString("message").strip().toString().contains("Notebook not found"));
        });
    }

    @Test
    // Assert that a HTTP POST request to /notebook/{path/to/notebook} endpoint results in an updated file containing the modifications contained in the request body.
    public void httpUpdateParagraphTest() {
        Assertions.assertDoesNotThrow(() -> {
        });
    }

}
