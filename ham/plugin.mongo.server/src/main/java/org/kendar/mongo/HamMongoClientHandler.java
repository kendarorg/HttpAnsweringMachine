package org.kendar.mongo;

import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.utils.LoggerBuilder;

import java.net.Socket;
import java.util.List;

public class HamMongoClientHandler extends MongoClientHandler{

    private final JsonTypedSerializer serializer;

    public HamMongoClientHandler(Socket client,
                                 List<MsgHandler> msgHandlers,
                                 List<CompressionHandler> compressionHandlers,
                                 LoggerBuilder loggerBuilder) {
        super(client, msgHandlers, compressionHandlers, loggerBuilder);
        this.serializer = new JsonTypedSerializer();
    }

    @Override
    protected MongoPacket mongoRoundTrip(MongoPacket clientPacket) {
        //Find the destination port
        //Find (eventually) the db
        //Serialize the packet
        var ser = serializer.newInstance();
        ser.write("data",clientPacket);
        var toSend = (String)ser.getSerialized();
        //Send it via webApi
        var response = "";
        var deser = serializer.newInstance();
        deser.deserialize(response);
        var serverPacket = (MongoPacket)deser.read("data");
        return serverPacket;
    }

    @Override
    protected void connectToClient() {

    }
}
