package org.kendar.mongo.model.payloads;

import org.kendar.janus.serialization.TypedSerializable;
import org.kendar.janus.serialization.TypedSerializer;

import java.util.ArrayList;
import java.util.List;

public class MsgSectionPayload implements BaseMsgPayload, TypedSerializable<MsgSectionPayload> {
    public List<MsgDocumentPayload> getDocuments() {
        return documents;
    }

    public void setDocuments(List<MsgDocumentPayload> documents) {
        this.documents = documents;
    }

    private List<MsgDocumentPayload> documents = new ArrayList<>();
    private int length;
    private String title;

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void serialize(TypedSerializer typedSerializer) {
        typedSerializer.write("title",title);
        typedSerializer.write("length",length);
        typedSerializer.write("documents",documents);
    }

    @Override
    public MsgSectionPayload deserialize(TypedSerializer typedSerializer) {
        title = typedSerializer.read("title");
        length = typedSerializer.read("length");
        documents = typedSerializer.read("documents");
        return this;
    }
}
