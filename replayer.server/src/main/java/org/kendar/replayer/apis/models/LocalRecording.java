package org.kendar.replayer.apis.models;

import org.kendar.replayer.ReplayerState;

public class LocalRecording {
    private String id;
    private ReplayerState state;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ReplayerState getState() {
        return state;
    }

    public void setState(ReplayerState state) {
        this.state = state;
    }
}
