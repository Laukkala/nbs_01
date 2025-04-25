package com.teragrep.nbs_01;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

    public final class Client implements Session.Listener {
        public static void main(String[] args){
            try{
                URI serverURI = URI.create("ws://localhost:8080/ws/list");
                WebSocketClient webSocketClient = new WebSocketClient(new HttpClient());
                webSocketClient.start();
                WebSocketConnection client = new WebSocketConnection(webSocketClient,serverURI);

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String message = "";
                while ((message = br.readLine()) != null){
                    client.sendText(message);
                }
                br.close();
            }catch (Exception exception){
                System.err.println(exception);
            }
        }
    }
