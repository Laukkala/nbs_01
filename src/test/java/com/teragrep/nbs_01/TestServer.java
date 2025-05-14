package com.teragrep.nbs_01;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// A simple main class for configuring and starting one or more NotebookServer threads.
public class TestServer {
    private static final Path notebookSource = Paths.get("src/test/resources");
    private static final Path notebookDirectory = Paths.get("target/notebooks");

    public static void main(String[] args){
        try{
            setUp();
            Configuration configuration = new Configuration(Paths.get("target/notebooks"),8080);
            NotebookServer server = new NotebookServer(configuration);
            server.start();
            server.join();
        } catch (InterruptedException exception){

        }
    }

    public static void copyFileRecursively(File fileToCopy, File destination){
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
    public static void deleteFileRecursively(File fileToDelete){
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
    static void setUp() {
        deleteFileRecursively(notebookDirectory.toFile());
        copyFileRecursively(notebookSource.toFile(),notebookDirectory.toFile());
    }
}
