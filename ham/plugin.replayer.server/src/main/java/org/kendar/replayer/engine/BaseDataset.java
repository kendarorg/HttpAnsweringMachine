package org.kendar.replayer.engine;

import org.kendar.replayer.ReplayerState;

import java.util.Map;

public interface BaseDataset {
    Long getName();
    void load(Long name, String description) throws Exception;
    ReplayerState getType();

    void setSpecialParams(Map<String, String> query);

    void setParams(Map<String, String> query);
}
