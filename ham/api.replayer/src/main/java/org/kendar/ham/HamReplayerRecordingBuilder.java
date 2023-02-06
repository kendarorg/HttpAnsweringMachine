package org.kendar.ham;

public interface HamReplayerRecordingBuilder {
    HamReplayerRecordingBuilder withName(String name);
    LocalRecording createRecording() throws HamException;
}
