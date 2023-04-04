package org.kendar.mongo.model;

import org.kendar.janus.serialization.TypedSerializer;

public class QueryPacket extends MongoPacket<QueryPacket> {
    private int flagBits;
    private String fullCollectionName;
    private int numberToSkip;
    private int numberToReturn;
    private String json;

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

    public void setNumberToSkip(int numberToSkip) {
        this.numberToSkip = numberToSkip;
    }

    public int getNumberToSkip() {
        return numberToSkip;
    }

    public void setNumberToReturn(int numberToReturn) {
        this.numberToReturn = numberToReturn;
    }

    public int getNumberToReturn() {
        return numberToReturn;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        super.serialize(typedSerializer);
        typedSerializer.write("flagBits",flagBits);
        typedSerializer.write("fullCollectionName",fullCollectionName);
        typedSerializer.write("numberToSkip",numberToSkip);
        typedSerializer.write("numberToReturn",numberToReturn);
        typedSerializer.write("json",json);
    }

    @Override
    public QueryPacket deserialize(TypedSerializer typedSerializer) {
        super.deserialize(typedSerializer);
        flagBits = typedSerializer.read("flagBits");
        fullCollectionName = typedSerializer.read("fullCollectionName");
        numberToSkip = typedSerializer.read("numberToSkip");
        numberToReturn = typedSerializer.read("numberToReturn");
        json = typedSerializer.read("json");
        return this;
    }
}
