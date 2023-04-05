package org.kendar.mongo.responder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.bson.BsonDocument;
import org.bson.Document;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.model.QueryPacket;

public class OpQueryResponder implements MongoResponder{
    @Override
    public OpCodes getOpCode() {
        return OpCodes.OP_QUERY;
    }

    private ObjectMapper mapper = new ObjectMapper();

    private String getDb(QueryPacket msgPacket)
    {
        try {
            var jsonTree = mapper.readTree(
                    msgPacket.getJson());
            var db = (JsonNode) jsonTree.get("$db");
            if(db==null)return "admin";
            return db.asText();

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public MongoPacket canRespond(MongoPacket clientPacket, MongoClient mongoClient, long connectionId) {
        var msgPacket = (QueryPacket)clientPacket;
        var db= getDb(msgPacket);
        var database = mongoClient.getDatabase(db);
        var command = (BsonDocument)BsonDocument.parse(msgPacket.getJson());
        if(command.containsKey("isMaster"))return null;
        Document commandResult = database.runCommand(command);
        return null;
    }
}
