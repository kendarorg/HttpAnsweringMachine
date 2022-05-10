package org.kendar.replayer.storage;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.util.Calendar;

public class ReplayerRow {
    private Calendar timestamp = Calendar.getInstance();
    private Request request;
    private Response response;
    private String requestHash;
    private String responseHash;
    private int id;
    private boolean stimulatedTest;

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
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

    private boolean done =false;
    public void markAsDone() {
        done =true;
    }

    public boolean done(){
        return done;
    }

    public boolean isStimulatedTest() {
        return stimulatedTest;
    }

    public void setStimulatedTest(boolean stimulatedTest) {
        this.stimulatedTest = stimulatedTest;
    }

}
