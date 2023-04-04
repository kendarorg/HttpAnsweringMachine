package org.kendar.mongo.model;

import org.kendar.janus.serialization.TypedSerializer;
import org.kendar.mongo.handlers.OpCodes;

public class CompressedMongoPacket extends MongoPacket<CompressedMongoPacket> {


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
    }

    @Override
    public CompressedMongoPacket deserialize(TypedSerializer typedSerializer) {
        super.deserialize(typedSerializer);
        originalOpCode = typedSerializer.read("originalOpCode");
        compressed = typedSerializer.read("compressed");
        return this;
    }
}
