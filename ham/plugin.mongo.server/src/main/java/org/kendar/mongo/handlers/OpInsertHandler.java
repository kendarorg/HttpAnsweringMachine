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
import org.springframework.stereotype.Component;

@Component
public class OpInsertHandler implements MsgHandler{
    @Override
    public OpCodes getOpCode() {
        return OpCodes.OP_INSERT;
    }

    @Override
    public MongoPacket<?> handleMsg(int requestId,int responseTo,ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet, int length) {
        //System.out.println("======HANDLE INSERT");
        int flagBits = bsonInput.readInt32();
        String fullCollectionName = bsonInput.readCString();

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
        BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
        BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);

        MongoNamespace namespace = new MongoNamespace(fullCollectionName);

        //System.out.println("Namespace: " + namespace);

        while (byteBuffer.hasRemaining()) {
            BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
            String json =  document.toJson(JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build());
            //System.out.println("Insert JSON: " + json);
        }
        return packet;
    }
}
