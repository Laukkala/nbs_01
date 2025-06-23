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

    private final String notebookName = "/my_note4_2A94M5J4Z.zpln";
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
        String expectedparagraphContent = "{\"id\":\"" + firstParagraphId
                + "\",\"title\":\""+firstParagraphTitle+"\",\"script\":{\"text\":\""+firstParagraphText+"\"}}";

        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/notebook" + notebookPath + "/paragraph/" + firstParagraphId
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
        String notebookId = "2A94M5J1Z";
        String paragraphId = "I_DONT_EXIST";
        String notebookPath = Paths
                .get("my_folder_2A94M5J1D", "my_second_folder_2A94M5J2D", "my_note1_" + notebookId + ".zpln")
                .toString();
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/" + paragraphId
                        )
                );
        // As the user made a request with bad data, the server should respond with a response code 400 BAD REQUEST
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        Assertions.assertTrue(response.body().getString("message").strip().toString().contains("Paragraph not found"));
    }

    // Searching for a paragraph from a notebook that doesn't exist should result in an error.
    @Test
    public void httpFindParagraphFromNonexistentNotebookTest() {
        String notebookId = "I_DONT_EXIST";
        String paragraphId = "20150210-015259_1403135953";
        String notebookPath = Paths
                .get("my_folder_2A94M5J1D", "my_second_folder_2A94M5J2D", notebookId + ".zpln")
                .toString();
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/" + paragraphId
                        )
                );
        // As the user made a request with bad data, the server should respond with a response code 400 BAD REQUEST
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        Assertions.assertTrue(response.body().getString("message").strip().toString().contains("Notebook not found"));
    }

    // Creating a paragraph should result in an existing notebook being saved to disk containing an additional paragraph.
    @Test
    public void httpCreateParagraphTest() {
        String notebookId = "2A94M5J1Z";
        String paragraphId = "1234_new_paragraph";
        String notebookPath = Paths
                .get("my_folder_2A94M5J1D", "my_second_folder_2A94M5J2D", "my_note1_" + notebookId + ".zpln")
                .toString();
        String expectedFileContent = "{\"id\":\"2A94M5J1Z\",\"name\":\"my_note1\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-231621_168813393\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Welcome to Zeppelin.\\\\n##### This is a live tutorial, you can run the code yourself. (Shift-Enter to Run)\\\"\"}},{\"id\":\"20150210-015259_1403135953\",\"title\":\"Load data into table\",\"script\":{\"text\":\"\\\"%test import org.apache.commons.io.IOUtils\\\\nimport java.net.URL\\\\nimport java.nio.charset.Charset\\\\n\\\\n// Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)\\\\n// So you don't need create them manually\\\\n\\\\n// load bank data\\\\nval bankText = sc.parallelize(\\\\n    IOUtils.toString(\\\\n        new URL(\\\\\\\"https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv\\\\\\\"),\\\\n        Charset.forName(\\\\\\\"utf8\\\\\\\")).split(\\\\\\\"\\\\\\\\n\\\\\\\"))\\\\n\\\\ncase class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)\\\\n\\\\nval bank = bankText.map(s => s.split(\\\\\\\";\\\\\\\")).filter(s => s(0) != \\\\\\\"\\\\\\\\\\\\\\\"age\\\\\\\\\\\\\\\"\\\\\\\").map(\\\\n    s => Bank(s(0).toInt, \\\\n            s(1).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(2).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(3).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(5).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\").toInt\\\\n        )\\\\n).toDF()\\\\nbank.registerTempTable(\\\\\\\"bank\\\\\\\")\\\"\"}},{\"id\":\"20150210-015302_1492795503\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value\\\\nfrom bank \\\\nwhere age < 30 \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150212-145404_867439529\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value \\\\nfrom bank \\\\nwhere age < ${maxAge=30} \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150213-230422_1600658137\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value \\\\nfrom bank \\\\nwhere marital=\\\\\\\"${marital=single,single|divorced|married}\\\\\\\" \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Congratulations, it's done.\\\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\\\"\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n\\\\nAbout bank data\\\\n\\\\n```\\\\nCitation Request:\\\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\\\n  Please include this citation if you plan to use this database:\\\\n\\\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\\\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\\\n\\\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\\\n```\\\"\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}},{\"id\":\""
                + paragraphId + "\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";
        // Make an HTTP PUT request to /notebook/{path/to/notebook/}/paragraph/{paragraphId}
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/" + paragraphId,
                                "{}"
                        )
                );
        // A PUT request should be responded to with the response code 201 CREATED
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Assertions.assertEquals("Created new paragraph " + paragraphId, response.body().getString("message"));

        // Assert that the new paragraph exists in the correct notebook's saved file
        String fileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath)));
        Assertions.assertEquals(expectedFileContent, fileContent);
    }

    // Trying to create a paragraph in a notebook that doesn't exist should result in an error.
    @Test
    public void httpCreateParagraphInNonexistentNotebookTest() {
        String notebookId = "I_DONT_EXIST";
        String paragraphId = "1234_new_paragraph";
        String notebookPath = Paths
                .get("my_folder_2A94M5J1D", "my_second_folder_2A94M5J2D", "my_note1_" + notebookId + ".zpln")
                .toString();
        // Make an HTTP PUT request to /notebook/{path/to/notebook/}/paragraph/{paragraphId}
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/" + paragraphId,
                                "{}"
                        )
                );
        // A PUT request should be responded to with the response code 201 CREATED
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions.assertEquals("Notebook doesn't exist!", response.body().getString("message"));
    }

    // Deleting a specific paragraph from a specific should result in the notebook being saved to disk without the specified paragraph.
    @Test
    public void httpDeleteParagraphTest() {
        String notebookId = "2A94M5J1Z";
        String paragraphId = "20150213-231621_168813393";
        String notebookPath = Paths
                .get("my_folder_2A94M5J1D", "my_second_folder_2A94M5J2D", "my_note1_" + notebookId + ".zpln")
                .toString();
        String expectedFileContent = "{\"id\":\"2A94M5J1Z\",\"name\":\"my_note1\",\"config\":{},\"paragraphs\":[{\"id\":\"20150210-015259_1403135953\",\"title\":\"Load data into table\",\"script\":{\"text\":\"\\\"%test import org.apache.commons.io.IOUtils\\\\nimport java.net.URL\\\\nimport java.nio.charset.Charset\\\\n\\\\n// Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)\\\\n// So you don't need create them manually\\\\n\\\\n// load bank data\\\\nval bankText = sc.parallelize(\\\\n    IOUtils.toString(\\\\n        new URL(\\\\\\\"https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv\\\\\\\"),\\\\n        Charset.forName(\\\\\\\"utf8\\\\\\\")).split(\\\\\\\"\\\\\\\\n\\\\\\\"))\\\\n\\\\ncase class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)\\\\n\\\\nval bank = bankText.map(s => s.split(\\\\\\\";\\\\\\\")).filter(s => s(0) != \\\\\\\"\\\\\\\\\\\\\\\"age\\\\\\\\\\\\\\\"\\\\\\\").map(\\\\n    s => Bank(s(0).toInt, \\\\n            s(1).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(2).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(3).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(5).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\").toInt\\\\n        )\\\\n).toDF()\\\\nbank.registerTempTable(\\\\\\\"bank\\\\\\\")\\\"\"}},{\"id\":\"20150210-015302_1492795503\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value\\\\nfrom bank \\\\nwhere age < 30 \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150212-145404_867439529\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value \\\\nfrom bank \\\\nwhere age < ${maxAge=30} \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150213-230422_1600658137\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value \\\\nfrom bank \\\\nwhere marital=\\\\\\\"${marital=single,single|divorced|married}\\\\\\\" \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Congratulations, it's done.\\\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\\\"\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n\\\\nAbout bank data\\\\n\\\\n```\\\\nCitation Request:\\\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\\\n  Please include this citation if you plan to use this database:\\\\n\\\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\\\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\\\n\\\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\\\n```\\\"\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";
        // Make an HTTP DELETE request to /notebook/{path/to/notebook/}/paragraph/{paragraphId}
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/" + paragraphId,
                                "{}"
                        )
                );
        // A DELETE Request should be responded to with the response code 204 NO CONTENT
        Assertions.assertEquals(HttpStatus.NO_CONTENT_204, response.status());

        // Assert that the deleted paragraph no longer exists in the correct notebook's saved file
        String fileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath)));
        Assertions.assertEquals(expectedFileContent, fileContent);
    }

    // Deleting a nonexistent paragraph should result in an error.
    @Test
    public void httpDeleteNonexistentParagraphTest() {
        String notebookId = "2A94M5J1Z";
        String paragraphId = "I_DONT_EXIST";
        String notebookPath = Paths
                .get("my_folder_2A94M5J1D", "my_second_folder_2A94M5J2D", "my_note1_" + notebookId + ".zpln")
                .toString();
        // Make an HTTP DELETE request to /notebook/{path/to/notebook/}/paragraph/{paragraphId}
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/" + paragraphId,
                                "{}"
                        )
                );
        // As the user is requesting a resource that does not exist, the server should respond with a response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
    }

    // Deleting a paragraph from a nonexistent notebook should result in an error.
    @Test
    public void httpDeleteParagraphFromNonexistentNotebookTest() {
        String notebookId = "I_DONT_EXIST";
        String paragraphId = "20150213-231621_168813393";
        String notebookPath = Paths
                .get("my_folder_2A94M5J1D", "my_second_folder_2A94M5J2D", "my_note1_" + notebookId + ".zpln")
                .toString();
        // Make an HTTP DELETE request to /notebook/{path/to/notebook/}/paragraph/{paragraphId}
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/" + paragraphId,
                                "{}"
                        )
                );
        // As the user is requesting a resource that does not exist, the server should respond with a response code 404 NOT FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());

    }

    // Updating a specific paragraph in a specific notebook should result in the notebook being saved to disk with the updated content
    @Test
    public void httpUpdateParagraphTest() {
        String notebookId = "2A94M5J1Z";
        String paragraphId = "20150213-231621_168813393";
        String editedText = "Hello, I am testing stuff";
        String expectedFileContent = "{\"id\":\"2A94M5J1Z\",\"name\":\"\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230422_1600658137\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value \\\\nfrom bank \\\\nwhere marital=\\\\\\\"${marital=single,single|divorced|married}\\\\\\\" \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Congratulations, it's done.\\\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\\\"\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n\\\\nAbout bank data\\\\n\\\\n```\\\\nCitation Request:\\\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\\\n  Please include this citation if you plan to use this database:\\\\n\\\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\\\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\\\n\\\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\\\n```\\\"\"}},{\"id\":\"20150210-015302_1492795503\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value\\\\nfrom bank \\\\nwhere age < 30 \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150210-015259_1403135953\",\"title\":\"Load data into table\",\"script\":{\"text\":\"\\\"%test import org.apache.commons.io.IOUtils\\\\nimport java.net.URL\\\\nimport java.nio.charset.Charset\\\\n\\\\n// Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)\\\\n// So you don't need create them manually\\\\n\\\\n// load bank data\\\\nval bankText = sc.parallelize(\\\\n    IOUtils.toString(\\\\n        new URL(\\\\\\\"https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv\\\\\\\"),\\\\n        Charset.forName(\\\\\\\"utf8\\\\\\\")).split(\\\\\\\"\\\\\\\\n\\\\\\\"))\\\\n\\\\ncase class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)\\\\n\\\\nval bank = bankText.map(s => s.split(\\\\\\\";\\\\\\\")).filter(s => s(0) != \\\\\\\"\\\\\\\\\\\\\\\"age\\\\\\\\\\\\\\\"\\\\\\\").map(\\\\n    s => Bank(s(0).toInt, \\\\n            s(1).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(2).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(3).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(5).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\").toInt\\\\n        )\\\\n).toDF()\\\\nbank.registerTempTable(\\\\\\\"bank\\\\\\\")\\\"\"}},{\"id\":\""
                + paragraphId + "\",\"title\":\"\",\"script\":{\"text\":\"" + editedText
                + "\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}},{\"id\":\"20150212-145404_867439529\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value \\\\nfrom bank \\\\nwhere age < ${maxAge=30} \\\\ngroup by age \\\\norder by age\\\"\"}}]}";
        String notebookPath = Paths
                .get("my_folder_2A94M5J1D", "my_second_folder_2A94M5J2D", "my_note1_" + notebookId + ".zpln")
                .toString();
        // Make an HTTP POST request to /notebook/{path/to/notebook/}/paragraph/{paragraphId}
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPOSTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/" + paragraphId,
                                "{\"text\":\"" + editedText + "\"}"
                        )
                );
        Assertions.assertEquals(HttpStatus.OK_200, response.status());

        // Assert that the modified paragraph exists in the correct notebook's saved file
        String fileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath)));
        Assertions.assertEquals(expectedFileContent, fileContent);
    }

    // Copying is not an atomic operation. You must first create a new paragraph, then update it with the output of the source paragraph.
    @Test
    public void httpCopyParagraphTest() {
        String notebookId = "2A94M5J1Z";
        String sourceParagraphId = "20150213-231621_168813393";
        String copyParagraphId = "copyParagraphId";
        String notebookPath = Paths
                .get("my_folder_2A94M5J1D", "my_second_folder_2A94M5J2D", "my_note1_" + notebookId + ".zpln")
                .toString();

        // Make an HTTP GET request to /notebook/{path/to/notebook/}/paragraph/{paragraphId}
        Response getResponse = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpGETRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + sourceParagraphId
                        )
                );
        Assertions.assertEquals(HttpStatus.OK_200, getResponse.status());
        JsonObject paragraph = Json
                .createReader(new StringReader(getResponse.body().getString("message")))
                .readObject();
        String text = paragraph.getJsonObject("script").getString("text");

        // Make an HTTP PUT request to /notebook/{path/to/notebook/}/paragraph/{paragraphId}
        Response putResponse = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + copyParagraphId,
                                "{}"
                        )
                );
        Assertions.assertEquals(HttpStatus.CREATED_201, putResponse.status());

        // Make an HTTP POST request to /notebook/{path/to/notebook/}/paragraph/{paragraphId}
        Response postResponse = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPOSTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath + "/paragraph/"
                                        + copyParagraphId,
                                "{\"text\":" + text + "}"
                        )
                );
        Assertions.assertEquals(HttpStatus.OK_200, postResponse.status());

        // Assert that the copied paragraph exists in the correct notebook's saved file
        String expectedFileContent = "{\"id\":\"2A94M5J1Z\",\"name\":\"\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230422_1600658137\",\"title\":\"\",\"script\":{\"text\":\"{\\\"text\\\":\\\"\\\\\\\"%test \\\\\\\\nselect age, count(1) value \\\\\\\\nfrom bank \\\\\\\\nwhere marital=\\\\\\\\\\\\\\\"${marital=single,single|divorced|married}\\\\\\\\\\\\\\\" \\\\\\\\ngroup by age \\\\\\\\norder by age\\\\\\\"\\\"}\"}},{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"{\\\"text\\\":\\\"\\\\\\\"%test\\\\\\\\n## Congratulations, it's done.\\\\\\\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\\\\\\\"\\\"}\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"{\\\"text\\\":\\\"\\\\\\\"%test\\\\\\\\n\\\\\\\\nAbout bank data\\\\\\\\n\\\\\\\\n```\\\\\\\\nCitation Request:\\\\\\\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\\\\\\\n  Please include this citation if you plan to use this database:\\\\\\\\n\\\\\\\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\\\\\\\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\\\\\\\n\\\\\\\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\\\\\\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\\\\\\\n```\\\\\\\"\\\"}\"}},{\"id\":\"20150210-015302_1492795503\",\"title\":\"\",\"script\":{\"text\":\"{\\\"text\\\":\\\"\\\\\\\"%test \\\\\\\\nselect age, count(1) value\\\\\\\\nfrom bank \\\\\\\\nwhere age < 30 \\\\\\\\ngroup by age \\\\\\\\norder by age\\\\\\\"\\\"}\"}},{\"id\":\"20150210-015259_1403135953\",\"title\":\"Load data into table\",\"script\":{\"text\":\"{\\\"text\\\":\\\"\\\\\\\"%test import org.apache.commons.io.IOUtils\\\\\\\\nimport java.net.URL\\\\\\\\nimport java.nio.charset.Charset\\\\\\\\n\\\\\\\\n// Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)\\\\\\\\n// So you don't need create them manually\\\\\\\\n\\\\\\\\n// load bank data\\\\\\\\nval bankText = sc.parallelize(\\\\\\\\n    IOUtils.toString(\\\\\\\\n        new URL(\\\\\\\\\\\\\\\"https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv\\\\\\\\\\\\\\\"),\\\\\\\\n        Charset.forName(\\\\\\\\\\\\\\\"utf8\\\\\\\\\\\\\\\")).split(\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\\n\\\\\\\\\\\\\\\"))\\\\\\\\n\\\\\\\\ncase class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)\\\\\\\\n\\\\\\\\nval bank = bankText.map(s => s.split(\\\\\\\\\\\\\\\";\\\\\\\\\\\\\\\")).filter(s => s(0) != \\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"age\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\").map(\\\\\\\\n    s => Bank(s(0).toInt, \\\\\\\\n            s(1).replaceAll(\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\"),\\\\\\\\n            s(2).replaceAll(\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\"),\\\\\\\\n            s(3).replaceAll(\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\"),\\\\\\\\n            s(5).replaceAll(\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\").toInt\\\\\\\\n        )\\\\\\\\n).toDF()\\\\\\\\nbank.registerTempTable(\\\\\\\\\\\\\\\"bank\\\\\\\\\\\\\\\")\\\\\\\"\\\"}\"}},{\"id\":\"20150213-231621_168813393\",\"title\":\"\",\"script\":{\"text\":\"{\\\"text\\\":\\\"\\\\\\\"%test\\\\\\\\n## Welcome to Zeppelin.\\\\\\\\n##### This is a live tutorial, you can run the code yourself. (Shift-Enter to Run)\\\\\\\"\\\"}\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"{\\\"text\\\":\\\"\\\"}\"}},{\"id\":\"20150212-145404_867439529\",\"title\":\"\",\"script\":{\"text\":\"{\\\"text\\\":\\\"\\\\\\\"%test \\\\\\\\nselect age, count(1) value \\\\\\\\nfrom bank \\\\\\\\nwhere age < ${maxAge=30} \\\\\\\\ngroup by age \\\\\\\\norder by age\\\\\\\"\\\"}\"}},{\"id\":\"copyParagraphId\",\"title\":\"\",\"script\":{\"text\":\"%test\\n## Welcome to Zeppelin.\\n##### This is a live tutorial, you can run the code yourself. (Shift-Enter to Run)\"}}]}";
        String fileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(Paths.get(notebookDirectory().toString(), notebookPath)));
        Assertions.assertEquals(expectedFileContent, fileContent);
    }

}
