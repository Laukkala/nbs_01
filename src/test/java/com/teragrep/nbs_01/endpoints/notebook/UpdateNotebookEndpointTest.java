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
package com.teragrep.nbs_01.endpoints.notebook;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.protocols.http.body.JSONBody;
import com.teragrep.nbs_01.repository.storage.LocalFilesystemStorage;
import com.teragrep.nbs_01.protocols.http.BasicHTTPRequest;
import com.teragrep.nbs_01.protocols.http.HTTPResponse;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

class UpdateNotebookEndpointTest extends AbstractNotebookServerTest {

    @Test
    public void httpUpdateNotebookTest() {
        // Destination notebook must exist
        Path notebookPath = notebook2();
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebookPath)));
        // Make a request editing the title of the notebook.
        String editedTitle = "testTitle";
        UpdateNotebookEndpoint endpoint = new UpdateNotebookEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        JsonObject body = Json.createObjectBuilder().add("title", editedTitle).build();
        HTTPResponse response = endpoint.createResponse(new BasicHTTPRequest(notebookPath, new JSONBody(body)));

        // Assert that we got the proper response.
        Assertions.assertEquals(HttpStatus.OK_200, response.status());
        Header expectedLocationHeader = new BasicHeader("Location", notebookPath.toString());
        Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());
        Assertions.assertDoesNotThrow(() -> Assertions.assertTrue(response.body().asString().contains(editedTitle)));

        String updatedFileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2())));
        // Assert that the notebook title has been saved to the file in the correct place.
        String expectedFileContent = "{\"name\":\"" + editedTitle
                + "\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"%test\\n## Congratulations, it's done.\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";
        Assertions.assertEquals(expectedFileContent, updatedFileContent);
    }

    // Assert that trying to update a nonexistent notebook results in an error.
    @Test
    public void httpUpdateNonexistentNotebookTest() {
        Path nonExistentNotebookPath = notebookDirectory().resolve("nonExistentNotebook");

        // Destination notebook must not exist
        Assertions.assertFalse(Files.exists(nonExistentNotebookPath));

        String editedTitle = "testTitle";
        UpdateNotebookEndpoint endpoint = new UpdateNotebookEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        JsonObject body = Json.createObjectBuilder().add("title", editedTitle).build();
        HTTPResponse response = endpoint
                .createResponse(new BasicHTTPRequest(nonExistentNotebookPath, new JSONBody(body)));
        // Assert that we got the proper response.

        // The endpoint should return an JsonResponse with the correct status and specified cause.

        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such file: " + nonExistentNotebookPath)
                .build();

        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(UpdateNotebookEndpoint.class).verify();
    }

}
