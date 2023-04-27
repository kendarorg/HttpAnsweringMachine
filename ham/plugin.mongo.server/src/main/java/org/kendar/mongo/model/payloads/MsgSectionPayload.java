package org.kendar.mongo.model.payloads;

import org.kendar.typed.serializer.TypedSerializable;
import org.kendar.typed.serializer.TypedSerializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.kendar.mongo.model.MongoPacket.writeCString;

public class MsgSectionPayload implements BaseMsgPayload, TypedSerializable<MsgSectionPayload> {
    private List<MsgDocumentPayload> documents = new ArrayList<>();
    private int length;
    private String title;

    public List<MsgDocumentPayload> getDocuments() {
        return documents;
    }

    public void setDocuments(List<MsgDocumentPayload> documents) {
        this.documents = documents;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        typedSerializer.write("title", title);
        typedSerializer.write("length", length);
        typedSerializer.write("documents", documents);
    }

    @Override
    public MsgSectionPayload deserialize(TypedSerializer typedSerializer) {
        title = typedSerializer.read("title");
        length = typedSerializer.read("length");
        documents = typedSerializer.read("documents");
        return this;
    }

    @Override
    public byte[] serialize() {
        ByteBuffer responseBuffer = ByteBuffer.allocate(64000);
        responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
        responseBuffer.putInt(0);//Length
        writeCString(responseBuffer, title);
        for (var document : documents) {
            responseBuffer.put(document.serialize());
        }
        var length = responseBuffer.position();
        responseBuffer.position(0);
        responseBuffer.putInt(length);
        responseBuffer.position(0);
        var res = new byte[length];
        for (var i = 0; i < length; i++) {
            res[i] = responseBuffer.get();
        }
        return res;
    }
}
