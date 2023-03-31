package org.kendar.mongo;

import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.handlers.MsgHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Component
public class MongoServer {
    public static final int DEFAULT_PORT = 27017;
    private List<MsgHandler> msgHandlers;
    private List<CompressionHandler> compressionHandlers;

    public MongoServer(List<MsgHandler> msgHandlers, List<CompressionHandler> compressionHandlers){

        this.msgHandlers = msgHandlers;
        this.compressionHandlers = compressionHandlers;
    }

    public void run(int port,AnsweringMongoServer answeringMongoServer) throws IOException {
        try(ServerSocket server = new ServerSocket(port)) {
            System.out.println("MongoDB server started on port " + port);

            while (answeringMongoServer.shouldRun()) {
                Socket client = server.accept();
                System.out.println("New client connected: " + client.getInetAddress().getHostAddress());
                // Handle the client connection in a separate thread
                Thread clientThread = new Thread(new MongoClientHandler(client, msgHandlers, compressionHandlers));
                clientThread.start();
            }
        }
    }
}
