package org.kendar.replayer.storage;

import org.kendar.events.EventQueue;
import org.kendar.replayer.Cache;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.events.NullCompleted;
import org.kendar.replayer.utils.JsReplayerExecutor;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.InternalRequester;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.proxy.SimpleProxyHandler;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class NullDataset extends ReplayerDataset{
    private final EventQueue eventQueue;
    private final InternalRequester internalRequester;
    private final Cache cache;
    private final SimpleProxyHandler simpleProxyHandler;
    private Long id;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final JsReplayerExecutor executor = new JsReplayerExecutor();

    public NullDataset(
            LoggerBuilder loggerBuilder,
            Md5Tester md5Tester, EventQueue eventQueue, InternalRequester internalRequester, Cache cache,
            SimpleProxyHandler simpleProxyHandler, HibernateSessionFactory sessionFactory,
            List<ReplayerEngine> replayerEngines) {
        super(loggerBuilder,md5Tester,sessionFactory,replayerEngines);
        this.eventQueue = eventQueue;
        this.internalRequester = internalRequester;
        this.cache = cache;
        this.simpleProxyHandler = simpleProxyHandler;
    }


    @Override
    public ReplayerState getType() {
        return ReplayerState.REPLAYING;
    }

    @Override
    public Long getName() {
        return name;
    }

    public Long start() throws Exception {
        var result = new TestResults();

        result.setTimestamp(Timestamp.from(Calendar.getInstance().toInstant()));
        result.setRecordingId(name);
        ArrayList<CallIndex> indexes = new ArrayList<>();

        sessionFactory.query(e -> {
            indexes.addAll(e.createQuery("SELECT e FROM CallIndex e WHERE " +
                    " e.recordingId=" + result.getRecordingId() +
                    " AND e.stimulatorTest=true ORDER BY e.id ASC").getResultList());

        });

        if(indexes.size()>0) {
            result.setType("NULL");
        }else{
            result.setType("PLAY");
            result.setDuration(System.currentTimeMillis());
        }



        sessionFactory.transactional(em -> {
            em.persist(result);
        });

        id = result.getId();
        Thread thread = new Thread(() -> {
            try {
                cache.set(id, "runid", id+"");
                runNullDataset(result,indexes);
                cache.remove(id);
            } catch (Exception e) {
                logger.error("ERROR EXECUTING RECORDING", e);
            }
        });
        thread.start();
        return id;
    }

    private void runNullDataset(TestResults testResult, List<CallIndex> indexes) throws Exception {
        running.set(true);
        long start = System.currentTimeMillis();
        try {


            boolean onIndex = false;
            long currentIndex = 0;
            try {
                for (var toCall : indexes) {
                    int maxWait = 60*1000;
                    while(pause.get()==true && maxWait>0){
                        Sleeper.sleep(1000);
                        maxWait-=1000;
                    }
                    onIndex = false;

                    currentIndex = toCall.getId();
                    if (!running.get()) break;
                    ReplayerRow reqResp = sessionFactory.queryResult(e -> {
                        return e.createQuery("SELECT e FROM ReplayerRow e WHERE " +
                                " e.recordingId=" + testResult.getRecordingId() + " " +
                                " AND e.id=" + toCall.getReference()).getResultList().get(0);
                    });
                    var response = new Response();
                    var request = reqResp.getRequest().copy();
                    var expectedResponse = reqResp.getResponse().copy();

                    var stringRequest = mapper.writeValueAsString(request);
                    stringRequest = cache.replaceAll(this.id, stringRequest);
                    request = mapper.readValue(stringRequest, Request.class);

                    var stringResponse = mapper.writeValueAsString(expectedResponse);
                    stringResponse = cache.replaceAll(this.id, stringResponse);
                    expectedResponse = mapper.readValue(stringResponse, Response.class);

                    if (toCall.getPreScript() != null && !toCall.getPreScript().isEmpty()) {
                        var jsCallback = toCall.getPreScript();

                        if (jsCallback != null && jsCallback.trim().length() > 0) {
                            var script = executor.prepare(jsCallback);
                            executor.run(this.id, request, response, expectedResponse, script);
                        }
                    }
                    request = simpleProxyHandler.translate(request);
                    internalRequester.callSite(request, response);
                    if(response.getStatusCode()!=expectedResponse.getStatusCode()){
                        throw new Exception("Response code failed for request "+currentIndex+
                                " Expected "+expectedResponse.getStatusCode()+
                                " Founded "+response.getStatusCode());
                    }
                    if (toCall.getPostScript() != null && !toCall.getPostScript().isEmpty()) {
                        var jsCallback = toCall.getPostScript();

                        if (jsCallback != null && jsCallback.trim().length() > 0) {
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
                }
            } catch (Exception ex) {
                var extra = "Error calling index " + currentIndex + " running " + (onIndex ? "index script" : "optimized script. ");
                testResult.setError(extra + "\n" + ex.getMessage());

                var resultLine = new TestResultsLine();
                resultLine.setResultId(testResult.getId());
                resultLine.setRecordingId(testResult.getRecordingId());
                resultLine.setExecutedLine(currentIndex);
                sessionFactory.transactional(em -> {

                    em.persist(resultLine);
                    em.merge(testResult);
                });
            }
        } catch (Exception e) {
            testResult.setError(e.getMessage());
        }
        if (indexes.size()>0) {
            //If it's a real stimulated test
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            testResult.setDuration(timeElapsed);

            sessionFactory.transactional(em -> {
                em.merge(testResult);
            });

            this.eventQueue.handle(new NullCompleted());
        }
    }
    public void stop() throws Exception {
        running.set(false);
        sessionFactory.transactional(e -> {
            var tr = (TestResults)e.createQuery("SELECT e FROM TestResults e WHERE " +
                    " e.id=" + id ).getResultList().get(0);
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - tr.getDuration();
            tr.setDuration(timeElapsed);
            e.merge(tr);

        });


        this.eventQueue.handle(new NullCompleted());
    }

    public void restart() {
        pause.set(false);
    }

    private AtomicBoolean pause = new AtomicBoolean(false);
    public void pause() {
        pause.set(true);
    }


}
