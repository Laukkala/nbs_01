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
    private String expectedFileContent = "{\"id\":\"2A94M5J3Z\",\"name\":\"my_note2\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Hello, I'm a new notebook. Totally different to the previous one, I have one less paragraphs, you see.\\\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\\\"\"}},{\"id\":\""+paragraphId+"\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";

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
                    "http://" + serverAddress() + "/notebook" + notebookName + "/paragraph/"+paragraphId, "{}"
            );
            // Assert that we receive the proper response.
            Assertions.assertEquals(HttpStatus.CREATED_201,response.status());
            Assertions.assertTrue(response.body().getString("message").contains("Created new paragraph "));
            // Assert that the paragraph was created into the file.
            Assertions
                    .assertEquals(
                            expectedFileContent, com.google.common.io.Files
                                    .readLines(Paths.get(notebookPath.toString()).toFile(), Charset.defaultCharset()).stream().collect(Collectors.joining())
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

            String newNotebookName = "testFileName_12345.zpln";
            Path newNotebookPath = Paths.get(newNotebookName);

            // Assert that the file we are creating doesn't already exist.
            Assertions.assertFalse(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
            Response response = makeHttpPUTRequest(
                    "http://" + serverAddress() + "/notebook/" + newNotebookPath + "?source=my_note3_2A94M5J3Z.zpln",
                    "{\"sourcePath\":\"/my_note4_2A94M5J4Z.zpln\",\"title\":\"copyNotebook\"}"
            );
            // Assert that we receive the proper response.
            Assertions.assertTrue(response.body().getString("message").contains("Created new notebook "));
            // Assert that the file was created.
            Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
        });
    }

    @Test
    // Assert that a HTTP DELETE request to /notebook/{path/to/notebook} endpoint results in a notebook being deleted
    public void httpDeleteTest() {
        Assertions.assertDoesNotThrow(() -> {

            String notebookName = "my_note3_2A94M5J3Z.zpln";
            Path notebookPath = Paths.get(notebookName);

            // Assert that the correct number of files exist
            Assertions.assertEquals(4, Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            // Assert that the file to be deleted exists.
            Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), notebookPath.toString())));
            Response response = makeHttpDELETERequest("http://" + serverAddress() + "/notebook/" + notebookName, "{}");
            Assertions.assertEquals(204, response.status());
            // Assert that a file was deleted.
            Assertions.assertEquals(3, Files.list(notebookDirectory()).collect(Collectors.toList()).size());
            // Assert that the correct file was deleted.
            Assertions.assertFalse(Files.exists(notebookPath));
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
            Assertions.assertTrue(response.body().getString("message").strip().toString().contains("Paragraph not found"));
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
                            + "/notebook/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/nonexistent_paragraph.zpln/paragraph/some_id"
            );
            Assertions.assertTrue(response.body().getString("message").strip().toString().contains("Notebook not found"));
        });
    }


    //@Test
    // Assert that a HTTP POST request to /notebook/{path/to/notebook} endpoint results in an updated file containing the modifications contained in the request body.
    public void httpUpdateParagraphTest() {
        Assertions.assertDoesNotThrow(() -> {
            // Assert that the file content is the same as in the resource files before edits.
            String notebookId = "2A94M5J2Z";
            Path notebookPath = Paths.get("my_folder_2A94M5J1D", "my_note2_2A94M5J2Z.zpln");
            String title = "editedTitle";
            String paragraphId = "20150326-214658_12335843";
            String paragraphContent = "test edit";
            String originalFileContent = "{  \"paragraphs\": [    {      \"text\": \"%test\\n## Congratulations, it\\u0027s done.\\n##### You can create your own notebook in \\u0027Notebook\\u0027 menu. Good luck!\",      \"config\": {        \"colWidth\": 12.0,        \"graph\": {          \"mode\": \"table\",          \"height\": 300.0,          \"optionOpen\": false,          \"keys\": [],          \"values\": [],          \"groups\": [],          \"scatter\": {}        },        \"editorHide\": true      },      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1423836268492_216498320\",      \"id\": \"20150213-230428_1231780373\",      \"results\": {        \"code\": \"SUCCESS\",        \"msg\": [          {            \"type\": \"HTML\",            \"data\": \"\\u003ch2\\u003eCongratulations, it\\u0027s done.\\u003c/h2\\u003e\\n\\u003ch5\\u003eYou can create your own notebook in \\u0027Notebook\\u0027 menu. Good luck!\\u003c/h5\\u003e\\n\"          }        ]      },      \"dateCreated\": \"Feb 13, 2015 11:04:28 PM\",      \"dateStarted\": \"Apr 1, 2015 9:12:18 PM\",      \"dateFinished\": \"Apr 1, 2015 9:12:18 PM\",      \"status\": \"FINISHED\",      \"progressUpdateIntervalMs\": 500    },    {      \"text\": \"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM\\u00272011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\",      \"config\": {        \"colWidth\": 12.0,        \"graph\": {          \"mode\": \"table\",          \"height\": 300.0,          \"optionOpen\": false,          \"keys\": [],          \"values\": [],          \"groups\": [],          \"scatter\": {}        },        \"editorHide\": true      },      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1427420818407_872443482\",      \"id\": \"20150326-214658_12335843\",      \"results\": {        \"code\": \"SUCCESS\",        \"msg\": [          {            \"type\": \"HTML\",            \"data\": \"\\u003cp\\u003eAbout bank data\\u003c/p\\u003e\\n\\u003cpre\\u003e\\u003ccode\\u003eCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM\\u00272011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n\\u003c/code\\u003e\\u003c/pre\\u003e\\n\"          }        ]      },      \"dateCreated\": \"Mar 26, 2015 9:46:58 PM\",      \"dateStarted\": \"Jul 3, 2015 1:44:56 PM\",      \"dateFinished\": \"Jul 3, 2015 1:44:56 PM\",      \"status\": \"FINISHED\",      \"progressUpdateIntervalMs\": 500    },    {      \"config\": {},      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1435955447812_-158639899\",      \"id\": \"20150703-133047_853701097\",      \"dateCreated\": \"Jul 3, 2015 1:30:47 PM\",      \"status\": \"READY\",      \"progressUpdateIntervalMs\": 500    }  ],  \"id\": \"2A94M5J2Z\",  \"name\": \"my_note2\",  \"angularObjects\": {},  \"config\": {    \"looknfeel\": \"default\"  },  \"info\": {}}";
            String expectedFileContent = "{\"id\":\"2A94M5J2Z\",\"name\":\"editedTitle\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Congratulations, it's done.\\\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\\\"\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"test edit\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";

            Assertions
                    .assertEquals(
                            originalFileContent,
                            Files.readAllLines(Paths.get(notebookDirectory().toString(), notebookPath.toString()), Charset.defaultCharset()).stream().collect(Collectors.joining())
                    );

            Response response = makeHttpPOSTRequest(
                    "http://" + serverAddress() + "/notebook/" + notebookPath.toString(),
                    "{\"title\":\"" + title + "\",\"paragraphId\":\"" + paragraphId + "\",\"paragraphText\":\""
                            + paragraphContent + "\"}"
            );
            // Assert that we got the proper response.
            Assertions
                    .assertTrue(response.body().getString("message").strip().contains("Notebook edited successfully"));
            // Assert that the file content has the edited paragraph saved to file in the correct place.
            Assertions
                    .assertEquals(
                            expectedFileContent, com.google.common.io.Files
                                    .readLines(Paths.get(notebookDirectory().toString(), notebookPath.toString()).toFile(), Charset.defaultCharset()).stream().collect(Collectors.joining())
                    );
        });
    }

}
