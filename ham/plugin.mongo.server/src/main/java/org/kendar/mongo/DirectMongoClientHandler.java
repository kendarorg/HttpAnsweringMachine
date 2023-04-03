package org.kendar.mongo;

import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.utils.LoggerBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class DirectMongoClientHandler extends MongoClientHandler {

    private Socket clientSocket;
    private OutputStream toMongoDb;
    private InputStream fromMongoDb;

    public DirectMongoClientHandler(Socket client,
                                    List<MsgHandler> msgHandlers,
                                    List<CompressionHandler> compressionHandlers,
                                    LoggerBuilder loggerBuilder) {
        super(client, msgHandlers, compressionHandlers, loggerBuilder);
    }

    protected MongoPacket readFromMongo() {
        try {
            byte[] mongoHeaderBytes = new byte[16];
            readBytes(fromMongoDb, mongoHeaderBytes);
            return readPacketsFromStream(fromMongoDb, mongoHeaderBytes);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void writeToMongoDb(MongoPacket clientPacket) {

        try {
            toMongoDb.write(clientPacket.getHeader());
            toMongoDb.write(clientPacket.getPayload());
            toMongoDb.flush();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void connectToClient() {
        try {
            clientSocket = new Socket("localhost", 27017);
            toMongoDb = clientSocket.getOutputStream();
            fromMongoDb = clientSocket.getInputStream();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
