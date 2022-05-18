package org.kendar.ham;

public interface HamReplayerBuilder {
    HamReplayerBuilderImpl init();
    void createRecording(String id);
    void uploadRecording(String id,String content);
    void deleteRecording(String id);
    HamReplayerStop startRecording(String id);
    HamReplayerStop startReplaying(String id);
    HamReplayerStop startPact(String id);
    HamReplayerStop startNullInfrastructure(String id);
}
