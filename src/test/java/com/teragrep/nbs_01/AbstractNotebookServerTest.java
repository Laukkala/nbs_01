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

import com.teragrep.nbs_01.http.body.ErrorBody;
import com.teragrep.nbs_01.http.body.JSONBody;
import com.teragrep.nbs_01.http.responses.BasicResponse;
import com.teragrep.nbs_01.http.responses.Response;
import com.teragrep.nbs_01.repository.LocalFilesystemStorage;
import com.teragrep.nbs_01.repository.Storage;
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
    private final Path notebook1 = Paths.get("my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
    private final Path notebook2 = Paths.get("my_folder_2A94M5J1D/my_note2_2A94M5J2Z.zpln");
    private final Path notebook3 = Paths.get("my_note3_2A94M5J3Z.zpln");
    private final Path notebook4 = Paths.get("my_note4_2A94M5J4Z.zpln");
    private final Path directory1 = Paths.get("my_folder_2A94M5J1D");
    private final Path directory2 = Paths.get("my_folder_2A94M5J1D/my_second_folder_2A94M5J2D");
    private final Path junkfile = Paths.get("junkfile");

    private final String notebook1Paragraph1 = "{\"id\":\"20150213-231621_168813393\",\"title\":\"\",\"script\":{\"text\":\"%test\\n## Welcome to Zeppelin.\\n##### This is a live tutorial, you can run the code yourself. (Shift-Enter to Run)\"}}";
    private final String notebook1Paragraph2 = "{\"id\":\"20150210-015259_1403135953\",\"title\":\"Load data into table\",\"script\":{\"text\":\"%test import org.apache.commons.io.IOUtils\\nimport java.net.URL\\nimport java.nio.charset.Charset\\n\\n// Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)\\n// So you don't need create them manually\\n\\n// load bank data\\nval bankText = sc.parallelize(\\n    IOUtils.toString(\\n        new URL(\\\"https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv\\\"),\\n        Charset.forName(\\\"utf8\\\")).split(\\\"\\\\n\\\"))\\n\\ncase class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)\\n\\nval bank = bankText.map(s => s.split(\\\";\\\")).filter(s => s(0) != \\\"\\\\\\\"age\\\\\\\"\\\").map(\\n    s => Bank(s(0).toInt, \\n            s(1).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(2).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(3).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(5).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\").toInt\\n        )\\n).toDF()\\nbank.registerTempTable(\\\"bank\\\")\"}}";
    private final String notebook1Paragraph3 = "{\"id\":\"20150210-015302_1492795503\",\"title\":\"\",\"script\":{\"text\":\"%test \\nselect age, count(1) value\\nfrom bank \\nwhere age < 30 \\ngroup by age \\norder by age\"}}";
    private final String notebook1Paragraph4 = "{\"id\":\"20150212-145404_867439529\",\"title\":\"\",\"script\":{\"text\":\"%test \\nselect age, count(1) value \\nfrom bank \\nwhere age < ${maxAge=30} \\ngroup by age \\norder by age\"}}";
    private final String notebook1Paragraph5 = "{\"id\":\"20150213-230422_1600658137\",\"title\":\"\",\"script\":{\"text\":\"%test \\nselect age, count(1) value \\nfrom bank \\nwhere marital=\\\"${marital=single,single|divorced|married}\\\" \\ngroup by age \\norder by age\"}}";
    private final String notebook1Paragraph6 = "{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"%test\\n## Congratulations, it's done.\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\"}}";
    private final String notebook1Paragraph7 = "{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"%test\\n\\nAbout bank data\\n\\n```\\nCitation Request:\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\n  Please include this citation if you plan to use this database:\\n\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\n\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\n```\"}}";
    private final String notebook1Paragraph8 = "{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}";
    private final Storage storage = new LocalFilesystemStorage(notebookDirectory);
    private final Configuration testConfiguration = new Configuration(storage, serverPort);
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

    public String notebook1Paragraph1() {
        return notebook1Paragraph1;
    }

    public String notebook1Paragraph2() {
        return notebook1Paragraph2;
    }

    public String notebook1Paragraph3() {
        return notebook1Paragraph3;
    }

    public String notebook1Paragraph4() {
        return notebook1Paragraph4;
    }

    public String notebook1Paragraph5() {
        return notebook1Paragraph5;
    }

    public String notebook1Paragraph6() {
        return notebook1Paragraph6;
    }

    public String notebook1Paragraph7() {
        return notebook1Paragraph7;
    }

    public String notebook1Paragraph8() {
        return notebook1Paragraph8;
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

    public Response makeHttpPOSTRequest(String urlString, String requestBody) throws IOException {
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
        return new BasicResponse(status, new JSONBody(message));
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
        JsonObject message = Json.createReader(new StringReader(messages.toString())).readObject();
        connection.disconnect();
        return new BasicResponse(status, new JSONBody(message));
    }

    public Response makeHttpPUTRequest(String urlString, String requestBody) throws IOException {
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
        return new BasicResponse(status, new JSONBody(message));
    }

    public Response makeHttpDELETERequest(String urlString, String requestBody) throws IOException {
        URL url = new URL(urlString);
        StringBuilder messages = new StringBuilder();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        byte[] bytes = (requestBody).getBytes(StandardCharsets.UTF_8);
        connection.setDoOutput(true);
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
                return new BasicResponse(status, new JSONBody(message));
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
                return new BasicResponse(status, new JSONBody(message));
            }

        }
        catch (IOException ioException) {
            return new BasicResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, new ErrorBody(new ErrorEvent(ioException)));
        }
    }
}
