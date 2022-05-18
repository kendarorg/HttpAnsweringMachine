package org.kendar.ham;

public interface HamReplayerBuilder {
    HamReplayerRecorderBuilderImpl init();
    void createRecording(String id) throws HamException;
    void uploadRecording(String id,String content) throws HamException;
    void deleteRecording(String id) throws HamException;
    HamReplayerRecorderStop startRecording(String id) throws HamException;
    HamReplayerWait startReplaying(String id) throws HamException;
    HamReplayerWait startPact(String id) throws HamException;
    HamReplayerWait startNullInfrastructure(String id) throws HamException;
}
