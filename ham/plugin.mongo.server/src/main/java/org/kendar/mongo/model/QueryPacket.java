package org.kendar.mongo.model;

import org.bson.BsonDocument;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.typed.serializer.TypedSerializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class QueryPacket extends MongoPacket<QueryPacket> implements MongoReqResPacket {
    private int flagBits;
    private String fullCollectionName;
    private int numberToSkip;
    private int numberToReturn;
    private String json;
    private int requestId;
    private int responseTo;

    public QueryPacket() {
        setOpCode(OpCodes.OP_QUERY);
    }

    public int getFlagBits() {
        return flagBits;
    }

    public void setFlagBits(int flagBits) {
        this.flagBits = flagBits;
    }

    public String getFullCollectionName() {
        return fullCollectionName;
    }

    public void setFullCollectionName(String fullCollectionName) {
        this.fullCollectionName = fullCollectionName;
    }

    public int getNumberToSkip() {
        return numberToSkip;
    }

    public void setNumberToSkip(int numberToSkip) {
        this.numberToSkip = numberToSkip;
    }

    public int getNumberToReturn() {
        return numberToReturn;
    }

    public void setNumberToReturn(int numberToReturn) {
        this.numberToReturn = numberToReturn;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        super.serialize(typedSerializer);
        typedSerializer.write("flagBits", flagBits);
        typedSerializer.write("fullCollectionName", fullCollectionName);
        typedSerializer.write("numberToSkip", numberToSkip);
        typedSerializer.write("numberToReturn", numberToReturn);
        typedSerializer.write("json", json);
        typedSerializer.write("requestId", requestId);
        typedSerializer.write("responseTo", responseTo);
    }

    @Override
    public QueryPacket deserialize(TypedSerializer typedSerializer) {
        super.deserialize(typedSerializer);
        flagBits = typedSerializer.read("flagBits");
        fullCollectionName = typedSerializer.read("fullCollectionName");
        numberToSkip = typedSerializer.read("numberToSkip");
        numberToReturn = typedSerializer.read("numberToReturn");
        json = typedSerializer.read("json");
        requestId = typedSerializer.read("requestId");
        responseTo = typedSerializer.read("responseTo");
        return this;
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

    public byte[] serialize() {
        var msgLength = 16;
        ByteBuffer responseBuffer = ByteBuffer.allocate(64000);
        responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
        responseBuffer.putInt(flagBits);
        writeCString(responseBuffer, fullCollectionName);
        responseBuffer.putInt(numberToSkip);
        responseBuffer.putInt(numberToReturn);

        byte[] query = toBytes(BsonDocument.parse(json));
        responseBuffer.put(query);
        msgLength += responseBuffer.position();

        responseBuffer.flip();
        var length = responseBuffer.position();
        responseBuffer.position(0);
        var res = new byte[msgLength];
        for (var i = 16; i < msgLength; i++) {
            res[i] = responseBuffer.get();
        }

        var header = buildHeader(msgLength, requestId, responseTo, OpCodes.OP_QUERY);
        System.arraycopy(header, 0, res, 0, 16);
        return res;
    }
}
