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
    private String preRequestJs;
    private String middleJs;
    private String postResponseJs;
    private String testType;
    private int id;

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

    public String getPreRequestJs() {
        return preRequestJs;
    }

    public void setPreRequestJs(String preRequestJs) {
        this.preRequestJs = preRequestJs;
    }

    public String getMiddleJs() {
        return middleJs;
    }

    public void setMiddleJs(String middleJs) {
        this.middleJs = middleJs;
    }

    public String getPostResponseJs() {
        return postResponseJs;
    }

    public void setPostResponseJs(String postResponseJs) {
        this.postResponseJs = postResponseJs;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }
}
