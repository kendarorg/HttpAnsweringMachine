package org.kendar.mongo.model.packets;

public class QueryMongoPacket implements BaseMongoPacket {
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
}
