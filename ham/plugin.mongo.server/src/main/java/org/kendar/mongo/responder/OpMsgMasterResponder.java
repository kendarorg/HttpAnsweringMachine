package org.kendar.mongo.responder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.kendar.mongo.MongoClientHandler;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.model.MsgPacket;
import org.kendar.mongo.model.payloads.MsgDocumentPayload;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class OpMsgMasterResponder implements MongoResponder {
    private final Logger logger;
    private final ObjectMapper mapper = new ObjectMapper();

    public OpMsgMasterResponder(LoggerBuilder loggerBuilder) {
        logger = loggerBuilder.build(OpMsgMasterResponder.class);
    }

    @Override
    public OpCodes getOpCode() {
        return OpCodes.OP_MSG;
    }

    private String getDb(MsgPacket message) {
        try {
            for (var payload : message.getPayloads()) {
                if (payload instanceof MsgDocumentPayload) {
                    var jsonTree = mapper.readTree(
                            ((MsgDocumentPayload) payload).getJson());
                    var db = (JsonNode) jsonTree.get("$db");
                    return db.asText();
                }

            }
        } catch (Exception ex) {

        }
        return "admin";
    }

    @Override
    public OpGeneralResponse canRespond(MongoPacket clientPacket, MongoClient mongoClient, long connectionId) {
        var msgPacket = (MsgPacket) clientPacket;
        var db = getDb(msgPacket);
        var database = mongoClient.getDatabase(db);
        if (database == null) {
            try {
                logger.error("Unable to find db for " + mapper.writeValueAsString(msgPacket));
            } catch (JsonProcessingException e) {

            }
            return null;
        }
        var docPayload = (MsgDocumentPayload) msgPacket.getPayloads().get(0);
        var command = (BsonDocument) BsonDocument.parse(docPayload.getJson());
        var finalMessage = command.containsKey("endSession");
        if (command.get("isMaster") == null) {
            return null;
        }

        var serverDescription = mongoClient.getClusterDescription().getServerDescriptions().get(0);

        Bson findCommand = new BsonDocument("hostInfo", new BsonInt64(1));
        Document commandResult = database.runCommand(findCommand);

        var resultMap = new Document();
        resultMap.put("ismaster", true);
        resultMap.put("maxBsonObjectSize", serverDescription.getMaxDocumentSize());
        resultMap.put("maxMessageSizeBytes", 48000000);
        resultMap.put("maxWriteBatchSize", 100000);
        resultMap.put("localTime", ((Document) commandResult.get("system")).get("currentTime"));
        resultMap.put("logicalSessionTimeoutMinutes", mongoClient.getClusterDescription().getLogicalSessionTimeoutMinutes());
        resultMap.put("connectionId", (int) connectionId);
        resultMap.put("minWireVersion", 0);
        resultMap.put("maxWireVersion", 8);
        resultMap.put("readOnly", false);
        resultMap.put("ok", 1.0);


        var result = new MsgPacket();
        var responseDocPayload = new MsgDocumentPayload();
        responseDocPayload.setJson(resultMap.toJson(JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build()));
        result.getPayloads().add(responseDocPayload);
        result.setResponseTo(msgPacket.getRequestId());
        result.setRequestId((int) MongoClientHandler.getRequestCounter());
        return new OpGeneralResponse((MongoPacket) result, finalMessage);
    }
}
