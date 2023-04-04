package org.kendar.mongo.model;

import org.kendar.janus.serialization.TypedSerializable;
import org.kendar.janus.serialization.TypedSerializer;

import java.util.ArrayList;
import java.util.List;

public class MsgSequence implements TypedSerializable<MsgSequence> {

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
    }

    @Override
    public MsgSequence deserialize(TypedSerializer typedSerializer) {
        title = typedSerializer.read("title");
        length = typedSerializer.read("length");
        return this;
    }
}
