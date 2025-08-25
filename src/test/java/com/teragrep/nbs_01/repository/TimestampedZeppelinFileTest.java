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

import static org.junit.jupiter.api.Assertions.*;

class TimestampedZeppelinFileTest {

    private final Path notebookSource = Paths.get("src/test/resources");
    private final Path notebookDirectory = Paths.get("target/notebooks");
    private final Path notebook1 = Paths
            .get("target/notebooks/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");

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

    @Test
    void testLastModified() {
        Assertions.assertDoesNotThrow(() -> {
            TimestampedZeppelinFile timestampedFile = new TimestampedZeppelinFile(new Notebook(notebook1));
            // Creating a new TimestampedZeppelinFile should use the latest edit timestamp.
            Assertions.assertEquals(timestampedFile.lastModified(), notebook1.toFile().lastModified());

            Files.write(notebook1, "Overwrote some text".getBytes());
            // Timestamp should not be updated when the file is edited.
            Assertions.assertNotEquals(timestampedFile.lastModified(), notebook1.toFile().lastModified());

            TimestampedZeppelinFile newTimestampedFile = new TimestampedZeppelinFile(new Notebook(notebook1));
            // Creating a new TimestampedZeppelinFile should use the latest edit timestamp.
            Assertions.assertEquals(newTimestampedFile.lastModified(), notebook1.toFile().lastModified());
        });
    }

    @Test
    public void testEquals() {
        Assertions.assertDoesNotThrow(() -> {
            TimestampedZeppelinFile timestampedFile = new TimestampedZeppelinFile(new Notebook(notebook1));
            // Creating a new TimestampedZeppelinFile should use the latest edit timestamp.
            Assertions.assertEquals(timestampedFile.lastModified(), notebook1.toFile().lastModified());

            Files.write(notebook1, "Overwrote some text".getBytes());
            // Timestamp should not be updated when the file is edited.
            Assertions.assertNotEquals(timestampedFile.lastModified(), notebook1.toFile().lastModified());

            TimestampedZeppelinFile newTimestampedFile = new TimestampedZeppelinFile(new Notebook(notebook1));
            // Creating a new TimestampedZeppelinFile should use the latest edit timestamp.
            Assertions.assertEquals(newTimestampedFile.lastModified(), notebook1.toFile().lastModified());
            Assertions.assertFalse(timestampedFile.equals(newTimestampedFile));
        });
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(TimestampedZeppelinFile.class).verify();
    }
}
