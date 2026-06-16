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
package com.teragrep.nbs_01.repository.storage;

import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.Paragraph;
import com.teragrep.nbs_01.repository.Script;
import com.teragrep.nbs_01.repository.identifiers.Identifier;
import com.teragrep.nbs_01.repository.identifiers.PathIdentifier;
import com.teragrep.nbs_01.repository.serialization.JsonNotebook;
import com.teragrep.nbs_01.repository.serialization.JsonParagraph;
import com.teragrep.nbs_01.repository.serialization.JsonScript;
import com.teragrep.nbs_01.repository.serialization.SerializedNotebook;
import com.teragrep.nbs_01.repository.serialization.SerializedParagraph;
import com.teragrep.nbs_01.repository.serialization.SerializedScript;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

//
public final class LocalFilesystemStorage implements Storage {

    private final Path root;
    private final Charset charset;

    public LocalFilesystemStorage(final Path root) {
        this(root, Charset.defaultCharset());
    }

    public LocalFilesystemStorage(final Path root, final Charset charset) {
        this.root = root;
        this.charset = charset;
    }

    public Identifier root() {
        return new PathIdentifier(root.toString());
    }

    @Override
    public void deleteDirectory(final Identifier identifier)
            throws NoSuchFileException, MalformedRequestException, IOException {
        final Path path = root.resolve(identifier.name());
        if (!Files.exists(path)) {
            throw new NoSuchFileException("No such file: " + root.relativize(path));
        }
        if (!Files.isDirectory(path)) {
            throw new MalformedRequestException(root.relativize(path) + " is not a Directory!");
        }
        delete(path);
    }

    @Override
    public void deleteFile(final Identifier identifier)
            throws NoSuchFileException, MalformedRequestException, IOException {
        final Path path = root.resolve(identifier.name());
        if (!Files.exists(path)) {
            throw new NoSuchFileException("No such file: " + root.relativize(path));
        }
        if (Files.isDirectory(path)) {
            throw new MalformedRequestException(root.relativize(path) + " is not a Notebook!");
        }
        delete(path);
    }

    @Override
    public void copyDirectory(final Identifier source, final Identifier destination)
            throws FileNotFoundException, FileAlreadyExistsException, IOException, MalformedRequestException {
        final List<Identifier> children = listFiles(new PathIdentifier(""));
        if (!children.contains(source)) {
            throw new FileNotFoundException("No such directory: " + source.name() + " !");
        }
        Files
                .walkFileTree(root.resolve(source.name()), new CopyFileVisitor(root.resolve(source.name()), root.resolve(destination.name())));
    }

    @Override
    public void writeDirectory(final Identifier identifier) throws FileAlreadyExistsException, IOException {
        final Path path = root.resolve(identifier.name());
        if (Files.exists(path)) {
            throw new FileAlreadyExistsException("Path at " + root.relativize(path) + " is already in use!");
        }
        Files.createDirectories(path);
    }

    @Override
    public String readFile(final Identifier identifier) throws FileNotFoundException, IOException {
        final Path path = root.resolve(identifier.name());
        if (!Files.exists(path) || Files.isDirectory(path)) {
            throw new FileNotFoundException("No such file: " + root.relativize(path));
        }
        return Files.readString(path);
    }

    @Override
    public String readDirectory(final Identifier identifier) throws IOException, MalformedRequestException {
        final Path path = root.resolve(identifier.name());
        if (!Files.exists(path)) {
            throw new FileNotFoundException("No such file: " + root.relativize(path));
        }
        if (!Files.isDirectory(path)) {
            throw new MalformedRequestException("File at path " + root.relativize(path) + " is not a directory!");
        }

        final ArrayList<String> fileNames = new ArrayList<>();
        final FileVisitor<Path> fileVisitor = new SimpleFileVisitor<>() {

            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                if (dir.equals(path)) {
                    return FileVisitResult.CONTINUE;
                }
                fileNames.add(dir.getFileName().toString());
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                fileNames.add(file.getFileName().toString());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                return super.visitFileFailed(file, exc);
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                return super.postVisitDirectory(dir, exc);
            }
        };

        Files.walkFileTree(path, fileVisitor);
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (final String fileName : fileNames) {
            arrayBuilder.add(fileName);
        }

