package org.kendar.mongo.model.payloads;

import org.bson.BsonDocument;
import org.kendar.typed.serializer.TypedSerializable;
import org.kendar.typed.serializer.TypedSerializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.kendar.mongo.model.MongoPacket.toBytes;

public class MsgDocumentPayload implements BaseMsgPayload, TypedSerializable<MsgDocumentPayload> {
    private String json;

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        typedSerializer.write("json", json);
    }

    @Override
    public MsgDocumentPayload deserialize(TypedSerializer typedSerializer) {
        json = typedSerializer.read("json");
        return this;
    }

    @Override
    public byte[] serialize() {

        ByteBuffer responseBuffer = ByteBuffer.allocate(64000);
        responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
        responseBuffer.put((byte) 0);
        responseBuffer.put(toBytes(BsonDocument.parse(json)));
        var length = responseBuffer.position();
        responseBuffer.position(0);
        var res = new byte[length];
        for (var i = 0; i < length; i++) {
            res[i] = responseBuffer.get();
        }
        return res;
    }
}
