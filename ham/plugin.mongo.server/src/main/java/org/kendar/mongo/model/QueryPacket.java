package org.kendar.mongo.model;

import org.bson.BsonDocument;
import org.kendar.janus.serialization.TypedSerializer;
import org.kendar.mongo.handlers.OpCodes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
        typedSerializer.write("requestId",requestId);
        typedSerializer.write("responseTo",responseTo);
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
    public QueryPacket(){
        setOpCode(OpCodes.OP_QUERY);
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

    public byte[] serialize(){
        var msgLength = 16;
        ByteBuffer responseBuffer = ByteBuffer.allocate(64000);
        responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
        responseBuffer.putInt(flagBits);
        writeCString(responseBuffer,fullCollectionName);
        responseBuffer.putInt(numberToSkip);
        responseBuffer.putInt(numberToReturn);

        byte[] query = toBytes(BsonDocument.parse(json));
        responseBuffer.put(query);
        msgLength += responseBuffer.position();

        responseBuffer.flip();
        var length = responseBuffer.position();
        var res = new byte[msgLength];
        responseBuffer.get(res,16,length);

        var header = buildHeader(msgLength,requestId,responseTo, OpCodes.OP_QUERY);
        for(var i =0;i<16;i++){
            res[i]=header[i];
        }
        return res;
    }
}
