package org.kendar.mongo.handlers;

import org.bson.ByteBuf;
import org.bson.io.ByteBufferBsonInput;
import org.kendar.mongo.model.MongoPacket;

public interface MsgHandler {
    int getOpCode();
    void handleMsg(ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet, int length);

}
