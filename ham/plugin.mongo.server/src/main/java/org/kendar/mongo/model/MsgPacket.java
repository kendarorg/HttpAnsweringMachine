package org.kendar.mongo.model;

import org.kendar.janus.serialization.TypedSerializer;
import org.kendar.mongo.model.payloads.BaseMsgPayload;

import java.util.ArrayList;
import java.util.List;

public class MsgPacket extends MongoPacket<MsgPacket> {

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

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        super.serialize(typedSerializer);
        typedSerializer.write("flagBits",flagBits);
        typedSerializer.write("checksum",checksum);
        typedSerializer.write("payloads",payloads);
        typedSerializer.write("requestId",requestId);
        typedSerializer.write("responseTo",responseTo);
    }

    @Override
    public MsgPacket deserialize(TypedSerializer typedSerializer) {
        super.deserialize(typedSerializer);
        flagBits = typedSerializer.read("flagBits");
        checksum = typedSerializer.read("checksum");
        payloads = typedSerializer.read("payloads");
        requestId = typedSerializer.read("requestId");
        responseTo = typedSerializer.read("responseTo");
        return this;
    }

    private int requestId;
    private int responseTo;

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setResponseTo(Integer responseTo) {
        this.responseTo = responseTo;
    }

    public int getResponseTo() {
        return responseTo;
    }
}
