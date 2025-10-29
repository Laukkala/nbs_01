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
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

class DirectoryTest {

    private final Path notebookSource = Paths.get("src/test/resources");
    private final Path notebookDirectory = Paths.get("target/notebooks");
    private final Path notebook1 = Paths
            .get("target/notebooks/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
    private final Path notebook2 = Paths.get("target/notebooks/my_folder_2A94M5J1D/my_note2_2A94M5J2Z.zpln");
    private final Path notebook3 = Paths.get("target/notebooks/my_note3_2A94M5J3Z.zpln");
    private final Path notebook4 = Paths.get("target/notebooks/my_note4_2A94M5J4Z.zpln");
    private final Path directory1 = Paths.get("target/notebooks/my_folder_2A94M5J1D");
    private final Path directory2 = Paths.get("target/notebooks/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D");
    private final Path junkfile = Paths.get("target/notebooks/junkfile");

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

    // Directory should contain a child for each file and directory within notebookDirectory after initialization.
    @Test
    void testInitializeDirectory() {
        Directory root = Assertions.assertDoesNotThrow(() -> new Directory(notebookDirectory).load());
        Assertions.assertEquals(3, root.children().size());
        Directory directory_1 = Assertions.assertDoesNotThrow(() -> new Directory(directory1).load());
        Assertions.assertEquals(2, directory_1.children().size());
        Directory directory_2 = Assertions.assertDoesNotThrow(() -> new Directory(directory2).load());
        Assertions.assertEquals(1, directory_2.children().size());
    }

    // List of all children should contain an ID for every directory and file.
    @Test
    void testListChildren() {
        Directory root = Assertions.assertDoesNotThrow(() -> new Directory(notebookDirectory).load());
        List<Path> rootPaths = root.children().keySet().stream().collect(Collectors.toList());
        Assertions.assertEquals(3, rootPaths.size());
        Assertions.assertTrue(rootPaths.contains(notebook3));
        Assertions.assertTrue(rootPaths.contains(notebook4));
        Assertions.assertTrue(rootPaths.contains(directory1));

        Directory directory_1 = Assertions.assertDoesNotThrow(() -> new Directory(directory1).load());
        List<Path> directory1Paths = directory_1.children().keySet().stream().collect(Collectors.toList());
        Assertions.assertEquals(2, directory1Paths.size());
        Assertions.assertTrue(directory1Paths.contains(notebook2));
        Assertions.assertTrue(directory1Paths.contains(directory2));

        Directory directory_2 = Assertions.assertDoesNotThrow(() -> new Directory(directory2).load());
        List<Path> directory2Paths = directory_2.children().keySet().stream().collect(Collectors.toList());
        Assertions.assertEquals(1, directory2Paths.size());
        Assertions.assertTrue(directory2Paths.contains(notebook1));
    }

    // Copying a directory should result in the original and a new copy existing on disk. TODO: continue here
    @Test
    void testCopy() {
        Directory root = Assertions.assertDoesNotThrow(() -> new Directory(notebookDirectory).load());
        Directory directory = Assertions.assertDoesNotThrow(() -> new Directory(directory2).load());
        Path copyDirectoryPath = Paths
                .get(
                        root.path().toString(),
                        directory.path().getFileName().toString().replace("_2A94M5J2D", "_copiedDirectory")
                );
        Directory copiedDirectory = Assertions.assertDoesNotThrow(() -> directory.copy(copyDirectoryPath));
        Assertions.assertDoesNotThrow(() -> copiedDirectory.save());

        // Both copied directory and the original directory (and their children) should exist
        Assertions
                .assertTrue(Files.exists(Paths.get(notebookDirectory.toString(), "my_second_folder_copiedDirectory")));
        Directory updatedCopyDirectory = Assertions.assertDoesNotThrow(() -> new Directory(copyDirectoryPath).load());
        Assertions.assertEquals(1, updatedCopyDirectory.children().size());
        Path childPath = copiedDirectory.children().values().stream().toList().get(0).path();
        // Assert that the newly created copy exists.
        Assertions.assertTrue(Files.exists(childPath));
        // Assert that the original directory still exists.
        Assertions.assertTrue(Files.exists(directory.path()));
        Assertions.assertTrue(Files.exists(notebook1));
    }

    // Calling json() should result in a valid JSON object.
    @Test
    void testJson() {
        Directory root = Assertions.assertDoesNotThrow(() -> new Directory(notebookDirectory).load());
        Assertions
                .assertEquals(
                        "{\"name\":\"notebooks\",\"children\":[\"" + notebook3.getFileName() + "\",\""
                                + directory1.getFileName() + "\",\"" + notebook4.getFileName() + "\"]}",
                        root.json().toString()
                );
    }

    // Creating and then saving a new directory containing a notebook should result in two new files on disk.
    @Test
    void testSave() {
        Path newDirectoryPath = Paths.get(notebookDirectory.toString(), "new_folder_newDirectoryId");
        Map<Path, Saveable> notebooks = new HashMap<>();
        Path newNotebookPath = Paths.get(newDirectoryPath.toString(), "newNotebook_newNotebook.zpln");
        Notebook newNotebook = new Notebook("title", newNotebookPath, new LinkedHashMap<>());
        notebooks.put(newNotebookPath, newNotebook);
        Directory newDirectory = new Directory(
                Paths.get(notebookDirectory.toString(), "newDirectory_newId"),
                notebooks
        );
        Assertions.assertDoesNotThrow(() -> newDirectory.save());

        // Files for both the directory and its children should exist after saving.
        Assertions.assertTrue(Files.exists(newDirectoryPath));
        Assertions.assertTrue(Files.exists(Paths.get(newDirectoryPath.toString(), "newNotebook_newNotebook.zpln")));
    }

    ///**
    // * Assert that when attempting to copy a directory into a path where a file already exists, the existing file is not
    // * overwritten, and an Exception is thrown.
    // */
    @Test
    void testCopyingToExistingPath() {
        Path sourcePath = Paths.get(notebookDirectory.toString(), "my_folder_2A94M5J1D");
        Path destinationPath = Paths.get(notebookDirectory.toString(), "my_note3_2A94M5J3Z.zpln");

        List<String> destinationContents = Assertions.assertDoesNotThrow(() -> Files.readAllLines(destinationPath));

        Directory sourceDirectory = new Directory(sourcePath);
        Assertions.assertThrows(FileAlreadyExistsException.class, () -> sourceDirectory.copy(destinationPath));

        List<String> destinationContentsAfterCopying = Assertions
                .assertDoesNotThrow(() -> Files.readAllLines(destinationPath));
        Assertions.assertEquals(destinationContents, destinationContentsAfterCopying);
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(Directory.class).verify();
    }
}
