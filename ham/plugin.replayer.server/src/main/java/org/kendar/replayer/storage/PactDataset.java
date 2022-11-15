package org.kendar.replayer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.kendar.events.EventQueue;
import org.kendar.replayer.Cache;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.events.PactCompleted;
import org.kendar.replayer.utils.JsReplayerExecutor;
import org.kendar.servers.db.HibernateSessionFactory;
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
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class PactDataset implements BaseDataset {
    private final Logger logger;
    private final EventQueue eventQueue;
    private final ExternalRequester externalRequester;
    private final Cache cache;
    private final SimpleProxyHandler simpleProxyHandler;
    private HibernateSessionFactory sessionFactory;
    private Long name;
    private String replayerDataDir;
    private Long id;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final JsReplayerExecutor executor = new JsReplayerExecutor();

    public PactDataset(LoggerBuilder loggerBuilder, EventQueue eventQueue, ExternalRequester externalRequester
            , Cache cache, SimpleProxyHandler simpleProxyHandler,
                       HibernateSessionFactory sessionFactory) {

        this.logger = loggerBuilder.build(PactDataset.class);
        this.eventQueue = eventQueue;
        this.externalRequester = externalRequester;
        this.cache = cache;
        this.simpleProxyHandler = simpleProxyHandler;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Long getName() {
        return name;
    }

    @Override
    public void load(Long name, String replayerDataDir, String description) {
        this.name = name;
        this.replayerDataDir = replayerDataDir;
    }


    @Override
    public ReplayerState getType() {
        return ReplayerState.PLAYING_PACT;
    }

    public Long start() throws Exception {
        var result = new TestResults();
        result.setType("NullInfrastructure");
        result.setTimestamp(Timestamp.from(Calendar.getInstance().toInstant()));
        result.setRecordingId(name);

        sessionFactory.transactional(em -> {
            em.persist(result);
        });

        id = result.getId();
        Thread thread = new Thread(() -> {
            try {
                cache.set(id, "runid", id+"");
                runPactDataset(result);
                cache.remove(id);
            } catch (IOException e) {
                logger.error("ERROR EXECUTING RECORDING", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        return id;
    }

    private void runPactDataset(TestResults testResult) throws Exception {

        long start = System.currentTimeMillis();
//        var rootPath = Path.of(replayerDataDir);
//        var stringPath = Path.of(rootPath + File.separator + name + ".json");
//        var pactsDir = Path.of(rootPath + File.separator + "pacts" + File.separator);
//        var resultsFile = Path.of(rootPath + File.separator + "pacts" + File.separator + name+"."+ id + ".json");

        try {
            running.set(true);

//            if (!Files.isDirectory(rootPath)) {
//                Files.createDirectory(rootPath);
//            }
//            if (!Files.exists(pactsDir)) {
//                Files.createDirectory(pactsDir);
//            }
//            var maps = new HashMap<Long, ReplayerRow>();
//
//
//            //FIXME HERE STARTS LOADING
//            ASDFASD
//            var replayerResult = mapper.readValue(FileUtils.readFileToString(stringPath.toFile(), "UTF-8"), ReplayerResult.class);
//            for (var call : replayerResult.getStaticRequests()) {
//                maps.put(call.getId(), call);
//            }
//            for (var call : replayerResult.getDynamicRequests()) {
//                maps.put(call.getId(), call);
//            }
            ArrayList<CallIndex> indexes = sessionFactory.queryResult(e->{
                return e.createQuery("SELECT e FROM CallIndex e WHERE " +
                        " e.recordingId="+testResult.getRecordingId()+
                        " AND e.pactTest=true ORDER BY e.id ASC").getResultList();

            });

            boolean onIndex = false;
            long currentIndex = 0;
            try {
                for (var toCall : indexes) {
                    onIndex =false;
                    currentIndex = toCall.getId();
                    if (!running.get()) break;
                    ReplayerRow reqResp = sessionFactory.queryResult(e->{
                        return e.createQuery("SELECT e FROM ReplayerRow e WHERE " +
                                " e.recordingId="+testResult.getRecordingId()+" " +
                                " AND e.id="+toCall.getReference()).getResultList().get(0);
                    });

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
                    if(toCall.getPreScript()!=null && !toCall.getPreScript().isEmpty()){
                        var jsCallback = toCall.getPreScript();
                        if(jsCallback!=null && jsCallback.trim().length()>0) {
                            var script = executor.prepare(jsCallback);
                            executor.run(this.id, request, response, expectedResponse, script);
                        }
                    }
                    request = simpleProxyHandler.translate(request);
                    externalRequester.callSite(request, response);
                    if(toCall.getPostScript()!=null && !toCall.getPostScript().isEmpty()){
                        var jsCallback = toCall.getPostScript();
                        if(jsCallback!=null && jsCallback.trim().length()>0) {
                            var script = executor.prepare(jsCallback);
                            executor.run(this.id, request, response, expectedResponse, script);
                        }
                    }
                    var resultLine = new TestResultsLine();
                    resultLine.setResultId(testResult.getId());
                    resultLine.setRecordingId(testResult.getRecordingId());
                    resultLine.setExecutedLine(toCall.getId());
                    sessionFactory.transactional(em -> {
                        em.persist(resultLine);
                    });
                    //result.getExecuted().add(toCall.getId());
                }
            }catch(Exception ex){
                var extra = "Error calling index "+currentIndex+" running "+(onIndex?"index script":"optimized script. ");
                testResult.setError(extra+ex.getMessage());
            }

        } catch (IOException e) {
            testResult.setError(e.getMessage());
        } catch (Exception e) {
            testResult.setError(e.getMessage());
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        testResult.setDuration(timeElapsed);

        sessionFactory.transactional(em->{
            em.merge(testResult);
        });
//        var toWrite = mapper.writeValueAsString(result);
//        Files.writeString(resultsFile,toWrite);
        this.eventQueue.handle(new PactCompleted());
    }

    public void stop() {
        running.set(false);
    }
}
