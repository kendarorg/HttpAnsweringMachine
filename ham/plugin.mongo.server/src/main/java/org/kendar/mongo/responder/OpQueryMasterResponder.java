package org.kendar.mongo.responder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.kendar.mongo.MongoClientHandler;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.model.QueryPacket;
import org.kendar.mongo.model.ReplyPacket;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class OpQueryMasterResponder implements MongoResponder {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public OpCodes getOpCode() {
        return OpCodes.OP_QUERY;
    }

    private String getDb(QueryPacket msgPacket) {
        try {
            var jsonTree = mapper.readTree(
                    msgPacket.getJson());
            var db = (JsonNode) jsonTree.get("$db");
            if (db == null) return "admin";
            return db.asText();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public OpGeneralResponse canRespond(MongoPacket clientPacket, MongoClient mongoClient, long connectionId) {
        var msgPacket = (QueryPacket) clientPacket;
        var db = getDb(msgPacket);
        var database = mongoClient.getDatabase(db);
        var command = (BsonDocument) BsonDocument.parse(msgPacket.getJson());
        if (!command.containsKey("isMaster")) return null;
        var serverDescription = mongoClient.getClusterDescription().getServerDescriptions().get(0);

        Bson findCommand = new BsonDocument("hostInfo", new BsonInt64(1));
        Document commandResult = database.runCommand(findCommand);

        var result = new HashMap<String, Object>();
        result.put("ismaster", true);
        result.put("maxBsonObjectSize", serverDescription.getMaxDocumentSize());
        result.put("maxMessageSizeBytes", 48000000);
        result.put("maxWriteBatchSize", 100000);
        result.put("localTime", ((Document) commandResult.get("system")).get("currentTime"));
        result.put("logicalSessionTimeoutMinutes", mongoClient.getClusterDescription().getLogicalSessionTimeoutMinutes());
        result.put("connectionId", (int) connectionId);
        result.put("minWireVersion", 0);
        result.put("maxWireVersion", 8);
        result.put("readOnly", false);
        result.put("ok", 1.0);

        var toret = new ReplyPacket();
        toret.setResponseFlags(8);
        toret.setCursorId(0);
        toret.setStartingFrom(0);
        toret.setNumberReturned(1);
        toret.setRequestId((int) MongoClientHandler.getRequestCounter());
        toret.setResponseTo(msgPacket.getRequestId());
        try {
            toret.getJsons().add(mapper.writeValueAsString(result));
        } catch (Exception ex) {
        }
        //Document commandResult = database.runCommand(command);
        return new OpGeneralResponse(toret, false);
    }
}
