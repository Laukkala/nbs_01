package com.teragrep.nbs_01.repository;

import org.junit.jupiter.api.*;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class DirectoryTest {
    private final Path notebookSource = Paths.get("src/test/resources");
    private final Path notebookDirectory = Paths.get("target/notebooks");

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

    @BeforeEach
    void setUp() {
        deleteFileRecursively(notebookDirectory.toFile());
        copyFileRecursively(notebookSource.toFile(),notebookDirectory.toFile());
    }

    @AfterEach
    void tearDown() {
        deleteFileRecursively(notebookDirectory.toFile());
    }

    // Directory should contain a child for each file and directory within notebookDirectory after initialization.
    @Test
    void testInitializeDirectory() {
        Assertions.assertDoesNotThrow(()->{
            Directory root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());
            Assertions.assertEquals(4,root.children().size());
            Assertions.assertEquals(2,root.children().get("2A94M5J1D").children().size());
            Assertions.assertEquals(1,root.children().get("2A94M5J1D").children().get("2A94M5J2D").children().size());
        });
    }

    // List of all children should contain an ID for every directory and file.
    @Test
    void testListAllChildren() {
        Assertions.assertDoesNotThrow(()->{
            Directory root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());
            List<String> ids = root.listAllChildren().stream().map(zeppelinFile -> {
                return zeppelinFile.id();
            }).collect(Collectors.toList());

            Assertions.assertEquals(7,ids.size());
            Assertions.assertTrue(ids.contains("2A94M5J1Z"));
            Assertions.assertTrue(ids.contains("2A94M5J2Z"));
            Assertions.assertTrue(ids.contains("2A94M5J3Z"));
            Assertions.assertTrue(ids.contains("2A94M5J4Z"));
            Assertions.assertTrue(ids.contains("2A94M5J1D"));
            Assertions.assertTrue(ids.contains("2A94M5J2D"));
            Assertions.assertTrue(ids.contains("junkfile"));
        });
    }

    // Searching in directories should result in a notebook or a directory object being returned with a valid ID.
    @Test
    void testFindFile() {
        Assertions.assertDoesNotThrow(()->{
            Directory root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());
            ZeppelinFile file = root.findFile("2A94M5J4Z");
            Assertions.assertFalse(file.isDirectory());
            Assertions.assertEquals(file.path(),Paths.get(notebookDirectory.toString(),"my_note4_2A94M5J4Z.zpln"));

            ZeppelinFile file2 = root.findFile("2A94M5J1D");
            Assertions.assertTrue(file2.isDirectory());
            Assertions.assertEquals(file2.path(),Paths.get(notebookDirectory.toString(),"my_folder_2A94M5J1D"));
        });
    }

    // Calling contains with a valid ID should return true, and false when called with an ID that doesn't exist
    @Test
    void testContains() {
        Assertions.assertDoesNotThrow(()->{
            Directory root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());
            Assertions.assertTrue(root.contains("2A94M5J1Z"));
            Assertions.assertFalse(root.contains("NonexistentId"));
        });
    }
    // Moving a directory should result in the directory being moved to the correct path along with its children.
    @Test
    void testMove() {
        Assertions.assertDoesNotThrow(()->{
            Directory root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());
            Directory directory = (Directory) root.findFile("2A94M5J2D");
            directory.move(Paths.get(root.path().toString(),directory.path().getFileName().toString()));

            // Re-initialize directory as we have made modifications.
            root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());
            directory = (Directory) root.findFile("2A94M5J2D");
            Assertions.assertEquals(Paths.get(notebookDirectory.toString(),"my_second_folder_2A94M5J2D").toString(),directory.path().toString());

            // Assert that the children of the moved directory were moved as well.
            ZeppelinFile subFile = directory.findFile("2A94M5J1Z");
            Assertions.assertEquals(Paths.get(root.path().toString(),"my_second_folder_2A94M5J2D","my_note1_2A94M5J1Z.zpln"),subFile.path());
        });
    }

    // Deleting a Directory should result in the deletion of the directory file as well as all of its children from disk.
    @Test
    void testDelete() {
        Assertions.assertDoesNotThrow(()->{
            Directory root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());
            Directory directory = (Directory) root.findFile("2A94M5J2D");
            ZeppelinFile child = directory.children().get("2A94M5J1Z");

            // Verify that the file we are about to delete exists.
            Assertions.assertTrue(Files.exists(directory.path()));
            directory.delete();

            // Assert that the directory we deleted (and its children) no longer exists.
            Assertions.assertFalse(Files.exists(directory.path()));
            Assertions.assertFalse(Files.exists(child.path()));
        });
    }

    // Copying a directory should result in the original and a new copy existing on disk.
    @Test
    void testCopy() {
        Assertions.assertDoesNotThrow(()->{
            Directory root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());
            Directory directory = (Directory) root.findFile("2A94M5J2D");
            directory.copy(Paths.get(root.path().toString(),directory.path().getFileName().toString().replace("_2A94M5J2D","_copiedDirectory")),"copiedDirectory");

            // Re-initialize directory as we have made modifications.
            root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());

            // Both copied directory and the original directory (and their children) should exist
            Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory.toString(),"my_second_folder_copiedDirectory")));
            ZeppelinFile copiedDirectory = root.findFile("copiedDirectory");
            Assertions.assertEquals(1,copiedDirectory.listAllChildren().size());
            String childId = copiedDirectory.listAllChildren().get(0).id();
            Assertions.assertTrue(Files.exists(Paths.get(copiedDirectory.path().toString(),"my_note1_"+childId+".zpln")));

            Assertions.assertTrue(Files.exists(directory.path()));
            Assertions.assertTrue(Files.exists(Paths.get(directory.path().toString(),"my_note1_2A94M5J1Z.zpln")));
        });
    }

    // Renaming a directory should result in the file being renamed.
    @Test
    void testRename() {
        Assertions.assertDoesNotThrow(()->{
            Directory root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());
            Directory directory = (Directory) root.findFile("2A94M5J2D");
            Path originalPath = directory.path();
            directory.rename("renamedDirectory_2A94M5J2D");

            // Re-initialize directory as we have made modifications.
            root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());
            directory = (Directory) root.findFile("2A94M5J2D");
            Path newPath = directory.path();

            // Assert that no file with the original name exists, and that the renamed file exists.
            Assertions.assertFalse(Files.exists(originalPath));
            Assertions.assertTrue(Files.exists(newPath));});
    }

    // Calling json() should result in a valid JSON object.
    @Test
    void testJson() {
        Assertions.assertDoesNotThrow(()->{
            Directory root = new Directory("root",notebookDirectory).initializeDirectory(notebookDirectory,new ConcurrentHashMap<>());
            Assertions.assertEquals("{\"id\":\"notebooks\",\"name\":\"notebooks\",\"chidlren\":\"[2A94M5J3Z, 2A94M5J4Z, 2A94M5J1D, junkfile]\"}",root.json().toString());
        });
    }

    // Creating and then saving a new directory containing a notebook should result in two new files on disk.
    @Test
    void testSave() {
        Assertions.assertDoesNotThrow(()->{
            Path newDirectoryPath = Paths.get(notebookDirectory.toString(),"new_folder_newDirectoryId");
            Map<String,ZeppelinFile> notebooks = new HashMap<>();
            Notebook newNotebook = new Notebook("title","newNotebook",Paths.get(newDirectoryPath.toString(),"newNotebook_newNotebook.zpln"),new LinkedHashMap<>());
            notebooks.put("newNotebook",newNotebook);
            Directory newDirectory = new Directory("newDirectory",Paths.get(notebookDirectory.toString(),"newDirectory_newId"),notebooks);
            newDirectory.save();

            // Files for both the directory and its children should exist after saving.
            Assertions.assertTrue(Files.exists(newDirectoryPath));
            Assertions.assertTrue(Files.exists(Paths.get(newDirectoryPath.toString(),"newNotebook_newNotebook.zpln")));
        });
    }
}