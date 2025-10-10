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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

class NotebookTest {

    private final Path notebookSource = Paths.get("src/test/resources");
    private final Path notebookDirectory = Paths.get("target/notebooks");
    private final Path notebook1 = Paths
            .get("target/notebooks/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
    private final Path notebook3 = Paths.get("target/notebooks/my_note3_2A94M5J3Z.zpln");
    private final Path notebook4 = Paths.get("target/notebooks/my_note4_2A94M5J4Z.zpln");

    public void copyFileRecursively(File fileToCopy, File destination) {
        if (fileToCopy.isDirectory()) {
            File[] children = fileToCopy.listFiles();
            for (File child : children) {
                copyFileRecursively(child, Paths.get(destination.toString(), child.getName()).toFile());
            }
        }
        if (!destination.exists()) {
            File parent = destination.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            Assertions.assertDoesNotThrow(() -> Files.copy(fileToCopy.toPath(), destination.toPath()));
        }
    }

    public void deleteFileRecursively(File fileToDelete) {
        File[] children = fileToDelete.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteFileRecursively(child);
            }
        }
        fileToDelete.delete();
    }

    @BeforeEach
    void setUp() {
        deleteFileRecursively(notebookDirectory.toFile());
        copyFileRecursively(notebookSource.toFile(), notebookDirectory.toFile());
    }

    @AfterEach
    void tearDown() {
        deleteFileRecursively(notebookDirectory.toFile());
    }

    // Deleting a notebook should result in the file no longer existing.
    @Test
    void testDelete() {
        Directory root = Assertions.assertDoesNotThrow(() -> new Directory(notebookDirectory).load());
        Notebook notebook = (Notebook) Assertions.assertDoesNotThrow(() -> root.findFile(notebook4).load());
        Assertions.assertTrue(Files.exists(notebook.path()));
        Assertions.assertDoesNotThrow(() -> notebook.delete());
        Assertions.assertFalse(Files.exists(notebook.path()));
    }

    // Notebooks should have the correct number of paragraphs
    @Test
    void testParagraphs() {
        Directory root = Assertions.assertDoesNotThrow(() -> new Directory(notebookDirectory).load());
        Notebook notebook = (Notebook) Assertions.assertDoesNotThrow(() -> root.findFile(notebook1).load());
        Map<String, Paragraph> paragraphs = notebook.paragraphs();
        Assertions.assertEquals(8, paragraphs.size());
    }

    // Calling json() should have the same content as in the test file.
    @Test
    void testJson() {
        Directory root = Assertions.assertDoesNotThrow(() -> new Directory(notebookDirectory).load());
        Notebook notebook = (Notebook) Assertions.assertDoesNotThrow(() -> root.findFile(notebook3).load());
        Assertions
                .assertEquals(
                        "{\"name\":\"my_note2\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"%test\\n## Hello, I'm a new notebook. Totally different to the previous one, I have one less paragraphs, you see.\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\"}}]}",
                        notebook.json().toString()
                );
    }

    // After copying a Notebook, both the original and the copied notebook should exist.
    @Test
    void testCopy() {
        Directory root = Assertions.assertDoesNotThrow(() -> new Directory(notebookDirectory).load());
        Notebook notebook = (Notebook) Assertions.assertDoesNotThrow(() -> root.findFile(notebook4).load());
        Assertions.assertTrue(Files.exists(notebook.path()));
        Notebook copy = Assertions
                .assertDoesNotThrow(() -> notebook.copy(Paths.get(root.path().toString(), "newName_copyId")));
        Assertions.assertDoesNotThrow(() -> copy.save());
        Assertions.assertTrue(Files.exists(notebook.path()));
        Assertions.assertTrue(Files.exists(copy.path()));
    }

    // Creating a new notebook and saving it should result in a new file being created.
    @Test
    void testSave() {
        Path notebookPath = Paths.get(notebookDirectory.toString(), "createdNotebook_newNotebookId");
        Notebook notebook = new Notebook("title", notebookPath, new LinkedHashMap<>());

        Assertions.assertFalse(Files.exists(notebook.path()));
        Assertions.assertDoesNotThrow(() -> notebook.save());
        Assertions.assertTrue(Files.exists(notebook.path()));
    }

    @Test
    void testRename() {
        Directory root = Assertions.assertDoesNotThrow(() -> new Directory(notebookDirectory).load());
        Notebook notebook = Assertions.assertDoesNotThrow(() -> (Notebook) root.findFile(notebook4).load());
        Path originalPath = notebook.path();
        Assertions.assertTrue(Files.exists(originalPath));
        Assertions.assertDoesNotThrow(() -> notebook.rename("renamedFile_2A94M5J4Z.zpln"));
        Assertions.assertFalse(Files.exists(originalPath));
        Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory.toString(), "renamedFile_2A94M5J4Z.zpln")));
    }

    @Test
    void testMove() {
        Directory root = Assertions.assertDoesNotThrow(() -> new Directory(notebookDirectory).load());
        Notebook notebook = (Notebook) Assertions.assertDoesNotThrow(() -> root.findFile(notebook4).load());
        Path originalPath = notebook.path();
        Assertions.assertTrue(Files.exists(originalPath));
        Path newPath = Paths.get(notebookDirectory.toString(), "my_folder_2A94M5J1D", "renamedFile_2A94M5J4Z.zpln");
        Assertions.assertDoesNotThrow(() -> notebook.move(newPath));
        Directory updatedDirectory = Assertions.assertDoesNotThrow(() -> new Directory(notebookDirectory).load());
        Assertions.assertFalse(Files.exists(originalPath));
        Assertions
                .assertTrue(Files.exists(Assertions.assertDoesNotThrow(() -> updatedDirectory.findFile(newPath).path())));
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(Notebook.class).verify();
    }
}
