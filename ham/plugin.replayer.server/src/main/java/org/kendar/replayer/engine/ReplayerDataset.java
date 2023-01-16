package org.kendar.replayer.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.replayer.Cache;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.events.ReplayCompleted;
import org.kendar.replayer.storage.*;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.JsReplayerExecutor;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.InternalRequester;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.proxy.ProxyConfigChanged;
import org.kendar.servers.proxy.SimpleProxyHandler;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ReplayerDataset implements BaseDataset{
  protected Logger logger;
  protected Md5Tester md5Tester;
  protected HibernateSessionFactory sessionFactory;

  private final EventQueue eventQueue;
  private final InternalRequester internalRequester;
  private final Cache cache;
  private final SimpleProxyHandler simpleProxyHandler;
  private Long id;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final JsReplayerExecutor executor = new JsReplayerExecutor();
  private List<ReplayerEngine> replayerEngines;

  protected final ObjectMapper mapper = new ObjectMapper();

  protected Long name;
  protected String description;
  protected final ConcurrentHashMap<Long, Object> states = new ConcurrentHashMap<>();

  protected ReplayerResult replayerResult;
  private Thread thread;

  public ReplayerDataset(
          LoggerBuilder loggerBuilder,
          Md5Tester md5Tester, EventQueue eventQueue, InternalRequester internalRequester, Cache cache,
          SimpleProxyHandler simpleProxyHandler, HibernateSessionFactory sessionFactory,
          List<ReplayerEngine> replayerEngines) {
    this.eventQueue = eventQueue;
    this.internalRequester = internalRequester;
    this.cache = cache;
    this.simpleProxyHandler = simpleProxyHandler;
    this.logger = loggerBuilder.build(ReplayerDataset.class);
    this.md5Tester = md5Tester;
    this.sessionFactory = sessionFactory;
    this.replayerEngines = replayerEngines.stream().map(re->re.create(loggerBuilder)).collect(Collectors.toList());
  }

  public Long getName() {
    return name;
  }

  @Override
  public void load(Long name, String description) throws Exception {
    this.name = name;
    this.description = description;
    for(var engine :replayerEngines){
      engine.loadDb(name);
    }

  }

  @Override
  public ReplayerState getType() {
    return ReplayerState.REPLAYING;
  }

  @Override
  public void setRecordDbCalls(boolean recordDbCalls) {

  }

  @Override
  public void setRecordVoidDbCalls(boolean recordVoidDbCalls) {

  }

  public Long start() throws Exception {
    var result = new TestResults();

    result.setTimestamp(Timestamp.from(Calendar.getInstance().toInstant()));
    result.setRecordingId(name);
    result.setType("PLAY");




    sessionFactory.transactional(em -> {
      em.persist(result);
    });

    id = result.getId();

    return id;
  }

  private void runAutoTest(TestResults testResult, List<CallIndex> indexes) throws Exception {
    running.set(true);
    long start = System.currentTimeMillis();
    try {

      Response obtainedResponse = null;
      Response expectedResponse = null;
      boolean onIndex = false;
      long currentIndex = 0;
      eventQueue.execute(new ProxyConfigChanged(),Void.TYPE);
      Thread.sleep(1000);
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
          obtainedResponse = new Response();
          var request = reqResp.getRequest().copy();
          logger.info("Stimulating "+
                  request.getProtocol()+"://"+
                  request.getHost()+
                  request.getPath());
          expectedResponse = reqResp.getResponse().copy();

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
              executor.run(this.id, request, obtainedResponse, expectedResponse, script);
            }
          }
          request = simpleProxyHandler.translate(request);
          internalRequester.callSite(request, obtainedResponse);
          if(obtainedResponse.getStatusCode()!=expectedResponse.getStatusCode()){
            throw new Exception("Response code failed for request "+currentIndex+
                    " Expected "+expectedResponse.getStatusCode()+
                    " Founded "+obtainedResponse.getStatusCode());
          }
          if (toCall.getPostScript() != null && !toCall.getPostScript().isEmpty()) {
            var jsCallback = toCall.getPostScript();

            if (jsCallback != null && jsCallback.trim().length() > 0) {
              var script = executor.prepare(jsCallback);
              executor.run(this.id, request, obtainedResponse, expectedResponse, script);
            }
          }
          var resultLine = new TestResultsLine();
          resultLine.setResultId(testResult.getId());
          resultLine.setRecordingId(testResult.getRecordingId());
          resultLine.setExecutedLine(toCall.getId());
          resultLine.setStimulator(true);
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
        resultLine.setStimulator(true);
        var er = mapper.writeValueAsString(expectedResponse);
        var or = mapper.writeValueAsString(obtainedResponse);
        resultLine.setExpectedResponse(er.substring(0,Math.min(er.length(),63999)));
        resultLine.setActualResponse(or.substring(0,Math.min(or.length(),63999)));
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

      this.eventQueue.handle(new ReplayCompleted());
    }
  }


  public Response findResponse(Request req) {
    try {

      String contentHash;
      if (req.isBinaryRequest()) {
        contentHash = md5Tester.calculateMd5(req.getRequestBytes());
      } else {
        contentHash = md5Tester.calculateMd5(req.getRequestText());
      }
      for(var engine:replayerEngines){
        var mayBeMatch= engine.findRequestMatch(req,contentHash);
        if(mayBeMatch!=null){
          return mayBeMatch;
        }

      }

      return null;
    } catch (Exception ex) {
      logger.error("ERROR!", ex);
      return null;
    }
  }
  public void add(ReplayerRow row) {
    if (row.getRequest().isStaticRequest()) {
      replayerResult.getStaticRequests().add(row);
    } else {
      replayerResult.getDynamicRequests().add(row);
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


    this.eventQueue.handle(new ReplayCompleted());
  }

  public void restart() {
    pause.set(false);
  }

  private AtomicBoolean pause = new AtomicBoolean(false);
  public void pause() {
    pause.set(true);
  }


  public void startStimulator() throws Exception {
    if(thread!=null){
      return;
    }
    ArrayList<CallIndex> indexes = new ArrayList<>();

    var result = (TestResults)sessionFactory.queryResult(em-> {
      return (TestResults) em.createQuery("SELECT e FROM TestResults e WHERE e.id=" + id).getResultList().get(0);
    });

    sessionFactory.query(e -> {
      indexes.addAll(e.createQuery("SELECT e FROM CallIndex e WHERE " +
              " e.recordingId=" + result.getRecordingId() +
              " AND e.stimulatorTest=true ORDER BY e.id ASC").getResultList());

    });

    if(indexes.size()>0) {
      result.setType("AUTO");
    }else{
      result.setType("PLAY");
      result.setDuration(System.currentTimeMillis());
    }
    sessionFactory.transactional(em -> {
      em.merge(result);
    });
    thread = new Thread(() -> {
      try {
        cache.set(id, "runid", id+"");
        runAutoTest(result,indexes);
        cache.remove(id);
      } catch (Exception e) {
        logger.error("ERROR EXECUTING RECORDING", e);
      }
    });
    thread.start();
  }
}
