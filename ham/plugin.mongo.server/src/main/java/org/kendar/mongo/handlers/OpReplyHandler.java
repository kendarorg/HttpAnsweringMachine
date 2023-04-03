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
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.model.packets.ReplyPacket;
import org.springframework.stereotype.Component;

@Component
public class OpReplyHandler implements MsgHandler{
    @Override
    public OpCodes getOpCode() {
        return OpCodes.OP_REPLY;
    }

    @Override
    public void handleMsg(ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet, int length) {
        try {

            int responseFlags = bsonInput.readInt32();
            long cursorId = bsonInput.readInt64();
            int startingFrom = bsonInput.readInt32();
            int numberReturned = bsonInput.readInt32();
            var replyPacket = new ReplyPacket();
            replyPacket.setResponseFlags(responseFlags);
            replyPacket.setCursorId(cursorId);
            replyPacket.setStartingFrom(startingFrom);
            replyPacket.setNumberReturned(numberReturned);
            packet.setMessage(replyPacket);


            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());



            for (int i = 0; i < numberReturned; i++) {
                BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
                BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
                BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                String json = document.toJson();
                replyPacket.getJsons().add(json);
            }
        } catch (Exception e) {
            System.err.println("Error decoding BSON reply message: " + e.getMessage());
        }
    }
}
