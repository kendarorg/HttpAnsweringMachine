package org.kendar.mongo.model;

import org.kendar.janus.serialization.TypedSerializer;

import java.util.ArrayList;
import java.util.List;

public class ReplyPacket extends MongoPacket<ReplyPacket> {
    private List<String> jsons = new ArrayList<>();
    private int responseFlags;
    private long cursorId;
    private int startingFrom;
    private int numberReturned;

    public void setResponseFlags(int responseFlags) {
        this.responseFlags = responseFlags;
    }

    public int getResponseFlags() {
        return responseFlags;
    }

    public void setCursorId(long cursorId) {
        this.cursorId = cursorId;
    }

    public long getCursorId() {
        return cursorId;
    }

    public void setStartingFrom(int sTartingFrom) {
        this.startingFrom = sTartingFrom;
    }

    public int getStartingFrom() {
        return startingFrom;
    }

    public void setNumberReturned(int numberReturned) {
        this.numberReturned = numberReturned;
    }

    public int getNumberReturned() {
        return numberReturned;
    }

    public List<String> getJsons() {
        return jsons;
    }

    public void setJsons(List<String> jsons) {
        this.jsons = jsons;
    }

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        super.serialize(typedSerializer);
        typedSerializer.write("jsons",jsons);
        typedSerializer.write("numberReturned",numberReturned);
        typedSerializer.write("startingFrom",startingFrom);
        typedSerializer.write("cursorId",cursorId);
        typedSerializer.write("responseFlags",responseFlags);
    }

    @Override
    public ReplyPacket deserialize(TypedSerializer typedSerializer) {
        super.deserialize(typedSerializer);
        jsons = typedSerializer.read("jsons");
        numberReturned = typedSerializer.read("numberReturned");
        startingFrom = typedSerializer.read("startingFrom");
        cursorId = typedSerializer.read("cursorId");
        responseFlags = typedSerializer.read("responseFlags");
        return this;
    }
}
