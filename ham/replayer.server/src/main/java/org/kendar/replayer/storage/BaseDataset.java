package org.kendar.replayer.storage;

import org.kendar.replayer.ReplayerState;

public interface BaseDataset {
    String getName();
    void load(String name, String replayerDataDir, String description);
    ReplayerState getType();
}
