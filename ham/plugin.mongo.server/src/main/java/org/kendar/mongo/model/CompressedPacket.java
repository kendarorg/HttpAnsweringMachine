package org.kendar.mongo.model;

import org.kendar.janus.serialization.TypedSerializer;
import org.kendar.mongo.handlers.OpCodes;

public class CompressedPacket extends MongoPacket<CompressedPacket> {


    private MongoPacket<?> compressed;
    public void setCompressed(MongoPacket<?> compressed) {
        this.compressed = compressed;
    }

    public MongoPacket<?> getCompressed() {
        return compressed;
    }
    private OpCodes originalOpCode;

    public void setOriginalOpCode(OpCodes originalOpCode) {
        this.originalOpCode = originalOpCode;
    }

    public OpCodes getOriginalOpCode() {
        return originalOpCode;
    }

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        super.serialize(typedSerializer);
        typedSerializer.write("originalOpCode",originalOpCode);
        typedSerializer.write("compressed",compressed);
        typedSerializer.write("requestId",requestId);
        typedSerializer.write("responseTo",responseTo);
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
