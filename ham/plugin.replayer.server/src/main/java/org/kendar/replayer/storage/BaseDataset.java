package org.kendar.replayer.storage;

import org.kendar.replayer.ReplayerState;

public interface BaseDataset {
    Long getName();
    void load(Long name, String replayerDataDir, String description) throws Exception;
    ReplayerState getType();

    void setRecordDbCalls(boolean recordDbCalls);

    void setRecordVoidDbCalls(boolean recordVoidDbCalls);
}
