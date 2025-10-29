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

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

// Represents a single Directory that can contain Filesystem objects.
// Is identified by a Path, and corresponds to a directory file on the filesystem.
public final class Directory implements Saveable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Directory.class);
    private final Map<Path, Saveable> children;
    private final Path path;

    public Directory(Path path) {
        this(path, new HashMap<>());
    }

    public Directory(Path path, Map<Path, Saveable> children) {
        this.path = path;
        this.children = children;
    }

    public Path path() {
        return path;
    }

    public Directory copy(Path destinationPath) throws IOException {
        if (Files.exists(destinationPath)) {
            throw new FileAlreadyExistsException("File at " + destinationPath + " already exists!");
        }
        Map<Path, Saveable> copiedChildren = new HashMap<>();
        for (Saveable child : children.values()) {
            Saveable copy = child.copy(destinationPath.resolve(child.path().getFileName()));
            copiedChildren.put(copy.path(), copy);
        }
        return new Directory(destinationPath, copiedChildren);
    }

    public JsonObject json() {
        JsonArrayBuilder childArray = Json.createArrayBuilder();
        for (Saveable child : children().values()) {
            childArray.add(child.path().getFileName().toString());
        }
        return Json
                .createObjectBuilder()
                .add("name", path.getFileName().toString())
                .add("children", childArray)
                .build();
    }

    public void save() throws IOException {
        if (!Files.exists(path())) {
            Files.createDirectory(path());
        }
        for (Saveable child : children.values()) {
            child.save();
        }
    }

    public Map<Path, Saveable> children() {
        return children;
    }

    public Directory load() throws IOException {
        return load(path);
    }

    // This method traverses the file tree recursively and depth first, and creates a Directory object with a complete map of child Directories and Notebooks.
    private Directory load(Path pathToVisit) throws IOException {
        Map<Path, Saveable> currentChildren = new HashMap<>();
        Files.walkFileTree(pathToVisit, new SimpleFileVisitor<Path>() {

            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // walkFileTree visits the root directory it's called on.
                // This method operates on the given directory's children so we skip the processing of the root directory here.
                if (dir.equals(pathToVisit)) {
                    return FileVisitResult.CONTINUE;
                }
                // Ignore .git directory. This could be moved somewhere else.
                if (dir.startsWith(pathToVisit + "/.git")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                Directory childDirectory = new Directory(dir).load();
                currentChildren.put(childDirectory.path(), childDirectory);
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    Notebook childNotebook = new Notebook(file).load();
                    currentChildren.put(childNotebook.path(), childNotebook);
                    return FileVisitResult.CONTINUE;
                }
                catch (JsonParsingException jsonParsingException) {
                    LOGGER.warn("Encountered a corrupted file: ", file);
                    return FileVisitResult.CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return super.visitFileFailed(file, exc);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return super.postVisitDirectory(dir, exc);
            }
        });
        Directory loadedDirectory = new Directory(pathToVisit, currentChildren);
        return loadedDirectory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Directory directory = (Directory) o;
        return Objects.equals(children, directory.children) && Objects.equals(path, directory.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, path);
    }
}
