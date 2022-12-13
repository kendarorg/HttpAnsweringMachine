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

  protected final ObjectMapper mapper = new ObjectMapper();

  protected Long name;
  protected String replayerDataDir;
  protected String description;
  protected final ConcurrentHashMap<Long, Object> states = new ConcurrentHashMap<>();

  protected ReplayerResult replayerResult;

  public ReplayerDataset(
          LoggerBuilder loggerBuilder,
          Md5Tester md5Tester,
          HibernateSessionFactory sessionFactory) {
    this.logger = loggerBuilder.build(ReplayerDataset.class);
    this.md5Tester = md5Tester;
    this.sessionFactory = sessionFactory;
  }

  public Long getName() {
    return name;
  }

  @Override
  public void load(Long name, String replayerDataDir, String description) {
    this.name = name;
    this.replayerDataDir = replayerDataDir;
    this.description = description;

  }

  @Override
  public ReplayerState getType() {
    return ReplayerState.REPLAYING;
  }



  private ReplayerRow findRequestMatch(Request sreq, String contentHash,boolean staticRequest) throws Exception {
    var matchingQuery = -1;
    ReplayerRow founded = null;
    var staticRequests = new ArrayList<ReplayerRow>();
    sessionFactory.query(em->{
      var query =em.createQuery("SELECT e FROM ReplayerRow  e WHERE " +
              " e.staticRequest=:sr " +
              " AND e.recordingId=:recordingId" +
              " AND e.path=:path" +
              " AND e.host=:host" +
              " ORDER BY e.id ASC");
      query.setParameter("sr",staticRequest);
      query.setParameter("recordingId",name);
      query.setParameter("path",sreq.getPath());
      query.setParameter("host",sreq.getHost());
      staticRequests.addAll(query.getResultList());
    });

    var indexes = staticRequests.stream().map(r->r.getIndex()).collect(Collectors.toList()).toArray(Long[]::new);
    var callIndexes = new ArrayList<CallIndex>();
    sessionFactory.query(em->{
      var query =em.createQuery("SELECT e FROM CallIndex  e WHERE " +
              " e.reference IN :reqs" +
              " AND e.recordingId=:recordingId" +
              " ORDER BY e.reference ASC");
      query.setParameter("recordingId",name);
      query.setParameter("reqs",indexes);
      callIndexes.addAll(query.getResultList());
    });

    for (var row : staticRequests) {
      if(!staticRequest){
        var st = states.get(row.getId());
        if(null!=st){
          continue;
        }
      }
      var rreq = row.getRequest();
      var callIndex = callIndexes.stream().filter(
              ci->ci.getReference()==row.getId()
      ).findFirst();
      if(!superMatch(row,callIndex.get()))continue;
      var matchedQuery=0;
      if (rreq.isBinaryRequest() == sreq.isBinaryRequest()) {
        if (row.getRequestHash().equalsIgnoreCase(contentHash)) {
          matchedQuery += 20;
        }
      }

      matchedQuery += matchQuery(rreq.getQuery(), sreq.getQuery());
      if (matchedQuery > matchingQuery) {
        matchingQuery = matchedQuery;
        founded = row;
      }
    }
    return founded;
  }

  protected boolean superMatch(ReplayerRow row, CallIndex callIndex) {
    return true;
  }


  public Response findResponse(Request req) {
    try {

      String contentHash;
      if (req.isBinaryRequest()) {
        contentHash = md5Tester.calculateMd5(req.getRequestBytes());
      } else {
        contentHash = md5Tester.calculateMd5(req.getRequestText());
      }
      ReplayerRow founded = findRequestMatch(req, contentHash,true);
      if (founded != null) {
        var result =  founded.getResponse();
        result.addHeader("X-REPLAYER-ID",founded.getId()+"");
        result.addHeader("X-REPLAYER-TYPE","STATIC");
        return result;
      }
      founded = findRequestMatch(req, contentHash,false);
      if (founded != null) {
        var result =  founded.getResponse();
        result.addHeader("X-REPLAYER-ID",founded.getId()+"");
        result.addHeader("X-REPLAYER-TYPE","DYNAMIC");
        states.put(founded.getId(),"");
        return result;
      }
      return null;
    } catch (Exception ex) {
      logger.error("ERror!", ex);
      return null;
    }
  }

  private int matchQuery(Map<String, String> left, Map<String, String> right) {
    var result = 0;
    for (var leftItem : left.entrySet()) {
      for (var rightItem : right.entrySet()) {
        if (leftItem.getKey().equalsIgnoreCase(rightItem.getKey())) {
          result++;
          if (leftItem.getValue() == null) {
            if (rightItem.getValue() == null) {
              result++;
            }
          } else if (leftItem.getValue().equalsIgnoreCase(rightItem.getValue())) {
            result++;
          }
        }
      }
    }
    return result;
  }

  public void add(ReplayerRow row) {
    if (row.getRequest().isStaticRequest()) {
      replayerResult.getStaticRequests().add(row);
    } else {
      replayerResult.getDynamicRequests().add(row);
    }
  }

}
