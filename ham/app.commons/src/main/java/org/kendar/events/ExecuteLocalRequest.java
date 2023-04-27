package org.kendar.events;

import org.kendar.servers.http.Request;

public class ExecuteLocalRequest implements Event {
    private Request request;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
