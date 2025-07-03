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
package com.teragrep.nbs_01.servlets;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.responses.JsonResponse;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParagraphServletTest extends AbstractNotebookServerTest {

    private final String notebookName = "my_note4_2A94M5J4Z.zpln";
    private final String firstParagraphId = "20150213-231621_168813393";
    private final String firstParagraphText = "%test\\n## Welcome to Zeppelin.\\n##### This is a live tutorial, you can run the code yourself. (Shift-Enter to Run)";
    private final String firstParagraphTitle = "";

    private final String secondParagraphId = "20150210-015259_1403135953";
    private final String secondParagraphText = "%test import org.apache.commons.io.IOUtils\\nimport java.net.URL\\nimport java.nio.charset.Charset\\n\\n// Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)\\n// So you don\\u0027t need create them manually\\n\\n// load bank data\\nval bankText \\u003d sc.parallelize(\\n    IOUtils.toString(\\n        new URL(\\\"https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv\\\"),\\n        Charset.forName(\\\"utf8\\\")).split(\\\"\\\\n\\\"))\\n\\ncase class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)\\n\\nval bank \\u003d bankText.map(s \\u003d\\u003e s.split(\\\";\\\")).filter(s \\u003d\\u003e s(0) !\\u003d \\\"\\\\\\\"age\\\\\\\"\\\").map(\\n    s \\u003d\\u003e Bank(s(0).toInt, \\n            s(1).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(2).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(3).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(5).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\").toInt\\n        )\\n).toDF()\\nbank.registerTempTable(\\\"bank\\\")";
    private final String secondParagraphTitle = "Load data into table\\";

    private final String thirdParagraphId = "20150210-015302_1492795503";
    private final String thirdParagraphText = "%test \\nselect age, count(1) value\\nfrom bank \\nwhere age \\u003c 30 \\ngroup by age \\norder by age";

    private final String fourthParagraphId = "20150213-230422_1600658137";
    private final String fourthParagraphText = "%test \nselect age, count(1) value \nfrom bank \nwhere marital\u003d\"${marital\u003dsingle,single|divorced|married}\" \ngroup by age \norder by age";

    private final String fifthParagraphId = "20150213-230428_1231780373";
    private final String fifthParagraphText = "%test\\n## Congratulations, it\\u0027s done.\\n##### You can create your own notebook in \\u0027Notebook\\u0027 menu. Good luck!";

    private final String sixthParagraphId = "20150326-214658_12335843";
    private final String sixthParagraphText = "%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM\\u00272011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```";
    private final Path notebookPath = Paths.get(notebookName);

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

    // Searching for a paragraph should result in a message with the contents of the specified paragraph within the specified Notebook
    @Test
    public void httpFindParagraphTest() {
        String expectedparagraphContent = "{\"id\":\"" + firstParagraphId + "\",\"title\":\"" + firstParagraphTitle
                + "\",\"script\":{\"text\":\"" + firstParagraphText + "\"}}";

        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + firstParagraphId
                        )
                );
        // Assert that a GET request is responded to with the response code 200 OK
        Assertions.assertEquals(HttpStatus.OK_200, response.status());
        // Assert that the body of the response matches with the paragraph's text saved on file
        Assertions.assertEquals(expectedparagraphContent, response.body().getString("message"));
    }

    // Searching for a nonexistent paragraph should result in an error
    @Test
    public void httpFindNonexistentParagraphTest() {
        String nonexistentParagraphId = "I_DONT_EXIST";
        String expectedResponseMessage = "Malformed request:\n"
                + "com.teragrep.nbs_01.exceptions.MalformedRequestException: java.io.FileNotFoundException: Paragraph not found!";
        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + nonexistentParagraphId
                        )
                );
        // Assert that a faulty GET request is responded to with the response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        // Assert that the body of the response contains a message mentioning that the paragraph was not found
        Assertions.assertEquals(expectedResponseMessage, response.body().getString("message"));
    }

    // Searching for a paragraph from a notebook that doesn't exist should result in an error.
    @Test
    public void httpFindParagraphFromNonexistentNotebookTest() {
        String nonexistentNotebookId = "I_DONT_EXIST";
        String expectedResponseMessage = "Malformed request:\n"
                + "com.teragrep.nbs_01.exceptions.MalformedRequestException: java.io.FileNotFoundException: Notebook or directory with path target/notebooks/"
                + nonexistentNotebookId + " not found!";
        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/notebook/" + nonexistentNotebookId + "/paragraph/"
                                        + firstParagraphId
                        )
                );
        // Assert that a faulty GET request is responded to with the response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        // Assert that the body of the response contains a message mentioning that the notebook was not found
        Assertions.assertEquals(expectedResponseMessage, response.body().getString("message"));
    }

    // Creating a paragraph should result in an existing notebook being saved to disk containing an additional paragraph.
    @Test
    public void httpCreateParagraphTest() {
        String newParagraphId = "2025-01-01-021311-132-133";
        String requestBody = Json.createObjectBuilder().build().toString();
        String expectedResponseMessage = "Created new paragraph " + newParagraphId;
        String expectedparagraphContent = "{\"id\":\"" + newParagraphId
                + "\",\"title\":\"\",\"script\":{\"text\":\"\"}}";

        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + newParagraphId,
                                requestBody
                        )
                );
        // Assert that a PUT request is responded to with the response code 201 CREATED
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        // Assert that the body of the response contains a message mentioning the creation of the paragraph
        Assertions.assertEquals(expectedResponseMessage, response.body().getString("message"));

        // Assert that the created paragraph is contained within the saved file of the notebook
        String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertTrue(fileContents.contains(expectedparagraphContent));
    }

    // Trying to create a paragraph in a notebook that doesn't exist should result in an error.
    @Test
    public void httpCreateParagraphInNonexistentNotebookTest() {
        String newParagraphId = "2025-01-01-021311-132-133";
        String nonexistentNotebookId = "I_DONT_EXIST";
        String requestBody = Json.createObjectBuilder().build().toString();
        String expectedResponseMessage = "Notebook doesn't exist!";

        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/notebook/" + nonexistentNotebookId + "/paragraph/"
                                        + newParagraphId,
                                requestBody
                        )
                );
        // Assert that a faulty PUT request is responded to with the response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        // Assert that the body of the response contains a message mentioning that the notebook doesn't exist
        Assertions.assertEquals(expectedResponseMessage, response.body().getString("message"));

        // Assert that the paragraph id is not contained within the saved file of the notebook
        String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertFalse(fileContents.contains(newParagraphId));
    }

    // Deleting a specific paragraph from a specific should result in the notebook being saved to disk without the specified paragraph.
    @Test
    public void httpDeleteParagraphTest() {
        String requestBody = Json.createObjectBuilder().build().toString();
        try {
            Thread.sleep(5000); // This should not be necessary
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + firstParagraphId,
                                requestBody
                        )
                );
        // Assert that a DELETE request is responded to with the response code 204 NO CONTENT
        Assertions.assertEquals(HttpStatus.NO_CONTENT_204, response.status());
        // Assert that the created paragraph is not contained within the saved file of the notebook after deletion
        String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertFalse(fileContents.contains(firstParagraphId));
        Assertions.assertFalse(fileContents.contains(firstParagraphText));
    }

    // Deleting a nonexistent paragraph should result in an error.
    @Test
    public void httpDeleteNonexistentParagraphTest() {
        String nonexistentParagraphId = "I_DONT_EXIST";
        String requestBody = Json.createObjectBuilder().build().toString();
        String expectedResponseMessage = "Paragraph " + nonexistentParagraphId + " doesn't exist!";

        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + nonexistentParagraphId,
                                requestBody
                        )
                );
        // Assert that a faulty DELETE request is responded to with the response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions.assertEquals(expectedResponseMessage, response.body().getString("message"));

        // Assert that the created paragraph is not contained within the saved file of the notebook
        String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertFalse(fileContents.contains(nonexistentParagraphId));
    }

    // Deleting a paragraph from a nonexistent notebook should result in an error.
    @Test
    public void httpDeleteParagraphFromNonexistentNotebookTest() {
        String nonexistentNotebookId = "I_DONT_EXIST";
        String requestBody = Json.createObjectBuilder().build().toString();
        String expectedResponseMessage = "Notebook doesn't exist!";

        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest(
                                "http://" + serverAddress() + "/notebook/" + nonexistentNotebookId + "/paragraph/"
                                        + firstParagraphId,
                                requestBody
                        )
                );
        // Assert that a faulty DELETE request is responded to with the response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions.assertEquals(expectedResponseMessage, response.body().getString("message"));

        // Assert that the created paragraph is not contained within the saved file of the notebook
        String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertTrue(fileContents.contains(firstParagraphId));
    }

    // Updating a specific paragraph in a specific notebook should result in the notebook being saved to disk with the updated content
    @Test
    public void httpUpdateParagraphTest() {
        String newParagraphTitle = "new_paragraph_title";
        String newParagraphText = "%test\ntesting_this";
        String requestBody = Json
                .createObjectBuilder()
                .add("title", newParagraphTitle)
                .add("text", newParagraphText)
                .build()
                .toString();
        String expectedResponseMessage = "Paragraph edited successfully";
        String expectedParagraphContent = Json
                .createObjectBuilder()
                .add("id", firstParagraphId)
                .add("title", newParagraphTitle)
                .add("script", Json.createObjectBuilder().add("text", newParagraphText))
                .build()
                .toString();
        JsonResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPOSTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + firstParagraphId,
                                requestBody
                        )
                );
        // Assert that a POST request is responded to with the response code 200 OK
        Assertions.assertEquals(HttpStatus.OK_200, response.status());
        // Assert that the body of the response contains a message mentioning the editing of the paragraph
        Assertions.assertEquals(expectedResponseMessage, response.body().getString("message"));

        // Assert that the edited paragraph is contained within the saved file of the notebook
        String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertTrue(fileContents.contains(expectedParagraphContent));
    }

    // Copying is not an atomic operation. You must first create a new paragraph, then update it with the output of the source paragraph.
    @Test
    public void httpCopyParagraphTest() {

        String newParagraphId = "2025-01-01-021311-132-133";

        // Make an HTTP GET request to /notebook/{path/to/notebook/}/paragraph/{paragraphId}
        JsonResponse getResponse = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + firstParagraphId
                        )
                );
        // Assert that the GET request is responded to with the response code 200 OK
        Assertions.assertEquals(HttpStatus.OK_200, getResponse.status());

        // Make an HTTP PUT request to /notebook/{path/to/notebook/}/paragraph/{paragraphId} to create the copy.
        String putRequestBody = Json.createObjectBuilder().build().toString();
        JsonResponse putResponse = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + newParagraphId,
                                putRequestBody
                        )
                );
        // Assert that the PUT request is responded to with the response code 201 CREATED
        Assertions.assertEquals(HttpStatus.CREATED_201, putResponse.status());

        // Make an HTTP POST request to /notebook/{path/to/notebook/}/paragraph/{paragraphId} to edit the copy with the same information as the source paragraph.
        // Read the received paragraph into a JSON object.
        JsonObject sourceParagraph = Json
                .createReader(new StringReader(getResponse.body().getString("message")))
                .readObject();
        String newParagraphTitle = sourceParagraph.getString("title");
        String newParagraphText = sourceParagraph.getJsonObject("script").getString("text");
        String postRequestBody = Json
                .createObjectBuilder()
                .add("title", newParagraphTitle)
                .add("text", newParagraphText)
                .build()
                .toString();
        String expectedParagraphContent = Json
                .createObjectBuilder()
                .add("id", newParagraphId)
                .add("title", newParagraphTitle)
                .add("script", Json.createObjectBuilder().add("text", newParagraphText))
                .build()
                .toString();

        String originalParagraphContent = Json
                .createObjectBuilder()
                .add("id", firstParagraphId)
                .add("title", newParagraphTitle)
                .add("script", Json.createObjectBuilder().add("text", newParagraphText))
                .build()
                .toString();

        JsonResponse postResponse = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPOSTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + newParagraphId,
                                postRequestBody
                        )
                );
        // Assert that the POST request is responded to with the response code 200 OK
        Assertions.assertEquals(HttpStatus.OK_200, postResponse.status());

        // Assert that the copied paragraph is contained within the saved file of the notebook
        String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertTrue(fileContents.contains(expectedParagraphContent));

        // Assert that the original paragraph is also contained within the saved file of the notebook
        Assertions.assertTrue(fileContents.contains(originalParagraphContent));
    }

}
