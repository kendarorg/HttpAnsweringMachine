package org.kendar.mongo;

import org.kendar.events.EventQueue;
import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Component
public class HamMongoServer {
    public static final int DEFAULT_PORT = 27017;
    private final Logger logger;
    private final EventQueue eventQueue;
    private final List<MsgHandler> msgHandlers;
    private final List<CompressionHandler> compressionHandlers;
    private final LoggerBuilder loggerBuilder;
    private ServerSocket thisServer;

    public HamMongoServer(List<MsgHandler> msgHandlers,
                          List<CompressionHandler> compressionHandlers,
                          LoggerBuilder loggerBuilder,
                          EventQueue eventQueue) {

        this.msgHandlers = msgHandlers;
        this.compressionHandlers = compressionHandlers;
        this.loggerBuilder = loggerBuilder;
        this.logger = loggerBuilder.build(HamMongoServer.class);
        this.eventQueue = eventQueue;
    }

    public HamMongoServer clone() {
        return new HamMongoServer(
                msgHandlers, compressionHandlers, loggerBuilder,
                eventQueue);
    }

    public void run(int port, AnsweringMongoServer answeringMongoServer) throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            thisServer = server;
            logger.info("MongoDB server started on port " + port);

            while (answeringMongoServer.isActive()) {
                Socket client = server.accept();
                //client.setSoTimeout(5000);
                logger.debug("New client connected: " + client.getInetAddress().getHostAddress());
                // Handle the client connection in a separate thread
                Thread clientThread = new Thread(
                        new HamMongoClientHandler(client,
                                msgHandlers, compressionHandlers, loggerBuilder,
                                eventQueue, port));
                clientThread.start();
            }
        } catch (Exception ex) {
            logger.warn("Disconnected server " + port);
        }
    }

    public void close() {
        try {
            thisServer.close();
        } catch (Exception ex) {

        }
    }
}
