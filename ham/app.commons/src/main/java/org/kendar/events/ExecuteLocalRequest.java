package org.kendar.events;

import org.kendar.events.Event;
import org.kendar.servers.http.Request;

public class ExecuteLocalRequest implements Event {
    private Request request;

    public void setRequest(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }
}
