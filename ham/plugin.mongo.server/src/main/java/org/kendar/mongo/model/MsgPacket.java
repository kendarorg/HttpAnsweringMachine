package org.kendar.mongo.model;

import java.util.ArrayList;
import java.util.List;

public class MsgPacket implements BaseMongoPacket{

    public List<BaseMsgPayload> getPayloads() {
        return payloads;
    }

    public void setPayloads(List<BaseMsgPayload> payloads) {
        this.payloads = payloads;
    }

    private List<BaseMsgPayload> payloads = new ArrayList<>();
    private int flagBits;
    private int checksum;

    public void setFlagBits(int flagBits) {
        this.flagBits = flagBits;
    }

    public int getFlagBits() {
        return flagBits;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public int getChecksum() {
        return checksum;
    }
}
