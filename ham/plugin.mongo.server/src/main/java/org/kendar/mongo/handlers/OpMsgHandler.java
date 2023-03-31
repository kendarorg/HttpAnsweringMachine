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
import org.kendar.mongo.model.MsgDocumentPayload;
import org.kendar.mongo.model.MsgPacket;
import org.kendar.mongo.model.MsgSectionPayload;
import org.springframework.stereotype.Component;

@Component
public class OpMsgHandler implements MsgHandler {
    @Override
    public int getOpCode() {
        return OpCodes.OP_MSG;
    }

    @Override
    public void handleMsg(ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet, int length) {
        try {
            System.out.println("======HANDLE MESSAGE");

            var subPacket = new MsgPacket();
            packet.setMessage(subPacket);
            int flagBits = bsonInput.readInt32();
            subPacket.setFlagBits(flagBits);

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());



            System.out.println("FlagBits: " + flagBits);

            while (byteBuffer.position()<length) {
                var remaining = length - byteBuffer.position();
                if(remaining==4){
                    subPacket.setChecksum(bsonInput.readInt32());
                    //Only checksum
                    break;
                }else {
                    int payloadType = bsonInput.readByte();
                    //int payloadType2 = bsonInput.readByte();
                    if (payloadType == 0) {
                        BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
                        BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
                        BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                        String json = document.toJson();
                        var pl = new MsgDocumentPayload();
                        pl.setJson(json);
                        subPacket.getPayloads().add(pl);
                        System.out.println("Document JSON: " + json);
                    } else if (payloadType == 1) {
                        var pl = new MsgSectionPayload();
                        subPacket.getPayloads().add(pl);

                        pl.setLength(bsonInput.readInt32());

                        var end = byteBuffer.position()+pl.getLength() - 4;
                        pl.setTitle(bsonInput.readCString());
                        System.out.println(pl.getTitle()+":"+pl.getLength());

                        while(byteBuffer.position()<end) {
                            System.out.println("READING DOC");
                            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
                            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
                            BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                            String json = document.toJson();
                            var doc = new MsgDocumentPayload();
                            doc.setJson(json);
                            pl.getDocuments().add(doc);
                        }
                    }
                }
                //String title = bsonInput.readCString();

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error decoding BSON message: " + e.getMessage());
        }
        System.out.println("ENDOFMSG");
    }
}
