package org.kendar.replayer.engine;

import org.kendar.events.EventQueue;
import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.replayer.Cache;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.events.ReplayCompleted;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
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
import java.util.List;
import java.util.Map;

@Component
public class ReplayerStatus {

    private static final String MAIN_FILE = "runall.json";
    private final LoggerBuilder loggerBuilder;
    private final FileResourcesUtils fileResourcesUtils;
    private final Md5Tester md5Tester;
    private final EventQueue eventQueue;
    private final Logger logger;
    private final ExternalRequester externalRequester;
    private final InternalRequester internalRequester;
    private final SimpleProxyHandler simpleProxyHandler;
    private final String localAddress;
    private HibernateSessionFactory sessionFactory;
    private List<ReplayerEngine> replayerEngines;
    private BaseDataset dataset;
    private ReplayerState state = ReplayerState.NONE;
//    private boolean recordDbCalls;
//    private boolean recordVoidDbCalls;

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

        this.loggerBuilder = loggerBuilder;
        this.localAddress=configuration.getConfiguration(GlobalConfig.class).getLocalAddress();
        this.logger= loggerBuilder.build(ReplayerStatus.class);
        this.fileResourcesUtils = fileResourcesUtils;
        this.md5Tester = md5Tester;
        this.eventQueue = eventQueue;
        this.externalRequester = externalRequester;
        this.internalRequester = internalRequester;
        this.simpleProxyHandler = simpleProxyHandler;
        this.sessionFactory = sessionFactory;
        this.replayerEngines = replayerEngines;
        eventQueue.register((a)-> testCompleted(), ReplayCompleted.class);
    }

    private void testCompleted() {
        dataset = null;
        state = ReplayerState.NONE;
    }

    public void startRecording(Long id, String description, Map<String, String> query) throws Exception {

        if (state != ReplayerState.NONE) return;
        logger.info("RECORDING START "+id);
        state = ReplayerState.RECORDING;
        dataset =
                new RecordingDataset( loggerBuilder, md5Tester,sessionFactory,replayerEngines);
        dataset.setSpecialParams(query);
        dataset.load(id, description);
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
        logger.info("RECORDING RESTART");
        state = ReplayerState.RECORDING;
    }

    public void pauseRecording() {
        if (state != ReplayerState.RECORDING) return;
        logger.info("RECORDING PAUSE");
        state = ReplayerState.PAUSED_RECORDING;
    }

    public void stopAndSave() throws IOException {

        if (state != ReplayerState.PAUSED_RECORDING && state != ReplayerState.RECORDING) return;

        logger.info("RECORDING STOP-AND-SAVE");
        state = ReplayerState.NONE;
        ((RecordingDataset)dataset).save();
        dataset = null;
    }

    public void restartReplaying(Long id) {
        if (state != ReplayerState.PAUSED_REPLAYING) return;
        logger.info("REPLAYING RE-START");
        ((ReplayerDataset)dataset).restart();
        state = ReplayerState.REPLAYING;
    }

    public void pauseReplaying(Long id) {
        if (state != ReplayerState.REPLAYING) return;
        logger.info("REPLAYING PAUSE");
        ((ReplayerDataset)dataset).pause();
        state = ReplayerState.PAUSED_REPLAYING;
    }

    public void stopReplaying() {
        logger.info("REPLAYING STOP");
        state = ReplayerState.NONE;
        dataset = null;
    }

    public Long startReplaying(Long id) throws Exception {
        if (state != ReplayerState.NONE) throw new RuntimeException("State not allowed");
        logger.info("REPLAY START");
        dataset = new ReplayerDataset(loggerBuilder,localAddress,md5Tester,eventQueue,
                internalRequester, new Cache(),simpleProxyHandler,sessionFactory,
                replayerEngines);
        dataset.load(id, null);
        var runId = ((ReplayerDataset)dataset).start();
        state = ReplayerState.REPLAYING;
        return runId;
    }

    public void startStimulator(Long id) throws Exception {
        if (state == ReplayerState.NONE){
            startReplaying(id);
        }
        logger.info("AUTO TEST START");
        ((ReplayerDataset)dataset).startStimulator();
    }

    public void stopReplaying(Long id) throws Exception {
        if (state != ReplayerState.REPLAYING) throw new RuntimeException("State not allowed");
        logger.info("REPLAY STOP");
        ((ReplayerDataset)dataset).stop();
    }
}
