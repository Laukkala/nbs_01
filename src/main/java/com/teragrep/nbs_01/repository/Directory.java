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
import jakarta.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

 // Represents a single Directory that can contain Filesystem objects.
 // Is identified by a Path, and corresponds to a directory file on the filesystem.
public final class Directory implements ZeppelinFile {

    private final Logger LOGGER = LoggerFactory.getLogger(Directory.class);
    private final Map<Path, ZeppelinFile> children;
    private final Path path;

    public Directory(Path path) {
        this(path, new HashMap<>());
    }

    public Directory(Path path, Map<Path, ZeppelinFile> children) {
        this.path = path;
        this.children = Collections.unmodifiableMap(children);
    }

    // Find a matching ZeppelinFile by Path
    public ZeppelinFile findFile(Path searchedPath) throws FileNotFoundException {
        if (path().equals(searchedPath)) {
            return this;
        }
        else {
            for (ZeppelinFile child : children.values()) {
                try {
                    return (child.findFile(searchedPath));
                }
                catch (FileNotFoundException exception) {
                    continue;
                }
            }
            throw new FileNotFoundException(
                    "Notebook or directory with path " + searchedPath.toString() + " not found!"
            );
        }
    }

    public Path path() {
        return path;
    }

    public boolean contains(Path searchedPath) {
        try {
            findFile(searchedPath);
            return true;
        }
        catch (IOException ioException) {
            return false;
        }
    }

    public void move(Path destinationPath) throws IOException {
        if (Files.exists(destinationPath)) {
            throw new IOException("Path at " + destinationPath + " is already in use!");
        }
        if (destinationPath.toAbsolutePath().startsWith(path().toAbsolutePath())) {
            if (destinationPath.toAbsolutePath().equals(path().toAbsolutePath())) {
                throw new IOException("Directory is already located in the given destination!");
            }
            throw new IOException("Cannot move a directory into one of its own children!");
        }
        Map<Path, ZeppelinFile> movedChildren = new HashMap<>();
        for (ZeppelinFile child : children.values()) {
            if (child.isStub()) {
                child = child.load();
            }
            ZeppelinFile movedChild = child.copy(destinationPath.resolve(child.path().getFileName()));
            movedChildren.put(movedChild.path(), movedChild);
        }

        Directory movedDirectory = new Directory(destinationPath, movedChildren);
        movedDirectory.save();
        delete();
    }

    public void move(Directory destinationDirectory) throws IOException {
        move(destinationDirectory.path.resolve(path().getFileName()));
    }

    public void delete() throws IOException {
        for (ZeppelinFile child : children.values()) {
            child.delete();
        }
        Files.delete(path());
    }

    public Directory copy(Path destinationPath) throws IOException {
        if (Files.exists(destinationPath)) {
            throw new FileAlreadyExistsException("Path at " + destinationPath + " is already in use!");
        }
        Map<Path, ZeppelinFile> copyChildren = new HashMap<>();
        for (ZeppelinFile child : children.values()) {
            if (child.isStub()) {
                child = child.load();
            }
            String childCopyFileName = child.path().getFileName().toString();
            Path copyChildPath = destinationPath.resolve(childCopyFileName);
            copyChildren.put(copyChildPath, child.copy(copyChildPath));
        }
        Directory copiedDirectory = new Directory(destinationPath, copyChildren);
        copiedDirectory.save();
        return copiedDirectory;
    }

    public Map<Path, ZeppelinFile> children() {
        return children;
    }

    public List<ZeppelinFile> listAllChildren() {
        ArrayList<ZeppelinFile> allChildren = new ArrayList<ZeppelinFile>();
        for (ZeppelinFile child : children.values()) {
            allChildren.add(child);
            allChildren.addAll(child.listAllChildren());
        }
        return allChildren;
    }

    public JsonObject json() {
        return Json
                .createObjectBuilder()
                .add("name", path.getFileName().toString())
                .add("children", children.keySet().stream().map((childPath) -> childPath.getFileName()).collect(Collectors.toList()).toString()).build();
    }

    public void save() throws IOException {
        if (!Files.exists(path())) {
            Files.createDirectory(path());
        }
        for (ZeppelinFile child : children.values()) {
            child.save();
        }
    }

    public void rename(String fileName) throws IOException {
        move(path.getParent().resolve(fileName));
    }

    public boolean isDirectory() {
        return true;
    }

    public void printTree() {
        LOGGER.debug("Dir, Path: {}", path());
        for (Map.Entry<Path, ZeppelinFile> child : children.entrySet()) {
            child.getValue().printTree();
        }
    }

    // Directories don't require any operation for lazy loading
    public Directory load() throws IOException {
        return this;
    }

    public boolean isStub() {
        return false;
    }

    // This method traverses the file tree recursively and depth first, and creates a Directory object with a complete map of child Directories and Notebooks.
    public Directory initializeDirectory(Path pathToVisit, Map<Path, ZeppelinFile> existingFiles) throws IOException {
        // Create a copy of existingFiles so that we don't make any direct edits to it.
        Map<Path, ZeppelinFile> directoryChildren = new HashMap<>();
        directoryChildren.putAll(existingFiles);
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
                // Here we create any child Directories by calling this function recursively.
                // First we will check if we already have the files of the child Directory in existingFiles, and pass them to the recursive call so that we don't do any unnecessary operations in later recursions.
                ConcurrentHashMap<Path, ZeppelinFile> childrenOfChildDirectory = new ConcurrentHashMap<>();
                if (directoryChildren.containsKey(dir)) {
                    childrenOfChildDirectory.putAll(directoryChildren.get(dir).children());
                }
                Directory childDirectory = initializeDirectory(dir, childrenOfChildDirectory);
                directoryChildren.put(childDirectory.path(), childDirectory);
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!directoryChildren.containsKey(file)) {
                    directoryChildren.put(file, new Notebook(file));
                }
                return FileVisitResult.CONTINUE;
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
        Directory root = new Directory(pathToVisit, directoryChildren);
        return root;
    }
}
