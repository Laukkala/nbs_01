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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

    public DirectoryTest() {
        deleteFileRecursively(notebookDirectory.toFile());
        copyFileRecursively(notebookSource.toFile(), notebookDirectory.toFile());
    }

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

    // Copying a directory should result in the original and a new copy existing on disk.
    @Test
    void testCopy() {

        Notebook testNotebook1 = new Notebook("testNotebook1");
        Notebook testNotebook2 = new Notebook("testNotebook2");
        List<FilesystemEntity> notebooks1 = new ArrayList<>();
        notebooks1.add(testNotebook1);
        notebooks1.add(testNotebook2);
        Directory subDirectory = new Directory("dir", notebooks1);

        List<FilesystemEntity> rootNotebooks = new ArrayList<>();
        Notebook testNotebook3 = new Notebook("testNotebook1");
        rootNotebooks.add(testNotebook3);
        rootNotebooks.add(subDirectory);
        Directory rootDir = new Directory("root", rootNotebooks);
        Path destinationDirectoryPath = notebookDirectory.resolve(Paths.get("destination"));

        Directory copiedDirectory = Assertions.assertDoesNotThrow(() -> rootDir.copy());

        // Both copied directory and the original directory (and their children) should exist
        copiedDirectory.equals(rootDir);
    }

    // Calling json() should result in a valid JSON object.
    @Test
    void testJson() {
        Notebook testNotebook1 = new Notebook(notebook1.getFileName().toString());
        Notebook testNotebook2 = new Notebook(notebook2.getFileName().toString());
        List<FilesystemEntity> notebooks1 = new ArrayList<>();
        notebooks1.add(testNotebook1);
        notebooks1.add(testNotebook2);
        Directory subDirectory = new Directory(directory1.getFileName().toString(), notebooks1);

        List<FilesystemEntity> rootNotebooks = new ArrayList<>();
        Notebook testNotebook3 = new Notebook(notebook3.getFileName().toString());
        rootNotebooks.add(testNotebook3);
        rootNotebooks.add(subDirectory);
        Directory rootDir = new Directory("root", rootNotebooks);

        Assertions
                .assertEquals(
                        "{\"name\":\"root\",\"children\":[\"" + notebook3.getFileName() + "\",\""
                                + directory1.getFileName() + "\"]}",
                        rootDir.json().toString()
                );
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(Directory.class).verify();
    }
}
