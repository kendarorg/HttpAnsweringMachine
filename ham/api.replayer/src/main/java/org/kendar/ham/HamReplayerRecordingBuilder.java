package org.kendar.ham;

public interface HamReplayerRecordingBuilder {
    HamReplayerRecordingBuilder withParameter(String paramName, Object parameter);
    HamReplayerRecordingBuilder withName(String name);
    LocalRecording createRecording() throws HamException;
}
