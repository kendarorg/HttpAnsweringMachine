package org.kendar.mongo.model;

public interface MongoReqResPacket {
    int getRequestId();

    void setRequestId(Integer requestId);

    int getResponseTo();

    void setResponseTo(Integer responseTo);
}
