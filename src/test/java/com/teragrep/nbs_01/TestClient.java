package com.teragrep.nbs_01;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

public final class TestClient implements Session.Listener {

    private static String[] selections = {"ping","find","new","list","delete","update"};
        public static void main(String[] args) throws IOException {
            try{
                System.out.print("Which endpoint to connect to?\n" +
                        "1) "+selections[0]+"\n" +
                        "2) "+selections[1]+"\n" +
                        "3) "+selections[2]+"\n" +
                        "4) "+selections[3]+"\n" +
                        "5) "+selections[4]+"\n" +
                        "6) "+selections[5]+"\n" +
                        "Selection: ");
                BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
                int selection = Integer.parseInt(br1.readLine());
                if(0 > selection || selection > selections.length){
                    System.out.println("Invalid selection!");
                    System.exit(1);
                }

                URI serverURI = URI.create("ws://localhost:8080/notebook/"+selections[selection-1]);
                WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
                webSocketClient.start();
                TestWebSocketClientEndpoint client = new TestWebSocketClientEndpoint(webSocketClient,serverURI);

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String message = "";
                while ((message = br.readLine()) != null){
                    client.sendText(message);
                }
                br1.close();
                br.close();
            }catch (Exception exception){
                System.err.println(exception);
            }
        }
    }
