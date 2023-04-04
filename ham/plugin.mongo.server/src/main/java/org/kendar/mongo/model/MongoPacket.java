package org.kendar.mongo.model;

import org.bson.ByteBufNIO;
import org.kendar.janus.serialization.TypedSerializable;
import org.kendar.janus.serialization.TypedSerializer;
import org.kendar.mongo.handlers.OpCodes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MongoPacket<T> implements TypedSerializable<T> {
    private OpCodes opCode;

    public boolean isFinale(){
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
}
