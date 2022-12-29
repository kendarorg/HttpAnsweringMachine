package org.kendar.replayer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReplayerDataset implements BaseDataset{
  protected static final String MAIN_FILE = "runall.json";
  protected final Logger logger;
  protected Md5Tester md5Tester;
  protected HibernateSessionFactory sessionFactory;
  private List<ReplayerEngine> replayerEngines;

  protected final ObjectMapper mapper = new ObjectMapper();

  protected Long name;
  protected String replayerDataDir;
  protected String description;
  protected final ConcurrentHashMap<Long, Object> states = new ConcurrentHashMap<>();

  protected ReplayerResult replayerResult;

  public ReplayerDataset(
          LoggerBuilder loggerBuilder,
          Md5Tester md5Tester,
          HibernateSessionFactory sessionFactory,
          List<ReplayerEngine> replayerEngines) {
    this.logger = loggerBuilder.build(ReplayerDataset.class);
    this.md5Tester = md5Tester;
    this.sessionFactory = sessionFactory;
    this.replayerEngines = replayerEngines;
  }

  public Long getName() {
    return name;
  }

  @Override
  public void load(Long name, String replayerDataDir, String description) throws Exception {
    this.name = name;
    this.replayerDataDir = replayerDataDir;
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
        if(mayBeMatch!=null)return mayBeMatch;

      }

      return null;
    } catch (Exception ex) {
      logger.error("ERror!", ex);
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

}
