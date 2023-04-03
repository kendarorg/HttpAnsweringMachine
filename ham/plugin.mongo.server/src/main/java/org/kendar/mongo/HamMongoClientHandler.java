package org.kendar.mongo;

import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.utils.LoggerBuilder;

import java.net.Socket;
import java.util.List;

public class HamMongoClientHandler extends MongoClientHandler{
    public HamMongoClientHandler(Socket client,
                                 List<MsgHandler> msgHandlers,
                                 List<CompressionHandler> compressionHandlers,
                                 LoggerBuilder loggerBuilder) {
        super(client, msgHandlers, compressionHandlers, loggerBuilder);
    }

    @Override
    protected MongoPacket readFromMongo() {
        return null;
    }

    @Override
    protected void writeToMongoDb(MongoPacket clientPacket) {

    }

    @Override
    protected void connectToClient() {

    }
}
