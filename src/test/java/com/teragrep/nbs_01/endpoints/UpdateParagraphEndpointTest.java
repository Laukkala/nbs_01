package com.teragrep.nbs_01.endpoints;

import com.google.common.io.Files;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UpdateParagraphEndpointTest extends AbstractNotebookServerTest {
    private final String testFileId = "2A94M5J1Z";
    private final Path testFilePath = Paths.get(notebookDirectory().toString(),"/my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
    private final String testParagraphId = "20150703-133047_853701097";

    @BeforeEach
    private void setUp(){
        copyFileRecursively(notebookResources().toFile(),notebookDirectory().toFile());
    }
    @AfterAll
    private void tearDown(){
        deleteFileRecursively(notebookDirectory().toFile());
    }
    @Test
    public void httpUpdateNotebookTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();

            URL serverURL = new URL("http://"+serverAddress()+"/notebook/update");
            HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            byte[] bytes = (testFileId+","+testParagraphId+","+"testEditMessage").getBytes();
            int length = bytes.length;

            connection.setFixedLengthStreamingMode(length);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            connection.connect();
            OutputStream output = connection.getOutputStream();
            output.write(bytes);

            int status = connection.getResponseCode();
            // Read the response received, and assert that we got the response we are expecting.
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line+"\n");
            }
            Assertions.assertEquals(200,status);
            Assertions.assertTrue(sb.toString().contains("Notebook edited successfully"));
            connection.disconnect();
            stopServer();
            // Assert that the message we wanted to edit can be found in the file.
            Assertions.assertTrue(Files.readLines(testFilePath.toFile(), Charset.defaultCharset()).get(0).contains("testEditMessage"));
        });
    }
    @Test
    // Assert that A WebSocket connection is established, and that it is closed after a call to WebSocketClient.close()
    public void webSocketUpdateTest(){
        Assertions.assertDoesNotThrow(()->{
            // Start server and wait for it to initialize.
            startServer();
            URI serverURI = URI.create("ws://"+serverAddress()+"/notebook/update");
            WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
            webSocketClient.start();
            TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient,serverURI);
            Assertions.assertEquals(1,webSocketClient.getOpenSessions().size());
            client.sendText(testFileId+","+testParagraphId+","+"testEditMessage");
            long startTime = System.currentTimeMillis();
            while (client.receivedMessages().size() == 0 && (System.currentTimeMillis()-startTime) < webSocketTimeoutMs){
                // Wait until a message is received or a timeout is reached.
            }
            // Read the WebSocket response and assert that we got the proper response.
            ArrayList<String> receivedMessages = client.receivedMessages();
            Assertions.assertEquals("Notebook edited successfully",receivedMessages.get(0));
            webSocketClient.close();
            Assertions.assertEquals(0,webSocketClient.getOpenSessions().size());
            stopServer();
            // Assert that the message we wanted to edit can be found in the file.
            Assertions.assertTrue(Files.readLines(testFilePath.toFile(), Charset.defaultCharset()).get(0).contains("testEditMessage"));
        });
    }

}