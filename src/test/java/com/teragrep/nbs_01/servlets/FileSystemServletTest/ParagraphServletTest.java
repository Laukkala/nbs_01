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
package com.teragrep.nbs_01.servlets.FileSystemServletTest;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.protocols.http.HTTPResponse;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ParagraphServletTest extends AbstractNotebookServerTest {

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

    // Searching for a paragraph should result in a message with the contents of the specified paragraph within the specified Notebook
    @Test
    public void httpFindParagraphTest() {
        final String expectedparagraphContent = "{\"id\":\"" + firstParagraphId + "\",\"title\":\""
                + firstParagraphTitle + "\",\"script\":{\"text\":\"" + firstParagraphText + "\"}}";

        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/paragraph/" + notebookPath + "/" + firstParagraphId
                        )
                );
        // Assert that a GET request is responded to with the response code 200 OK
        Assertions.assertEquals(HttpStatus.OK_200, response.status());
        // Assert that the body of the response matches with the paragraph's text saved on file
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedparagraphContent, response.body().asString()));
    }

    // Searching for a nonexistent paragraph should result in an error
    @Test
    public void httpFindNonexistentParagraphTest() {
        final String nonexistentParagraphId = "I_DONT_EXIST";
        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/paragraph/" + notebookPath + "/"
                                        + nonexistentParagraphId
                        )
                );
        // Assert that a faulty GET request is responded to with the response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        // Assert that the body of the response contains a message mentioning that the paragraph was not found
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "Paragraph with id " + nonexistentParagraphId + " not found!")
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    // Searching for a paragraph from a notebook that doesn't exist should result in an error.
    @Test
    public void httpFindParagraphFromNonexistentNotebookTest() {
        final String nonexistentNotebookName = "I_DONT_EXIST";
        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/paragraph/" + nonexistentNotebookName + "/"
                                        + firstParagraphId
                        )
                );
        // Assert that a faulty GET request is responded to with the response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        // Assert that the body of the response contains a message mentioning that the notebook was not found
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such file: " + nonexistentNotebookName)
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    // Creating a paragraph should result in an existing notebook being saved to disk containing an additional paragraph.
    @Test
    public void httpCreateParagraphTest() {
        final String newParagraphId = "2025-01-01-021311-132-133";
        final String requestBody = Json.createObjectBuilder().build().toString();
        final String expectedparagraphContent = "{\"id\":\"" + newParagraphId
                + "\",\"title\":\"\",\"script\":{\"text\":\"\"}}";

        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/paragraph/" + notebookPath + "/" + newParagraphId,
                                requestBody
                        )
                );
        // Assert that a PUT request is responded to with the response code 201 CREATED
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        // Assert that the body of the response contains a message mentioning the creation of the paragraph
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("id", newParagraphId)
                .add("title", "")
                .add("script", Json.createObjectBuilder().add("text", "").build())
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));

        // Assert that the created paragraph is contained within the saved file of the notebook
        final String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertTrue(fileContents.contains(expectedparagraphContent));
    }

    // Trying to create a paragraph in a notebook that doesn't exist should result in an error.
    @Test
    public void httpCreateParagraphInNonexistentNotebookTest() {
        final String newParagraphId = "2025-01-01-021311-132-133";
        final String nonexistentNotebookId = "I_DONT_EXIST";
        final Path nonexistentNotebookPath = Paths.get(notebookDirectory().toString(), nonexistentNotebookId);
        final String requestBody = Json.createObjectBuilder().build().toString();

        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/paragraph/" + nonexistentNotebookId + "/"
                                        + newParagraphId,
                                requestBody
                        )
                );
        // Assert that a faulty PUT request is responded to with the response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        // Assert that the body of the response contains a message mentioning that the notebook doesn't exist
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such file: " + nonexistentNotebookId)
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));

        // Assert that the paragraph id is not contained within the saved file of the notebook
        final String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertFalse(fileContents.contains(newParagraphId));
    }

    // Deleting a specific paragraph from a specific notebook should result in the notebook being saved to disk without the specified paragraph.
    @Test
    public void httpDeleteParagraphTest() {
        final String requestBody = "";
        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest(
                                "http://" + serverAddress() + "/paragraph/" + notebookPath + "/" + firstParagraphId,
                                requestBody
                        )
                );
        // Assert that a DELETE request is responded to with the response code 204 NO CONTENT
        Assertions.assertEquals(HttpStatus.NO_CONTENT_204, response.status());
        // Assert that the created paragraph is not contained within the saved file of the notebook after deletion
        final String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertFalse(fileContents.contains(firstParagraphId));
        Assertions.assertFalse(fileContents.contains(firstParagraphText));
    }

    // Deleting a nonexistent paragraph should result in an error.
    @Test
    public void httpDeleteNonexistentParagraphTest() {
        final String nonexistentParagraphId = "I_DONT_EXIST";
        final String requestBody = Json.createObjectBuilder().build().toString();
        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest(
                                "http://" + serverAddress() + "/paragraph/" + notebookPath + "/"
                                        + nonexistentParagraphId,
                                requestBody
                        )
                );
        // Assert that a faulty DELETE request is responded to with the response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "Paragraph " + nonexistentParagraphId + " doesn't exist!")
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));

        // Assert that the created paragraph is not contained within the saved file of the notebook
        final String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertFalse(fileContents.contains(nonexistentParagraphId));
    }

    // Deleting a paragraph from a nonexistent notebook should result in an error.
    @Test
    public void httpDeleteParagraphFromNonexistentNotebookTest() {
        final String nonexistentNotebookId = "I_DONT_EXIST";
        final Path nonexistentNotebookPath = Paths.get(notebookDirectory().toString(), nonexistentNotebookId);
        Assertions.assertFalse(Files.exists(nonexistentNotebookPath));
        final String requestBody = Json.createObjectBuilder().build().toString();
        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest(
                                "http://" + serverAddress() + "/paragraph/" + nonexistentNotebookId + "/"
                                        + firstParagraphId,
                                requestBody
                        )
                );
        // Assert that a faulty DELETE request is responded to with the response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such file: " + nonexistentNotebookId)
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));

        // Assert that the created paragraph is not contained within the saved file of the notebook
        final String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertTrue(fileContents.contains(firstParagraphId));
    }

    // Updating a specific paragraph in a specific notebook should result in the notebook being saved to disk with the updated content
    @Test
    public void httpUpdateParagraphTest() {
        final String newParagraphTitle = "new_paragraph_title";
        final String newParagraphText = "%test\ntesting_this";
        final String requestBody = Json
                .createObjectBuilder()
                .add("title", newParagraphTitle)
                .add("text", newParagraphText)
                .build()
                .toString();
        final String expectedParagraphContent = Json
                .createObjectBuilder()
                .add("id", firstParagraphId)
                .add("title", newParagraphTitle)
                .add("script", Json.createObjectBuilder().add("text", newParagraphText))
                .build()
                .toString();
        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPOSTRequest(
                                "http://" + serverAddress() + "/paragraph/" + notebookPath + "/" + firstParagraphId,
                                requestBody
                        )
                );
        // Assert that a POST request is responded to with the response code 200 OK
        Assertions.assertEquals(HttpStatus.OK_200, response.status());
        // Assert that the body of the response contains a message mentioning the editing of the paragraph
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("id", firstParagraphId)
                .add("title", newParagraphTitle)
                .add("script", Json.createObjectBuilder().add("text", newParagraphText).build())
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));

        // Assert that the edited paragraph is contained within the saved file of the notebook
        final String fileContents = Assertions
                .assertDoesNotThrow(
                        () -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath.toString()))
                );
        Assertions.assertTrue(fileContents.contains(expectedParagraphContent));
    }

    // Copying a paragraph into the same notebook should result in a new paragraph with the same content as the source appearing in the notebook.
    @Test
    public void httpCopyParagraphTest() {

        final String newParagraphId = "new_paragraph";

        // Assert that the copied paragraph content is not contained within the saved file of the target notebook
        final String originalFileContents = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2())));
        Assertions.assertFalse(originalFileContents.contains(firstParagraphText));
        Assertions.assertFalse(originalFileContents.contains(newParagraphId));

        // Make an HTTP PUT request to /paragraph/{path/to/notebook/}/{paragraphId} to create the copy.
        final String putRequestBody = Json
                .createObjectBuilder()
                .add("sourcePath", notebookName)
                .add("sourceParagraphId", firstParagraphId)
                .build()
                .toString();
        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/paragraph/" + notebook2() + "/" + newParagraphId,
                                putRequestBody
                        )
                );
        // Assert that the PUT request is responded to with the response code 201 CREATED
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());

        // Assert that the copied paragraph is contained within the saved file of the target notebook
        final String fileContents = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2())));
        Assertions.assertTrue(fileContents.contains(firstParagraphText));
        Assertions.assertTrue(fileContents.contains(newParagraphId));
    }

    // Assert that a HTTP GET request to /paragraph/{/../../../path/to/server/file}/{paragraphId} endpoint results in an error
    @Test
    public void httpFindUnauthorizedParagraphTest() {
        // Write a secret file to target to which NBS_01 should not be able to touch
        final Path secretFile = Paths.get("target", "secretFile.txt");
        Assertions
                .assertDoesNotThrow(() -> Files.write(secretFile, "very_secret_information_pls_dont_leak".getBytes()));
        Assertions.assertTrue(Files.exists(secretFile));
        // Define a path that would get resolved to secretFile by NBS_01
        final Path relativePath = Paths.get("..", "secretFile.txt");

        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/paragraph/" + relativePath + "/" + firstParagraphId
                        )
                );
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
    }

    // Assert that a HTTP POST request to /paragraph/{/../../../path/to/server/file}/{paragraphId} endpoint results in an error
    @Test
    public void httpUpdateUnauthorizedParagraphTest() {
        // Write a secret file to target to which NBS_01 should not be able to touch
        final Path secretFile = Paths.get("target", "secretFile.txt");
        Assertions
                .assertDoesNotThrow(() -> Files.write(secretFile, "very_secret_information_pls_dont_leak".getBytes()));
        Assertions.assertTrue(Files.exists(secretFile));
        // Define a path that would get resolved to secretFile by NBS_01
        final Path relativePath = Paths.get("..", "secretFile.txt");

        final String newParagraphTitle = "new_paragraph_title";
        final String newParagraphText = "%test\ntesting_this";
        final String requestBody = Json
                .createObjectBuilder()
                .add("title", newParagraphTitle)
                .add("text", newParagraphText)
                .build()
                .toString();
        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPOSTRequest(
                                "http://" + serverAddress() + "/paragraph/" + relativePath + "/" + firstParagraphId,
                                requestBody
                        )
                );
        // Response code should indicate a user error
        Assertions.assertTrue(400 < response.status() && response.status() < 500);
        Assertions
                .assertEquals(
                        "very_secret_information_pls_dont_leak",
                        Assertions.assertDoesNotThrow(() -> Files.readString(secretFile))
                );
    }

    // Assert that a HTTP DELETE request to /paragraph/{/../../../path/to/server/file}/{paragraphId} endpoint results in an error
    @Test
    public void httpDELETEUnauthorizedParagraphTest() {
        // Write a secret file to target to which NBS_01 should not be able to touch
        final Path secretFile = Paths.get("target", "secretFile.txt");
        Assertions
                .assertDoesNotThrow(() -> Files.write(secretFile, "very_secret_information_pls_dont_leak".getBytes()));
        Assertions.assertTrue(Files.exists(secretFile));
        // Define a path that would get resolved to secretFile by NBS_01
        final Path relativePath = Paths.get("..", "secretFile.txt");

        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest(
                                "http://" + serverAddress() + "/paragraph/" + relativePath + "/" + firstParagraphId, ""
                        )
                );
        // Response code should indicate a user error
        Assertions.assertTrue(400 < response.status() && response.status() < 500);
        Assertions.assertTrue(Files.exists(secretFile));
    }

    // Assert that a HTTP PUT request to /paragraph/{/../../../path/to/server/file}/{paragraphId} endpoint results in an error
    @Test
    public void httpCreateUnauthorizedParagraphTest() {
        // Define a path which NBS_01 should not be able to touch
        final Path secretFile = Paths.get("target", "secretFile.txt");
        if (Files.exists(secretFile)) {
            Assertions.assertDoesNotThrow(() -> Files.delete(secretFile));
        }
        Assertions.assertFalse(Files.exists(secretFile));
        // Define a path that would get resolved to secretFile by NBS_01
        final Path relativePath = Paths.get("..", "secretFile.txt");

        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/paragraph/" + relativePath + "/" + firstParagraphId, ""
                        )
                );
        // Response code should indicate a user error
        Assertions.assertTrue(400 < response.status() && response.status() < 500);
        Assertions.assertFalse(Files.exists(secretFile));
    }

    // Assert that a HTTP PUT request to /paragraph/{/../../../path/to/server/file}/{paragraphId} endpoint results in an error
    @Test
    public void httpCopyUnauthorizedParagraphTest() {
        // Define a path which NBS_01 should not be able to touch
        final Path secretFile = Paths.get("target", "secretFile.txt");
        if (Files.exists(secretFile)) {
            Assertions.assertDoesNotThrow(() -> Files.delete(secretFile));
        }
        Assertions.assertFalse(Files.exists(secretFile));
        // Define a path that would get resolved to secretFile by NBS_01
        final Path relativePath = Paths.get("..", "secretFile.txt");

        final String putRequestBody = Json
                .createObjectBuilder()
                .add("sourcePath", "my_note4_2A94M5J4Z.zpln")
                .add("sourceParagraphId", "20150326-214658_12335843")
                .build()
                .toString();

        final HTTPResponse response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/paragraph/" + relativePath + "/" + firstParagraphId,
                                putRequestBody
                        )
                );
        // Response code should indicate a user error
        Assertions.assertTrue(400 < response.status() && response.status() < 500);
        Assertions.assertFalse(Files.exists(secretFile));
    }

}
