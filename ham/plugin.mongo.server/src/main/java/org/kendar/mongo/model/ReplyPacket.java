package org.kendar.mongo.model;

import java.util.ArrayList;
import java.util.List;

public class ReplyPacket implements BaseMongoPacket{
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
}
