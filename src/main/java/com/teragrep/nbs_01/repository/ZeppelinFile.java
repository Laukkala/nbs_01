package com.teragrep.nbs_01.repository;

import jakarta.json.JsonObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ZeppelinFile extends Stubable {

    void delete() throws IOException;
    ZeppelinFile findFile(String id) throws FileNotFoundException;
    ZeppelinFile findFile(Path path) throws FileNotFoundException;
    String id();
    Path path();
    void save() throws IOException;
    boolean isDirectory();
    ZeppelinFile copy(Path path, String id) throws IOException;
    Map<String,ZeppelinFile> children();
    void printTree();
    ZeppelinFile load() throws IOException;
    void move(Path path) throws IOException;
    void rename(String name) throws IOException;
    List<ZeppelinFile> listAllChildren();
    JsonObject json();
}
