package org.kendar.replayer;

public enum ReplayerState {
    NONE,
    RECORDING,
    PAUSED_RECORDING,
    REPLAYING,
    PAUSED_REPLAYING,
    PLAYING_NULL_INFRASTRUCTURE,
    PLAYING_PACT
}
