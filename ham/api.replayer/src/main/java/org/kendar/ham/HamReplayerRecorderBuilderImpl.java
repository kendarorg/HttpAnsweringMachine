package org.kendar.ham;

import org.kendar.utils.ConstantsMime;

import java.util.List;
import java.util.stream.Collectors;

class HamReplayerRecorderBuilderImpl implements HamReplayerBuilder, HamReplayerRecordingBuilder {

    final HamInternalBuilder hamBuilder;
    private String name;

    public HamReplayerRecorderBuilderImpl(HamInternalBuilder hamBuilder) {
        this.hamBuilder = hamBuilder;
    }

    @Override
    public HamReplayerRecorderBuilderImpl init() {
        return null;
    }


    @Override
    public HamReplayerRecordingBuilder withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public HamReplayerRecordingBuilder setupRecording() throws HamException {
        return this;
    }

    @Override
    public LocalRecording createRecording() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/plugins/replayer/recording")
                .withHamFile(name + ".json", "{}", ConstantsMime.JSON);
        var response = hamBuilder.call(request.build());
        var id = Long.parseLong(response.getResponseText());
        return retrieveRecording(id);
    }

    @Override
    public LocalRecording uploadRecording(String name, String jsonContent) throws HamException {
        var request = hamBuilder.newRequest()
                .withPost()
                .withPath("/api/plugins/replayer/recording")
                .withHamFile(name + ".json", jsonContent, ConstantsMime.JSON);
        var response = hamBuilder.call(request.build());
        var id = Long.parseLong(response.getResponseText());
        return retrieveRecording(id);
    }

    @Override
    public String downloadRecording(long id) throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/plugins/replayer/recording/" + id + "/full");
        var response = hamBuilder.call(request.build());
        return response.getResponseText();
    }

    @Override
    public List<LocalRecording> retrieveRecordings() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/plugins/replayer/recording");
        return hamBuilder.callJsonList(request.build(), LocalRecording.class).stream()
                .map(r -> {
                    try {
                        return r.init(this);
                    } catch (HamException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public List<LocalRecording> retrieveRecordings(String name) throws HamException {
        return retrieveRecordings().stream().filter(a -> a.getName().startsWith(name))
                .collect(Collectors.toList());
    }

    @Override
    public LocalRecording retrieveRecording(long id) throws HamException {
        var res = retrieveRecordings().stream().filter(a -> a.getId() == id)
                .findFirst();
        if (res.isPresent()) return res.get();
        return null;
    }

    @Override
    public List<RecordingResult> retrieveResults(long id) throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/plugins/replayer/results?id=" + id);
        return hamBuilder.callJsonList(request.build(), RecordingResult.class).stream().collect(Collectors.toList());
    }


    public static class ReplayerStatus {
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


}
