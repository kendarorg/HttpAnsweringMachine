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
import org.kendar.mongo.model.MsgPacket;
import org.kendar.mongo.model.payloads.MsgDocumentPayload;
import org.kendar.mongo.model.payloads.MsgSectionPayload;
import org.springframework.stereotype.Component;

@Component
public class OpMsgHandler implements MsgHandler {
    @Override
    public OpCodes getOpCode() {
        return OpCodes.OP_MSG;
    }

    @Override
    public MongoPacket<?> handleMsg(int requestId, int responseTo, ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet, int length) {
        try {

            var realPacket = new MsgPacket();
            realPacket.setPayload(packet.getPayload());
            realPacket.setHeader(packet.getHeader());
            realPacket.setOpCode(OpCodes.OP_MSG);
            realPacket.setRequestId(requestId);
            realPacket.setResponseTo(responseTo);
            int flagBits = bsonInput.readInt32();
            realPacket.setFlagBits(flagBits);

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());

            while (byteBuffer.position() < length) {
                var remaining = length - byteBuffer.position();
                if (remaining == 4) {
                    realPacket.setChecksum(bsonInput.readInt32());
                    //Only checksum
                    break;
                } else {
                    int payloadType = bsonInput.readByte();
                    //int payloadType2 = bsonInput.readByte();
                    if (payloadType == 0) {
                        BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
                        BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
                        BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                        String json = document.toJson(JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build());
                        var pl = new MsgDocumentPayload();
                        pl.setJson(json);
                        realPacket.getPayloads().add(pl);
                    } else if (payloadType == 1) {
                        var pl = new MsgSectionPayload();
                        realPacket.getPayloads().add(pl);

                        pl.setLength(bsonInput.readInt32());

                        var end = byteBuffer.position() + pl.getLength() - 4;
                        pl.setTitle(bsonInput.readCString());

                        while (byteBuffer.position() < end) {
                            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
                            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
                            BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                            String json = document.toJson(JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build());
                            var doc = new MsgDocumentPayload();
                            doc.setJson(json);
                            pl.getDocuments().add(doc);
                        }
                    }
                }
                //String title = bsonInput.readCString();

            }
            return realPacket;
        } catch (Exception e) {
            throw new RuntimeException("Error decoding BSON message", e);
        }
    }
}