        final JsonObject json = Json
                .createObjectBuilder()
                .add("title", identifier.displayName())
                .add("children", arrayBuilder.build())
                .build();
        return json.toString();
    }

    @Override
    public Notebook deserializeNotebook(final Identifier identifier) throws IOException {
        final JsonObject sourceJson = Json.createReader(new StringReader(readFile(identifier))).readObject();
        final SerializedNotebook serializedSource = new JsonNotebook(sourceJson);
        return new Notebook(serializedSource.title(), serializedSource.paragraphs());
    }

    @Override
    public SerializedNotebook serializeNotebook(final Notebook notebook) {
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("title", notebook.title());
        //compatibility fields//
        builder.add("config", Json.createObjectBuilder(new HashMap<>()).build());
        // end //
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (final Paragraph paragraph : notebook.paragraphs().values()) {
            final JsonObjectBuilder paragraphBuilder = Json.createObjectBuilder();
            paragraphBuilder.add("id", paragraph.id().name());
            paragraphBuilder.add("title", paragraph.title());
            final JsonObjectBuilder scriptBuilder = Json.createObjectBuilder();
            scriptBuilder.add("text", paragraph.script().text());
            paragraphBuilder.add("script", scriptBuilder.build());
            arrayBuilder.add(paragraphBuilder.build());
        }
        final JsonArray paragraphJsonArray = arrayBuilder.build();
        builder.add("paragraphs", paragraphJsonArray);
        final JsonObject json = builder.build();

        final JsonNotebook jsonNotebook = new JsonNotebook(json);
        return jsonNotebook;
    }

    @Override
    public SerializedParagraph serializeParagraph(final Paragraph paragraph) {
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("id", paragraph.id().name());
        builder.add("title", paragraph.title() != null ? paragraph.title() : "");
        final JsonObjectBuilder scriptBuilder = Json.createObjectBuilder();
        scriptBuilder.add("text", paragraph.script().text());
        builder.add("script", scriptBuilder.build());
        return new JsonParagraph(builder.build());
    }

    @Override
    public SerializedScript serializeScript(final Script script) {
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("text", script.text());
        return new JsonScript(builder.build());
    }

    @Override
    public void writeFile(final Identifier identifier, final String content)
            throws MalformedRequestException, IOException {
        final Path path = root.resolve(identifier.name());
        if (Files.exists(path) && Files.isDirectory(path)) {
            throw new MalformedRequestException("File at path: " + root.relativize(path) + " is a Directory!");
        }
        Files.write(path, content.getBytes(charset));
    }

    @Override
    public boolean exists(final Identifier identifier) {
        return Files.exists(root.resolve(identifier.name()));
    }

    @Override
    public List<Identifier> listFiles(final Identifier identifier)
            throws FileNotFoundException, MalformedRequestException, IOException {
        final Path path = root.resolve(identifier.name());
        if (!Files.exists(path)) {
            throw new FileNotFoundException("No such file: " + root.relativize(path));
        }
        if (!Files.isDirectory(path)) {
            throw new MalformedRequestException("File at path " + root.relativize(path) + " is not a directory!");
        }

        final ArrayList<Identifier> files = new ArrayList<>();
        final FileVisitor<Path> fileVisitor = new SimpleFileVisitor<>() {

            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                if (dir.equals(path)) {
                    return FileVisitResult.CONTINUE;
                }
                files.add(new PathIdentifier(root.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                files.add(new PathIdentifier(root.relativize(file)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                return super.visitFileFailed(file, exc);
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                return super.postVisitDirectory(dir, exc);
            }
        };
        Files.walkFileTree(path, fileVisitor);
        return files;
    }

    private void delete(final Path path) throws NoSuchFileException, IOException {
        final Stream<Path> files = Files.walk(path);
        files.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    private class CopyFileVisitor extends SimpleFileVisitor<Path> {

        private final Path sourcePath;
        private final Path destinationPath;

        public CopyFileVisitor(final Path sourcePath, final Path destinationPath) {
            this.sourcePath = sourcePath;
            this.destinationPath = destinationPath;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            final Path path = destinationPath.resolve(sourcePath.relativize(dir));
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            else {
                throw new FileAlreadyExistsException("Destination " + root.relativize(path) + " is already in use!");
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            final Path path = destinationPath.resolve(sourcePath.relativize(file));
            if (!Files.exists(path)) {
                Files.copy(file, path);
            }
            else {
                throw new FileAlreadyExistsException("Destination " + root.relativize(path) + " is already in use!");
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
            return super.visitFileFailed(file, exc);
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            return super.postVisitDirectory(dir, exc);
        }
    }
}
