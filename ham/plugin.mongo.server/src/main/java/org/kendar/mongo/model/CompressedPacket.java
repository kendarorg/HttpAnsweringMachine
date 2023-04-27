package org.kendar.mongo.model;

import org.kendar.mongo.handlers.OpCodes;
import org.kendar.typed.serializer.TypedSerializer;

public class CompressedPacket extends MongoPacket<CompressedPacket> {


    private MongoPacket<?> compressed;
    private OpCodes originalOpCode;
    private int requestId;
    private int responseTo;

    public MongoPacket<?> getCompressed() {
        return compressed;
    }

    public void setCompressed(MongoPacket<?> compressed) {
        this.compressed = compressed;
    }

    public OpCodes getOriginalOpCode() {
        return originalOpCode;
    }

    public void setOriginalOpCode(OpCodes originalOpCode) {
        this.originalOpCode = originalOpCode;
    }

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        super.serialize(typedSerializer);
        typedSerializer.write("originalOpCode", originalOpCode);
        typedSerializer.write("compressed", compressed);
        typedSerializer.write("requestId", requestId);
        typedSerializer.write("responseTo", responseTo);
    }

    @Override
    public CompressedPacket deserialize(TypedSerializer typedSerializer) {
        super.deserialize(typedSerializer);
        originalOpCode = typedSerializer.read("originalOpCode");
        compressed = typedSerializer.read("compressed");
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
}
