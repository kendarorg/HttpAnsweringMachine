package org.kendar.ham;

public interface HamReplayerWait extends HamReplayerRecorderStop{
    boolean isCompleted() throws HamException;

}
