package org.kendar.mongo.responder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.kendar.mongo.MongoClientHandler;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.model.MsgPacket;
import org.kendar.mongo.model.payloads.MsgDocumentPayload;
import org.kendar.mongo.model.payloads.MsgSectionPayload;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class OpMsgResponder implements MongoResponder{
    private final Logger logger;

    public OpMsgResponder(LoggerBuilder loggerBuilder) {
        logger = loggerBuilder.build(OpMsgResponder.class);
    }

    @Override
    public OpCodes getOpCode() {
        return OpCodes.OP_MSG;
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
        return "admin";
    }

    @Override
    public OpGeneralResponse canRespond(MongoPacket clientPacket, MongoClient mongoClient, long connectionId) {
        var msgPacket = (MsgPacket)clientPacket;
        var db= getDb(msgPacket);
        var database = mongoClient.getDatabase(db);
        if(database==null){
            try {
                logger.error("Unable to find db for "+mapper.writeValueAsString(msgPacket));
            } catch (JsonProcessingException e) {

            }
            return null;
        }
        var docPayload = (MsgDocumentPayload)msgPacket.getPayloads().get(0);
        var command = (BsonDocument)BsonDocument.parse(docPayload.getJson());
        var finalMessage = command.containsKey("endSession");

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
        responseDocPayload.setJson(commandResult.toJson(JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build()));
        result.getPayloads().add(responseDocPayload);
        result.setResponseTo(msgPacket.getRequestId());
        result.setRequestId((int)MongoClientHandler.getRequestCounter());
        return new OpGeneralResponse((MongoPacket)result,finalMessage);
    }
}
