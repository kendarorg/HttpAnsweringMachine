package org.kendar.mongo.model;

public class MongoPacket {
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
}
