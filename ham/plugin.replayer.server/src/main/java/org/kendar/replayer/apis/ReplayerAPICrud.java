package org.kendar.replayer.apis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.Example;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.replayer.apis.models.ListAllRecordList;
import org.kendar.replayer.apis.models.LocalRecording;
import org.kendar.replayer.apis.models.ScriptData;
import org.kendar.replayer.storage.DbRecording;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.models.JsonFileData;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPICrud implements FilteringClass {
  final ObjectMapper mapper = new ObjectMapper();
  private final Logger logger;
  private final ReplayerStatus replayerStatus;
  private final Md5Tester md5Tester;
  private HibernateSessionFactory sessionFactory;
  private final String replayerData;

  private final FileResourcesUtils fileResourcesUtils;
  private final LoggerBuilder loggerBuilder;

  public ReplayerAPICrud(
          FileResourcesUtils fileResourcesUtils,
          LoggerBuilder loggerBuilder,
          ReplayerStatus replayerStatus,
          Md5Tester md5Tester,
          JsonConfiguration configuration,
          HibernateSessionFactory sessionFactory) {

    this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();

    this.fileResourcesUtils = fileResourcesUtils;

    this.loggerBuilder = loggerBuilder;
    this.logger = loggerBuilder.build(ReplayerAPICrud.class);
    this.replayerStatus = replayerStatus;
    this.md5Tester = md5Tester;
    this.sessionFactory = sessionFactory;
  }

  @Override
  public String getId() {
    return "org.kendar.replayer.apis.ReplayerAPICrud";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording",
      method = "GET")
  @HamDoc(description = "Retrieves the list of recordings",tags = {"plugin/replayer"},
    responses = @HamResponse(body = String[].class))
  public void listAllLocalRecordings(Request req, Response res) throws Exception {

    var listOfItems = new ArrayList<LocalRecording>();
    var currentScript = replayerStatus.getCurrentScript();
    sessionFactory.query((em -> {
      List< DbRecording> allRecs= em.createQuery("SELECT e FROM DbRecording e").getResultList();
      for(var rs: allRecs){
        var lr = new LocalRecording();
        lr.setId(rs.getId());
        lr.setState(ReplayerState.NONE);
        if (rs.getId()==currentScript) {
          lr.setState(replayerStatus.getStatus());
        }
        listOfItems.add(lr);
      }
    }));
    res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
    res.setResponseText(mapper.writeValueAsString(listOfItems));
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}",
      method = "GET")
  @HamDoc(description = "Retrieve the content of a single recording",tags = {"plugin/replayer"},
          path = @PathParameter(key = "id"),
          responses = @HamResponse(body =ListAllRecordList.class)
  )
  public void listAllRecordingSteps(Request req, Response res) throws Exception {
    var id = Long.parseLong(req.getPathParameter("id"));




    //var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    //var dataset =
    //    new ReplayerDataset( loggerBuilder, null, md5Tester);
    //dataset.load(id, rootPath.toString(),null);
    //var datasetContent = dataset.load();
    ListAllRecordList result = new ListAllRecordList(sessionFactory, id,true);
    result.getLines().sort(Comparator.comparingLong(ReplayerRow::getId));
    res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
    res.setResponseText(mapper.writeValueAsString(result));
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}",
      method = "DELETE")
  @HamDoc(description = "Delete a recording",tags = {"plugin/replayer"},
          path = @PathParameter(key = "id")
  )
  public void deleteRecordin(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, id + ".json"));
    if (Files.exists(rootPath)) {
      Files.delete(rootPath);
    }
    res.setStatusCode(200);
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}",
      method = "PUT")
  @HamDoc(description = "Modify an exesting recording",tags = {"plugin/replayer"},
          path = @PathParameter(key = "id"),
          requests = @HamRequest(body = ReplayerResult.class)
  )
  public void updateRecord(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, id + ".json"));
    if (Files.exists(rootPath)) {
      var fileContent = FileUtils.readFileToString(rootPath.toFile(),"UTF-8");
      var result = mapper.readValue(fileContent, ReplayerResult.class);
      var scriptData = mapper.readValue(req.getRequestText(), ScriptData.class);
      result.setDescription(scriptData.getDescription());
      result.setFilter(scriptData.getFilter());

      //Update indexes
      //Update fulls
      for (var indexLine : result.getIndexes()) {
        indexLine.setPactTest(scriptData.getPactTest().stream().anyMatch(a->a.intValue()==indexLine.getId()));
      }
      for (var indexLine : result.getIndexes()) {
        indexLine.setStimulatorTest(scriptData.getStimulatorTest().stream().anyMatch(a->a.intValue()==indexLine.getId()));
      }

      for (var row : result.getDynamicRequests()) {
        row.setStimulatedTest(scriptData.getStimulatedTest().stream().anyMatch(a->a.intValue()==row.getId()));
      }
      for (var row : result.getStaticRequests()) {
        row.setStimulatedTest(scriptData.getStimulatedTest().stream().anyMatch(a->a.intValue()==row.getId()));
      }

      var resultInFile = mapper.writeValueAsString(result);
      Files.write(rootPath, resultInFile.getBytes(StandardCharsets.UTF_8));
    }
    res.setStatusCode(200);
  }


  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/full",
          method = "GET")
  @HamDoc(description = "Alternative retrieval of recording",tags = {"plugin/replayer"},
          path = @PathParameter(key = "id"),
          responses = @HamResponse(body = String.class)

  )
  public void getFull(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, id + ".json"));
    if (Files.exists(rootPath)) {
      var fileContent = FileUtils.readFileToString(rootPath.toFile(),"UTF-8");
      res.setResponseText(fileContent);
      res.addHeader(ConstantsHeader.CONTENT_TYPE,ConstantsMime.JSON);
      res.addHeader("Content-Disposition", "attachment;"+id+".json");
      res.setStatusCode(200);
    }else {
      res.setStatusCode(404);
    }
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording",
      method = "POST")
  @HamDoc(description = "Create/update recording",tags = {"plugin/replayer"},
    requests = @HamRequest(body=JsonFileData.class))
  public void uploadRecording(Request req, Response res) throws Exception {
    JsonFileData jsonFileData = mapper.readValue(req.getRequestText(), JsonFileData.class);
    String fileFullPath = jsonFileData.getName();

    var scriptName = fileFullPath.substring(0, fileFullPath.lastIndexOf('.'));
    var crud = mapper.readValue(jsonFileData.readAsString(),ReplayerResult.class);
    crud.setDescription(scriptName);

    var dirPath = new File(Path.of(fileResourcesUtils.buildPath(replayerData)).toString());
    if(!dirPath.exists()){
      if(!dirPath.mkdir()){
        res.setResponseText("ERROR CREATING "+ dirPath);
        res.setStatusCode(500);
        return;
      }
    }
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, scriptName + ".json"));
    var resultInFile = mapper.writeValueAsString(crud);
    Files.write(rootPath, resultInFile.getBytes(StandardCharsets.UTF_8));
    logger.info("Uploaded replayer binary script " + rootPath);
    res.setStatusCode(200);
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/deletelines",
          method = "POST")
  @HamDoc(description = "Delete multiple lines of script",tags = {"plugin/replayer"},
          path = @PathParameter(key = "id"),
          requests = @HamRequest(body = String[].class,
          examples = @Example(example = "[1,2,3]"))
  )
  public void deleteLines(Request req, Response res) throws Exception {
    List<Integer> jsonFileData = Arrays.stream(mapper.readValue(req.getRequestText(), Integer[].class)).collect(Collectors.toList());
    var id = req.getPathParameter("id");
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, id + ".json"));
    if (Files.exists(rootPath)) {
      var index = new HashSet<Long>();
      var fileContent = FileUtils.readFileToString(rootPath.toFile(),"UTF-8");
      var result = mapper.readValue(fileContent, ReplayerResult.class);
      for(var i = result.getDynamicRequests().size()-1;i>=0;i--){
        var dq = result.getDynamicRequests().get(i);
        if(jsonFileData.stream().anyMatch(a->a==dq.getId())){
          index.add(dq.getId());
          result.getDynamicRequests().remove(i);

        }
      }
      for(var i = result.getStaticRequests().size()-1;i>=0;i--){
        var dq = result.getStaticRequests().get(i);
        if(jsonFileData.stream().anyMatch(a->a==dq.getId())){
          index.add(dq.getId());
          result.getStaticRequests().remove(i);
        }
      }
      for(var i = result.getIndexes().size()-1;i>=0;i--){
        var dq = result.getIndexes().get(i);
        if(index.contains(dq.getReference())){
          result.getIndexes().remove(i);
        }
      }

      var resultInFile = mapper.writeValueAsString(result);
      Files.write(rootPath, resultInFile.getBytes(StandardCharsets.UTF_8));
    }
    res.setStatusCode(200);
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/clone/{newid}",
          method = "POST")
  @HamDoc(description = "Clone the selected lines in a new request",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id"),@PathParameter(key = "newid")},
          requests = @HamRequest(body = String[].class,
          examples = @Example(example = "[1,2,3]"))
  )
  public void clone(Request req, Response res) throws Exception {
    List<Integer> jsonFileData = Arrays.stream(mapper.readValue(req.getRequestText(), Integer[].class)).collect(Collectors.toList());
    var id = req.getPathParameter("id");
    var newid = req.getPathParameter("newid");
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, id + ".json"));
    var newRootPath = Path.of(fileResourcesUtils.buildPath(replayerData, newid + ".json"));

    if (Files.exists(rootPath)) {
      var fileContent = FileUtils.readFileToString(rootPath.toFile(),"UTF-8");
      var result = mapper.readValue(fileContent, ReplayerResult.class);

      var newReplayerData = new ReplayerResult();
      newReplayerData.setDescription(result.getDescription());
      newReplayerData.setFilter(result.getFilter());
      newReplayerData.setDynamicRequests(new ArrayList<>());
      newReplayerData.setStaticRequests(new ArrayList<>());

      for(var i = result.getDynamicRequests().size()-1;i>=0;i--){
        var dq = result.getDynamicRequests().get(i);
        if(jsonFileData.stream().anyMatch(a->a==dq.getId())){
          newReplayerData.getDynamicRequests().add(dq);
        }
      }
      for(var i = result.getStaticRequests().size()-1;i>=0;i--){
        var dq = result.getStaticRequests().get(i);
        if(jsonFileData.stream().anyMatch(a->a==dq.getId())){
          newReplayerData.getStaticRequests().add(dq);
        }
      }

      var resultInFile = mapper.writeValueAsString(newReplayerData);
      Files.write(newRootPath, resultInFile.getBytes(StandardCharsets.UTF_8));
    }
    res.setStatusCode(200);
  }
}
