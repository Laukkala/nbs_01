package com.teragrep.nbs_01.endpoints;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.TestWebSocketClientEndpoint;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FindEndPointTest extends AbstractNotebookServerTest
{
    private final Path testFilePath = Paths.get("src/test/resources/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
    private final String expectedFileContent = "{\"id\":\"2A94M5J1Z\",\"name\":\"my_note1\",\"config\":{},\"paragraphs\":[{\"id\":\"20150213-231621_168813393\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Welcome to Zeppelin.\\\\n##### This is a live tutorial, you can run the code yourself. (Shift-Enter to Run)\\\"\"}},{\"id\":\"20150210-015259_1403135953\",\"title\":\"Load data into table\",\"script\":{\"text\":\"\\\"%test import org.apache.commons.io.IOUtils\\\\nimport java.net.URL\\\\nimport java.nio.charset.Charset\\\\n\\\\n// Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)\\\\n// So you don't need create them manually\\\\n\\\\n// load bank data\\\\nval bankText = sc.parallelize(\\\\n    IOUtils.toString(\\\\n        new URL(\\\\\\\"https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv\\\\\\\"),\\\\n        Charset.forName(\\\\\\\"utf8\\\\\\\")).split(\\\\\\\"\\\\\\\\n\\\\\\\"))\\\\n\\\\ncase class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)\\\\n\\\\nval bank = bankText.map(s => s.split(\\\\\\\";\\\\\\\")).filter(s => s(0) != \\\\\\\"\\\\\\\\\\\\\\\"age\\\\\\\\\\\\\\\"\\\\\\\").map(\\\\n    s => Bank(s(0).toInt, \\\\n            s(1).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(2).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(3).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\"),\\\\n            s(5).replaceAll(\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\", \\\\\\\"\\\\\\\").toInt\\\\n        )\\\\n).toDF()\\\\nbank.registerTempTable(\\\\\\\"bank\\\\\\\")\\\"\"}},{\"id\":\"20150210-015302_1492795503\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value\\\\nfrom bank \\\\nwhere age < 30 \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150212-145404_867439529\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value \\\\nfrom bank \\\\nwhere age < ${maxAge=30} \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150213-230422_1600658137\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test \\\\nselect age, count(1) value \\\\nfrom bank \\\\nwhere marital=\\\\\\\"${marital=single,single|divorced|married}\\\\\\\" \\\\ngroup by age \\\\norder by age\\\"\"}},{\"id\":\"20150213-230428_1231780373\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n## Congratulations, it's done.\\\\n##### You can create your own notebook in 'Notebook' menu. Good luck!\\\"\"}},{\"id\":\"20150326-214658_12335843\",\"title\":\"\",\"script\":{\"text\":\"\\\"%test\\\\n\\\\nAbout bank data\\\\n\\\\n```\\\\nCitation Request:\\\\n  This dataset is public available for research. The details are described in [Moro et al., 2011]. \\\\n  Please include this citation if you plan to use this database:\\\\n\\\\n  [Moro et al., 2011] S. Moro, R. Laureano and P. Cortez. Using Data Mining for Bank Direct Marketing: An Application of the CRISP-DM Methodology. \\\\n  In P. Novais et al. (Eds.), Proceedings of the European Simulation and Modelling Conference - ESM'2011, pp. 117-121, Guimarães, Portugal, October, 2011. EUROSIS.\\\\n\\\\n  Available at: [pdf] http://hdl.handle.net/1822/14838\\\\n                [bib] http://www3.dsi.uminho.pt/pcortez/bib/2011-esm-1.txt\\\\n```\\\"\"}},{\"id\":\"20150703-133047_853701097\",\"title\":\"\",\"script\":{\"text\":\"\"}}]}";
    private final String testFileId = "2A94M5J1Z";
    @BeforeEach
    private void setUp(){
        copyFileRecursively(notebookResources().toFile(),notebookDirectory().toFile());
    }
    @AfterAll
    private void tearDown(){
        deleteFileRecursively(notebookDirectory().toFile());
    }
    @Test
    // Assert that a simple HTTP request to /notebook/list endpoint results in a list of notebook IDs
    public void httpFindTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();

            URL serverURL = new URL("http://"+serverAddress()+"/notebook/find");
            HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            byte[] bytes = URLEncoder.encode(testFileId, StandardCharsets.UTF_8).getBytes();
            int length = bytes.length;

            connection.setFixedLengthStreamingMode(length);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            connection.connect();
            OutputStream output = connection.getOutputStream();
            output.write(bytes);
            int status = connection.getResponseCode();
            // Read the response received, and assert that we have a list of notebook IDs matching files saved in the notebook directory.
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line+"\n");
            }
            Assertions.assertEquals(200,status);
            Assertions.assertEquals(expectedFileContent+"\n",sb.toString());
            connection.disconnect();
            stopServer();
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketFindTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();
            URI serverURI = URI.create("ws://"+serverAddress()+"/notebook/find");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient,serverURI);
            Assertions.assertEquals(1,webSocketClient.getOpenSessions().size());
            client.sendText(testFileId);
            long startTime = System.currentTimeMillis();
            while (client.receivedMessages().size() == 0 && (System.currentTimeMillis()-startTime) < webSocketTimeoutMs){
                // Wait until a message is received or a timeout is reached.
            }
            // Read the WebSocket response and assert that we got the proper list of notebook IDs.
            ArrayList<String> receivedMessages = client.receivedMessages();
            Assertions.assertEquals(expectedFileContent,receivedMessages.get(0));
            webSocketClient.close();
            Assertions.assertEquals(0,webSocketClient.getOpenSessions().size());
            stopServer();
        });
    }

    @Test
    public void notebookNotFoundTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();

            URL serverURL = new URL("http://"+serverAddress()+"/notebook/find");
            HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            byte[] bytes = URLEncoder.encode("nonexistentID",StandardCharsets.UTF_8).getBytes();
            int length = bytes.length;

            connection.setFixedLengthStreamingMode(length);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            connection.connect();
            OutputStream output = connection.getOutputStream();
            output.write(bytes);

            int status = connection.getResponseCode();
            // Read the response received, and assert that we have a list of notebook IDs matching files saved in the notebook directory.
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            Assertions.assertEquals(200,status);
            Assertions.assertEquals("Notebook not found!",sb.toString());
            connection.disconnect();
            stopServer();
        });
    }
}
