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
package com.teragrep.nbs_01.endpoints.directory;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.repository.LocalFilesystemStorage;
import com.teragrep.nbs_01.http.requests.BasicRequest;
import com.teragrep.nbs_01.http.responses.Response;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeleteDirectoryEndPointTest extends AbstractNotebookServerTest {

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterEach
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    // Assert that a HTTP request to /notebook/new endpoint results in new directory being saved on disk.
    public void httpDeleteDirectoryTest() {
        Path deletedDirectoryPath = directory2();
        // Destination directory must exist
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(deletedDirectoryPath)));
        DeleteDirectoryEndpoint endPoint = new DeleteDirectoryEndpoint(new LocalFilesystemStorage(notebookDirectory()));
        Response response = endPoint.createResponse(new BasicRequest(deletedDirectoryPath));
        // Assert that we receive the proper response.
        Assertions.assertEquals(204, response.status());
        Header expectedLocationHeader = new BasicHeader("Location", deletedDirectoryPath.toString());
        Assertions.assertEquals(expectedLocationHeader.toString(), response.headers().get(0).toString());
        // Destination directory must not exist
        Assertions.assertFalse(Files.exists(deletedDirectoryPath));
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(DeleteDirectoryEndpoint.class).verify();
    }
}
