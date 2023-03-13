package org.kendar.http;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.util.HashMap;

public class FilterInput {
    private HttpFilterType phase;
    private HashMap<String, String> matches;
    private Request request;
    private Response response;

    public HttpFilterType getPhase() {
        return phase;
    }

    public void setPhase(HttpFilterType phase) {
        this.phase = phase;
    }

    public HashMap<String, String> getMatches() {
        return matches;
    }

    public void setMatches(HashMap<String, String> matches) {
        this.matches = matches;
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
}
