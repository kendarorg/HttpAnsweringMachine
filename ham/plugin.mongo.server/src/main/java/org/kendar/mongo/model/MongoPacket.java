package org.kendar.mongo.model;

import org.kendar.janus.serialization.TypedSerializable;
import org.kendar.janus.serialization.TypedSerializer;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.mongo.model.packets.BaseMongoPacket;

public class MongoPacket implements TypedSerializable<MongoPacket> {
    private OpCodes opCode;

    public BaseMongoPacket getMessage() {
        return message;
    }

    public boolean isFinale(){
        for(var i=0;i<header.length;i++){
            if(header[i]!=0x00)return false;
        }
        return true;
    }

    public void setMessage(BaseMongoPacket message) {
        this.message = message;
    }

    private BaseMongoPacket message;
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
        typedSerializer.write("message",message);
        typedSerializer.write("header",header);
        typedSerializer.write("payload",payload);
    }

    @Override
    public MongoPacket deserialize(TypedSerializer typedSerializer) {
        opCode = typedSerializer.read("opCode");
        message = typedSerializer.read("message");
        header = typedSerializer.read("header");
        payload = typedSerializer.read("payload");
        return this;
    }
}
