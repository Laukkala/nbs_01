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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Directory implements ZeppelinFile {

    private final Map<String, ZeppelinFile> children;
    private final String id;
    private final Path path;

    public Directory(String id, Path path) {
        this(id, path, new HashMap<>());
    }

    public Directory(String id, Path path, Map<String, ZeppelinFile> children) {
        this.id = id;
        this.path = path;
        this.children = Collections.unmodifiableMap(children);
    }

    // Find a matching ZeppelinFile by ID
    public ZeppelinFile findFile(String id) throws FileNotFoundException {
        if (id().equals(id)) {
            return this;
        }
        else {
            for (ZeppelinFile child : children.values()) {
                try {
                    return (child.findFile(id));
                }
                catch (FileNotFoundException exception) {
                    continue;
                }
            }
            throw new FileNotFoundException("Notebook or directory " + id + " not found!");
        }
    }

    // Find a matching ZeppelinFile by Path
    public ZeppelinFile findFile(Path path) throws FileNotFoundException {
        if (path().equals(path)) {
            return this;
        }
        else {
            for (ZeppelinFile child : children.values()) {
                try {
                    return (child.findFile(path));
                }
                catch (FileNotFoundException exception) {
                    continue;
                }
            }
            throw new FileNotFoundException("Notebook or directory with path " + path.toString() + " not found!");
        }
    }

    public String id() {
        return id;
    }

    public Path path() {
        return path;
    }

    public boolean contains(String id) {
        try {
            findFile(id);
            return true;
        }
        catch (FileNotFoundException fileNotFoundException) {
            return false;
        }
    }

    public boolean contains(Path path) {
        try {
            findFile(path);
            return true;
        }
        catch (FileNotFoundException fileNotFoundException) {
            return false;
        }
    }

    public void move(Path destination) throws IOException {
        if (destination.toAbsolutePath().startsWith(path().toAbsolutePath())) {
            throw new IOException("Cannot move a directory into one of its own children!");
        }
        HashMap<String, ZeppelinFile> movedChildren = new HashMap<>();
        for (ZeppelinFile child : children.values()) {
            if (child.isStub()) {
                child = child.load();
            }
            ZeppelinFile movedChild = child
                    .copy(Paths.get(destination.toString(), child.path().getFileName().toString()), child.id());
            movedChildren.put(movedChild.id(), movedChild);
        }

        Directory movedDirectory = new Directory(id(), destination, movedChildren);
        movedDirectory.save();
        delete();
    }

    public void move(Directory destinationDirectory, String name) throws IOException {
        move(Paths.get(destinationDirectory.path().toString(), name));
    }

    public void delete() throws IOException {
        for (ZeppelinFile child : children.values()) {
            child.delete();
        }
        Files.delete(path());
    }

    public Directory copy(Path path, String id) throws IOException {
        HashMap<String, ZeppelinFile> copyChildren = new HashMap<>();
        for (ZeppelinFile child : children.values()) {
            if (child.isStub()) {
                child = child.load();
            }
            String copyId = UUID.randomUUID().toString();
            String childFileName = child.path().getFileName().toString();
            StringBuilder sb = new StringBuilder(childFileName);
            sb
                    .replace(childFileName.lastIndexOf("_") + 1, (childFileName.endsWith(".zpln") ? childFileName.lastIndexOf(".zpln") : childFileName.length()), copyId);
            Path copyChildPath = Paths.get(path.toString(), sb.toString());
            copyChildren.put(copyId, child.copy(copyChildPath, copyId));
        }
        Directory copiedDirectory = new Directory(id, path, copyChildren);
        copiedDirectory.save();
        return copiedDirectory;
    }

    public Map<String, ZeppelinFile> children() {
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
                .add("id", id)
                .add("name", path.getFileName().toString())
                .add("chidlren", children.keySet().toString())
                .build();
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
        move(Paths.get(path().getParent().toString(), fileName));
    }

    public boolean isDirectory() {
        return true;
    }

    public void printTree() {
        System.out.println("Dir; Id: " + id() + ", Path: " + path());
        for (Map.Entry<String, ZeppelinFile> child : children.entrySet()) {
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

    public Directory initializeDirectory(Path path, ConcurrentHashMap<String, ZeppelinFile> existingFiles)
            throws IOException {
        // Create a copy of existingFiles so that we don't make any direct edits to it.
        HashMap<String, ZeppelinFile> children = new HashMap<>();
        children.putAll(existingFiles);
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // walkFileTree visits the root directory it's called on.
                // This method operates on the given directory's children so we skip the processing of the root directory here.
                if (dir.equals(path)) {
                    return FileVisitResult.CONTINUE;
                }
                // Ignore .git directory. This could be moved somewhere else.
                if (dir.startsWith(path + "/.git")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                // Read an ID from the file name
                String directoryId = extractIDFromFileName(dir);
                try {
                    // Here we create any child Directories by calling this function recursively.
                    // First we will check if we already have the files of the child Directory in existingFiles, and pass them to the recursive call so that we don't do any unnecessary operations in later recursions.
                    ConcurrentHashMap<String, ZeppelinFile> subtreeChildren = new ConcurrentHashMap<>();
                    if (children.containsKey(directoryId)) {
                        subtreeChildren.putAll(children.get(directoryId).children());
                    }
                    Directory subtree = initializeDirectory(dir, subtreeChildren);
                    children.put(subtree.id(), subtree);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String notebookId = extractIDFromFileName(file);
                if (!children.containsKey(notebookId)) {
                    children.put(notebookId, new UnloadedNotebook(notebookId, file));
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
        Directory root = new Directory(extractIDFromFileName(path), path, children);
        return root;
    }

    private static String extractIDFromFileName(Path file) {
        return extractIDFromFileName(file, "_");
    }

    private static String extractIDFromFileName(Path file, String delimiter) {
        String fileName = file.getFileName().toString();
        if (fileName.contains(delimiter)) {
            int idStartIndex = fileName.lastIndexOf(delimiter);
            if (fileName.endsWith(".zpln")) {
                int idEndIndex = fileName.lastIndexOf(".zpln");
                return fileName.substring(idStartIndex + 1, idEndIndex);
            }
            return fileName.substring(idStartIndex + 1);
        }
        // If filename doesn't conform to naming conventions, return the filename iteself.
        else {
            return fileName;
        }
    }
}
