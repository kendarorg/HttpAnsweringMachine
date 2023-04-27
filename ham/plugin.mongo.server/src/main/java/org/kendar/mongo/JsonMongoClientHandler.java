package org.kendar.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.config.MongoProxy;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.responder.MongoResponder;
import org.kendar.mongo.responder.OpGeneralResponse;
import org.kendar.utils.LoggerBuilder;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonMongoClientHandler extends MongoClientHandler {


    private final Map<OpCodes, List<MongoResponder>> responders;
    private final MongoProxy proxy;
    private MongoClient mongoClient;

    public JsonMongoClientHandler(MongoProxy proxy,
                                  List<MsgHandler> msgHandlers,
                                  List<CompressionHandler> compressionHandlers,
                                  LoggerBuilder loggerBuilder,
                                  List<MongoResponder> responders) {
        super(null, msgHandlers, compressionHandlers, loggerBuilder);
        this.proxy = proxy;
        this.responders = new HashMap<>();
        for (var responder : responders) {
            if (!this.responders.containsKey(responder.getOpCode())) {
                this.responders.put(responder.getOpCode(), new ArrayList<>());
            }
            this.responders.get(responder.getOpCode()).add(responder);
        }
    }

    public JsonMongoClientHandler(Socket client, MongoProxy proxy,
                                  List<MsgHandler> msgHandlers,
                                  List<CompressionHandler> compressionHandlers,
                                  LoggerBuilder loggerBuilder,
                                  List<MongoResponder> responders) {
        super(client, msgHandlers, compressionHandlers, loggerBuilder);
        this.proxy = proxy;
        this.responders = new HashMap<>();
        for (var responder : responders) {
            if (!this.responders.containsKey(responder.getOpCode())) {
                this.responders.put(responder.getOpCode(), new ArrayList<>());
            }
            this.responders.get(responder.getOpCode()).add(responder);
        }
    }

    public OpGeneralResponse mongoRoundTrip(MongoPacket clientPacket, long connectionId) {
        try {
            if (mongoClient == null) {
                var remote = proxy.getRemote();
                mongoClient = MongoClients.create(remote.getConnectionString());
            }
            var responderByOpCode = this.responders.get(clientPacket.getOpCode());
            if (responderByOpCode != null) {
                for (var responder : responderByOpCode) {
                    var res = responder.canRespond(clientPacket, mongoClient, connectionId);

                    if (res != null) {
                        if (res.isFinalMessage()) {
                            mongoClient.close();
                            mongoClient = null;
                        }
                        return res;
                    }
                }
            }
            throw new RuntimeException("Missing handler for opcode " + responderByOpCode);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    protected void connectToClient() {

    }


    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
