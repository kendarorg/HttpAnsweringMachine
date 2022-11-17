package org.kendar.ham;

import org.kendar.utils.ConstantsMime;
import org.kendar.utils.Sleeper;

class HamReplayerRecorderBuilderImpl implements HamReplayerBuilder, HamReplayerRecorderStop,HamReplayerWait {

    private HamInternalBuilder hamBuilder;
    private String lastUsedType;
    private long lastUsedId;

    public HamReplayerRecorderBuilderImpl(HamInternalBuilder hamBuilder){
        this.hamBuilder = hamBuilder;
    }

    @Override
    public HamReplayerRecorderBuilderImpl init() { return null; }

    @Override
    public long createRecording(String id) throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/plugins/replayer/recording")
                .withHamFile(id+".json","{}", ConstantsMime.JSON);
        var response = hamBuilder.call(request.build());
        return Long.parseLong(response.getResponseText());
    }

    @Override
    public long uploadRecording(String id, String jsonContent) throws HamException {
        var request = hamBuilder.newRequest()
                .withPost()
                .withPath("/api/plugins/replayer/recording")
                .withHamFile(id+".json",jsonContent,ConstantsMime.JSON);
        var response = hamBuilder.call(request.build());
        return Long.parseLong(response.getResponseText());
    }

    @Override
    public void deleteRecording(long id) throws HamException {
        var request = hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/plugins/replayer/recording/"+id);
        hamBuilder.call(request.build());
    }

    @Override
    public HamReplayerRecorderStop startRecording(long id) throws HamException {
        return executeAct("start","record", id);
    }

    private HamReplayerRecorderBuilderImpl executeAct(String action, String usedType, long id) throws HamException {
        lastUsedType = usedType;
        lastUsedId = id;
        var request = hamBuilder.newRequest()
                .withPath("/api/plugins/replayer/recording/"+ id +"/"+usedType+"/"+action);
        hamBuilder.call(request.build());
        return this;
    }

    @Override
    public HamReplayerWait startReplaying(long id) throws HamException {
        return (HamReplayerWait)executeAct("start","replay", id);
    }

    @Override
    public HamReplayerWait startPact(long id) throws HamException {
        return (HamReplayerWait)executeAct("start","pact", id);
    }

    @Override
    public HamReplayerWait startNullInfrastructure(long id) throws HamException {
        return (HamReplayerWait)executeAct("start","null", id);
    }

    @Override
    public String downloadRecording(long id) throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/plugins/replayer/recording/"+ id +"/full");
        var response = hamBuilder.call(request.build());
        return response.getResponseText();
    }

    @Override
    public String stop() throws HamException {
         executeAct("stop",lastUsedType, lastUsedId);
         Sleeper.sleep(1000);
         return null;
    }

    public static class ReplayerStatus{
        private String status;
        private String running;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRunning() {
            return running;
        }

        public void setRunning(String running) {
            this.running = running;
        }
    }

    @Override
    public boolean isCompleted() throws HamException {
        Sleeper.sleep(500);
        var request = hamBuilder.newRequest()
                .withPath("/api/plugins/replayer/status");
        var status = hamBuilder.callJson(request.build(),ReplayerStatus.class);
        return status.status.equalsIgnoreCase("none");
    }
}
