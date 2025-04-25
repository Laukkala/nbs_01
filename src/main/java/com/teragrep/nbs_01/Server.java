package com.teragrep.nbs_01;

import java.nio.file.Paths;

public class Server {
    public static void main(String[] args){
        try{
            NotebookServer server = new NotebookServer(Paths.get("target/notebooks"));
            server.start();
            server.join();
        } catch (InterruptedException exception){

        }
    }
}
