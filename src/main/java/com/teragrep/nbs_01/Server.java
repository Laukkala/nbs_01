package com.teragrep.nbs_01;
import java.nio.file.Paths;

// A simple main class for configuring and starting one or more NotebookServer threads.
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
