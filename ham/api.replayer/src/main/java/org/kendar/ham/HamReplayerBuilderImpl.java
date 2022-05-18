package org.kendar.ham;

class HamReplayerBuilderImpl implements HamReplayerBuilder,HamReplayerStop {

    private HamInternalBuilder hamBuilder;

    public HamReplayerBuilderImpl(HamInternalBuilder hamBuilder){
        this.hamBuilder = hamBuilder;
    }

    @Override
    public HamReplayerBuilderImpl init() { return null; }

    @Override
    public void createRecording(String id) {

    }

    @Override
    public void uploadRecording(String id, String content) {

    }

    @Override
    public void deleteRecording(String id) {

    }

    @Override
    public HamReplayerStop startRecording(String id) {
        return this;
    }

    @Override
    public HamReplayerStop startReplaying(String id) {
        return this;
    }

    @Override
    public HamReplayerStop startPact(String id) {
        return this;
    }

    @Override
    public HamReplayerStop startNullInfrastructure(String id) {
        return this;
    }

    @Override
    public String stop() {
        return null;
    }
}
