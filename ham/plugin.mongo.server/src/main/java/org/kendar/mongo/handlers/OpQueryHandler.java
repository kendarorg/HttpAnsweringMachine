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
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.model.QueryPacket;
import org.springframework.stereotype.Component;

@Component
public class OpQueryHandler implements MsgHandler{
    @Override
    public OpCodes getOpCode() {
        return OpCodes.OP_QUERY;
    }

    @Override
    public MongoPacket<?> handleMsg(int requestId,int responseTo,ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet, int length) {
        try {
            var realPacket = new QueryPacket();
            realPacket.setPayload(packet.getPayload());
            realPacket.setHeader(packet.getHeader());
            realPacket.setOpCode(OpCodes.OP_QUERY);
            realPacket.setRequestId(requestId);
            realPacket.setResponseTo(responseTo);
            realPacket.setFlagBits(bsonInput.readInt32());
            String fullCollectionName = bsonInput.readCString();
            realPacket.setFullCollectionName(fullCollectionName);
            realPacket.setNumberToSkip(bsonInput.readInt32());
            realPacket.setNumberToReturn(bsonInput.readInt32());

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
            BsonDocument query = documentCodec.decode(bsonReader, DecoderContext.builder().build());


            // Convert BSON document to JSON
            String json = query.toJson(JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build());
            realPacket.setJson(json);
            return realPacket;
        } catch (Exception e) {
            throw new RuntimeException("Error decoding BSON query message ",e);
        }
    }
}
