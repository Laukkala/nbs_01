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
package com.teragrep.nbs_01;

import com.teragrep.nbs_01.responses.JsonResponse;
import com.teragrep.nbs_01.responses.Response;
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

public class AbstractNotebookServerTest {

    private final int serverPort = 8080;
    private final String serverAddress = "localhost:" + serverPort;
    private final Path notebookResources = Paths.get("src/test/resources");
    private final Path notebookDirectory = Paths.get("target/notebooks");
    private final Configuration testConfiguration = new Configuration(notebookDirectory, serverPort);
    private final NotebookServer server = new NotebookServer(testConfiguration);
    public final int webSocketTimeoutMs = 1000;

    public void startServer() {
        if (server.getState() == Thread.State.NEW) {
            try {
                server.start();
                Thread.sleep(1000);
            }
            catch (InterruptedException interruptedException) {
                throw new RuntimeException(interruptedException);
            }
        }
    }

    public void stopServer() throws InterruptedException {
        server.join();
    }

    public Path notebookDirectory() {
        return notebookDirectory;
    }

    public Path notebookResources() {
        return notebookResources;
    }

    public String serverAddress() {
        return serverAddress;
    }

    public void copyFileRecursively(File fileToCopy, File destination) {
        Assertions.assertDoesNotThrow(() -> {
            if (fileToCopy.isDirectory()) {
                File[] children = fileToCopy.listFiles();
                for (File child : children) {
                    copyFileRecursively(child, Paths.get(destination.toString(), child.getName()).toFile());
                }
            }
            if (!destination.exists()) {
                File parent = destination.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                Files.copy(fileToCopy.toPath(), destination.toPath());
            }
        });
    }

    public void deleteFileRecursively(File fileToDelete) {
        Assertions.assertDoesNotThrow(() -> {
            File[] children = fileToDelete.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFileRecursively(child);
                }
            }
            fileToDelete.delete();
        });
    }

    public Response makeHttpPOSTRequest(String urlString, String requestBody) throws IOException {
        URL url = new URL(urlString);
        StringBuilder messages = new StringBuilder();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        byte[] bytes = (requestBody).getBytes();
        int length = bytes.length;

        connection.setFixedLengthStreamingMode(length);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.connect();
        OutputStream output = connection.getOutputStream();
        output.write(bytes);
        int status = connection.getResponseCode();
        InputStreamReader connectionInputStreamReader;
        if (status == 200) {
            connectionInputStreamReader = new InputStreamReader(connection.getInputStream());
        }
        else {
            connectionInputStreamReader = new InputStreamReader(connection.getErrorStream());
        }
        // Read the response received from either ErrorStream or InputStream, depending on HTTP Response code received.
        BufferedReader reader = new BufferedReader(connectionInputStreamReader);

        String line;
        while ((line = reader.readLine()) != null) {
            messages.append(line + "\n");
        }
        connection.disconnect();
        return new JsonResponse(status, messages.toString());
    }

    public Response makeHttpGETRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        StringBuilder messages = new StringBuilder();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int status = connection.getResponseCode();
        InputStreamReader connectionInputStreamReader;
        if (status == 200) {
            connectionInputStreamReader = new InputStreamReader(connection.getInputStream());
        }
        else {
            connectionInputStreamReader = new InputStreamReader(connection.getErrorStream());
        }
        // Read the response received from either ErrorStream or InputStream, depending on HTTP Response code received.
        BufferedReader reader = new BufferedReader(connectionInputStreamReader);

        String line;
        while ((line = reader.readLine()) != null) {
            messages.append(line);
        }
        connection.disconnect();
        return new JsonResponse(status, messages.toString());
    }

    public Response makeWebSocketRequest(String url, String requestBody) throws Exception {
        // Start server and wait for it to initialize.
        URI serverURI = URI.create(url);
        WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
        webSocketClient.start();
        TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient, serverURI);
        client.sendText(requestBody);
        long startTime = System.currentTimeMillis();
        while (client.receivedMessages().size() == 0 && (System.currentTimeMillis() - startTime) < webSocketTimeoutMs) {
            // Wait until a message is received or a timeout is reached.
        }
        // Read the WebSocket response and assert that we got the proper list of notebook IDs.
        ArrayList<String> receivedMessages = client.receivedMessages();
        webSocketClient.close();
        return new JsonResponse(200, receivedMessages.toString());
    }
}
