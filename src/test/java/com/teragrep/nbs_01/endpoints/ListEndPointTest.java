package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListEndPointTest extends AbstractNotebookServerTest
{
    private List<String> savedFileNames;
    public List<String> readFilesOnDisk(){
        try{
            return Files.list(notebookDirectory())
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }catch (IOException ioException){
            throw new RuntimeException("Failed to initialize test!",ioException);
        }
    }
    @BeforeAll
    private void setUp(){
        copyFileRecursively(notebookResources().toFile(),notebookDirectory().toFile());
        savedFileNames = readFilesOnDisk();
    }
    @AfterAll
    private void tearDown(){
        deleteFileRecursively(notebookDirectory().toFile());
    }
    @Test
    // Assert that a simple HTTP request to /notebook/list endpoint results in a list of notebook IDs
    public void httpListAllTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            Map<Integer, List<String>> response = makeHttpPOSTRequest("http://"+serverAddress()+"/notebook/list","");
            for (String filename :savedFileNames) {
                Assertions.assertTrue(response.get(200).stream().anyMatch(filename::contains));
            }
            stopServer();
        });
    }
    @Test
    // Assert that a simple HTTP request to /notebook/list endpoint results in a list of notebook IDs
    public void httpListWithinFolderTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            Map<Integer, List<String>> response = makeHttpPOSTRequest("http://"+serverAddress()+"/notebook/list","2A94M5J1D");
            ArrayList<String> savedIdsInFolder = new ArrayList<>();
            savedFileNames.add("2A94M5J1Z");
            savedFileNames.add("2A94M5J2Z");
            for (String filename :savedIdsInFolder) {
                Assertions.assertTrue(response.get(200).stream().anyMatch(filename::equals));
            }
            stopServer();
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketListAllTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            Map<Integer, List<String>> response = makeWebSocketRequest("ws://"+serverAddress()+"/notebook/list","");
            List<String> ids = Arrays.stream(response.get(200).get(0).split("\n")).toList();
            ids.stream().anyMatch(savedFileNames::contains);
            stopServer();
        });
    }

    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketListWithinFolderTest(){
        Assertions.assertDoesNotThrow(()->{
            startServer();
            Map<Integer, List<String>> response = makeWebSocketRequest("ws://"+serverAddress()+"/notebook/list","2A94M5J1D");
            ArrayList<String> savedIdsInFolder = new ArrayList<>();
            savedFileNames.add("2A94M5J1Z");
            savedFileNames.add("2A94M5J2Z");

            List<String> receivedIds = Arrays.stream(response.get(200).get(0).split("\n")).toList();
            for (String filename :savedIdsInFolder) {
                Assertions.assertTrue(receivedIds.stream().anyMatch(filename::equals));
            }
            stopServer();
        });
    }
}
