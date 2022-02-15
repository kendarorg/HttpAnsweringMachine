package org.kendar.replayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.replayer.storage.DataReorganizer;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ReplayerStatus {

  private static final String MAIN_FILE = "runall.json";
  private final LoggerBuilder loggerBuilder;
  private final DataReorganizer dataReorganizer;
  private final FileResourcesUtils fileResourcesUtils;
  private final ObjectMapper mapper = new ObjectMapper();
  private ReplayerDataset dataset;
  private ReplayerState state = ReplayerState.NONE;
  private final String replayerData;
  private final Md5Tester md5Tester;

  public ReplayerStatus(
      LoggerBuilder loggerBuilder,
      DataReorganizer dataReorganizer,
      FileResourcesUtils fileResourcesUtils,
      Md5Tester md5Tester,
      JsonConfiguration configuration) {

    this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();
    this.loggerBuilder = loggerBuilder;
    this.dataReorganizer = dataReorganizer;
    this.fileResourcesUtils = fileResourcesUtils;
    this.md5Tester = md5Tester;
  }

  public void startRecording(String id, String description) throws IOException {
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));
    if (!Files.isDirectory(rootPath)) {
      Files.createDirectory(rootPath);
    }
    if (state != ReplayerState.NONE) return;
    state = ReplayerState.RECORDING;
    dataset =
        new ReplayerDataset(
            id, rootPath.toString(), description, loggerBuilder, dataReorganizer, md5Tester);
  }

  public void addRequest(Request req, Response res) {
    if (state != ReplayerState.RECORDING) return;
    dataset.add(req, res);
  }

  public boolean replay(Request req, Response res) {
    if (state != ReplayerState.REPLAYING) return false;
    Response response = dataset.findResponse(req);
    if (response != null) {
      res.setBinaryResponse(response.isBinaryResponse());
      if (response.isBinaryResponse()) {
        res.setResponseBytes(response.getResponseBytes());
      } else {
        res.setResponseText(response.getResponseText());
      }
      res.setHeaders(response.getHeaders());
      res.setStatusCode(response.getStatusCode());
      return true;
    }
    return false;
  }

  public ReplayerState getStatus() {
    if (state == null) return ReplayerState.NONE;
    return state;
  }

  public String getCurrentScript() {
    if (dataset == null) return null;
    return dataset.getName();
  }

  public void restartRecording() {
    if (state != ReplayerState.PAUSED_RECORDING) return;
    state = ReplayerState.RECORDING;
  }

  public void pauseRecording() {
    if (state != ReplayerState.RECORDING) return;
    state = ReplayerState.PAUSED_RECORDING;
  }

  public void stopAndSave() throws IOException {

    if (state != ReplayerState.PAUSED_RECORDING && state != ReplayerState.RECORDING) return;
    state = ReplayerState.NONE;
    dataset.save();
    dataset = null;
  }

  public void startReplaying(String id) throws IOException {
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));
    if (!Files.isDirectory(rootPath)) {
      Files.createDirectory(rootPath);
    }
    if (state != ReplayerState.NONE) return;
    state = ReplayerState.REPLAYING;
    dataset =
        new ReplayerDataset(
            id, rootPath.toString(), null, loggerBuilder, dataReorganizer, md5Tester);
    dataset.load();
  }

  public void restartReplaying() {
    if (state != ReplayerState.PAUSED_REPLAYING) return;
    state = ReplayerState.REPLAYING;
  }

  public void pauseReplaying() {
    if (state != ReplayerState.REPLAYING) return;
    state = ReplayerState.PAUSED_REPLAYING;
  }

  public void stopReplaying() {
    state = ReplayerState.NONE;
    dataset = null;
  }
}
