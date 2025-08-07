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

import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
        Directory root = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        Assertions.assertEquals(4, root.children().size());
        Assertions.assertEquals(2, root.children().get(directory1).children().size());
        Assertions.assertEquals(1, root.children().get(directory1).children().get(directory2).children().size());
    }

    // List of all children should contain an ID for every directory and file.
    @Test
    void testListAllChildren() {
        Directory root = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        List<Path> paths = root.listAllChildren().stream().map(zeppelinFile -> {
            return zeppelinFile.path();
        }).collect(Collectors.toList());

        Assertions.assertEquals(7, paths.size());
        Assertions.assertTrue(paths.contains(notebook1));
        Assertions.assertTrue(paths.contains(notebook2));
        Assertions.assertTrue(paths.contains(notebook3));
        Assertions.assertTrue(paths.contains(notebook4));
        Assertions.assertTrue(paths.contains(directory1));
        Assertions.assertTrue(paths.contains(directory2));
        Assertions.assertTrue(paths.contains(junkfile));
    }

    // Searching in directories should result in a notebook or a directory object being returned with a valid ID.
    @Test
    void testFindFile() {
        Directory root = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        ZeppelinFile file = Assertions.assertDoesNotThrow(() -> root.findFile(notebook4));
        Assertions.assertFalse(file.isDirectory());
        Assertions.assertEquals(file.path(), Paths.get(notebookDirectory.toString(), "my_note4_2A94M5J4Z.zpln"));

        ZeppelinFile file2 = Assertions.assertDoesNotThrow(() -> root.findFile(directory1));
        Assertions.assertTrue(file2.isDirectory());
        Assertions.assertEquals(file2.path(), Paths.get(notebookDirectory.toString(), "my_folder_2A94M5J1D"));
    }

    // Calling contains with a valid ID should return true, and false when called with an ID that doesn't exist
    @Test
    void testContains() {
        Directory root = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        Assertions.assertTrue(root.contains(notebook1));
        Assertions.assertFalse(root.contains(Paths.get("NonexistentPath")));
    }

    // Moving a directory should result in the directory being moved to the correct path along with its children.
    @Test
    void testMoveToPath() {
        Directory roote = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        Directory directorye = (Directory) Assertions.assertDoesNotThrow(() -> roote.findFile(directory2));
        Path destinationPath = Paths.get(roote.path().toString(), directorye.path().getFileName().toString());
        Assertions.assertDoesNotThrow(() -> directorye.move(destinationPath));
        // Re-initialize directory as we have made modifications.
        Directory updatedroot = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        Directory updatedDirectory = (Directory) Assertions
                .assertDoesNotThrow(() -> updatedroot.findFile(destinationPath));
        Assertions
                .assertEquals(Paths.get(notebookDirectory.toString(), "my_second_folder_2A94M5J2D").toString(), updatedDirectory.path().toString());

        // Assert that the children of the moved directory were moved as well.
        Path subfilePath = destinationPath.resolve(Paths.get("my_note1_2A94M5J1Z.zpln"));
        ZeppelinFile subFile = Assertions.assertDoesNotThrow(() -> updatedDirectory.findFile(subfilePath));
        Assertions
                .assertEquals(
                        Paths.get(updatedroot.path().toString(), "my_second_folder_2A94M5J2D", "my_note1_2A94M5J1Z.zpln"), subFile.path()
                );
    }

    @Test
    void testMoveToDirectory() {
        Directory root = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        Directory directory = (Directory) Assertions.assertDoesNotThrow(() -> root.findFile(directory2));
        Directory parentDirectory = root;
        Assertions.assertDoesNotThrow(() -> directory.move(parentDirectory));

        // Re-initialize directory as we have made modifications.
        Directory updatedRoot = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        Directory updatedDirectory = (Directory) Assertions
                .assertDoesNotThrow(() -> updatedRoot.findFile(Paths.get("target/notebooks/my_second_folder_2A94M5J2D")));
        Assertions
                .assertEquals(Paths.get(parentDirectory.path().toString(), "my_second_folder_2A94M5J2D").toString(), updatedDirectory.path().toString());

        // Assert that the children of the moved directory were moved as well.
        ZeppelinFile subFile = Assertions
                .assertDoesNotThrow(
                        () -> updatedDirectory
                                .findFile(
                                        Paths.get("target/notebooks/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln")
                                )
                );
        Assertions
                .assertEquals(
                        Paths
                                .get(
                                        parentDirectory.path().toString(), "my_second_folder_2A94M5J2D",
                                        "my_note1_2A94M5J1Z.zpln"
                                ),
                        subFile.path()
                );
    }

    // Deleting a Directory should result in the deletion of the directory file as well as all of its children from disk.
    @Test
    void testDelete() {
        Directory root = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        Directory directory = (Directory) Assertions.assertDoesNotThrow(() -> root.findFile(directory2));
        ZeppelinFile child = directory.children().get(notebook1);

        // Verify that the file we are about to delete exists.
        Assertions.assertTrue(Files.exists(directory.path()));
        Assertions.assertDoesNotThrow(() -> directory.delete());

        // Assert that the directory we deleted (and its children) no longer exists.
        Assertions.assertFalse(Files.exists(directory.path()));
        Assertions.assertFalse(Files.exists(child.path()));
    }

    // Copying a directory should result in the original and a new copy existing on disk.
    @Test
    void testCopy() {
        Directory root = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        Directory directory = (Directory) Assertions.assertDoesNotThrow(() -> root.findFile(directory2));
        Path copyDirectoryPath = Paths
                .get(
                        root.path().toString(),
                        directory.path().getFileName().toString().replace("_2A94M5J2D", "_copiedDirectory")
                );
        Assertions.assertDoesNotThrow(() -> directory.copy(copyDirectoryPath));
        // Re-initialize directory as we have made modifications.
        Directory updatedRoot = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));

        // Both copied directory and the original directory (and their children) should exist
        Assertions
                .assertTrue(Files.exists(Paths.get(notebookDirectory.toString(), "my_second_folder_copiedDirectory")));
        ZeppelinFile copiedDirectory = Assertions.assertDoesNotThrow(() -> updatedRoot.findFile(copyDirectoryPath));
        Assertions.assertEquals(1, copiedDirectory.listAllChildren().size());
        Path childPath = copiedDirectory.listAllChildren().get(0).path();
        // Assert that the newly created copy exists.
        Assertions.assertTrue(Files.exists(childPath));
        // Assert that the original directory still exists.
        Assertions.assertTrue(Files.exists(directory.path()));
        Assertions.assertTrue(Files.exists(notebook1));
    }

    // Renaming a directory should result in the file being renamed.
    @Test
    void testRename() {
        Directory root = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        Directory directory = (Directory) Assertions.assertDoesNotThrow(() -> root.findFile(directory2));
        Assertions.assertDoesNotThrow(() -> directory.rename("renamedDirectory_2A94M5J2D"));
        // Re-initialize directory as we have made modifications.
        Directory updatedRoot = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        Directory updatedDirectory = (Directory) Assertions
                .assertDoesNotThrow(
                        () -> updatedRoot
                                .findFile(Paths.get("target/notebooks/my_folder_2A94M5J1D/renamedDirectory_2A94M5J2D"))
                );

        // Assert that no file with the original name exists, and that the renamed file exists.
        Assertions.assertFalse(Files.exists(directory.path()));
        Assertions.assertTrue(Files.exists(updatedDirectory.path()));
    }

    // Calling json() should result in a valid JSON object.
    @Test
    void testJson() {
        Directory root = Assertions
                .assertDoesNotThrow(() -> new Directory(notebookDirectory).initializeDirectory(notebookDirectory, new ConcurrentHashMap<>()));
        Assertions
                .assertEquals(
                        "{\"name\":\"notebooks\",\"children\":\"[" + notebook3.getFileName() + ", "
                                + junkfile.getFileName() + ", " + directory1.getFileName() + ", "
                                + notebook4.getFileName() + "]\"}",
                        root.json().toString()
                );
    }

    // Creating and then saving a new directory containing a notebook should result in two new files on disk.
    @Test
    void testSave() {
        Path newDirectoryPath = Paths.get(notebookDirectory.toString(), "new_folder_newDirectoryId");
        Map<Path, ZeppelinFile> notebooks = new HashMap<>();
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

    /**
     * Assert that when attempting to copy a directory into a path where a file already exists, the existing file is not
     * overwritten, and an Exception is thrown.
     */
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
}
