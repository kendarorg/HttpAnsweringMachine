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
    private ReplayerFileData requestFile;
    private ReplayerFileData responseFile;
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

    public ReplayerFileData getRequestFile() {
        return requestFile;
    }

    public void setRequestFile(ReplayerFileData requestFile) {
        this.requestFile = requestFile;
    }

    public ReplayerFileData getResponseFile() {
        return responseFile;
    }

    public void setResponseFile(ReplayerFileData responseFile) {
        this.responseFile = responseFile;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
