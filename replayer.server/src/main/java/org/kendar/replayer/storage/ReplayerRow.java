package org.kendar.replayer.storage;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.SerializableRequest;
import org.kendar.servers.http.SerializableResponse;

import java.util.Calendar;
import java.util.Date;

public class ReplayerRow {
    private Calendar timestamp = Calendar.getInstance();
    private SerializableRequest request;
    private SerializableResponse response;
    private String requestHash;
    private String responseHash;
    private int id;

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public SerializableRequest getRequest() {
        return request;
    }

    public void setRequest(SerializableRequest request) {
        this.request = request;
    }

    public SerializableResponse getResponse() {
        return response;
    }

    public void setResponse(SerializableResponse response) {
        this.response = response;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public String getResponseHash() {
        return responseHash;
    }

    public void setResponseHash(String responseHash) {
        this.responseHash = responseHash;
    }
}
