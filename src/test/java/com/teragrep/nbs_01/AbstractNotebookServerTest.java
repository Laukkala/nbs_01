package com.teragrep.nbs_01;

import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AbstractNotebookServerTest {
    private final int serverPort = 8080;
    private final String serverAddress = "localhost:"+serverPort;
    private final Path notebookResources = Paths.get("src/test/resources");
    private final Path notebookDirectory = Paths.get("target/notebooks");
    private final Configuration testConfiguration = new Configuration(notebookDirectory,serverPort);
    private final NotebookServer server = new NotebookServer(testConfiguration);
    public final int webSocketTimeoutMs = 1000;
    public void startServer(){
        if(server.getState() == Thread.State.NEW){
            try {
                server.start();
                Thread.sleep(1000);
            }
            catch (InterruptedException interruptedException){
                throw new RuntimeException(interruptedException);
            }
        }
    }

    public void stopServer() throws InterruptedException {
        server.join();
    }

    public Path notebookDirectory(){
        return notebookDirectory;
    }
    public Path notebookResources(){
        return notebookResources;
    }
    public String serverAddress(){
        return serverAddress;
    }
    public void copyFileRecursively(File fileToCopy, File destination){
        Assertions.assertDoesNotThrow(()->{
            if(fileToCopy.isDirectory()){
                File[] children = fileToCopy.listFiles();
                for(File child : children){
                    copyFileRecursively(child,Paths.get(destination.toString(),child.getName()).toFile());
                }
            }
            if(!destination.exists()){
                File parent = destination.getParentFile();
                if(!parent.exists()){
                    parent.mkdirs();
                }
                Files.copy(fileToCopy.toPath(),destination.toPath());
            }
        });
    }
    public void deleteFileRecursively(File fileToDelete){
        Assertions.assertDoesNotThrow(()->{
            File[] children = fileToDelete.listFiles();
            if(children != null){
                for(File child : children){
                    deleteFileRecursively(child);
                }
            }
            fileToDelete.delete();
        });
    }
}
