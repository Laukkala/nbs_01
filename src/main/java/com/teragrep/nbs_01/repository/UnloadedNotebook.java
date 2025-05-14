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
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an unloaded notebook. Can only report its name and ID. Other operatoins require you to load the notebook
 * from the Path.
 */
public final class UnloadedNotebook implements ZeppelinFile {

    private final String id;
    private final Path path;

    private static final Logger LOGGER = LoggerFactory.getLogger(UnloadedNotebook.class);
    private final Map<String, Paragraph> paragraphs;

    public UnloadedNotebook(String id, Path path) {
        this.id = id;
        this.path = path;
        this.paragraphs = new HashMap<>();
    }

    @Override
    public void delete() throws IOException {
        Files.delete(path());
    }

    @Override
    public ZeppelinFile findFile(String searchedId) throws FileNotFoundException {
        if (searchedId.equals(id())) {
            return this;
        }
        else {
            throw new FileNotFoundException("Searched id " + searchedId + " does not match with" + id());
        }
    }

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

    @Override
    public Path path() {
        return path;
    }

    public String title() {
        throw new UnsupportedOperationException("UnloadedNotebook can have no title!");
    }

    public Map<String, Paragraph> paragraphs() {
        throw new UnsupportedOperationException("UnloadedNotebook can have no paragraphs!");
    }

    public UnloadedNotebook runAll() throws Exception {
        throw new UnsupportedOperationException("Cannot run UnloadedNotebook!");
    }

    public JsonObject json() {
        throw new UnsupportedOperationException("Cannot turn UnloadedNotebook into JSON!");
    }

    @Override
    public UnloadedNotebook copy(Path destinationPath, String copyId) throws IOException {
        throw new UnsupportedOperationException("Cannot copy UnloadedNotebook!");
    }

    @Override
    public Map<String, ZeppelinFile> children() {
        return new HashMap<>();
    }

    @Override
    public void printTree() {
        throw new UnsupportedOperationException("Cannot print tree on a UnloadedNotebook!");
    }

    private String readFile() throws IOException {
        List<String> lines = Files.readAllLines(path(), Charset.defaultCharset());
        String concatenatedLines = lines.stream().map(n -> String.valueOf(n)).collect(Collectors.joining("\n"));
        return concatenatedLines;
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
    public void move(Path destinationPath) throws IOException {
        throw new UnsupportedOperationException("Cannot move an Unloaded Notebook!");
    }

    @Override
    public void rename(String name) throws IOException {
        throw new UnsupportedOperationException("Cannot rename an Unloaded Notebook!");
    }

    @Override
    public List<ZeppelinFile> listAllChildren() {
        return new ArrayList<>();
    }

    @Override
    public void save() throws IOException {
        throw new UnsupportedOperationException("Cannot save a UnloadedNotebook!");
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public boolean isStub() {
        return true;
    }

}
