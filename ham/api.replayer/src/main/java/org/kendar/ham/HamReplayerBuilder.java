package org.kendar.ham;

import java.util.List;

public interface HamReplayerBuilder {
    HamReplayerRecorderBuilderImpl init();

    HamReplayerRecordingBuilder setupRecording() throws HamException;

    LocalRecording uploadRecording(String name, String content) throws HamException;

    String downloadRecording(long id) throws HamException;

    List<LocalRecording> retrieveRecordings() throws HamException;

    List<LocalRecording> retrieveRecordings(String name) throws HamException;

    LocalRecording retrieveRecording(long id) throws HamException;
}
