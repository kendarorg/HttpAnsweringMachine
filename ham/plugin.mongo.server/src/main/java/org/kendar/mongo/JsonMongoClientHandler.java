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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonMongoClientHandler extends MongoClientHandler {


    private Map<OpCodes,List<MongoResponder>> responders;
    private MongoProxy proxy;

    public JsonMongoClientHandler(MongoProxy proxy,
                                  List<MsgHandler> msgHandlers,
                                  List<CompressionHandler> compressionHandlers,
                                  LoggerBuilder loggerBuilder,
                                  List<MongoResponder> responders) {
        super(null, msgHandlers, compressionHandlers, loggerBuilder);
        this.proxy = proxy;
        this.responders = new HashMap<>();
        for(var responder:responders){
            if(!this.responders.containsKey(responder.getOpCode())){
                this.responders.put(responder.getOpCode(),new ArrayList<>());
            }
            this.responders.get(responder.getOpCode()).add(responder);
        }
    }

    private MongoClient mongoClient;

    public OpGeneralResponse mongoRoundTrip(MongoPacket clientPacket, long connectionId) {
        try {
            if(mongoClient==null){
                var remote = proxy.getRemote();
                mongoClient = MongoClients.create(remote.getConnectionString());
            }
            var responderByOpCode = this.responders.get(clientPacket.getOpCode());
            if(responderByOpCode!=null){
                for(var responder: responderByOpCode ){
                    var res= responder.canRespond(clientPacket,mongoClient,connectionId);

                    if(res!=null){
                        if(res.isFinalMessage()){
                            mongoClient.close();
                            mongoClient = null;
                        }
                        return res;
                    }
                }
            }
            throw new RuntimeException("Missing handler for opcode "+responderByOpCode);
//            var header = clientPacket.getHeader();// (byte[]) createHeader(clientPacket);
//            var payload = clientPacket.getPayload();//byte[]) createPayload(clientPacket);
//            toMongoDb.write(header);
//            toMongoDb.write(payload);
//            toMongoDb.flush();
//
//            byte[] mongoHeaderBytes = new byte[16];
//            readBytes(fromMongoDb, mongoHeaderBytes);
//            var result = readPacketsFromStream(fromMongoDb, mongoHeaderBytes);
//            return new OpGeneralResponse(result,false);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }





    protected void connectToClient() {

    }



    public void close() {
        if(mongoClient!=null){
            mongoClient.close();
        }
    }
}
