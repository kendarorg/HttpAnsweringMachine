package org.kendar.replayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.janus.results.VoidResult;
import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.replayer.events.NullCompleted;
import org.kendar.replayer.storage.*;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.http.ExternalRequester;
import org.kendar.servers.http.InternalRequester;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.proxy.SimpleProxyHandler;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReplayerStatus {

    private static final String MAIN_FILE = "runall.json";
    private final LoggerBuilder loggerBuilder;
    private final FileResourcesUtils fileResourcesUtils;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String replayerData;
    private final Md5Tester md5Tester;
    private final EventQueue eventQueue;
    private final Logger logger;
    private final ExternalRequester externalRequester;
    private final InternalRequester internalRequester;
    private final SimpleProxyHandler simpleProxyHandler;
    private HibernateSessionFactory sessionFactory;
    private List<ReplayerEngine> replayerEngines;
    private BaseDataset dataset;
    private ReplayerState state = ReplayerState.NONE;
    private boolean recordDbCalls;
    private boolean recordVoidDbCalls;

    public ReplayerStatus(
            LoggerBuilder loggerBuilder,
            FileResourcesUtils fileResourcesUtils,
            Md5Tester md5Tester,
            JsonConfiguration configuration,
            EventQueue eventQueue, ExternalRequester externalRequester,
            DnsMultiResolver multiResolver, InternalRequester internalRequester,
            SimpleProxyHandler simpleProxyHandler,
            HibernateSessionFactory sessionFactory,
            List<ReplayerEngine> replayerEngines) {

        this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();
        this.loggerBuilder = loggerBuilder;
        this.logger= loggerBuilder.build(ReplayerStatus.class);
        this.fileResourcesUtils = fileResourcesUtils;
        this.md5Tester = md5Tester;
        this.eventQueue = eventQueue;
        this.externalRequester = externalRequester;
        this.internalRequester = internalRequester;
        this.simpleProxyHandler = simpleProxyHandler;
        this.sessionFactory = sessionFactory;
        this.replayerEngines = replayerEngines;
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

    public void startRecording(Long id, String description, boolean recordDbCalls, boolean recordVoidDbCalls) throws Exception {
        this.recordDbCalls = recordDbCalls;
        this.recordVoidDbCalls = recordVoidDbCalls;
        Path rootPath = getRootPath();
        if (state != ReplayerState.NONE) return;
        logger.info("RECORDING START");
        state = ReplayerState.RECORDING;
        dataset =
                new RecordingDataset( loggerBuilder, md5Tester,sessionFactory);
        dataset.setRecordDbCalls(recordDbCalls);
        dataset.setRecordVoidDbCalls(recordVoidDbCalls);
        dataset.load(id, rootPath.toString(), description);
    }

    public void addRequest(Request req, Response res) throws Exception {
        if (state != ReplayerState.RECORDING) return;
        ((RecordingDataset)dataset).add(req, res);
    }

    private final JsonTypedSerializer serializer = new JsonTypedSerializer();

    public boolean replay(Request req, Response res) {
        if (state != ReplayerState.REPLAYING ) return false;
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
        //When void calls are made to db
        if(req.getPath().startsWith("/api/db")){
            if(req.getPathParameter("targetType")!=null) {
                var ser = serializer.newInstance();
                ser.write("result", new VoidResult());
                res.getHeaders().put("content-type", "application/json");
                res.setResponseText((String) ser.getSerialized());
                res.setStatusCode(200);
                return true;
            }
        }
        return false;
    }

    public ReplayerState getStatus() {
        if (state == null) return ReplayerState.NONE;
        return state;
    }

    public Long getCurrentScript() {
        if (dataset != null) return dataset.getName();
        return null;
    }

    public void restartRecording() {
        if (state != ReplayerState.PAUSED_RECORDING) return;
        state = ReplayerState.RECORDING;
    }

    public void pauseRecording() {
        if (state != ReplayerState.RECORDING) return;
        logger.info("RECORDING RE-START");
        state = ReplayerState.PAUSED_RECORDING;
    }

    public void stopAndSave() throws IOException {

        if (state != ReplayerState.PAUSED_RECORDING && state != ReplayerState.RECORDING) return;

        logger.info("RECORDING STOP-AND-SAVE");
        state = ReplayerState.NONE;
        ((RecordingDataset)dataset).save();
        dataset = null;
    }

    public void restartReplayingNull(Long id) {
        if (state != ReplayerState.PAUSED_REPLAYING) return;
        logger.info("REPLAYING RE-START");
        ((NullDataset)dataset).restart();
        state = ReplayerState.REPLAYING;
    }

    public void pauseReplayingNull(Long id) {
        if (state != ReplayerState.REPLAYING) return;
        logger.info("REPLAYING PAUSE");
        ((NullDataset)dataset).pause();
        state = ReplayerState.PAUSED_REPLAYING;
    }

    public void stopReplaying() {
        logger.info("REPLAYING STOP");
        state = ReplayerState.NONE;
        dataset = null;
    }

    public Long startNull(Long id) throws Exception {
        Path rootPath = getRootPath();
        if (state != ReplayerState.NONE) throw new RuntimeException("State not allowed");
        logger.info("NULL START");
        dataset = new NullDataset(loggerBuilder,md5Tester,eventQueue,
                internalRequester, new Cache(),simpleProxyHandler,sessionFactory,
                replayerEngines);
        dataset.load(id, rootPath.toString(),null);
        var runId = ((NullDataset)dataset).start();
        state = ReplayerState.REPLAYING;
        return runId;
    }

    private Path getRootPath() throws IOException {
        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));
        if (!Files.isDirectory(rootPath)) {
            Files.createDirectory(rootPath);
        }
        return rootPath;
    }

    public void stopNull(Long id) throws Exception {
        if (state != ReplayerState.REPLAYING) throw new RuntimeException("State not allowed");
        logger.info("NULL STOP");
        ((NullDataset)dataset).stop();
    }

    public boolean isRecordDbCalls() {
        return recordDbCalls;
    }

    public boolean isRecordVoidDbCalls() {
        return recordVoidDbCalls;
    }
}
