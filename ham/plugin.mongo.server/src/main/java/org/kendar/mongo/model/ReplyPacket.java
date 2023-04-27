package org.kendar.mongo.model;

import org.bson.BsonDocument;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.typed.serializer.TypedSerializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ReplyPacket extends MongoPacket<ReplyPacket> implements MongoReqResPacket {
    private List<String> jsons = new ArrayList<>();
    private int responseFlags;
    private long cursorId;
    private int startingFrom;
    private int numberReturned;
    private int requestId;
    private int responseTo;

    public ReplyPacket() {
        setOpCode(OpCodes.OP_REPLY);
    }

    public int getResponseFlags() {
        return responseFlags;
    }

    public void setResponseFlags(int responseFlags) {
        this.responseFlags = responseFlags;
    }

    public long getCursorId() {
        return cursorId;
    }

    public void setCursorId(long cursorId) {
        this.cursorId = cursorId;
    }

    public int getStartingFrom() {
        return startingFrom;
    }

    public void setStartingFrom(int sTartingFrom) {
        this.startingFrom = sTartingFrom;
    }

    public int getNumberReturned() {
        return numberReturned;
    }

    public void setNumberReturned(int numberReturned) {
        this.numberReturned = numberReturned;
    }

    public List<String> getJsons() {
        return jsons;
    }

    public void setJsons(List<String> jsons) {
        this.jsons = jsons;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public int getResponseTo() {
        return responseTo;
    }

    public void setResponseTo(Integer responseTo) {
        this.responseTo = responseTo;
    }

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        super.serialize(typedSerializer);
        typedSerializer.write("jsons", jsons);
        typedSerializer.write("numberReturned", numberReturned);
        typedSerializer.write("startingFrom", startingFrom);
        typedSerializer.write("cursorId", cursorId);
        typedSerializer.write("responseFlags", responseFlags);
        typedSerializer.write("requestId", requestId);
        typedSerializer.write("responseTo", responseTo);
    }

    @Override
    public ReplyPacket deserialize(TypedSerializer typedSerializer) {
        super.deserialize(typedSerializer);
        jsons = typedSerializer.read("jsons");
        numberReturned = typedSerializer.read("numberReturned");
        startingFrom = typedSerializer.read("startingFrom");
        cursorId = typedSerializer.read("cursorId");
        responseFlags = typedSerializer.read("responseFlags");
        requestId = typedSerializer.read("requestId");
        responseTo = typedSerializer.read("responseTo");
        return this;
    }

    public byte[] serialize() {
        var msgLength = 16;
        ByteBuffer responseBuffer = ByteBuffer.allocate(64000);
        responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
        responseBuffer.putInt(responseFlags);
        responseBuffer.putLong(cursorId);
        responseBuffer.putInt(startingFrom);
        responseBuffer.putInt(jsons.size());
        for (int i = 0; i < jsons.size(); i++) {
            byte[] query = toBytes(BsonDocument.parse(jsons.get(i)));
            responseBuffer.put(query);
            msgLength += responseBuffer.position();
        }


        responseBuffer.flip();
        var length = responseBuffer.position();
        responseBuffer.position(0);
        var res = new byte[msgLength];
        for (var i = 16; i < msgLength; i++) {
            res[i] = responseBuffer.get();
        }
        //responseBuffer.get(res,16,length);

        var header = buildHeader(msgLength, requestId, responseTo, OpCodes.OP_REPLY);
        System.arraycopy(header, 0, res, 0, 16);
        return res;
    }
}
