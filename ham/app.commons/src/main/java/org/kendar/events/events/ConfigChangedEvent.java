package org.kendar.events.events;

import org.kendar.events.Event;

public class ConfigChangedEvent implements Event {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
