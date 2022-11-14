package org.kendar.replayer.apis.models;

import org.kendar.replayer.ReplayerState;

public class LocalRecording {
    private long id;
    private ReplayerState state;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ReplayerState getState() {
        return state;
    }

    public void setState(ReplayerState state) {
        this.state = state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
