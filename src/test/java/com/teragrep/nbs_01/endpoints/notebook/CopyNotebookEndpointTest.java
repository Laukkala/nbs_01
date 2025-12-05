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
import com.teragrep.nbs_01.http.body.JSONBody;
import com.teragrep.nbs_01.repository.LocalFilesystemStorage;
import com.teragrep.nbs_01.http.requests.BasicRequest;
import com.teragrep.nbs_01.http.responses.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class CopyNotebookEndpointTest extends AbstractNotebookServerTest {

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterEach
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    // Assert that a proper request to CopyNotebookEndpoint results in a correct response and a file being saved to disk.
    public void httpCopyNotebookTest() {
        // Destination file must not exist
        Path destinationFile = Paths.get("testNotebookName");
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(destinationFile)));

        // Source file must exist
        Path sourceFile = notebook2();
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(sourceFile)));
        String sourceFileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(sourceFile)));

        CopyNotebookEndpoint endPoint = new CopyNotebookEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        JsonObject body = Json.createObjectBuilder().add("sourcePath", notebook2().toString()).build();
        Response response = endPoint.createResponse(new BasicRequest(destinationFile, new JSONBody(body)));

        // Assert that we receive the proper response and that it contains the text from all the paragraphs from the source notebook
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Header expectedLocationHeader = new BasicHeader("Location", destinationFile.toString());
        Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
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
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertTrue(response.body().asString().contains("\"script\":{\"text\":\"\"}"))
                );
        // Assert that the copied file was created to proper path.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(destinationFile)));
        // Source file must not have changed
        Assertions
                .assertEquals(
                        sourceFileContent,
                        Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook2())))
                );
    }

    @Test
    // Assert that a proper request to CopyNotebookEndpoint results in a correct response and a file being overwritten to disk.
    public void httpCopyAndOverwriteNotebookTest() {
        // Destination file must exist
        Path destinationFile = notebook3();
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(destinationFile)));
        String destinationFileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(destinationFile)));

        // Source file must exist
        Path sourceFile = notebook2();
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(sourceFile)));
        String sourceFileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(sourceFile)));

        CopyNotebookEndpoint endPoint = new CopyNotebookEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        JsonObject body = Json.createObjectBuilder().add("sourcePath", notebook2().toString()).build();
        Response response = endPoint.createResponse(new BasicRequest(destinationFile, new JSONBody(body)));

        // Assert that we receive the proper response and that it contains the text from all the paragraphs from the source notebook
        Assertions.assertEquals(HttpStatus.CREATED_201, response.status());
        Header expectedLocationHeader = new BasicHeader("Location", notebook3().toString());
        Header expectedContentTypeHeader = new BasicHeader("Content-Type", "application/json");
        Assertions.assertEquals(expectedContentTypeHeader.toString(), response.headers().get(1).toString());
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
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
        Assertions
                .assertDoesNotThrow(
                        () -> Assertions.assertTrue(response.body().asString().contains("\"script\":{\"text\":\"\"}"))
                );
        // Assert that the copied file was created to proper path.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(destinationFile)));
        // Source file must not have changed
        Assertions
                .assertEquals(
                        sourceFileContent,
                        Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(sourceFile)))
                );
        // Destination file must have changed
        Assertions
                .assertNotEquals(
                        destinationFileContent,
                        Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(destinationFile)))
                );
    }

    @Test
    // Assert that a request to CopyNotebookEndpoint with a source path that does not have a file results in an error
    public void httpCopyNonExistentSourceNotebookTest() {
        // Destination file must not exist
        Path destinationFile = Paths.get("testNotebookName");
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(destinationFile)));

        // Source file must not exist
        Path sourceFile = Paths.get("I_DONT_EXIST");
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(sourceFile)));

        CopyNotebookEndpoint endPoint = new CopyNotebookEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        JsonObject body = Json.createObjectBuilder().add("sourcePath", sourceFile.toString()).build();
        Response response = endPoint.createResponse(new BasicRequest(destinationFile, new JSONBody(body)));

        // The endpoint should return an JsonResponse with the correct status and specified cause.
        Assertions.assertEquals(HttpStatus.NOT_FOUND_404, response.status());
        // Assert that the file was not created.
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(destinationFile)));
    }

    @Test
    // Assert that a request to CopyNotebookEndpoint to a path that contains a directory results in an error
    public void httpCopyNotebookIntoUnavailablePathTest() {
        // Destination file must exist and be a directory
        Path destinationDirectory = directory1();
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(destinationDirectory)));
        Assertions.assertTrue(Files.isDirectory(notebookDirectory().resolve(destinationDirectory)));

        // Source file must exist
        Path sourceFile = notebook1();
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(sourceFile)));
        String sourceFileContent = Assertions
                .assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(sourceFile)));

        CopyNotebookEndpoint endPoint = new CopyNotebookEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        JsonObject body = Json.createObjectBuilder().add("sourcePath", sourceFile.toString()).build();
        Response response = endPoint.createResponse(new BasicRequest(destinationDirectory, new JSONBody(body)));

        JsonObject expectedJson = Json
                .createObjectBuilder()
                .add("message", "File at path: " + directory1() + " is a Directory!")
                .build();

        // The endpoint should return an JsonResponse with the correct status and specified cause.
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.status());
        Assertions.assertEquals(expectedJson.toString(), response.body().asString());

        // Source file must not have changed
        Assertions
                .assertEquals(
                        sourceFileContent,
                        Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(sourceFile)))
                );
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(CopyNotebookEndpoint.class).verify();
    }

}
