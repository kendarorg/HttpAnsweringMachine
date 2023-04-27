package org.kendar.mongo.handlers;

import com.mongodb.MongoClientSettings;
import org.bson.BsonBinaryReader;
import org.bson.BsonDocument;
import org.bson.ByteBuf;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.io.ByteBufferBsonInput;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.model.ReplyPacket;
import org.springframework.stereotype.Component;

@Component
public class OpReplyHandler implements MsgHandler {
    @Override
    public OpCodes getOpCode() {
        return OpCodes.OP_REPLY;
    }

    @Override
    public MongoPacket<?> handleMsg(int requestId, int responseTo, ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet, int length) {
        try {

            int responseFlags = bsonInput.readInt32();
            long cursorId = bsonInput.readInt64();
            int startingFrom = bsonInput.readInt32();
            int numberReturned = bsonInput.readInt32();
            var replyPacket = new ReplyPacket();
            replyPacket.setRequestId(requestId);
            replyPacket.setResponseTo(responseTo);
            replyPacket.setResponseFlags(responseFlags);
            replyPacket.setCursorId(cursorId);
            replyPacket.setStartingFrom(startingFrom);
            replyPacket.setNumberReturned(numberReturned);
            replyPacket.setPayload(packet.getPayload());
            replyPacket.setHeader(packet.getHeader());
            replyPacket.setOpCode(OpCodes.OP_REPLY);


            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());


            for (int i = 0; i < numberReturned; i++) {
                BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
                BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
                BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                String json = document.toJson(JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build());
                replyPacket.getJsons().add(json);
            }
            return replyPacket;
        } catch (Exception e) {
            throw new RuntimeException("Error decoding BSON reply message", e);
        }
    }
}
