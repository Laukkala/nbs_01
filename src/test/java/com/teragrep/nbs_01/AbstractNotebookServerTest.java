package com.teragrep.nbs_01;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Assertions;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<Integer, List<String>> makeHttpPOSTRequest(String urlString, String requestBody) throws IOException {
        HashMap<Integer,List<String>> response = new HashMap<>();
            URL url = new URL(urlString);
            ArrayList<String> messages = new ArrayList<>();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            byte[] bytes = (requestBody).getBytes();
            int length = bytes.length;

            connection.setFixedLengthStreamingMode(length);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            connection.connect();
            OutputStream output = connection.getOutputStream();
            output.write(bytes);
            int status = connection.getResponseCode();
            InputStreamReader connectionInputStreamReader;
            if(status == 200){
                connectionInputStreamReader = new InputStreamReader(connection.getInputStream());
            }
            else {
                connectionInputStreamReader = new InputStreamReader(connection.getErrorStream());
            }
            // Read the response received from either ErrorStream or InputStream, depending on HTTP Response code received.
            BufferedReader reader = new BufferedReader(connectionInputStreamReader);

            String line;
            while ((line = reader.readLine()) != null) {
                messages.add(line);
            }
            connection.disconnect();
            response.put(status,messages);
        return response;
    }

    public Map<Integer, List<String>> makeHttpGETRequest(String urlString) throws IOException{
        HashMap<Integer,List<String>> response = new HashMap<>();
        URL url = new URL(urlString);
        ArrayList<String> messages = new ArrayList<>();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int status = connection.getResponseCode();
        InputStreamReader connectionInputStreamReader;
        if(status == 200){
            connectionInputStreamReader = new InputStreamReader(connection.getInputStream());
        }
        else {
            connectionInputStreamReader = new InputStreamReader(connection.getErrorStream());
        }
        // Read the response received from either ErrorStream or InputStream, depending on HTTP Response code received.
        BufferedReader reader = new BufferedReader(connectionInputStreamReader);

        String line;
        while ((line = reader.readLine()) != null) {
            messages.add(line);
        }
        connection.disconnect();
        response.put(status,messages);
        return response;
    }
    public Map<Integer,List<String>> makeWebSocketRequest(String url, String requestBody) throws Exception {
        HashMap<Integer,List<String>> response = new HashMap<>();
                // Start server and wait for it to initialize.
                URI serverURI = URI.create(url);
                WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
                webSocketClient.start();
                TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient,serverURI);
                client.sendText(requestBody);
                long startTime = System.currentTimeMillis();
                while (client.receivedMessages().size() == 0 && (System.currentTimeMillis()-startTime) < webSocketTimeoutMs){
                    // Wait until a message is received or a timeout is reached.
                }
                // Read the WebSocket response and assert that we got the proper list of notebook IDs.
                ArrayList<String> receivedMessages = client.receivedMessages();
                response.put(200,receivedMessages);
                webSocketClient.close();
        return response;
    }
}
