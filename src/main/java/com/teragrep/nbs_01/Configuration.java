package com.teragrep.nbs_01;

import java.nio.file.Path;

public final class Configuration {
    private final Path notebookDirectory;
    private final int serverPort;

    public Configuration(Path notebookDirectory, int serverPort){
        this.notebookDirectory = notebookDirectory;
        this.serverPort = serverPort;
    }

    public int serverPort(){
        return serverPort;
    }

    public Path notebookDirectory() {
        return notebookDirectory;
    }
}
