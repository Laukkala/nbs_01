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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class LocalFilesystemStorageTest {

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

    public LocalFilesystemStorageTest() {
        deleteFileRecursively(notebookDirectory.toFile());
        copyFileRecursively(notebookSource.toFile(), notebookDirectory.toFile());
    }

    private void deleteFileRecursively(File fileToDelete) {
        File[] children = fileToDelete.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteFileRecursively(child);
            }
        }
        fileToDelete.delete();
    }

    private void copyFileRecursively(File fileToCopy, File destination) {
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

    @Test
    void move() {
        LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory);
        Path destinationPath = notebookDirectory.resolve("testPath");
        Assertions.assertTrue(Files.exists(directory1));
        Assertions.assertTrue(Files.exists(directory2));
        Assertions.assertTrue(Files.exists(notebook1));
        Assertions.assertTrue(Files.exists(notebook2));
        Assertions
                .assertDoesNotThrow(() -> root.move(new PathIdentifier(notebookDirectory.relativize(directory1).toString()), new PathIdentifier(notebookDirectory.relativize(destinationPath).toString())));
        Assertions.assertTrue(Files.exists(destinationPath));
        Assertions.assertTrue(Files.exists(destinationPath.resolve("my_second_folder_2A94M5J2D")));
        Assertions
                .assertTrue(Files.exists(destinationPath.resolve("my_second_folder_2A94M5J2D").resolve("my_note1_2A94M5J1Z.zpln")));
        Assertions.assertTrue(Files.exists(destinationPath.resolve("my_note2_2A94M5J2Z.zpln")));

        // Source files should not exist
        Assertions.assertFalse(Files.exists(directory1));
        Assertions.assertFalse(Files.exists(directory2));
        Assertions.assertFalse(Files.exists(notebook1));
        Assertions.assertFalse(Files.exists(notebook2));
    }

    @Test
    void deleteDirectory() {
        LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory);
        Assertions.assertTrue(Files.exists(directory1));
        Assertions.assertTrue(Files.exists(directory2));
        Assertions.assertTrue(Files.exists(notebook1));
        Assertions.assertTrue(Files.exists(notebook2));

        Assertions
                .assertDoesNotThrow(
                        () -> root.deleteDirectory(new PathIdentifier(notebookDirectory.relativize(directory1).toString()))
                );

        Assertions.assertFalse(Files.exists(directory1));
        Assertions.assertFalse(Files.exists(directory2));
        Assertions.assertFalse(Files.exists(notebook1));
        Assertions.assertFalse(Files.exists(notebook2));

    }

    @Test
    void deleteNotebook() {
        LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory);
        Assertions.assertTrue(Files.exists(notebook1));
        Assertions
                .assertDoesNotThrow(() -> root.deleteNotebook(new PathIdentifier(notebookDirectory.relativize(notebook1).toString())));
        Assertions.assertFalse(Files.exists(notebook1));
    }

    @Test
    void copy() {
        LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory);
        Path destinationPath = notebookDirectory.resolve("testPath");
        Assertions.assertTrue(Files.exists(directory1));
        Assertions.assertTrue(Files.exists(directory2));
        Assertions.assertTrue(Files.exists(notebook1));
        Assertions.assertTrue(Files.exists(notebook2));
        Assertions
                .assertDoesNotThrow(() -> root.copy(new PathIdentifier(notebookDirectory.relativize(directory1).toString()), new PathIdentifier(notebookDirectory.relativize(destinationPath).toString())));
        Assertions.assertTrue(Files.exists(destinationPath));
        Assertions.assertTrue(Files.exists(destinationPath.resolve("my_second_folder_2A94M5J2D")));
        Assertions
                .assertTrue(Files.exists(destinationPath.resolve("my_second_folder_2A94M5J2D").resolve("my_note1_2A94M5J1Z.zpln")));
        Assertions.assertTrue(Files.exists(destinationPath.resolve("my_note2_2A94M5J2Z.zpln")));

        // Source notebooks should continue to exist.
        Assertions.assertTrue(Files.exists(directory1));
        Assertions.assertTrue(Files.exists(directory2));
        Assertions.assertTrue(Files.exists(notebook1));
        Assertions.assertTrue(Files.exists(notebook2));
    }

    @Test
    void createDirectory() {
        LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory);
        Path testDirectory = notebookDirectory.resolve(Paths.get("testDirectory"));
        Assertions
                .assertDoesNotThrow(
                        () -> root.createDirectory(new PathIdentifier(notebookDirectory.relativize(testDirectory).toString()))
                );
        Assertions.assertTrue(Files.exists(testDirectory));
    }

    @Test
    void read() {
        LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory);
        String notebook1Content = Assertions
                .assertDoesNotThrow(() -> root.read(new PathIdentifier(notebookDirectory.relativize(notebook1).toString())));
        Assertions.assertEquals(Assertions.assertDoesNotThrow(() -> Files.readString(notebook1)), notebook1Content);
    }

    @Test
    void write() {
        LocalFilesystemStorage root = new LocalFilesystemStorage(notebookDirectory);
        Path notebookPath = Paths.get(notebookDirectory.toString(), "createdNotebook_newNotebookId");
        Notebook notebook = new Notebook("title");

        Assertions.assertFalse(Files.exists(notebookPath));
        Assertions
                .assertDoesNotThrow(() -> root.write(new PathIdentifier(notebookDirectory.relativize(notebookPath).toString()), notebook.json().toString()));
        Assertions.assertTrue(Files.exists(notebookPath));
    }

    @Test
    void testImmediateChildren() {
        LocalFilesystemStorage root = Assertions
                .assertDoesNotThrow(() -> new LocalFilesystemStorage(notebookDirectory));
        List<Identifier> rootChildren = Assertions
                .assertDoesNotThrow(() -> root.immediateChildren(new PathIdentifier("")));
        List<Identifier> directory1Children = Assertions
                .assertDoesNotThrow(
                        () -> root.immediateChildren(new PathIdentifier(notebookDirectory.relativize(directory1).toString()))
                );
        List<Identifier> directory2Children = Assertions
                .assertDoesNotThrow(
                        () -> root.immediateChildren(new PathIdentifier(notebookDirectory.relativize(directory2).toString()))
                );

        Assertions.assertDoesNotThrow(() -> Assertions.assertEquals(4, rootChildren.size()));
        Assertions.assertDoesNotThrow(() -> Assertions.assertEquals(2, directory1Children.size()));
        Assertions.assertDoesNotThrow(() -> Assertions.assertEquals(1, directory2Children.size()));

        Assertions.assertTrue(rootChildren.contains(new PathIdentifier(notebook3.toString())));
        Assertions.assertTrue(rootChildren.contains(new PathIdentifier(notebook4.toString())));
        Assertions.assertTrue(rootChildren.contains(new PathIdentifier(directory1.toString())));

        Assertions.assertEquals(2, directory1Children.size());
        Assertions.assertTrue(directory1Children.contains(new PathIdentifier(notebook2.toString())));
        Assertions.assertTrue(directory1Children.contains(new PathIdentifier(directory2.toString())));

        Assertions.assertEquals(1, directory2Children.size());
        Assertions.assertTrue(directory2Children.contains(new PathIdentifier(notebook1.toString())));
    }

    @Test
    void testChildren() {
        LocalFilesystemStorage root = Assertions
                .assertDoesNotThrow(() -> new LocalFilesystemStorage(notebookDirectory));
        List<Identifier> allChildren = Assertions.assertDoesNotThrow(() -> root.children(new PathIdentifier("")));
        Assertions.assertEquals(7, allChildren.size());
    }
}
