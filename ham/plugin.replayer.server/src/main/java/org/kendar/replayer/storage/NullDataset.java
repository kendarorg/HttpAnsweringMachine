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
            SimpleProxyHandler simpleProxyHandler, HibernateSessionFactory sessionFactory) {
        super(loggerBuilder,md5Tester,sessionFactory);
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
    public Long getName() {
        return name;
    }

    public Long start() throws Exception {
        var result = new TestResults();
        result.setType("NULL");
        result.setTimestamp(Timestamp.from(Calendar.getInstance().toInstant()));
        result.setRecordingId(name);

        sessionFactory.transactional(em -> {
            em.persist(result);
        });

        id = result.getId();
        Thread thread = new Thread(() -> {
            try {
                cache.set(id, "runid", id+"");
                runNullDataset(result);
                cache.remove(id);
            } catch (Exception e) {
                logger.error("ERROR EXECUTING RECORDING", e);
            }
        });
        thread.start();
        return id;
    }

    @Override
    protected boolean superMatch(ReplayerRow row, CallIndex callIndex) {
        return callIndex.isStimulatedTest();
    }

    private void runNullDataset(TestResults testResult) throws Exception {
        running.set(true);
        long start = System.currentTimeMillis();
        try {
            var indexes = new ArrayList<CallIndex>();
            sessionFactory.query(e->{
                indexes.addAll(e.createQuery("SELECT e FROM CallIndex e WHERE " +
                        " e.recordingId="+testResult.getRecordingId()+
                        " AND e.stimulatorTest=true ORDER BY e.id ASC").getResultList());

            });
            boolean onIndex = false;
            long currentIndex = 0;
            try {
                for (var toCall : indexes) {
                    onIndex = false;

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

                    if(toCall.getPreScript()!=null && !toCall.getPreScript().isEmpty()){
                        var jsCallback = toCall.getPreScript();

                        if(jsCallback!=null && jsCallback.trim().length()>0) {
                            var script = executor.prepare(jsCallback);
                            executor.run(this.id, request, response, expectedResponse, script);
                        }
                    }
                    request = simpleProxyHandler.translate(request);
                    internalRequester.callSite(request, response);
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
                }
            } catch (Exception ex) {
                var extra = "Error calling index "+currentIndex+" running "+(onIndex?"index script":"optimized script. ");
                testResult.setError(extra+"\n"+ex.getMessage());

                var resultLine = new TestResultsLine();
                resultLine.setResultId(testResult.getId());
                resultLine.setRecordingId(testResult.getRecordingId());
                resultLine.setExecutedLine(currentIndex);
                sessionFactory.transactional(em -> {
                    em.persist(resultLine);
                });
            }
        }catch (Exception e){
            testResult.setError(e.getMessage());
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        testResult.setDuration(timeElapsed);

        sessionFactory.transactional(em->{
            em.merge(testResult);
        });

        this.eventQueue.handle(new NullCompleted());
    }
    public void stop() {
        running.set(false);
    }
}
