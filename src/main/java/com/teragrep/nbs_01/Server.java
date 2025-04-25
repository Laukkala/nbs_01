package com.teragrep.nbs_01;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    public static void main(String[] args){
        try{
            Configuration configuration = new Configuration(Paths.get("target/notebooks"),8080);
            NotebookServer server = new NotebookServer(configuration);
            server.start();
            server.join();
        } catch (InterruptedException exception){

        }
    }
}
