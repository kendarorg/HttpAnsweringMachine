package org.kendar.mongo.model;

import com.mongodb.MongoClientSettings;
import org.bson.*;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.io.BasicOutputBuffer;
import org.kendar.janus.serialization.TypedSerializable;
import org.kendar.janus.serialization.TypedSerializer;
import org.kendar.mongo.handlers.OpCodes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class MongoPacket<T> implements TypedSerializable<T> {
    private OpCodes opCode;

    public boolean isFinale(){
        if(opCode==OpCodes.OP_NONE) return true;
        if(header==null) return false;
        for(var i=0;i<header.length;i++){
            if(header[i]!=0x00)return false;
        }
        return true;
    }
    private byte[] payload;

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    private byte[] header;

    public MongoPacket() {
    }

    public void setOpCode(OpCodes opCode) {

        this.opCode = opCode;
    }

    public OpCodes getOpCode() {
        return opCode;
    }

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        typedSerializer.write("opCode",opCode);
        typedSerializer.write("header",header);
        typedSerializer.write("payload",payload);
    }

    @Override
    public T deserialize(TypedSerializer typedSerializer) {
        opCode = typedSerializer.read("opCode");
        header = typedSerializer.read("header");
        payload = typedSerializer.read("payload");
        return (T)this;
    }

    public static byte[] buildHeader(int fullSize, int requestId, int responseTo, OpCodes originalOpCode) {
        ByteBuffer responseBuffer = ByteBuffer.allocate(16);
        responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
        responseBuffer.putInt(fullSize); // messageLength
        responseBuffer.putInt(requestId); // requestID
        responseBuffer.putInt(responseTo); // responseTo
        responseBuffer.putInt(originalOpCode.getValue()); // OP_REPLY
        responseBuffer.flip();
        responseBuffer.position(0);
        return responseBuffer.array();
    }

    private static final Codec<Document> DOCUMENT_CODEC = new DocumentCodec();

    public static byte[] toBytes(BsonDocument document) {
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
        BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(buffer);
        documentCodec.encode(writer, document, EncoderContext.builder().isEncodingCollectibleDocument(true).build());
        return buffer.toByteArray();
    }

    public static void writeCString(ByteBuffer buffer,String value) {
        writeCharacters(buffer,value, true);
    }

    public static int writeCharacters(ByteBuffer buffer,String str, boolean checkForNullCharacters) {
        int len = str.length();
        int total = 0;

        int c;
        for(int i = 0; i < len; i += Character.charCount(c)) {
            c = Character.codePointAt(str, i);
            if (checkForNullCharacters && c == 0) {
                throw new BsonSerializationException(String.format("BSON cstring '%s' is not valid because it contains a null character at index %d", str, i));
            }

            if (c < 128) {
                buffer.put((byte)c);
                ++total;
            } else if (c < 2048) {
                buffer.put((byte)(192 + (c >> 6)));
                buffer.put((byte)(128 + (c & 63)));
                total += 2;
            } else if (c < 65536) {
                buffer.put((byte)(224 + (c >> 12)));
                buffer.put((byte)(128 + (c >> 6 & 63)));
                buffer.put((byte)(128 + (c & 63)));
                total += 3;
            } else {
                buffer.put((byte)(240 + (c >> 18)));
                buffer.put((byte)(128 + (c >> 12 & 63)));
                buffer.put((byte)(128 + (c >> 6 & 63)));
                buffer.put((byte)(128 + (c & 63)));
                total += 4;
            }
        }

        buffer.put((byte)0);
        ++total;
        return total;
    }

    public byte[] serialize(){
        return new byte[]{};
    }
}
