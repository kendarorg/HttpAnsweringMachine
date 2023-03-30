package org.kendar.mongo.model;

public class MsgDocumentPayload implements BaseMsgPayload{
    private String json;

    public void setJson(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }
}
