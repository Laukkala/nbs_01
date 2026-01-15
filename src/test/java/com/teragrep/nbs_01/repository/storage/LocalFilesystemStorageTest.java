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
package com.teragrep.nbs_01.repository.storage;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.identifiers.Identifier;
import com.teragrep.nbs_01.repository.identifiers.PathIdentifier;
import com.teragrep.nbs_01.repository.serialization.SerializedNotebook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocalFilesystemStorageTest extends AbstractNotebookServerTest {

    @Test
    void deleteDirectory() {
        final LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory());
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory1())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory2())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook1())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook2())));

        Assertions.assertDoesNotThrow(() -> root.deleteDirectory(new PathIdentifier(directory1().toString())));

        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(directory1())));
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(directory2())));
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(notebook1())));
        Assertions.assertFalse(Files.exists(notebookDirectory().resolve(notebook2())));

    }

    @Test
    void deleteNotebook() {
        final LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory());
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook1())));
        Assertions.assertDoesNotThrow(() -> root.deleteFile(new PathIdentifier(notebook1().toString())));
        Assertions.assertFalse(Files.exists(notebook1()));
    }

    @Test
    void copy() {
        final LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory());
        final Path destinationPath = Path.of("testPath");
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory1())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory2())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook1())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook2())));
        Assertions
                .assertDoesNotThrow(() -> root.copyDirectory(new PathIdentifier(directory1()), new PathIdentifier(destinationPath)));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(destinationPath)));
        Assertions
                .assertTrue(Files.exists(notebookDirectory().resolve(destinationPath).resolve("my_second_folder_2A94M5J2D")));
        Assertions
                .assertTrue(Files.exists(notebookDirectory().resolve(destinationPath).resolve("my_second_folder_2A94M5J2D").resolve("my_note1_2A94M5J1Z.zpln")));
        Assertions
                .assertTrue(Files.exists(notebookDirectory().resolve(destinationPath).resolve("my_note2_2A94M5J2Z.zpln")));

        // Source notebooks should continue to exist.
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory1())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(directory2())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook1())));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebook2())));
    }

    @Test
    void createDirectory() {
        final LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory());
        final Path testDirectory = Paths.get("testDirectory");
        Assertions.assertDoesNotThrow(() -> root.writeDirectory(new PathIdentifier(testDirectory)));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(testDirectory)));
    }

    @Test
    void read() {
        final LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory());
        final String notebook1Content = Assertions
                .assertDoesNotThrow(() -> root.readFile(new PathIdentifier(notebook1())));
        Assertions
                .assertEquals(
                        Assertions.assertDoesNotThrow(() -> Files.readString(notebookDirectory().resolve(notebook1()))), notebook1Content
                );
    }

    @Test
    void write() {
        final LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory());
        final Path notebookPath = Paths.get("createdNotebook_newNotebookId");
        final Notebook notebook = new Notebook("title");
        final SerializedNotebook serializedNotebook = Assertions
                .assertDoesNotThrow(() -> root.serializeNotebook(notebook));

        Assertions.assertFalse(Files.exists(notebookPath));
        Assertions
                .assertDoesNotThrow(() -> root.writeFile(new PathIdentifier(notebookPath), serializedNotebook.serialize()));
        Assertions.assertTrue(Files.exists(notebookDirectory().resolve(notebookPath)));
    }

    @Test
    void testReadDirectory() {
        final LocalFilesystemStorage root = Assertions
                .assertDoesNotThrow(() -> new LocalFilesystemStorage(notebookDirectory()));
        final String rootChildren = Assertions.assertDoesNotThrow(() -> root.readDirectory(new PathIdentifier("")));
        final String directory1Children = Assertions
                .assertDoesNotThrow(() -> root.readDirectory(new PathIdentifier(directory1().toString())));
        final String directory2Children = Assertions
                .assertDoesNotThrow(() -> root.readDirectory(new PathIdentifier(directory2().toString())));

        Assertions.assertTrue(rootChildren.contains(notebook3().getFileName().toString()));
        Assertions.assertTrue(rootChildren.contains(notebook4().getFileName().toString()));
        Assertions.assertTrue(rootChildren.contains(directory1().getFileName().toString()));

        Assertions.assertTrue(directory1Children.contains(notebook2().getFileName().toString()));
        Assertions.assertTrue(directory1Children.contains(directory2().getFileName().toString()));

        Assertions.assertTrue(directory2Children.contains(notebook1().getFileName().toString()));
    }

    @Test
    void testChildren() {
        final LocalFilesystemStorage root = Assertions
                .assertDoesNotThrow(() -> new LocalFilesystemStorage(notebookDirectory()));
        final List<Identifier> allChildren = Assertions
                .assertDoesNotThrow(() -> root.listFiles(new PathIdentifier("")));
        Assertions.assertEquals(7, allChildren.size());
    }
}
