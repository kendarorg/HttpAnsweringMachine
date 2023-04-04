package org.kendar.mongo.model;

import org.kendar.janus.serialization.TypedSerializable;
import org.kendar.janus.serialization.TypedSerializer;

public class MsgDocumentPayload implements BaseMsgPayload, TypedSerializable<MsgDocumentPayload> {
    private String json;

    public void setJson(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        typedSerializer.write("json",json);
    }

    @Override
    public MsgDocumentPayload deserialize(TypedSerializer typedSerializer) {
        json = typedSerializer.read("json");
        return this;
    }
}
