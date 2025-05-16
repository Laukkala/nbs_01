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

import com.google.common.io.Files;
import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.responses.Response;
import org.junit.jupiter.api.*;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UpdateParagraphEndpointTest extends AbstractNotebookServerTest {

    private final String notebookId = "2A94M5J2Z";
    private final Path notebookPath = Paths
            .get(notebookDirectory().toString(), "my_folder_2A94M5J1D", "my_note2_2A94M5J2Z.zpln");
    private final String paragraphId = "20150326-214658_12335843";
    private final String paragraphContent = "test edit";
    private final String originalFileContent = "{  \"paragraphs\": [    {      \"text\": \"%test\\n## Congratulations, it\\u0027s done.\\n##### You can create your own notebook in \\u0027Notebook\\u0027 menu. Good luck!\",      \"config\": {        \"colWidth\": 12.0,        \"graph\": {          \"mode\": \"table\",          \"height\": 300.0,          \"optionOpen\": false,          \"keys\": [],          \"values\": [],          \"groups\": [],          \"scatter\": {}        },        \"editorHide\": true      },      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1423836268492_216498320\",      \"id\": \"20150213-230428_1231780373\",      \"results\": {        \"code\": \"SUCCESS\",        \"msg\": [          {            \"type\": \"HTML\",            \"data\": \"\\u003ch2\\u003eCongratulations, it\\u0027s done.\\u003c/h2\\u003e\\n\\u003ch5\\u003eYou can create your own notebook in \\u0027Notebook\\u0027 menu. Good luck!\\u003c/h5\\u003e\\n\"          }        ]      },      \"dateCreated\": \"Feb 13, 2015 11:04:28 PM\",      \"dateStarted\": \"Apr 1, 2015 9:12:18 PM\",      \"dateFinished\": \"Apr 1, 2015 9:12:18 PM\",      \"status\": \"FINISHED\",      \"progressUpdateIntervalMs\": 500    },    {      \"text\": \"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM\\u00272011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\",      \"config\": {        \"colWidth\": 12.0,        \"graph\": {          \"mode\": \"table\",          \"height\": 300.0,          \"optionOpen\": false,          \"keys\": [],          \"values\": [],          \"groups\": [],          \"scatter\": {}        },        \"editorHide\": true      },      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1427420818407_872443482\",      \"id\": \"20150326-214658_12335843\",      \"results\": {        \"code\": \"SUCCESS\",        \"msg\": [          {            \"type\": \"HTML\",            \"data\": \"\\u003cp\\u003eAbout bank data\\u003c/p\\u003e\\n\\u003cpre\\u003e\\u003ccode\\u003eCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM\\u00272011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n\\u003c/code\\u003e\\u003c/pre\\u003e\\n\"          }        ]      },      \"dateCreated\": \"Mar 26, 2015 9:46:58 PM\",      \"dateStarted\": \"Jul 3, 2015 1:44:56 PM\",      \"dateFinished\": \"Jul 3, 2015 1:44:56 PM\",      \"status\": \"FINISHED\",      \"progressUpdateIntervalMs\": 500    },    {      \"config\": {},      \"settings\": {        \"params\": {},        \"forms\": {}      },      \"jobName\": \"paragraph_1435955447812_-158639899\",      \"id\": \"20150703-133047_853701097\",      \"dateCreated\": \"Jul 3, 2015 1:30:47 PM\",      \"status\": \"READY\",      \"progressUpdateIntervalMs\": 500    }  ],  \"id\": \"2A94M5J2Z\",  \"name\": \"my_note2\",  \"angularObjects\": {},  \"config\": {    \"looknfeel\": \"default\"  },  \"info\": {}}";
    private final String expectedFileContent = "{\"id\":\"2A94M5J2Z\",\"name\":\"my_note2\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Congratulations, it's done.\\\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\\\"\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"test edit\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterEach
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    public void httpUpdateNotebookTest() {
        Assertions.assertDoesNotThrow(() -> {
            // Assert that the file content is the same as in the resource files before edits.
            Assertions
                    .assertEquals(originalFileContent, Files.readLines(notebookPath.toFile(), Charset.defaultCharset()).stream().collect(Collectors.joining()));

            // Start server and wait for it to initialize.
            startServer();
            Response response = makeHttpPOSTRequest(
                    "http://" + serverAddress() + "/notebook/update",
                    "{\"notebookId\":\"" + notebookId + "\",\"paragraphId\":\"" + paragraphId
                            + "\",\"paragraphText\":\"" + paragraphContent + "\"}"
            );
            stopServer();
            // Assert that we got the proper response.
            Assertions
                    .assertTrue(response.body().getString("message").strip().contains("Paragraph edited successfully"));
            // Assert that the file content has the edited paragraph saved to file in the correct place.
            Assertions
                    .assertEquals(expectedFileContent, Files.readLines(notebookPath.toFile(), Charset.defaultCharset()).stream().collect(Collectors.joining()));
        });
    }

    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketUpdateTest() {
        Assertions.assertDoesNotThrow(() -> {
            // Assert that the file content is the same as in the resource files before edits.
            Assertions
                    .assertEquals(originalFileContent, Files.readLines(notebookPath.toFile(), Charset.defaultCharset()).stream().collect(Collectors.joining()));

            // Start server and wait for it to initialize.
            startServer();
            Response response = makeWebSocketRequest(
                    "ws://" + serverAddress() + "/notebook/update",
                    "{\"notebookId\":\"" + notebookId + "\",\"paragraphId\":\"" + paragraphId
                            + "\",\"paragraphText\":\"" + paragraphContent + "\"}"
            );
            stopServer();
            // Assert that we got the proper response.
            Assertions
                    .assertTrue(response.body().getString("message").strip().contains("Paragraph edited successfully"));
            // Assert that the file content has the edited paragraph saved to file in the correct place.
            Assertions
                    .assertEquals(expectedFileContent, Files.readLines(notebookPath.toFile(), Charset.defaultCharset()).stream().collect(Collectors.joining()));
        });
    }
}
