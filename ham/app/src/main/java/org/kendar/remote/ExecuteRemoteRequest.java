package org.kendar.remote;

import org.kendar.events.Event;
import org.kendar.servers.http.Request;

public class ExecuteRemoteRequest implements Event {
    private Request request;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
