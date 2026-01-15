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
package com.teragrep.nbs_01.repository;

import com.teragrep.nbs_01.repository.identifiers.PathIdentifier;
import com.teragrep.nbs_01.repository.serialization.JsonNotebook;
import com.teragrep.nbs_01.repository.serialization.SerializedNotebook;
import com.teragrep.nbs_01.repository.storage.LocalFilesystemStorage;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

class NotebookTest {

    private final Path notebookSource = Paths.get("src/test/resources");
    private final Path notebookDirectory = Paths.get("target/notebooks");
    private final Path notebook1 = Paths
            .get("target/notebooks/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
    private final Path notebook4 = Paths.get("target/notebooks/my_note4_2A94M5J4Z.zpln");

    public NotebookTest() {
        deleteFileRecursively(notebookDirectory.toFile());
        copyFileRecursively(notebookSource.toFile(), notebookDirectory.toFile());
    }

    public void copyFileRecursively(final File fileToCopy, final File destination) {
        if (fileToCopy.isDirectory()) {
            final File[] children = fileToCopy.listFiles();
            for (final File child : children) {
                copyFileRecursively(child, Paths.get(destination.toString(), child.getName()).toFile());
            }
        }
        if (!destination.exists()) {
            final File parent = destination.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            Assertions.assertDoesNotThrow(() -> Files.copy(fileToCopy.toPath(), destination.toPath()));
        }
    }

    public void deleteFileRecursively(final File fileToDelete) {
        final File[] children = fileToDelete.listFiles();
        if (children != null) {
            for (final File child : children) {
                deleteFileRecursively(child);
            }
        }
        fileToDelete.delete();
    }

    // Notebooks should have the correct number of paragraphs
    @Test
    void testParagraphs() {
        final LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory);
        final JsonObject json = Assertions
                .assertDoesNotThrow(
                        () -> Json
                                .createReader(
                                        new StringReader(
                                                root.readFile(new PathIdentifier(notebookDirectory.relativize(notebook1).toString()))
                                        )
                                )
                                .readObject()
                );
        final JsonNotebook jsonNotebook = new JsonNotebook(json);
        final Notebook notebook = new Notebook(jsonNotebook.title(), jsonNotebook.paragraphs());
        final Map<String, Paragraph> paragraphs = notebook.paragraphs();
        Assertions.assertEquals(8, paragraphs.size());
    }

    // After copying a Notebook, both the original and the copied notebook should exist.
    @Test
    void testCopy() {
        final LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory);
        final JsonObject json = Assertions
                .assertDoesNotThrow(
                        () -> Json
                                .createReader(
                                        new StringReader(
                                                root.readFile(new PathIdentifier(notebookDirectory.relativize(notebook4).toString()))
                                        )
                                )
                                .readObject()
                );
        final JsonNotebook jsonNotebook = new JsonNotebook(json);
        final Notebook notebook = new Notebook(jsonNotebook.title(), jsonNotebook.paragraphs());
        Assertions.assertTrue(Files.exists(notebook4));
        final Path destinationPath = Paths.get("newName_copyId");
        final Notebook copy = Assertions.assertDoesNotThrow(() -> notebook.copy());
        final SerializedNotebook serializedNotebook = Assertions.assertDoesNotThrow(() -> root.serializeNotebook(copy));
        Assertions
                .assertDoesNotThrow(() -> root.writeFile(new PathIdentifier(destinationPath.toString()), serializedNotebook.serialize()));
        Assertions.assertTrue(Files.exists(notebook4));
        Assertions.assertTrue(Files.exists(notebookDirectory.resolve(destinationPath)));
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(Notebook.class).verify();
    }
}
