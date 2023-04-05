package org.kendar.mongo.responder;

import com.mongodb.client.MongoClient;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.mongo.model.MongoPacket;

public interface MongoResponder {
    OpCodes getOpCode();
    MongoPacket canRespond(MongoPacket mongoPacket, MongoClient mongoClient, long connectionId);
}
