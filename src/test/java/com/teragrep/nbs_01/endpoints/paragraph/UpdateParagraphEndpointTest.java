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
import com.teragrep.nbs_01.protocols.http.body.JSONBody;
import com.teragrep.nbs_01.protocols.http.path.HTTPParagraphRequestPath;
import com.teragrep.nbs_01.repository.storage.LocalFilesystemStorage;
import com.teragrep.nbs_01.protocols.http.BasicHTTPRequest;
import com.teragrep.nbs_01.protocols.http.HTTPResponse;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class UpdateParagraphEndpointTest extends AbstractNotebookServerTest {

    private final String paragraphId = "20150213-230428_1231780373";
    private final String editedParagraphText = "test edit";
    private final String editedTitle = "testTitle";

    @Test
    public void httpUpdateParagraphTest() {
        // Assert that a request to UpdateParagraphEndpoint results in a modified file being saved on disk.
        Assertions
                .assertTrue(Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2()))).contains("%test\\n## Congratulations, it\\u0027s done.\\n##### You can create your own notebook in \\u0027Notebook\\u0027 menu. Good luck!"));

        Assertions
                .assertFalse(
                        Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2()))).contains("\"title\":\"" + editedTitle + "\"")
                );

        final String expectedFileContent = "{\"title\":\"my_note2\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\""
                + editedTitle + "\",\"script\":{\"text\":\"" + editedParagraphText
                + "\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";

        // Make a request editing the title of the notebook as well as the text of a paragraph, identified with an ID.
        final UpdateParagraphEndpoint endpoint = new UpdateParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );

        final Path requestPath = Paths.get(notebook2().toString(), paragraphId);
        final JsonObject body = Json
                .createObjectBuilder()
                .add("title", editedTitle)
                .add("text", editedParagraphText)
                .build();
        final HTTPResponse response = endpoint
                .createResponse(new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath), new JSONBody(body)));
        // Assert that we got the proper response.
        Assertions.assertEquals(HttpStatus.OK_200, response.status());
        final Header expectedLocationHeader = new BasicHeader(
                "Location",
                requestPath.subpath(0, requestPath.getNameCount() - 1).toString()
        );
        final Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());

        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("id", paragraphId)
                .add("title", editedTitle)
                .add("script", Json.createObjectBuilder().add("text", editedParagraphText))
                .build();

        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        // Assert that the file content has the edited paragraph saved to file in the correct place.
        Assertions
                .assertEquals(
                        expectedFileContent,
                        Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2())))
                );
    }

    @Test
    public void httpUpdateParagraphTextTest() {
        // Assert that a request to UpdateParagraphEndpoint results in a modified file where only the text has been changed being saved on disk.
        Assertions
                .assertTrue(Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2()))).contains("%test\\n## Congratulations, it\\u0027s done.\\n##### You can create your own notebook in \\u0027Notebook\\u0027 menu. Good luck!"));

        final String expectedFileContent = "{\"title\":\"my_note2\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\""
                + editedParagraphText
                + "\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";

        // Make a request editing the title of the notebook as well as the text of a paragraph, identified with an ID.
        final UpdateParagraphEndpoint endpoint = new UpdateParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );

        final Path requestPath = Paths.get(notebook2().toString(), paragraphId);
        final JsonObject body = Json.createObjectBuilder().add("text", editedParagraphText).build();
        final HTTPResponse response = endpoint
                .createResponse(new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath), new JSONBody(body)));
        // Assert that we got the proper response.

        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("id", paragraphId)
                .add("title", "")
                .add("script", Json.createObjectBuilder().add("text", editedParagraphText))
                .build();

        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        // Assert that the file content has the edited paragraph saved to file in the correct place.
        Assertions
                .assertEquals(
                        expectedFileContent,
                        Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2())))
                );
    }

    @Test
    public void httpUpdateParagraphTitleTest() {
        // Assert that a request to UpdateParagraphEndpoint results in a modified file where only the title has been changed being saved on disk.
        Assertions
                .assertFalse(
                        Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2()))).contains("\"title\":\"" + editedTitle + "\"")
                );

        final String expectedFileContent = "{\"title\":\"my_note2\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\""
                + editedTitle
                + "\",\"script\":{\"text\":\"%test\\n## Congratulations, it's done.\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";

        // Make a request editing the title of the notebook as well as the text of a paragraph, identified with an ID.
        final UpdateParagraphEndpoint endpoint = new UpdateParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );

        final Path requestPath = Paths.get(notebook2().toString(), paragraphId);
        final JsonObject body = Json.createObjectBuilder().add("title", editedTitle).build();
        final HTTPResponse response = endpoint
                .createResponse(new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath), new JSONBody(body)));
        // Assert that we got the proper response.

        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("id", paragraphId)
                .add("title", editedTitle)
                .add(
                        "script",
                        Json
                                .createObjectBuilder()
                                .add(
                                        "text",
                                        "%test\n## Congratulations, it's done.\n##### You can create your own notebook in 'Notebook' menu. Good luck!"
                                )
                )
                .build();

        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
        // Assert that the file content has the edited paragraph saved to file in the correct place.
        Assertions
                .assertEquals(
                        expectedFileContent,
                        Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2())))
                );
    }

    @Test
    public void httpUpdateParagraphInNonexistentNotebookTest() {
        // Assert that a request to UpdateParagraphEndpoint with a nonexistent notebook path results in an error.
        final String nonExistentNotebookName = "nonExistentNotebook";

        // Make a request editing the title of a notebook that doesn't exist.
        final UpdateParagraphEndpoint endpoint = new UpdateParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );

        final Path requestPath = Paths.get(nonExistentNotebookName, paragraphId);
        final JsonObject body = Json
                .createObjectBuilder()
                .add("title", editedTitle)
                .add("text", editedParagraphText)
                .build();
        final HTTPResponse response = endpoint
                .createResponse(new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath), new JSONBody(body)));

        // The endpoint should return a Response with the correct status and message.
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "No such file: " + nonExistentNotebookName)
                .build();
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    public void httpUpdateNonexistentParagraphTest() {
        // Assert that a request to UpdateParagraphEndpoint with a nonexistent paragraphId results in an error.
        final String nonexistentParagraphId = "nonexistentId";

        // Make a request editing the title of the notebook as well as the text of a paragraph, identified with an ID.
        final UpdateParagraphEndpoint endpoint = new UpdateParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );

        final Path requestPath = Paths.get(notebook2().toString(), nonexistentParagraphId);
        final JsonObject body = Json
                .createObjectBuilder()
                .add("title", editedTitle)
                .add("paragraphId", nonexistentParagraphId)
                .add("text", editedParagraphText)
                .build();
        final HTTPResponse response = endpoint
                .createResponse(new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath), new JSONBody(body)));

        // The endpoint should return a Response with the correct status and message.
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "Paragraph with Id " + nonexistentParagraphId + " not found!")
                .build();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    public void httpInvalidRequestFormatTest() {
        // Assert that a request to UpdateParagraphEndpoint with an improperly formatted Request results in an error.
        final String nonexistentNotebookName = "nonexistentNotebookPath";

        // Make a request editing the title of the notebook as well as the text of a paragraph, identified with an ID.
        final UpdateParagraphEndpoint endpoint = new UpdateParagraphEndpoint(
                new LocalFilesystemStorage(notebookDirectory())
        );

        final Path requestPath = Paths.get(nonexistentNotebookName);
        final JsonObject body = Json.createObjectBuilder().add("text", editedParagraphText).build();
        final HTTPResponse response = endpoint
                .createResponse(new BasicHTTPRequest(new HTTPParagraphRequestPath(requestPath), new JSONBody(body)));

        // The endpoint should return a Response with the correct status and message.
        final JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "Request has a malformed identifier!")
                .build();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        Assertions
                .assertDoesNotThrow(() -> Assertions.assertEquals(expectedJson.toString(), response.body().asString()));
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(UpdateParagraphEndpoint.class).verify();
    }
}
