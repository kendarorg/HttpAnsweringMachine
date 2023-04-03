package org.kendar.mongo;

import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.utils.LoggerBuilder;
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
    private LoggerBuilder loggerBuilder;

    public MongoServer(List<MsgHandler> msgHandlers, List<CompressionHandler> compressionHandlers, LoggerBuilder loggerBuilder){

        this.msgHandlers = msgHandlers;
        this.compressionHandlers = compressionHandlers;
        this.loggerBuilder = loggerBuilder;
    }

    public void run(int port,AnsweringMongoServer answeringMongoServer) throws IOException {
        try(ServerSocket server = new ServerSocket(port)) {
            System.out.println("MongoDB server started on port " + port);

            while (answeringMongoServer.shouldRun()) {
                Socket client = server.accept();
                System.out.println("New client connected: " + client.getInetAddress().getHostAddress());
                // Handle the client connection in a separate thread
                Thread clientThread = new Thread(new HamMongoClientHandler(client, msgHandlers, compressionHandlers, loggerBuilder));
                clientThread.start();
            }
        }
    }
}
