package org.kendar.mongo.model;

import java.util.ArrayList;
import java.util.List;

public class MsgSectionPayload implements BaseMsgPayload{
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
}
