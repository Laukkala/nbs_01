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
import com.teragrep.nbs_01.http.responses.Response;
import com.teragrep.nbs_01.servlets.FileSystemServlet;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotebookServletTest extends AbstractNotebookServerTest {

    public NotebookServletTest() {
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/notebook} endpoint results in a new file being saved on disk.
    public void httpCreateNotebookTest() {

        Path newNotebookPath = Paths.get("testFileName_12345.zpln");
        String newNotebookTitle = "newTitle";
        String requestBody = Json.createObjectBuilder().add("title", newNotebookTitle).build().toString();
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(newNotebookPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/notebook/" + newNotebookPath, requestBody
                        )
                );
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());

        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("name", newNotebookTitle)
                .add("config", Json.createObjectBuilder().build())
                .add("paragraphs", Json.createArrayBuilder())
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(newNotebookPath)));
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/existing/notebook} endpoint results in a file being overwritten.
    public void httpCreateNotebookIntoExistingPathTest() {

        Path newNotebookPath = notebook1();
        String newNotebookTitle = "newTitle";
        String requestBody = Json.createObjectBuilder().add("title", newNotebookTitle).build().toString();
        String existingFileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(newNotebookPath)));
        // Assert that the file we are creating already exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(newNotebookPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/notebook/" + newNotebookPath, requestBody
                        )
                );
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        // Assert that the original file still exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(newNotebookPath)));
        String overwrittenFileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(newNotebookPath)));
        // Assert that the contents of the file were overwritten
        Assertions.assertNotEquals(existingFileContent, overwrittenFileContent);
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/notebook} endpoint results in a new file being saved on disk, even if no title parameter is provided.
    public void httpCreateNotebookWithNoTitleTest() {

        Path newNotebookPath = Paths.get("testFileName_12345.zpln");
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(newNotebookPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest("http://" + serverAddress() + "/notebook/" + newNotebookPath, "")
                );
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("name", "")
                .add("config", Json.createObjectBuilder().build())
                .add("paragraphs", Json.createArrayBuilder())
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(newNotebookPath)));
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/notebook} endpoint results in a copied file being saved on disk.
    public void httpCopyNotebookTest() {

        Path newNotebookPath = Paths.get("testFileName_12345.zpln");
        Path sourceNotebookPath = notebook4();

        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(newNotebookPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/notebook/" + newNotebookPath,
                                "{\"sourcePath\":\"" + sourceNotebookPath + "\",\"title\":\"copyNotebook\"}"
                        )
                );
        // Assert that the response contains all paragraphs thet were present in the source notebook
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions
                                .assertTrue(
                                        response
                                                .body()
                                                .asString()
                                                .contains(
                                                        "\"script\":{\"text\":\"%test\\n## Congratulations, it's done.\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\"}"
                                                )
                                )
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions
                                .assertTrue(
                                        response
                                                .body()
                                                .asString()
                                                .contains(
                                                        "\"script\":{\"text\":\"%test import org.apache.commons.io.IOUtils\\nimport java.net.URL\\nimport java.nio.charset.Charset\\n\\n// Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)\\n// So you don't need create them manually\\n\\n// load bank data\\nval bankText = sc.parallelize(\\n    IOUtils.toString(\\n        new URL(\\\"https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv\\\"),\\n        Charset.forName(\\\"utf8\\\")).split(\\\"\\\\n\\\"))\\n\\ncase class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)\\n\\nval bank = bankText.map(s => s.split(\\\";\\\")).filter(s => s(0) != \\\"\\\\\\\"age\\\\\\\"\\\").map(\\n    s => Bank(s(0).toInt, \\n            s(1).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(2).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(3).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(5).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\").toInt\\n        )\\n).toDF()\\nbank.registerTempTable(\\\"bank\\\")\"}"
                                                )
                                )
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions
                                .assertTrue(
                                        response
                                                .body()
                                                .asString()
                                                .contains(
                                                        "\"script\":{\"text\":\"%test \\nselect age, count(1) value\\nfrom bank \\nwhere age < 30 \\ngroup by age \\norder by age\"}"
                                                )
                                )
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions
                                .assertTrue(
                                        response
                                                .body()
                                                .asString()
                                                .contains(
                                                        "\"script\":{\"text\":\"%test \\nselect age, count(1) value \\nfrom bank \\nwhere marital=\\\"${marital=single,single|divorced|married}\\\" \\ngroup by age \\norder by age\"}"
                                                )
                                )
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions
                                .assertTrue(
                                        response
                                                .body()
                                                .asString()
                                                .contains(
                                                        "\"script\":{\"text\":\"%test\\n## Congratulations, it's done.\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\"}"
                                                )
                                )
                );
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions
                                .assertTrue(
                                        response
                                                .body()
                                                .asString()
                                                .contains(
                                                        "\"script\":{\"text\":\"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\"}"
                                                )
                                )
                );
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(newNotebookPath)));
        // Assert that the original file also exists
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(sourceNotebookPath)));
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/directory} endpoint results in an error
    public void httpCopyNotebookWithDirectoryPathTest() {

        Path newNotebookPath = directory1();
        Path sourceNotebookPath = notebook4();

        // Assert that the file we are creating already exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(newNotebookPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/notebook/" + newNotebookPath,
                                "{\"sourcePath\":\"" + sourceNotebookPath + "\",\"title\":\"copyNotebook\"}"
                        )
                );
        // Assert that we receive the proper response.
        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "File at path: " + newNotebookPath + " is a Directory!")
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        // Assert that the original file still exists
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(sourceNotebookPath)));
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/existing/notebook} endpoint results in file being overwritten
    public void httpCopyNotebookIntoExistingPathTest() {

        Path newNotebookPath = notebook1();
        Path sourceNotebookPath = notebook4();

        // Assert that the file we are creating already exists.
        String existingFileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook1())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(newNotebookPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPUTRequest(
                                "http://" + serverAddress() + "/notebook/" + newNotebookPath,
                                "{\"sourcePath\":\"" + sourceNotebookPath + "\",\"title\":\"copyNotebook\"}"
                        )
                );

        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        // Assert that the original and copied file both exist
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(sourceNotebookPath)));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(newNotebookPath)));
        String overwrittenFileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(newNotebookPath)));
        // Assert that the contents of the file were overwritten
        Assertions.assertNotEquals(existingFileContent, overwrittenFileContent);
    }

    @Test
    // Assert that a HTTP DELETE request to /notebook/{path/to/notebook} endpoint results in a notebook being deleted
    public void httpDeleteNotebookTest() {

        Path notebookPath = notebook2();
        Path directoryPath = directory1();
        // Assert that the correct number of files exist
        Assertions
                .assertEquals(
                        2, Assertions.assertDoesNotThrow(() -> Files.list(notebookDirectory().resolve(directoryPath)).collect(Collectors.toList()).size())
                );
        // Assert that the file to be deleted exists.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebookPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest("http://" + serverAddress() + "/notebook/" + notebookPath, "")
                );
        Assertions.assertEquals(204, response.status());
        // Assert that a file was deleted.
        Assertions
                .assertEquals(
                        1, Assertions.assertDoesNotThrow(() -> Files.list(notebookDirectory().resolve(directoryPath)).collect(Collectors.toList()).size())
                );
        // Assert that the correct file was deleted.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(notebookPath)));
    }

    @Test
    // Assert that a HTTP GET request to /notebook/{path/to/directory} endpoint with a path corresponding to a directory results in a response with the expected contents
    public void httpDeleteNotebookWithDirectoryPathTest() {
        Path directoryPath = directory1();
        // Assert that the path we are looking for exists, even though it's not a directory.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest("http://" + serverAddress() + "/notebook/" + directoryPath, "{}")
                );
        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", directoryPath + " is not a Notebook!")
                .build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
    }

    @Test
    // Assert that a HTTP DELETE request to /notebook/{path/to/nonexistant/notebook} endpoint results in an error
    public void httpDeleteNonexistantNotebookTest() {

        Path notebookPath = Paths.get("I_DONT_EXIST");
        // Assert that the correct number of files exist
        Assertions
                .assertEquals(4, Assertions.assertDoesNotThrow(() -> Files.list(notebookDirectory()).collect(Collectors.toList()).size()));
        // Assert that the file to be deleted doesn't exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(notebookPath)));
        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpDELETERequest("http://" + serverAddress() + "/notebook/" + notebookPath, "{}")
                );
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        // Assert that no file was deleted.
        Assertions
                .assertEquals(4, Assertions.assertDoesNotThrow(() -> Files.list(notebookDirectory()).collect(Collectors.toList()).size()));
    }

    @Test
    // Assert that a HTTP GET request to /notebook/{path/to/notebook} endpoint results in a response with the expected file contents
    public void httpFindNotebookTest() {
        String expectedFileContent = "{\"name\":\"my_note2\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"%test\\n## Congratulations, it's done.\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";
        // Assert that the file exists.
        Path notebookPath = notebook2();
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebookPath)));
        Response response = Assertions
                .assertDoesNotThrow(() -> makeHttpGETRequest("http://" + serverAddress() + "/notebook/" + notebookPath));
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertEquals(expectedFileContent, response.body().asString().strip().toString())
                );
    }

    @Test
    // Assert that a HTTP GET request to /notebook/{path/to/directory} endpoint with a path corresponding to a directory results in a response with the expected contents
    public void httpFindNotebookWithDirectoryPathTest() {
        Path directoryPath = directory1();
        // Assert that the path we are looking for exists, even though it's not a notebook.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directoryPath)));
        Response response = Assertions
                .assertDoesNotThrow(() -> makeHttpGETRequest("http://" + serverAddress() + "/notebook/" + directoryPath));
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        JsonObject expectedJson = Json.createObjectBuilder().add("message", "No such file: " + directoryPath).build();
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    // Assert that a HTTP GET request to /notebook/{path/to/nonexistant/notebook} endpoint results in an error
    public void httpFindNonexistantNotebookTest() {
        Path notebookPath = Paths.get("I_DONT_EXIST");
        // Assert that the file does not exist.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(notebookPath)));
        Response response = Assertions
                .assertDoesNotThrow(() -> makeHttpGETRequest("http://" + serverAddress() + "/notebook/" + notebookPath));
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
    }

    @Test
    // Assert that a HTTP POST request to /notebook/{path/to/notebook} endpoint results in an updated file containing the modifications contained in the request body.
    public void httpUpdateNotebookTest() {
        // Assert that the file content is the same as in the resource files before edits.
        String title = "editedTitle";
        String originalFileContent = "{  \"paragraphs\": [    {      \"text\": \"%test\\n## Congratulations, it\\u0027s done.\\n##### You can create your own notebook in \\u0027Notebook\\u0027 menu. Good luck!\",      \"config\": {        \"colWidth\": 12.0,        \"graph\": {          \"mode\": \"table\",          \"height\": 300.0,          \"optionOpen\": false,          \"keys\": [],          \"values\": [],          \"groups\": [],          \"scatter\": {}        },        \"editorHide\": true      },      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1423836268492_216498320\",      \"id\": \"20150213-230428_1231780373\",      \"results\": {        \"code\": \"SUCCESS\",        \"msg\": [          {            \"type\": \"HTML\",            \"data\": \"\\u003ch2\\u003eCongratulations, it\\u0027s done.\\u003c/h2\\u003e\\n\\u003ch5\\u003eYou can create your own notebook in \\u0027Notebook\\u0027 menu. Good luck!\\u003c/h5\\u003e\\n\"          }        ]      },      \"dateCreated\": \"Feb 13, 2015 11:04:28 PM\",      \"dateStarted\": \"Apr 1, 2015 9:12:18 PM\",      \"dateFinished\": \"Apr 1, 2015 9:12:18 PM\",      \"status\": \"FINISHED\",      \"progressUpdateIntervalMs\": 500    },    {      \"text\": \"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM\\u00272011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\",      \"config\": {        \"colWidth\": 12.0,        \"graph\": {          \"mode\": \"table\",          \"height\": 300.0,          \"optionOpen\": false,          \"keys\": [],          \"values\": [],          \"groups\": [],          \"scatter\": {}        },        \"editorHide\": true      },      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1427420818407_872443482\",      \"id\": \"20150326-214658_12335843\",      \"results\": {        \"code\": \"SUCCESS\",        \"msg\": [          {            \"type\": \"HTML\",            \"data\": \"\\u003cp\\u003eAbout bank data\\u003c/p\\u003e\\n\\u003cpre\\u003e\\u003ccode\\u003eCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM\\u00272011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n\\u003c/code\\u003e\\u003c/pre\\u003e\\n\"          }        ]      },      \"dateCreated\": \"Mar 26, 2015 9:46:58 PM\",      \"dateStarted\": \"Jul 3, 2015 1:44:56 PM\",      \"dateFinished\": \"Jul 3, 2015 1:44:56 PM\",      \"status\": \"FINISHED\",      \"progressUpdateIntervalMs\": 500    },    {      \"config\": {},      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1435955447812_-158639899\",      \"id\": \"20150703-133047_853701097\",      \"dateCreated\": \"Jul 3, 2015 1:30:47 PM\",      \"status\": \"READY\",      \"progressUpdateIntervalMs\": 500    }  ],  \"id\": \"2A94M5J2Z\",  \"name\": \"my_note2\",  \"angularObjects\": {},  \"config\": {    \"looknfeel\": \"default\"  },  \"info\": {}}";
        String expectedFileContent = "{\"name\":\"" + title
                + "\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"%test\\n## Congratulations, it's done.\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";

        Path notebookPath = notebook2();
        Assertions
                .assertEquals(
                        originalFileContent,
                        Assertions.assertDoesNotThrow(() -> Files.readAllLines(notebookDirectory().resolve(notebookPath)).stream().collect(Collectors.joining()))
                );

        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPOSTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath,
                                "{\"title\":\"" + title + "\"}"
                        )
                );
        // Assert that we got the proper response.
        Assertions.assertDoesNotThrow(() -> Assertions.assertTrue(response.body().asString().contains(title)));
        // Assert that the file content has the edited paragraph saved to file in the correct place.
        Assertions
                .assertEquals(
                        expectedFileContent,
                        Assertions.assertDoesNotThrow(() -> Files.readAllLines(notebookDirectory().resolve(notebookPath)).stream().collect(Collectors.joining()))
                );
    }

    @Test
    // Assert that a HTTP POST request to /notebook/{path/to/directory} endpoint results in an error
    public void httpUpdateNotebookWithDirectoryPathTest() {
        // Assert that the file content is the same as in the resource files before edits.
        String title = "editedTitle";

        Path notebookPath = directory1();

        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPOSTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath,
                                "{\"title\":\"" + title + "\"}"
                        )
                );
        // Assert that we got the proper response.
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
    }

    @Test
    // Assert that a HTTP POST request to /notebook/{path/to/nonexistent/notebook} endpoint results in an error
    public void httpUpdateNonexistentNotebookTest() {
        // Assert that the file content is the same as in the resource files before edits.
        String title = "editedTitle";

        Path notebookPath = Paths.get("I_DONT_EXIST");

        Response response = Assertions
                .assertDoesNotThrow(
                        () -> makeHttpPOSTRequest(
                                "http://" + serverAddress() + "/notebook/" + notebookPath,
                                "{\"title\":\"" + title + "\"}"
                        )
                );
        // Assert that we got the proper response.
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
    }

    @Test
    public void testContract() {
        EqualsVerifier
                .forClass(FileSystemServlet.class)
                // legacyHeadHandling is a boolean within jakarta.servlet.http.HttpServlet, which FileSystemServlet extends.
                // EqualsVerifier complains that it is not included in FileSystemServlets equals() method, but FileSystemServlet cannot access the boolen, so it is ignored
                .withIgnoredFields("legacyHeadHandling")
                .verify();
    }

}
