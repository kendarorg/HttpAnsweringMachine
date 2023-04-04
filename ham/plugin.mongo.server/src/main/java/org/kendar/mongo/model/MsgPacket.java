package org.kendar.mongo.model;

import org.kendar.janus.serialization.TypedSerializable;
import org.kendar.janus.serialization.TypedSerializer;
import org.kendar.mongo.model.packets.BaseMongoPacket;

import java.util.ArrayList;
import java.util.List;

public class MsgPacket implements BaseMongoPacket, TypedSerializable<MsgPacket> {

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
        typedSerializer.write("flagBits",flagBits);
        typedSerializer.write("checksum",checksum);
        typedSerializer.write("payloads",payloads);
    }

    @Override
    public MsgPacket deserialize(TypedSerializer typedSerializer) {
        flagBits = typedSerializer.read("flagBits");
        checksum = typedSerializer.read("checksum");
        payloads = typedSerializer.read("payloads");
        return this;
    }
}
