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

import jakarta.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a single Notebook within Zeppelin
 */
public final class Notebook implements ZeppelinFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(Notebook.class);
    private final Map<String, Paragraph> paragraphs;
    private final String title;
    private final String id;
    private final Path path;

    public Notebook(String title, String id, Path path, Map<String, Paragraph> paragraphs) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.paragraphs = paragraphs;
    }

    // Remove file from disk
    @Override
    public void delete() throws IOException {
        Files.delete(path());
    }

    // Checks if searched ID matches with this Notebooks ID.
    @Override
    public ZeppelinFile findFile(String searchedId) throws FileNotFoundException {
        if (id().equals(searchedId)) {
            return this;
        }
        else {
            throw new FileNotFoundException("Searched id " + searchedId + " does not match with" + id());
        }
    }

    // Checks if searched path matches with this Notebooks path.
    @Override
    public ZeppelinFile findFile(Path searchedPath) throws FileNotFoundException {
        if (path().equals(searchedPath)) {
            return this;
        }
        else {
            throw new FileNotFoundException("Searched path " + searchedPath + " does not match with" + path());
        }
    }

    public String id() {
        return id;
    }

    public Path path() {
        return path;
    }

    public String title() {
        return title;
    }

    public Map<String, Paragraph> paragraphs() {
        return paragraphs;
    }

    public JsonObject json() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("id", id());
        builder.add("name", title);
        //compatibility fields//
        builder.add("config", Json.createObjectBuilder(new HashMap<>()).build());
        // end //
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (Paragraph paragraph : paragraphs.values()) {
            arrayBuilder.add(paragraph.json());
        }
        JsonArray paragraphJsonArray = arrayBuilder.build();
        builder.add("paragraphs", paragraphJsonArray);
        return builder.build();
    }

    public Notebook copy(Path destinationPath, String copyId) throws IOException {
        return copy(title, destinationPath, copyId);
    }

    @Override
    public Map<String, ZeppelinFile> children() {
        return new HashMap<>();
    }

    @Override
    public void printTree() {
        System.out.println("File ID: " + id() + ", Path: " + path());
        LOGGER.debug("File ID: {}, Path: {}", id(), path());
    }

    @Override
    public Notebook load() throws IOException {
        String content = readFile().toString();
        StringReader stringReader = new StringReader(content);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        stringReader.close();

        String savedName = object.getString("name");
        String savedId = object.getString("id");
        JsonArray paragraphJsonArray = object.getJsonArray("paragraphs");
        Map<String, Paragraph> savedParagraphs = new LinkedHashMap<>();
        for (JsonObject paragraphJson : paragraphJsonArray.getValuesAs(JsonObject.class)) {
            NullParagraph nullParagraph = new NullParagraph();
            Paragraph paragraph = nullParagraph.fromJson(paragraphJson);
            savedParagraphs.put(paragraph.id(), paragraph);
        }
        return new Notebook(savedName, savedId, path(), savedParagraphs);
    }

    @Override
    public List<ZeppelinFile> listAllChildren() {
        return new ArrayList<>();
    }

    public Notebook copy(String copyTitle, Path destinationPath, String copyId) throws IOException {
        Map<String, Paragraph> copyParagraphs = new LinkedHashMap<String, Paragraph>();
        for (Paragraph paragraph : paragraphs.values()) {
            String copyParagraphId = UUID.randomUUID().toString();
            Script copyScript = new Script(paragraph.script().text());
            Paragraph copyParagraph = new Paragraph(copyParagraphId, paragraph.title(), copyScript);
            copyParagraphs.put(copyParagraph.id(), copyParagraph);
        }
        Notebook copyNotebook = new Notebook(copyTitle, copyId, destinationPath, copyParagraphs);
        copyNotebook.save();
        return copyNotebook;
    }

    public void save() throws IOException {
        try {
            StringWriter stringWriter = new StringWriter();
            Files.createDirectories(path().getParent());
            Files.write(path(), json().toString().getBytes());
            stringWriter.close();
        }
        catch (IOException exception) {
            throw new IOException("Failed to save notebook to path" + path() + "!", exception);
        }
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public void rename(String fileName) throws IOException {
        move(Paths.get(path().getParent().toString(), fileName));
    }

    public void move(Path destinationPath) throws IOException {
        Notebook movedNotebook = copy(destinationPath, id());
        movedNotebook.save();
        delete();
    }

    public Notebook move(Directory parentDirectory) throws IOException {
        return move(parentDirectory, path().getFileName().toString());
    }

    public Notebook move(Directory parentDirectory, String fileName) throws IOException {
        Notebook movedNotebook = copy(Paths.get(parentDirectory.path().toString(), fileName), id());
        movedNotebook.save();
        delete();
        return movedNotebook;
    }

    public boolean isStub() {
        return false;
    }

    private String readFile() throws IOException {
        List<String> lines = Files.readAllLines(path(), Charset.defaultCharset());
        String concatenatedLines = lines.stream().map(n -> String.valueOf(n)).collect(Collectors.joining("\n"));
        return concatenatedLines;
    }
}
