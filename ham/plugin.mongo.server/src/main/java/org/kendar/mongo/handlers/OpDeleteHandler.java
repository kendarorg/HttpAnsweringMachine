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
import org.springframework.stereotype.Component;

@Component
public class OpDeleteHandler implements MsgHandler{
    @Override
    public OpCodes getOpCode() {
        return OpCodes.OP_DELETE;
    }

    @Override
    public MongoPacket<?> handleMsg(ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet, int length) {
        try {
            System.out.println("======HANDLE DELETE");
            bsonInput.readInt32(); // skip ZERO
            String fullCollectionName = bsonInput.readCString();
            int flagBits = bsonInput.readInt32();

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);

            BsonDocument selector = documentCodec.decode(bsonReader, DecoderContext.builder().build());

            MongoNamespace namespace = new MongoNamespace(fullCollectionName);

            // Convert BSON document to JSON
            String selectorJson = selector.toJson();

            // Print out the JSON representation of the message
            System.out.println("Namespace: " + namespace);
            System.out.println("Delete JSON: " + selectorJson);
            return packet;
        } catch (Exception e) {
            throw new RuntimeException("Error decoding BSON delete message",e);
        }
    }
}
