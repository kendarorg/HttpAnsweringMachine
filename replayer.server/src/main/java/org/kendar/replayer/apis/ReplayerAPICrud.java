package org.kendar.replayer.apis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.replayer.apis.models.ListAllRecordList;
import org.kendar.replayer.apis.models.LocalRecording;
import org.kendar.replayer.apis.models.ScriptData;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.models.JsonFileData;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPICrud implements FilteringClass {
  final ObjectMapper mapper = new ObjectMapper();
  private final Logger logger;
  private final ReplayerStatus replayerStatus;
  private final Md5Tester md5Tester;
  private final String replayerData;

  private final FileResourcesUtils fileResourcesUtils;
  private final LoggerBuilder loggerBuilder;

  public ReplayerAPICrud(
      FileResourcesUtils fileResourcesUtils,
      LoggerBuilder loggerBuilder,
      ReplayerStatus replayerStatus,
      Md5Tester md5Tester,
      JsonConfiguration configuration) {

    this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();

    this.fileResourcesUtils = fileResourcesUtils;

    this.loggerBuilder = loggerBuilder;
    this.logger = loggerBuilder.build(ReplayerAPICrud.class);
    this.replayerStatus = replayerStatus;
    this.md5Tester = md5Tester;
  }

  @Override
  public String getId() {
    return "org.kendar.replayer.apis.ReplayerAPICrud";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording",
      method = "GET",
      id = "4000daa6-277f-11ec-9621-0242ac1afe002")
  public boolean listAllLocalRecordings(Request req, Response res) throws JsonProcessingException {
    var realPath = fileResourcesUtils.buildPath(replayerData);
    var f = new File(realPath);
    var pathNames = f.list();
    var listOfItems = new ArrayList<LocalRecording>();
    var currentScript = replayerStatus.getCurrentScript();
    if (pathNames != null) {
      for (var pathname : pathNames) {
        if (pathname.toLowerCase(Locale.ROOT).endsWith(".json")) {
          var lr = new LocalRecording();
          var tocheck = pathname.substring(0, pathname.length() - 5);
          lr.setId(tocheck);
          lr.setState(ReplayerState.NONE);
          if (tocheck.toLowerCase(Locale.ROOT).equalsIgnoreCase(currentScript)) {
            lr.setState(replayerStatus.getStatus());
          }
          listOfItems.add(lr);
        }
      }
    }
    res.addHeader("Content-type", "application/json");
    res.setResponseText(mapper.writeValueAsString(listOfItems));
    return false;
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}",
      method = "GET",
      id = "4001daa6-277f-11ec-9621-0242ac1afe002")
  public boolean listAllRecordingSteps(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
        new ReplayerDataset(id, rootPath.toString(), null, loggerBuilder, null, md5Tester);
    var datasetContent = dataset.load();
    ListAllRecordList result = new ListAllRecordList(datasetContent, id);
    res.addHeader("Content-type", "application/json");
    res.setResponseText(mapper.writeValueAsString(result));
    return false;
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}",
      method = "DELETE",
      id = "4002daa6-277f-11ec-9621-0242ac1afe002")
  public boolean deleteRecordin(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, id + ".json"));
    if (Files.exists(rootPath)) {
      Files.delete(rootPath);
    }
    res.setStatusCode(200);
    return false;
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}",
      method = "PUT",
      id = "4003daa6-277f-11ec-9621-0242ac1afe002")
  public boolean updateRecord(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, id + ".json"));
    if (Files.exists(rootPath)) {
      var fileContent = Files.readString(rootPath);
      var result = mapper.readValue(fileContent, ReplayerResult.class);
      var scriptData = mapper.readValue(req.getRequestText(), ScriptData.class);
      result.setDescription(scriptData.getDescription());
      result.setFilter(scriptData.getFilter());
      var resultInFile = mapper.writeValueAsString(result);
      Files.write(rootPath, resultInFile.getBytes(StandardCharsets.UTF_8));
    }
    res.setStatusCode(200);
    return false;
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording",
      method = "POST",
      id = "4004daa6-277f-11ec-9621-0242ac1afe002")
  public boolean uploadRecording(Request req, Response res) throws Exception {
    var jsonFileData = mapper.readValue(req.getRequestText(), JsonFileData.class);
    var fileFullPath = jsonFileData.getName();

    var scriptName = fileFullPath.substring(0, fileFullPath.lastIndexOf('.'));
    var crud = mapper.readValue(jsonFileData.readAsString(),ReplayerResult.class);
    crud.setDescription(scriptName);

    var dirPath = new File(Path.of(fileResourcesUtils.buildPath(replayerData)).toString());
    if(!dirPath.exists()){
      dirPath.mkdir();
    }
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, scriptName + ".json"));
    var resultInFile = mapper.writeValueAsString(crud);
    Files.write(rootPath, resultInFile.getBytes(StandardCharsets.UTF_8));
    logger.info("Uploaded replayer binary script " + rootPath);
    res.setStatusCode(200);

    return false;
  }
}
