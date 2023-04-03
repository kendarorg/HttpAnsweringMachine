package org.kendar.mongo.handlers;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import org.bson.BsonBinaryReader;
import org.bson.BsonDocument;
import org.bson.ByteBuf;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.io.ByteBufferBsonInput;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.model.packets.QueryMongoPacket;
import org.springframework.stereotype.Component;

@Component
public class OpQueryHandler implements MsgHandler{
    @Override
    public OpCodes getOpCode() {
        return OpCodes.OP_QUERY;
    }

    @Override
    public void handleMsg(ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet, int length) {
        try {
            var payload = new QueryMongoPacket();
            payload.setFlagBits(bsonInput.readInt32());
            String fullCollectionName = bsonInput.readCString();
            payload.setFullCollectionName(fullCollectionName);
            payload.setNumberToSkip(bsonInput.readInt32());
            payload.setNumberToReturn(bsonInput.readInt32());

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
            BsonDocument query = documentCodec.decode(bsonReader, DecoderContext.builder().build());

            MongoNamespace namespace = new MongoNamespace(fullCollectionName);

            // Convert BSON document to JSON
            String json = query.toJson();
            payload.setJson(json);
            packet.setMessage(payload);
        } catch (Exception e) {
            System.err.println("Error decoding BSON query message: " + e.getMessage());
        }
    }
}
