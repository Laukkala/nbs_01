package com.teragrep.nbs_01;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AbstractNotebookServerTest {
    private final int serverPort = 8080;
    private final String serverAddress = "localhost:"+serverPort;
    private final Path notebookDirectory = Paths.get("target/notebooks");
    private final Configuration testConfiguration = new Configuration(notebookDirectory,serverPort);
    private final NotebookServer server = new NotebookServer(testConfiguration);
    public void startServer(){
        if(server.getState() == Thread.State.NEW){
            try {
                server.start();
                Thread.sleep(500);
            }
            catch (InterruptedException interruptedException){
                throw new RuntimeException(interruptedException);
            }
        }
    }

    public void stopServer() throws InterruptedException {
        server.join();
    }

    public Path notebookDirectory(){
        return notebookDirectory;
    }
    public String serverAddress(){
        return serverAddress;
    }
}
