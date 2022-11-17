package org.kendar.ham;

public interface HamReplayerBuilder {
    HamReplayerRecorderBuilderImpl init();
    long createRecording(String id) throws HamException;
    long uploadRecording(String id, String content) throws HamException;
    void deleteRecording(long id) throws HamException;
    HamReplayerRecorderStop startRecording(long id) throws HamException;
    HamReplayerWait startReplaying(long id) throws HamException;
    HamReplayerWait startPact(long id) throws HamException;
    HamReplayerWait startNullInfrastructure(long id) throws HamException;

    String downloadRecording(long id) throws HamException;
}
