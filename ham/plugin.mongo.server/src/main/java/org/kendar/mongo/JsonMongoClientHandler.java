package org.kendar.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.model.payloads.MsgDocumentPayload;
import org.kendar.mongo.model.MsgPacket;
import org.kendar.mongo.model.payloads.MsgSectionPayload;
import org.kendar.utils.LoggerBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class JsonMongoClientHandler extends MongoClientHandler {

    private Socket clientSocket;
    private OutputStream toMongoDb;
    private InputStream fromMongoDb;
    private String targetIp;
    private int targetPort;

    public JsonMongoClientHandler(Socket client,
                                  List<MsgHandler> msgHandlers,
                                  List<CompressionHandler> compressionHandlers,
                                  LoggerBuilder loggerBuilder) {
        super(client, msgHandlers, compressionHandlers, loggerBuilder);
    }

    private MongoClient mongoClient;

    protected MongoPacket mongoRoundTrip(MongoPacket clientPacket) {
        Object otherResult = null;
        try {
            if(mongoClient==null){
                mongoClient = MongoClients.create("mongodb://127.0.0.1:27017");
            }
             if(clientPacket.getOpCode()== OpCodes.OP_MSG){
                 return translateOpMsg((MsgPacket) clientPacket);
             }else {
                var header = clientPacket.getHeader();// (byte[]) createHeader(clientPacket);
                var payload = clientPacket.getPayload();//byte[]) createPayload(clientPacket);
                toMongoDb.write(header);
                toMongoDb.write(payload);
                toMongoDb.flush();
            }
            byte[] mongoHeaderBytes = new byte[16];
            readBytes(fromMongoDb, mongoHeaderBytes);
            var result = readPacketsFromStream(fromMongoDb, mongoHeaderBytes);
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private MsgPacket translateOpMsg(MsgPacket clientPacket) {
        var msgPacket = clientPacket;
        var db= getDb(msgPacket);
        var database = mongoClient.getDatabase(db);
        var docPayload = (MsgDocumentPayload)msgPacket.getPayloads().get(0);

        var command = (BsonDocument)BsonDocument.parse(docPayload.getJson());
        if(msgPacket.getPayloads().size()>0) {

            for (var i = 1; i < msgPacket.getPayloads().size(); i++) {
                var pack = msgPacket.getPayloads().get(i);
                if(pack instanceof MsgDocumentPayload) {
                    throw new RuntimeException("MISSING MsgDocumentPayload");
//                    var tl = new BsonArray();
//                    var doc = (MsgDocumentPayload) pack;
//                    var bdoc = (BsonDocument) BsonDocument.parse(doc.getJson());
//                    tl.add(bdoc);
                }else{
                    var doc = (MsgSectionPayload) pack;
                    var tl = new BsonArray();
                    for(var j=0;j<doc.getDocuments().size();j++){
                        var bdoc = (BsonDocument) BsonDocument.parse(doc.getDocuments().get(j).getJson());
                        tl.add(bdoc);
                    }
                    command.put("documents",tl);
                    //tl.add(bdoc);
                }
            }
        }

        command.remove("$db");
        command.remove("lsid");
        Document commandResult = database.runCommand(command);
        var result = new MsgPacket();
        var  responseDocPayload = new MsgDocumentPayload();
        responseDocPayload.setJson(commandResult.toJson());
        result.getPayloads().add(responseDocPayload);
        result.setResponseTo(msgPacket.getRequestId());
        //var packet =new MongoPacket();
        //packet.setMessage(result);
        //toMongoDb.write(result.serialize());
        System.out.println("test");
        return result;
    }

    private ObjectMapper mapper = new ObjectMapper();

    private String getDb(MsgPacket message) {
        try {
            for (var payload : message.getPayloads()) {
                if (payload instanceof MsgDocumentPayload) {
                    var jsonTree = mapper.readTree(
                            ((MsgDocumentPayload) payload).getJson());
                    var db = (JsonNode)jsonTree.get("$db");
                    return db.asText();
                }

            }
        }catch (Exception ex){

        }
        return null;
    }

    protected void connectToClient() {
        try {
            clientSocket = new Socket(targetIp, targetPort);
            toMongoDb = clientSocket.getOutputStream();
            fromMongoDb = clientSocket.getInputStream();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setTarget(String targetIp, int targetPort) {

        this.targetIp = targetIp;
        this.targetPort = targetPort;
    }
}
