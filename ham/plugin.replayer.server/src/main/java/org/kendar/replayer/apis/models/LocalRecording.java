package org.kendar.replayer.apis.models;

import org.kendar.replayer.ReplayerState;

public class LocalRecording {
    private long id;
    private ReplayerState state;

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
}
