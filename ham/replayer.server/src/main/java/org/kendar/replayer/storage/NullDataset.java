package org.kendar.replayer.storage;

import org.kendar.events.EventQueue;
import org.kendar.replayer.Cache;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.events.NullCompleted;
import org.kendar.replayer.utils.JsReplayerExecutor;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.http.InternalRequester;
import org.kendar.servers.http.Response;
import org.kendar.servers.proxy.SimpleProxyHandler;
import org.kendar.utils.LoggerBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class NullDataset extends ReplayerDataset{
    private EventQueue eventQueue;
    private InternalRequester internalRequester;
    private Cache cache;
    private SimpleProxyHandler simpleProxyHandler;
    private Thread thread;
    private String id;
    private AtomicBoolean running = new AtomicBoolean(false);
    private JsReplayerExecutor executor = new JsReplayerExecutor();

    public NullDataset(
            LoggerBuilder loggerBuilder,
            DataReorganizer dataReorganizer,
            Md5Tester md5Tester, EventQueue eventQueue, InternalRequester internalRequester, Cache cache,
            SimpleProxyHandler simpleProxyHandler) {
        super(loggerBuilder,dataReorganizer,md5Tester);
        this.eventQueue = eventQueue;
        this.internalRequester = internalRequester;
        this.cache = cache;
        this.simpleProxyHandler = simpleProxyHandler;
    }


    @Override
    public ReplayerState getType() {
        return ReplayerState.PLAYING_NULL_INFRASTRUCTURE;
    }

    @Override
    public String getName() {
        return name;
    }

    public String start() {
        id = UUID.randomUUID().toString();
        thread = new Thread(()-> {
            try {
                cache.set(id,"runid",id);
                runNullDataset(id);
                cache.remove(id);
            } catch (Exception e) {
                logger.error("ERROR EXECUTING RECORDING",e);
            }
        });
        thread.start();
        return id;
    }

    @Override
    protected boolean superMatch(ReplayerRow row) {
        return row.isStimulatedTest();
    }

    private void runNullDataset(String id) throws IOException {
        running.set(true);
        var result = new TestResults();
        result.setType("NullInfrastructure");
        result.setTimestamp(Calendar.getInstance());
        result.setRecordingId(name);
        long start = System.currentTimeMillis();
        var rootPath = Path.of(replayerDataDir);

        var stringPath = Path.of(rootPath + File.separator + name + ".json");
        var nullDir = Path.of(rootPath + File.separator + "null" + File.separator);
        var resultsFile =  Path.of(rootPath + File.separator + "null" + File.separator+ name+"."+id+".json");
        try {
            if (!Files.isDirectory(rootPath)) {
                Files.createDirectory(rootPath);
            }
            if (!Files.exists(nullDir)) {
                Files.createDirectory(nullDir);
            }
            var maps = new HashMap<Integer, ReplayerRow>();
            var replayerResult = mapper.readValue(stringPath.toFile(), ReplayerResult.class);
            for (var call : replayerResult.getStaticRequests()) {
                maps.put(call.getId(), call);
            }
            for (var call : replayerResult.getDynamicRequests()) {
                maps.put(call.getId(), call);
            }
            var indexes = replayerResult.getIndexes().stream()
                    .filter(a -> a.isStimulatorTest())
                    .sorted(Comparator.comparingInt(CallIndex::getId))
                    .collect(Collectors.toList());
            boolean onIndex = false;
            int currentIndex = 0;
            try {
                for (var toCall : indexes) {
                    onIndex = false;
                    currentIndex = toCall.getId();
                    if (!running.get()) break;
                    var reqResp = maps.get(toCall.getReference());
                    var response = new Response();
                    var request = reqResp.getRequest().copy();
                    var expectedResponse = reqResp.getResponse().copy();
                    if(replayerResult.getPreScript().containsKey(currentIndex+"")){
                        var jsCallback = replayerResult.getPreScript().get(currentIndex+"");
                        var script = executor.prepare(jsCallback);
                        executor.run(this.id,request, response, expectedResponse, script);
                    }
                    internalRequester.callSite(request, response);
                    if(replayerResult.getPostScript().containsKey(currentIndex+"")){
                        var jsCallback = replayerResult.getPostScript().get(currentIndex+"");
                        var script = executor.prepare(jsCallback);
                        executor.run(this.id,request, response, expectedResponse, script);
                    }
                    result.getExecuted().add(toCall.getId());
                }
            } catch (Exception ex) {
                var extra = "Error calling index "+currentIndex+" running "+(onIndex?"index script":"optimized script. ");
                result.setError(extra+ex.getMessage());
            }
        }catch (Exception e){
            result.setError(e.getMessage());
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        result.setDuration(timeElapsed);
        var toWrite = mapper.writeValueAsString(result);
        Files.writeString(resultsFile,toWrite);
        this.eventQueue.handle(new NullCompleted());
    }
    public void stop() {
        running.set(false);
    }
}
