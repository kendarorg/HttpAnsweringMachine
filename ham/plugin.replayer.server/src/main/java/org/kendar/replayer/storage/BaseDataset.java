package org.kendar.replayer.storage;

import org.kendar.replayer.ReplayerState;

public interface BaseDataset {
    Long getName();
    void load(Long name, String replayerDataDir, String description);
    ReplayerState getType();
}
