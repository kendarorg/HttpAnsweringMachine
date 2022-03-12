package org.kendar.replayer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplayerDataset implements BaseDataset{
  protected static final String MAIN_FILE = "runall.json";
  protected final Logger logger;
  protected DataReorganizer dataReorganizer;
  protected Md5Tester md5Tester;

  protected final ObjectMapper mapper = new ObjectMapper();

  protected String name;
  protected String replayerDataDir;
  protected String description;
  protected final ConcurrentHashMap<Integer, Object> states = new ConcurrentHashMap<>();

  protected ReplayerResult replayerResult;

  public ReplayerDataset(
      LoggerBuilder loggerBuilder,
      DataReorganizer dataReorganizer,
      Md5Tester md5Tester) {
    this.logger = loggerBuilder.build(ReplayerDataset.class);
    this.dataReorganizer = dataReorganizer;
    this.md5Tester = md5Tester;
  }

  public String getName() {
    return name;
  }

  @Override
  public void load(String name, String replayerDataDir, String description) {
    this.name = name;
    this.replayerDataDir = replayerDataDir;
    this.description = description;

  }

  @Override
  public ReplayerState getType() {
    return ReplayerState.REPLAYING;
  }


  public ReplayerResult load() throws IOException {
    var rootPath = Path.of(replayerDataDir);
    if (!Files.isDirectory(rootPath)) {
      Files.createDirectory(rootPath);
    }
    var stringPath = Path.of(rootPath + File.separator + name + ".json");
    replayerResult = mapper.readValue(stringPath.toFile(), ReplayerResult.class);
    return replayerResult;
  }

  private ReplayerRow findStaticMatch(Request sreq, String contentHash) {
    var matchingQuery = -1;
    ReplayerRow founded = null;
    for (var row : replayerResult.getStaticRequests()) {
      var rreq = row.getRequest();
      if (!sreq.getPath().equals(rreq.getPath())) continue;
      if (!sreq.getHost().equals(rreq.getHost())) continue;
      if(!superMatch(row))continue;
      var matchedQuery = matchQuery(rreq.getQuery(), sreq.getQuery());
      if (rreq.isBinaryRequest() == sreq.isBinaryRequest()) {
        if (row.getRequestHash().equalsIgnoreCase(contentHash)) {
          matchedQuery += 20;
        }
      }

      if (matchedQuery > matchingQuery) {
        matchingQuery = matchedQuery;
        founded = row;
      }
    }
    return founded;
  }

  protected boolean superMatch(ReplayerRow row) {
    return true;
  }

  private ReplayerRow findDynamicMatch(Request sreq) {
    var matchingQuery = -1;
    ReplayerRow founded = null;
    for (var row : replayerResult.getDynamicRequests()) {
      var rreq = row.getRequest();
      // Avoid already running stuffs
      if(row.done())continue;
      if (states.contains(row.getId())) continue;
      if (!sreq.getPath().equals(rreq.getPath())) continue;
      if (!sreq.getHost().equals(rreq.getHost())) continue;
      if(!superMatch(row))continue;
      var matchedQuery = matchQuery(rreq.getQuery(), sreq.getQuery());
      if (rreq.isBinaryRequest() == sreq.isBinaryRequest()) {
        matchedQuery += 1;
      }

      if (matchedQuery > matchingQuery) {
        matchingQuery = matchedQuery;
        founded = row;
      }
    }
    if (founded != null) {
      states.put(founded.getId(), "");
    }
    return founded;
  }

  public Response findResponse(Request req) {
    try {

      String contentHash;
      if (req.isBinaryRequest()) {
        contentHash = md5Tester.calculateMd5(req.getRequestBytes());
      } else {
        contentHash = md5Tester.calculateMd5(req.getRequestText());
      }
      ReplayerRow founded = findStaticMatch(req, contentHash);
      if (founded != null) {
        var result =  founded.getResponse();
        result.addHeader("X-REPLAYER-ID",founded.getId()+"");
        result.addHeader("X-REPLAYER-TYPE","STATIC");
        return result;
      }
      founded = findDynamicMatch(req);
      if (founded != null) {
        var result =  founded.getResponse();
        result.addHeader("X-REPLAYER-ID",founded.getId()+"");
        result.addHeader("X-REPLAYER-TYPE","DYNAMIC");
        founded.markAsDone();
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

  public void delete(int line) {
    List<ReplayerRow> staticRequests = replayerResult.getStaticRequests();
    for (int i = staticRequests.size()-1; i >=0 ; i--) {
      ReplayerRow entry = staticRequests.get(i);
      if (entry.getId() == line) {
        staticRequests.remove(i);
        return;
      }
    }
    List<ReplayerRow> dynamicRequests = replayerResult.getDynamicRequests();
    for (int i = dynamicRequests.size()-1; i >=0 ; i--) {
      ReplayerRow entry = dynamicRequests.get(i);
      if (entry.getId() == line) {
        dynamicRequests.remove(i);
        return;
      }
    }
    List<CallIndex> steps = replayerResult.getIndexes();
    for (int i = steps.size()-1; i >=0 ; i--) {
      CallIndex entry = steps.get(i);
      if (entry.getReference() == line) {
        steps.remove(i);
        return;
      }
    }
  }

  public void add(ReplayerRow row) {
    if (row.getRequest().isStaticRequest()) {
      replayerResult.getStaticRequests().add(row);
    } else {
      replayerResult.getDynamicRequests().add(row);
    }
  }

  public void saveMods() throws IOException {
    var partialResult = new ArrayList<ReplayerRow>();
    partialResult.addAll(replayerResult.getDynamicRequests());
    partialResult.addAll(replayerResult.getStaticRequests());
    replayerResult.setStaticRequests(new ArrayList<>());
    replayerResult.setDynamicRequests(new ArrayList<>());
    dataReorganizer.reorganizeData(replayerResult, partialResult);
    justSave(replayerResult);
  }

  public void justSave(Object data) throws IOException {
    var rootPath = Path.of(replayerDataDir);
    var allDataString = mapper.writeValueAsString(data);
    var stringPath = rootPath + File.separator + name + ".json";
    FileWriter myWriter = new FileWriter(stringPath);
    myWriter.write(allDataString);
    myWriter.close();
  }


  public void deleteIndex(int line) {
      List<CallIndex> steps = replayerResult.getIndexes();
      for (int i = steps.size()-1; i >=0 ; i--) {
        CallIndex entry = steps.get(i);
        if (entry.getId() == line) {
          steps.remove(i);
          return;
        }
      }
    }
}
