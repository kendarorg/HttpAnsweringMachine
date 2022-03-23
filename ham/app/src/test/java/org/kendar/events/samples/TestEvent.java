package org.kendar.events.samples;

import org.kendar.events.Event;

public class TestEvent implements Event {
    private String string;
    private int integer;

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public int getInteger() {
        return integer;
    }

    public void setInteger(int integer) {
        this.integer = integer;
    }
}
