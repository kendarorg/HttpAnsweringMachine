package org.kendar.replayer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.kendar.events.EventQueue;
import org.kendar.replayer.Cache;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.events.PactCompleted;
import org.kendar.replayer.utils.JsReplayerExecutor;
import org.kendar.servers.http.ExternalRequester;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.proxy.SimpleProxyHandler;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

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

@Component
public class PactDataset implements BaseDataset {
    private final Logger logger;
    private EventQueue eventQueue;
    private ExternalRequester externalRequester;
    private Cache cache;
    private SimpleProxyHandler simpleProxyHandler;
    private String name;
    private String replayerDataDir;
    private Thread thread;
    private String id;
    private ObjectMapper mapper = new ObjectMapper();
    private AtomicBoolean running = new AtomicBoolean(false);
    private JsReplayerExecutor executor = new JsReplayerExecutor();

    public PactDataset(LoggerBuilder loggerBuilder, EventQueue eventQueue, ExternalRequester externalRequester
            , Cache cache,
                       SimpleProxyHandler simpleProxyHandler) {

        this.logger = loggerBuilder.build(PactDataset.class);
        this.eventQueue = eventQueue;
        this.externalRequester = externalRequester;
        this.cache = cache;
        this.simpleProxyHandler = simpleProxyHandler;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void load(String name, String replayerDataDir, String description) {
        this.name = name;
        this.replayerDataDir = replayerDataDir;
    }


    @Override
    public ReplayerState getType() {
        return ReplayerState.PLAYING_PACT;
    }

    public String start() {
        id = UUID.randomUUID().toString();
        thread = new Thread(() -> {
            try {
                cache.set(id,"runid",id);
                runPactDataset(id);
                cache.remove(id);
            } catch (IOException e) {
                logger.error("ERROR EXECUTING RECORDING",e);
            }
        });
        thread.start();
        return id;
    }

    private void runPactDataset(String id) throws IOException {
        var result = new TestResults();
        result.setType("Pact");
        result.setTimestamp(Calendar.getInstance());
        result.setRecordingId(name);
        long start = System.currentTimeMillis();
        var rootPath = Path.of(replayerDataDir);
        var stringPath = Path.of(rootPath + File.separator + name + ".json");
        var pactsDir = Path.of(rootPath + File.separator + "pacts" + File.separator);
        var resultsFile = Path.of(rootPath + File.separator + "pacts" + File.separator + name+"."+ id + ".json");

        try {
            running.set(true);

            if (!Files.isDirectory(rootPath)) {
                Files.createDirectory(rootPath);
            }
            if (!Files.exists(pactsDir)) {
                Files.createDirectory(pactsDir);
            }
            var maps = new HashMap<Integer, ReplayerRow>();

            var replayerResult = mapper.readValue(FileUtils.readFileToString(stringPath.toFile(), "UTF-8"), ReplayerResult.class);
            for (var call : replayerResult.getStaticRequests()) {
                maps.put(call.getId(), call);
            }
            for (var call : replayerResult.getDynamicRequests()) {
                maps.put(call.getId(), call);
            }
            var indexes = replayerResult.getIndexes().stream()
                    .filter(a -> a.isPactTest())
                    .sorted(Comparator.comparingInt(CallIndex::getId))
                    .collect(Collectors.toList());
            boolean onIndex = false;
            int currentIndex = 0;
            try {
                for (var toCall : indexes) {
                    onIndex =false;
                    currentIndex = toCall.getId();
                    if (!running.get()) break;
                    var reqResp = maps.get(toCall.getReference());

                    var response = new Response();
                    var request = reqResp.getRequest().copy();
                    var expectedResponse = reqResp.getResponse().copy();

                    var stringRequest = mapper.writeValueAsString(request);
                    stringRequest = cache.replaceAll(this.id,stringRequest);
                    request = mapper.readValue(stringRequest, Request.class);

                    var stringResponse = mapper.writeValueAsString(expectedResponse);
                    stringResponse = cache.replaceAll(this.id,stringResponse);
                    expectedResponse = mapper.readValue(stringResponse, Response.class);

                    //Call request
                    if(replayerResult.getPreScript().containsKey(currentIndex+"")){
                        var jsCallback = replayerResult.getPreScript().get(currentIndex+"");
                        if(jsCallback!=null && jsCallback.trim().length()>0) {
                            var script = executor.prepare(jsCallback);
                            executor.run(this.id, request, response, expectedResponse, script);
                        }
                    }
                    request = simpleProxyHandler.translate(request);
                    externalRequester.callSite(request, response);
                    if(replayerResult.getPostScript().containsKey(currentIndex+"")){
                        var jsCallback = replayerResult.getPostScript().get(currentIndex+"");
                        if(jsCallback!=null && jsCallback.trim().length()>0) {
                            var script = executor.prepare(jsCallback);
                            executor.run(this.id, request, response, expectedResponse, script);
                        }
                    }
                    result.getExecuted().add(toCall.getId());
                }
            }catch(Exception ex){
                var extra = "Error calling index "+currentIndex+" running "+(onIndex?"index script":"optimized script. ");
                result.setError(extra+ex.getMessage());
            }

        } catch (IOException e) {
            result.setError(e.getMessage());
        } catch (Exception e) {
            result.setError(e.getMessage());
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        result.setDuration(timeElapsed);
        var toWrite = mapper.writeValueAsString(result);
        Files.writeString(resultsFile,toWrite);
        this.eventQueue.handle(new PactCompleted());
    }

    public void stop() {
        running.set(false);
    }
}
