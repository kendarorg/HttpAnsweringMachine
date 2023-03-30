package org.kendar.mongo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MongoServerOld {
    private static final int PORT = 27017;

    public void run(int port,AnsweringMongoServer answeringMongoServer) throws IOException {
        ServerSocket server = new ServerSocket(port);
        System.out.println("MongoDB server started on port " + port);

        while (answeringMongoServer.shouldRun()) {
            Socket client = server.accept();
            System.out.println("New client connected: " + client.getInetAddress().getHostAddress());



            // Handle the client connection in a separate thread
            Thread clientThread = new Thread(new MongoClientHandler(client));
            clientThread.start();
        }
    }
}
