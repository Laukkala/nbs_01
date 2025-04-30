package com.teragrep.nbs_01.repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ZeppelinFile extends Stubable {

    public abstract void delete() throws IOException;
    public abstract ZeppelinFile findFile(String id) throws FileNotFoundException;
    public abstract ZeppelinFile findFile(Path path) throws FileNotFoundException;
    public String id();
    public Path path();
    public void save() throws IOException;
    public  boolean isDirectory();
    public abstract ZeppelinFile copy(Path path, String id) throws IOException;
    public abstract Map<String,ZeppelinFile> children();
    public abstract void printTree();
    public abstract String readFile() throws IOException;
    public abstract ZeppelinFile load() throws IOException;
    public abstract void move(Path path) throws IOException;
    public abstract void rename(String name) throws IOException;
    public abstract List<ZeppelinFile> listAllChildren();
}
