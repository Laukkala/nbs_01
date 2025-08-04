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

import com.teragrep.nbs_01.responses.SimpleResponse;
import com.teragrep.nbs_01.responses.JsonResponse;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AbstractNotebookServerTest {

    private final int serverPort = 8080;
    private final String serverAddress = "localhost:" + serverPort;
    private final Path notebookResources = Paths.get("src/test/resources");
    private final Path notebookDirectory = Paths.get("target/notebooks");
    private final Path notebook1 = Paths
            .get("target/notebooks/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
    private final Path notebook2 = Paths.get("target/notebooks/my_folder_2A94M5J1D/my_note2_2A94M5J2Z.zpln");
    private final Path notebook3 = Paths.get("target/notebooks/my_note3_2A94M5J3Z.zpln");
    private final Path notebook4 = Paths.get("target/notebooks/my_note4_2A94M5J4Z.zpln");
    private final Path directory1 = Paths.get("target/notebooks/my_folder_2A94M5J1D");
    private final Path directory2 = Paths.get("target/notebooks/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D");
    private final Path junkfile = Paths.get("target/notebooks/junkfile");

    private final Configuration testConfiguration = new Configuration(notebookDirectory, serverPort);
    private final NotebookServer server = new NotebookServer(testConfiguration);

    @BeforeEach
    public synchronized void startServer() throws Exception {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
        server.call();
    }

    @AfterEach
    public synchronized void stopServer() throws Exception {
        deleteFileRecursively(notebookDirectory().toFile());
        server.stop();
    }

    public Path notebookDirectory() {
        return notebookDirectory;
    }

    public Path notebook1() {
        return notebook1;
    }

    public Path notebook2() {
        return notebook2;
    }

    public Path notebook3() {
        return notebook3;
    }

    public Path notebook4() {
        return notebook4;
    }

    public Path directory1() {
        return directory1;
    }

    public Path directory2() {
        return directory2;
    }

    public Path junkfile() {
        return junkfile;
    }

    public Path notebookResources() {
        return notebookResources;
    }

    public String serverAddress() {
        return serverAddress;
    }

    public void copyFileRecursively(File fileToCopy, File destination) {
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
            Assertions.assertDoesNotThrow(() -> Files.copy(fileToCopy.toPath(), destination.toPath()));
        }
    }

    public void deleteFileRecursively(File fileToDelete) {
        File[] children = fileToDelete.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteFileRecursively(child);
            }
        }
        fileToDelete.delete();
    }

    public JsonResponse makeHttpPOSTRequest(String urlString, String requestBody) throws IOException {
        URL url = new URL(urlString);
        StringBuilder messages = new StringBuilder();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        byte[] bytes = (requestBody).getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.connect();
        OutputStream output = connection.getOutputStream();
        output.write(bytes);
        output.close();
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
        JsonObject message = Json.createReader(new StringReader(messages.toString())).readObject();
        connection.disconnect();
        return new SimpleResponse(status, message);
    }

    public JsonResponse makeHttpGETRequest(String urlString) throws IOException {
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
        JsonObject message = Json.createReader(new StringReader(messages.toString())).readObject();
        connection.disconnect();
        return new SimpleResponse(status, message);
    }

    public JsonResponse makeHttpPUTRequest(String urlString, String requestBody) throws IOException {
        URL url = new URL(urlString);
        StringBuilder messages = new StringBuilder();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);

        byte[] bytes = (requestBody).getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.connect();
        OutputStream output = connection.getOutputStream();
        output.write(bytes);
        output.close();
        int status = connection.getResponseCode();
        InputStreamReader connectionInputStreamReader;
        if (status == 201) {
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
        JsonObject message = Json.createReader(new StringReader(messages.toString())).readObject();
        connection.disconnect();
        return new SimpleResponse(status, message);
    }

    public JsonResponse makeHttpDELETERequest(String urlString, String requestBody) throws IOException {
        URL url = new URL(urlString);
        StringBuilder messages = new StringBuilder();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setDoOutput(true);

        byte[] bytes = (requestBody).getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.connect();
        OutputStream output = connection.getOutputStream();
        output.write(bytes);
        output.close();
        int status;
        try {
            status = connection.getResponseCode();
            if (status == 204) {
                // Successful responses to DELETE requests should have no content.
                JsonObject message = JsonValue.EMPTY_JSON_OBJECT;
                connection.disconnect();
                return new SimpleResponse(status, message);
            }
            else {
                InputStreamReader connectionInputStreamReader;
                if (connection.getErrorStream() != null) {
                    connectionInputStreamReader = new InputStreamReader(connection.getErrorStream());
                }
                else {
                    try {
                        connectionInputStreamReader = new InputStreamReader(connection.getInputStream());
                    }
                    catch (IOException ioException) {
                        throw new IOException("Error while reading input from connection", ioException);
                    }
                }
                BufferedReader reader = new BufferedReader(connectionInputStreamReader);
                String line;
                while ((line = reader.readLine()) != null) {
                    messages.append(line + "\n");
                }
                JsonObject message = Json.createReader(new StringReader(messages.toString())).readObject();
                return new SimpleResponse(status, message);
            }

        }
        catch (IOException e) {
            System.out.println(e);
            return new SimpleResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, e.toString());
        }
    }
}
