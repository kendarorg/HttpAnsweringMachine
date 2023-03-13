package org.kendar.events;

public class ServiceStarted implements Event {
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ServiceStarted withTye(String type) {
        this.type = type;
        return this;
    }
}
