package org.kendar.replayer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplayerDataset {
  private static final String MAIN_FILE = "runall.json";
  private final Logger logger;
  private final DataReorganizer dataReorganizer;
  private final ObjectMapper mapper = new ObjectMapper();
  private final ConcurrentLinkedQueue<ReplayerRow> dynamicData = new ConcurrentLinkedQueue<>();
  private final ConcurrentHashMap<String, ReplayerRow> staticData = new ConcurrentHashMap<>();
  private final ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
  private final AtomicInteger counter = new AtomicInteger(0);
  private final String name;
  private final String replayerDataDir;
  private final String description;
  private final ConcurrentHashMap<Integer, Object> states = new ConcurrentHashMap<>();
  private Md5Tester md5Tester;
  private ReplayerResult replayerResult;

  public ReplayerDataset(
      String name,
      String replayerDataDir,
      String description,
      LoggerBuilder loggerBuilder,
      DataReorganizer dataReorganizer,
      Md5Tester md5Tester) {
    this.name = name;
    this.replayerDataDir = replayerDataDir;
    this.description = description;
    this.logger = loggerBuilder.build(ReplayerDataset.class);
    this.dataReorganizer = dataReorganizer;
    this.md5Tester = md5Tester;
  }

  public String getName() {
    return name;
  }

  public void save() throws IOException {
    synchronized (this) {
      var result = new ReplayerResult();
      var partialResult = new ArrayList<ReplayerRow>();
      var rootPath = Path.of(replayerDataDir);
      if (!Files.isDirectory(rootPath)) {
        Files.createDirectory(rootPath);
      }
      for (var staticRow : this.staticData.entrySet()) {
        var rowValue = staticRow.getValue();
        partialResult.add(rowValue);
      }

      while (!dynamicData.isEmpty()) {
        // consume element
        var rowValue = dynamicData.poll();
        partialResult.add(rowValue);
      }

      while (!errors.isEmpty()) {
        // consume element
        result.addError(errors.poll());
      }

      result.setDescription(description);
      dataReorganizer.reorganizeData(result, partialResult);
      var allDataString = mapper.writeValueAsString(result);
      var stringPath = rootPath + File.separator + name + ".json";
      FileWriter myWriter = new FileWriter(stringPath);
      myWriter.write(allDataString);
      myWriter.close();
    }
  }

  public void add(Request req, Response res) {
    var path = req.getHost() + req.getPath();
    try {

      String responseHash;

      if (req.isStaticRequest() && staticData.containsKey(path)) {
        var alreadyPresent = staticData.get(path);
        if (res.isBinaryResponse()) {
          responseHash = md5Tester.calculateMd5(res.getResponseBytes());
        } else {
          responseHash = md5Tester.calculateMd5(res.getResponseText());
        }

        if (!responseHash.equalsIgnoreCase(alreadyPresent.getResponseHash())) {
          errors.add("Static request was dynamic " + path);
          throw new Exception("Static request was dynamic " + path);
        }
        return;
      }
      var replayerRow = new ReplayerRow();
      if (res.isBinaryResponse()) {
        responseHash = md5Tester.calculateMd5(res.getResponseBytes());
      } else {
        responseHash = md5Tester.calculateMd5(res.getResponseText());
      }
      replayerRow.setId(counter.getAndIncrement());
      replayerRow.setRequest(req);
      replayerRow.setResponse(res);
      if (req.isBinaryRequest()) {
        replayerRow.setRequestHash(md5Tester.calculateMd5(req.getRequestBytes()));
      } else {
        replayerRow.setRequestHash(md5Tester.calculateMd5(req.getRequestText()));
      }
      replayerRow.setResponseHash(responseHash);

      if (req.isStaticRequest()) {
        staticData.put(path, replayerRow);
      } else {
        dynamicData.add(replayerRow);
      }
      // ADD the crap
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Error recording request " + path, e);
    }
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

  private ReplayerRow findDynamicMatch(Request sreq) {
    var matchingQuery = -1;
    ReplayerRow founded = null;
    for (var row : replayerResult.getDynamicRequests()) {
      var rreq = row.getRequest();
      // Avoid already running stuffs
      if (states.contains(row.getId())) continue;
      if (!sreq.getPath().equals(rreq.getPath())) continue;
      if (!sreq.getHost().equals(rreq.getHost())) continue;
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
        return founded.getResponse();
      }
      founded = findDynamicMatch(req);
      if (founded != null) {
        return founded.getResponse();
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
    for (int i = 0; i < staticRequests.size(); i++) {
      ReplayerRow entry = staticRequests.get(i);
      if (entry.getId() == line) {
        staticRequests.remove(i);
        return;
      }
    }
    List<ReplayerRow> dynamicRequests = replayerResult.getDynamicRequests();
    for (int i = 0; i < dynamicRequests.size(); i++) {
      ReplayerRow entry = dynamicRequests.get(i);
      if (entry.getId() == line) {
        dynamicRequests.remove(i);
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
    var rootPath = Path.of(replayerDataDir);
    var allDataString = mapper.writeValueAsString(replayerResult);
    var stringPath = rootPath + File.separator + name + ".json";
    FileWriter myWriter = new FileWriter(stringPath);
    myWriter.write(allDataString);
    myWriter.close();
  }
}
