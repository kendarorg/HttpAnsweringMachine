package org.kendar.mongo;

import org.bson.Document;

public class MongoPacket {

    private final int requestId;
    private final int responseId;
    private final int opCode;
    private final byte[] payload;

    public int getRequestId() {
        return requestId;
    }

    public int getResponseId() {
        return responseId;
    }

    public int getOpCode() {
        return opCode;
    }

    public byte[] getPayload() {
        return payload;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    private Document document;

    public MongoPacket(int requestId, int responseId, int opCode, byte[] payload, Document document) {
        this.document = document;
        this.requestId = requestId;
        this.responseId = responseId;
        this.opCode = opCode;
        this.payload = payload;
    }

}
