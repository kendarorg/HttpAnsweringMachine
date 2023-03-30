package org.kendar.mongo;

import org.bson.Document;

public class MongoPacket {

    private int flagBits;
    private String fullCollectionName;
    private byte[] payload;
    private byte[] header;

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getResponseTo() {
        return responseTo;
    }

    public void setResponseTo(int responseTo) {
        this.responseTo = responseTo;
    }

    public int getOpCode() {
        return opCode;
    }

    public void setOpCode(int opCode) {
        this.opCode = opCode;
    }

    private int messageLength;
    private int requestId;
    private int responseTo;
    private int opCode;

    public MongoPacket(int messageLength, int requestId, int responseTo, int opCode) {

        this.messageLength = messageLength;
        this.requestId = requestId;
        this.responseTo = responseTo;
        this.opCode = opCode;
    }

    public void setFlagBits(int flagBits) {
        this.flagBits = flagBits;
    }

    public int getFlagBits() {
        return flagBits;
    }

    public void setFullCollectionName(String fullCollectionName) {
        this.fullCollectionName = fullCollectionName;
    }

    public String getFullCollectionName() {
        return fullCollectionName;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public byte[] getHeader() {
        return header;
    }
}
