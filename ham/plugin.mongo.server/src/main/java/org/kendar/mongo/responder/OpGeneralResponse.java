package org.kendar.mongo.responder;

import org.kendar.mongo.model.MongoPacket;


public class OpGeneralResponse {
    private MongoPacket result;
    private boolean finalMessage;

    public OpGeneralResponse(MongoPacket result, boolean finalMessage) {

        this.result = result;
        this.finalMessage = finalMessage;
    }

    public MongoPacket getResult() {
        return result;
    }

    public void setResult(MongoPacket result) {
        this.result = result;
    }

    public boolean isFinalMessage() {
        return finalMessage;
    }

    public void setFinalMessage(boolean finalMessage) {
        this.finalMessage = finalMessage;
    }
}
