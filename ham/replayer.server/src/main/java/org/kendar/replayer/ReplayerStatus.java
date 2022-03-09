package org.kendar.replayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.replayer.events.NullCompleted;
import org.kendar.replayer.events.PactCompleted;
import org.kendar.replayer.storage.*;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.http.ExternalRequester;
import org.kendar.servers.http.InternalRequester;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ReplayerStatus {

    private static final String MAIN_FILE = "runall.json";
    private final LoggerBuilder loggerBuilder;
    private final DataReorganizer dataReorganizer;
    private final FileResourcesUtils fileResourcesUtils;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String replayerData;
    private final Md5Tester md5Tester;
    private final EventQueue eventQueue;
    private ExternalRequester externalRequester;
    private DnsMultiResolver multiResolver;
    private InternalRequester internalRequester;
    private BaseDataset dataset;
    private ReplayerState state = ReplayerState.NONE;

    public ReplayerStatus(
            LoggerBuilder loggerBuilder,
            DataReorganizer dataReorganizer,
            FileResourcesUtils fileResourcesUtils,
            Md5Tester md5Tester,
            JsonConfiguration configuration,
            EventQueue eventQueue, ExternalRequester externalRequester,
            DnsMultiResolver multiResolver, InternalRequester internalRequester) {

        this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();
        this.loggerBuilder = loggerBuilder;
        this.dataReorganizer = dataReorganizer;
        this.fileResourcesUtils = fileResourcesUtils;
        this.md5Tester = md5Tester;
        this.eventQueue = eventQueue;
        this.externalRequester = externalRequester;
        this.multiResolver = multiResolver;
        this.internalRequester = internalRequester;
        eventQueue.register((a)->pactCompleted(), PactCompleted.class);
        eventQueue.register((a)->nullCompleted(), NullCompleted.class);
    }

    private void nullCompleted() {
        dataset = null;
        state = ReplayerState.NONE;
    }

    private void pactCompleted() {
        dataset = null;
        state = ReplayerState.NONE;
    }

    public void startRecording(String id, String description) throws IOException {
        Path rootPath = getRootPath();
        if (state != ReplayerState.NONE) return;
        state = ReplayerState.RECORDING;
        dataset =
                new RecordingDataset( loggerBuilder, dataReorganizer, md5Tester);
        dataset.load(id, rootPath.toString(), description);
    }

    public void addRequest(Request req, Response res) {
        if (state != ReplayerState.RECORDING) return;
        ((RecordingDataset)dataset).add(req, res);
    }

    public boolean replay(Request req, Response res) {
        if (state != ReplayerState.REPLAYING  && state != ReplayerState.PLAYING_NULL_INFRASTRUCTURE) return false;
        Response response = ((ReplayerDataset)dataset).findResponse(req);
        if (response != null) {
            res.setBinaryResponse(response.isBinaryResponse());
            if (response.isBinaryResponse()) {
                res.setResponseBytes(response.getResponseBytes());
            } else {
                res.setResponseText(response.getResponseText());
            }
            res.setHeaders(response.getHeaders());
            res.setStatusCode(response.getStatusCode());
            return true;
        }
        return false;
    }

    public ReplayerState getStatus() {
        if (state == null) return ReplayerState.NONE;
        return state;
    }

    public String getCurrentScript() {
        if (dataset != null) return dataset.getName();
        return null;
    }

    public void restartRecording() {
        if (state != ReplayerState.PAUSED_RECORDING) return;
        state = ReplayerState.RECORDING;
    }

    public void pauseRecording() {
        if (state != ReplayerState.RECORDING) return;
        state = ReplayerState.PAUSED_RECORDING;
    }

    public void stopAndSave() throws IOException {

        if (state != ReplayerState.PAUSED_RECORDING && state != ReplayerState.RECORDING) return;
        state = ReplayerState.NONE;
        ((RecordingDataset)dataset).save();
        dataset = null;
    }

    public void startReplaying(String id) throws IOException {
        Path rootPath = getRootPath();
        if (state != ReplayerState.NONE) return;
        state = ReplayerState.REPLAYING;
        dataset =
                new ReplayerDataset(  loggerBuilder, dataReorganizer, md5Tester);
        dataset.load(id, rootPath.toString(),null);
    }

    public void restartReplaying() {
        if (state != ReplayerState.PAUSED_REPLAYING) return;
        state = ReplayerState.REPLAYING;
    }

    public void pauseReplaying() {
        if (state != ReplayerState.REPLAYING) return;
        state = ReplayerState.PAUSED_REPLAYING;
    }

    public void stopReplaying() {
        state = ReplayerState.NONE;
        dataset = null;
    }

    public String startPact(String id) throws IOException {
        Path rootPath = getRootPath();
        if (state != ReplayerState.NONE) throw new RuntimeException("State not allowed");
        dataset = new PactDataset(loggerBuilder,eventQueue,externalRequester);
        dataset.load(id, rootPath.toString(),null);
        var runId = ((PactDataset)dataset).start();
        state = ReplayerState.PLAYING_PACT;
        return runId;
    }

    public void stopPact(String id) {
        if (state != ReplayerState.PLAYING_PACT) throw new RuntimeException("State not allowed");
        ((PactDataset)dataset).stop();
        state = ReplayerState.NONE;

    }

    public String startNull(String id) throws IOException {
        Path rootPath = getRootPath();
        if (state != ReplayerState.NONE) throw new RuntimeException("State not allowed");
        dataset = new NullDataset(loggerBuilder,dataReorganizer,md5Tester,eventQueue,internalRequester);
        dataset.load(id, rootPath.toString(),null);
        var runId = ((NullDataset)dataset).start();
        state = ReplayerState.PLAYING_NULL_INFRASTRUCTURE;
        return runId;
    }

    private Path getRootPath() throws IOException {
        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));
        if (!Files.isDirectory(rootPath)) {
            Files.createDirectory(rootPath);
        }
        return rootPath;
    }

    public void stopNull(String id) {
        if (state != ReplayerState.PLAYING_NULL_INFRASTRUCTURE) throw new RuntimeException("State not allowed");
        ((NullDataset)dataset).stop();
    }
}
