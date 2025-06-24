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
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotebookServletTest extends AbstractNotebookServerTest {

    private final String notebookName = "my_note2_2A94M5J2Z.zpln";
    private final Path notebookPath = Paths.get(notebookName);
    private final String directoryName = "my_second_folder_2A94M5J2D";
    private final Path directoryPath = Paths.get(notebookName);



    public NotebookServletTest() {
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
    public void httpCreateNotebookTest() {

        String newNotebookName = "testFileName_12345.zpln";
        Path newNotebookPath = Paths.get(newNotebookName);
        String newNotebookTitle = "newTitle";
        String requestBody = Json.createObjectBuilder().add("title",newNotebookTitle).build().toString();
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
        Response response = Assertions.assertDoesNotThrow(()->makeHttpPUTRequest(
                "http://" + serverAddress() + "/notebook/" + newNotebookPath, requestBody
        ));
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201,response.status());
        Assertions.assertTrue(response.body().getString("message").contains("Created new notebook "));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/notebook} endpoint results in a new file being saved on disk, even if no title parameter is provided.
    public void httpCreateNotebookWithNoTitleTest() {

        String newNotebookName = "testFileName_12345.zpln";
        Path newNotebookPath = Paths.get(newNotebookName);
        String requestBody = Json.createObjectBuilder().build().toString();
        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
        Response response = Assertions.assertDoesNotThrow(()->makeHttpPUTRequest(
                "http://" + serverAddress() + "/notebook/" + newNotebookPath, requestBody
        ));
        // Assert that we receive the proper response.
        Assertions.assertEquals(HttpStatus.CREATED_201,response.status());
        Assertions.assertTrue(response.body().getString("message").contains("Created new notebook "));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/directory} endpoint results in a new file being saved on disk.
    public void httpCreateDirectoryTest() {

        String newDirectoryName = "testFolderName/";
        Path newDirectoryPath = Paths.get(newDirectoryName);
        String requestBody = Json.createObjectBuilder().build().toString();

        // Assert that the file we are creating doesn't already exist.
        Assertions.assertFalse(Files.exists(Paths.get(notebookDirectory().toString(), newDirectoryPath.toString())));
        Assertions.assertDoesNotThrow(()->Thread.sleep(5000)); // TODO: ???? Why does waiting 5 seconds fix it
        Response response = Assertions.assertDoesNotThrow(()->makeHttpPUTRequest(
                "http://" + serverAddress() + "/notebook/" + newDirectoryName, requestBody
        ));
        // Assert that we receive the proper response.
        Assertions.assertTrue(response.body().getString("message").contains("Created new directory "));
        // Assert that the file was created.
        Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), newDirectoryPath.toString())));
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/directory} endpoint results in a new file being saved on disk.
    public void httpCopyDirectoryTest() {
            String copyDirectoryName = "testCopyFolderName/";
            Path copyDirectoryPath = Paths.get(copyDirectoryName);
            String sourceDirectoryName = "my_folder_2A94M5J1D/";
            Path sourceDirectoryPath = Paths.get("/",sourceDirectoryName);
            String requestBody = Json.createObjectBuilder().add("sourcePath",sourceDirectoryPath.toString()).build().toString();

            // Assert that the file we are creating doesn't already exist.
            Assertions.assertFalse(Files.exists(Paths.get(notebookDirectory().toString(), copyDirectoryPath.toString())));
            //Assertions.assertDoesNotThrow(()->Thread.sleep(5000)); // TODO: ???? Why does waiting 5 seconds fix it
            Response response = Assertions.assertDoesNotThrow(()->makeHttpPUTRequest(
                    "http://" + serverAddress() + "/notebook/" + copyDirectoryName,
                    requestBody
            ));
            // Assert that we receive the proper response.
            Assertions.assertEquals(("Created new directory "+copyDirectoryName.substring(0,copyDirectoryName.length()-1)),response.body().getString("message"));
            // Assert that the file was created.
            Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), copyDirectoryPath.toString())));
    }

    @Test
    // Assert that a HTTP PUT request to /notebook/{path/to/notebook} endpoint results in a copied file being saved on disk.
    public void httpCopyNotebookTest() {

            String newNotebookName = "testFileName_12345.zpln";
            Path newNotebookPath = Paths.get(newNotebookName);

            // Assert that the file we are creating doesn't already exist.
            Assertions.assertFalse(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
            Response response = Assertions.assertDoesNotThrow(()->makeHttpPUTRequest(
                    "http://" + serverAddress() + "/notebook/" + newNotebookPath,
                    "{\"sourcePath\":\"/my_note4_2A94M5J4Z.zpln\",\"title\":\"copyNotebook\"}"
            ));
            // Assert that we receive the proper response.
            Assertions.assertTrue(response.body().getString("message").contains("Created new notebook "));
            // Assert that the file was created.
            Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), newNotebookPath.toString())));
    }

    @Test
    // Assert that a HTTP DELETE request to /notebook/{path/to/notebook} endpoint results in a notebook being deleted
    public void httpDeleteNotebookTest() {

            String notebookName = "my_note3_2A94M5J3Z.zpln";
            Path notebookPath = Paths.get(notebookName);

            // Assert that the correct number of files exist
            Assertions.assertEquals(4, Assertions.assertDoesNotThrow(()->Files.list(notebookDirectory()).collect(Collectors.toList()).size()));
            // Assert that the file to be deleted exists.
            Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), notebookPath.toString())));
            Response response = Assertions.assertDoesNotThrow(()->makeHttpDELETERequest("http://" + serverAddress() + "/notebook/" + notebookName, "{}"));
            Assertions.assertEquals(204, response.status());
            // Assert that a file was deleted.
            Assertions.assertEquals(3, Assertions.assertDoesNotThrow(()->Files.list(notebookDirectory()).collect(Collectors.toList()).size()));
            // Assert that the correct file was deleted.
            Assertions.assertFalse(Files.exists(notebookPath));
    }

    @Test
    // Assert that a HTTP DELETE request to /notebook/{path/to/notebook} endpoint results in a notebook being deleted
    public void httpDeleteDirectoryTest() {

            String directoryName = "my_folder_2A94M5J1D";
            Path directoryPath = Paths.get(directoryName);

            // Assert that the correct number of files exist
            Assertions.assertEquals(4, Assertions.assertDoesNotThrow(()->Files.list(notebookDirectory()).collect(Collectors.toList()).size()));
            // Assert that the file to be deleted exists.
            Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), directoryPath.toString())));
            Response response = Assertions.assertDoesNotThrow(()->makeHttpDELETERequest("http://" + serverAddress() + "/notebook/" + directoryName, "{}"));
            Assertions.assertEquals(204, response.status());
            // Assert that a file was deleted.
            Assertions.assertEquals(3, Assertions.assertDoesNotThrow(()->Files.list(notebookDirectory()).collect(Collectors.toList()).size()));
            // Assert that the correct file was deleted.
            Assertions.assertFalse(Files.exists(directoryPath));
    }

    @Test
    // Assert that a HTTP GET request to /notebook/{path/to/notebook} endpoint results in a response with the expected file contents
    public void httpFindNotebookTest() {
            Path notebookPath = Paths
                    .get("src/test/resources/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
            String expectedFileContent = "{\"id\":\"2A94M5J1Z\",\"name\":\"my_note1\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-231621_168813393\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Welcome to Zeppelin.\\\\n##### This is a live tutorial, you can run the code yourself. (Shift-Enter to Run)\\\"\"}},{\"id\":\"20150210-015259_1403135953\",\"title\":\"Load data into table\",\"script\":{\"text\":\"\\\"%test import org.apache.commons.io.IOUtils\\\\nimport java.net.URL\\\\nimport java.nio.charset.Charset\\\\n\\\\n// Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)\\\\n// So you don't need create them manually\\\\n\\\\n// load bank data\\\\nval bankText = sc.parallelize(\\\\n    IOUtils.toString(\\\\n        new URL(\\\\\\\"https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv\\\\\\\"),\\\\n        Charset.forName(\\\\\\\"utf8\\\\\\\")).split(\\\\\\\"\\\\\\\\n\\\\\\\"))\\\\n\\\\ncase class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)\\\\n\\\\nval bank = bankText.map(s => s.split(\\\\\\\";\\\\\\\")).filter(s => s(0) != \\\\\\\"\\\\\\\\\\\\\\\"age\\\\\\\\\\\\\\\"\\\\\\\").map(\\\\n    s => Bank(s(0).toInt, \\\\n            s(1).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(2).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(3).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(5).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\").toInt\\\\n        )\\\\n).toDF()\\\\nbank.registerTempTable(\\\\\\\"bank\\\\\\\")\\\"\"}},{\"id\":\"20150210-015302_1492795503\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value\\\\nfrom bank \\\\nwhere age < 30 \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150212-145404_867439529\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value \\\\nfrom bank \\\\nwhere age < ${maxAge=30} \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150213-230422_1600658137\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value \\\\nfrom bank \\\\nwhere marital=\\\\\\\"${marital=single,single|divorced|married}\\\\\\\" \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Congratulations, it's done.\\\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\\\"\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n\\\\nAbout bank data\\\\n\\\\n```\\\\nCitation Request:\\\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\\\n  Please include this citation if you plan to use this database:\\\\n\\\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\\\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\\\n\\\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\\\n```\\\"\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";
            String notebookId = "2A94M5J1Z";
            // Assert that the file exists.
            Assertions.assertTrue(Files.exists(notebookPath));
            Response response = Assertions.assertDoesNotThrow(()->makeHttpGETRequest(
                    "http://" + serverAddress()
                            + "/notebook/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln"
            ));
            Assertions.assertEquals(expectedFileContent, response.body().getString("message").strip().toString());
    }

    @Test
    // Assert that a HTTP POST request to /notebook/{path/to/notebook} endpoint results in an updated file containing the modifications contained in the request body.
    public void httpUpdateNotebookTest() {
            // Assert that the file content is the same as in the resource files before edits.
            Path notebookPath = Paths.get("my_folder_2A94M5J1D", "my_note2_2A94M5J2Z.zpln");
            String title = "editedTitle";
            String originalFileContent = "{  \"paragraphs\": [    {      \"text\": \"%test\\n## Congratulations, it\\u0027s done.\\n##### You can create your own notebook in \\u0027Notebook\\u0027 menu. Good luck!\",      \"config\": {        \"colWidth\": 12.0,        \"graph\": {          \"mode\": \"table\",          \"height\": 300.0,          \"optionOpen\": false,          \"keys\": [],          \"values\": [],          \"groups\": [],          \"scatter\": {}        },        \"editorHide\": true      },      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1423836268492_216498320\",      \"id\": \"20150213-230428_1231780373\",      \"results\": {        \"code\": \"SUCCESS\",        \"msg\": [          {            \"type\": \"HTML\",            \"data\": \"\\u003ch2\\u003eCongratulations, it\\u0027s done.\\u003c/h2\\u003e\\n\\u003ch5\\u003eYou can create your own notebook in \\u0027Notebook\\u0027 menu. Good luck!\\u003c/h5\\u003e\\n\"          }        ]      },      \"dateCreated\": \"Feb 13, 2015 11:04:28 PM\",      \"dateStarted\": \"Apr 1, 2015 9:12:18 PM\",      \"dateFinished\": \"Apr 1, 2015 9:12:18 PM\",      \"status\": \"FINISHED\",      \"progressUpdateIntervalMs\": 500    },    {      \"text\": \"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM\\u00272011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\",      \"config\": {        \"colWidth\": 12.0,        \"graph\": {          \"mode\": \"table\",          \"height\": 300.0,          \"optionOpen\": false,          \"keys\": [],          \"values\": [],          \"groups\": [],          \"scatter\": {}        },        \"editorHide\": true      },      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1427420818407_872443482\",      \"id\": \"20150326-214658_12335843\",      \"results\": {        \"code\": \"SUCCESS\",        \"msg\": [          {            \"type\": \"HTML\",            \"data\": \"\\u003cp\\u003eAbout bank data\\u003c/p\\u003e\\n\\u003cpre\\u003e\\u003ccode\\u003eCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM\\u00272011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n\\u003c/code\\u003e\\u003c/pre\\u003e\\n\"          }        ]      },      \"dateCreated\": \"Mar 26, 2015 9:46:58 PM\",      \"dateStarted\": \"Jul 3, 2015 1:44:56 PM\",      \"dateFinished\": \"Jul 3, 2015 1:44:56 PM\",      \"status\": \"FINISHED\",      \"progressUpdateIntervalMs\": 500    },    {      \"config\": {},      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1435955447812_-158639899\",      \"id\": \"20150703-133047_853701097\",      \"dateCreated\": \"Jul 3, 2015 1:30:47 PM\",      \"status\": \"READY\",      \"progressUpdateIntervalMs\": 500    }  ],  \"id\": \"2A94M5J2Z\",  \"name\": \"my_note2\",  \"angularObjects\": {},  \"config\": {    \"looknfeel\": \"default\"  },  \"info\": {}}";
            String expectedFileContent = "{\"id\":\"2A94M5J2Z\",\"name\":\"editedTitle\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Congratulations, it's done.\\\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\\\"\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n\\\\nAbout bank data\\\\n\\\\n```\\\\nCitation Request:\\\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\\\n  Please include this citation if you plan to use this database:\\\\n\\\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\\\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\\\n\\\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\\\n```\\\"\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";

            Assertions
                    .assertEquals(
                            originalFileContent,
                            Assertions.assertDoesNotThrow(()->Files.readAllLines(Paths.get(notebookDirectory().toString(), notebookPath.toString()), Charset.defaultCharset()).stream().collect(Collectors.joining()))
                    );

            Response response = Assertions.assertDoesNotThrow(()->makeHttpPOSTRequest(
                    "http://" + serverAddress() + "/notebook/" + notebookPath.toString(),
                    "{\"title\":\"" + title + "\"}"
            ));
            // Assert that we got the proper response.
            Assertions
                    .assertTrue(response.body().getString("message").strip().contains("Notebook edited successfully"));
            // Assert that the file content has the edited paragraph saved to file in the correct place.
            Assertions
                    .assertEquals(
                            expectedFileContent, Assertions.assertDoesNotThrow(()->com.google.common.io.Files
                                    .readLines(Paths.get(notebookDirectory().toString(), notebookPath.toString()).toFile(), Charset.defaultCharset()).stream().collect(Collectors.joining()))
                    );
    }

}
