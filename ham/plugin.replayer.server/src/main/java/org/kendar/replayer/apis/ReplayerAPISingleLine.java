package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.apis.models.ListAllRecordList;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.DataReorganizer;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPISingleLine implements FilteringClass {

  private final FileResourcesUtils fileResourcesUtils;
  private final LoggerBuilder loggerBuilder;
  private final DataReorganizer dataReorganizer;
  final ObjectMapper mapper = new ObjectMapper();
  private final Md5Tester md5Tester;
  private final String replayerData;

  public ReplayerAPISingleLine(
      FileResourcesUtils fileResourcesUtils,
      LoggerBuilder loggerBuilder,
      DataReorganizer dataReorganizer,
      Md5Tester md5Tester,
      JsonConfiguration configuration) {

    this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();

    this.fileResourcesUtils = fileResourcesUtils;
    this.loggerBuilder = loggerBuilder;
    this.dataReorganizer = dataReorganizer;
    this.md5Tester = md5Tester;
  }

  @Override
  public String getId() {
    return "org.kendar.replayer.apis.ReplayerAPISingleLine";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}",
      method = "GET")
  @HamDoc(
          tags = {"plugin/replayer"},
          description = "Returns a single line data",
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")},
          responses = @HamResponse(
                  body = ReplayerRow.class
          )
  )
  public void retrieveSingleLineData(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var line = Integer.parseInt(req.getPathParameter("line"));

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
        new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
    dataset.load(id, rootPath.toString(),null);
    var datasetContent = dataset.load();
    var result = new ListAllRecordList(datasetContent, id,false).getLines();
    result.sort(Comparator.comparingInt(ReplayerRow::getId));
    for (var i=0;i<result.size();i++) {
      var singleLine = result.get(i);
      if (singleLine.getId() == line) {
        var prev = -1;
        var next = -1;
        if(i>0){
          prev=result.get(i-1).getId();
        }
        if(i<(result.size()-1)){
          next=result.get(i+1).getId();
        }
        res.addHeader("X-NEXT", ""+next);
        res.addHeader("X-PREV", ""+prev);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(singleLine));
        return;
      }
    }
    res.setStatusCode(404);
    res.setResponseText("Missing id " + id + " with line " + line);
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}",
      method = "PUT")
  @HamDoc(description = "Modify a rreplayer row",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")},
          requests = @HamRequest(
                  body = ReplayerRow.class
          )
  )
  public void modifySingleLineData(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var line = Integer.parseInt(req.getPathParameter("line"));

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
        new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
    dataset.load(id, rootPath.toString(),null);
    var datasetContent = dataset.load();
    for (var destination : datasetContent.getStaticRequests()) {
      if (destination.getId() == line) {
        var source = mapper.readValue(req.getRequestText(), ReplayerRow.class);
        cloneToRow(destination, source);
        dataset.saveMods();
        return;
      }
    }
    for (var destination : datasetContent.getDynamicRequests()) {
      if (destination.getId() == line) {
        var source = mapper.readValue(req.getRequestText(), ReplayerRow.class);
        cloneToRow(destination, source);
        dataset.saveMods();
        return ;
      }
    }
    res.setStatusCode(404);
    res.setResponseText("Missing id " + id + " with line " + line);
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}",
      method = "POST")
  @HamDoc(description = "Add a replayer row",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")},
          requests = @HamRequest(
                  body = ReplayerRow.class
          )
  )
  public void addLineData(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var line = Integer.parseInt(req.getPathParameter("line"));

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
        new ReplayerDataset( loggerBuilder, dataReorganizer, md5Tester);
    dataset.load(id, rootPath.toString(),null);
    var datasetContent = dataset.load();
    for (var destination : datasetContent.getStaticRequests()) {
      if (destination.getId() == line) {
        res.setStatusCode(409);
        res.setResponseText("Duplicate id");
        return;
      }
    }
    for (var destination : datasetContent.getDynamicRequests()) {
      if (destination.getId() == line) {
        res.setStatusCode(409);
        res.setResponseText("Duplicate id");
        return;
      }
    }

    var source = mapper.readValue(req.getRequestText(), ReplayerRow.class);
    datasetContent.getDynamicRequests().add(source);
    dataset.saveMods();
    res.setStatusCode(200);
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}",
      method = "DELETE")
  @HamDoc(description = "Remove a replayer row",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")}
  )
  public void deleteSingleLineData(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var line = Integer.parseInt(req.getPathParameter("line"));

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
        new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
    dataset.load(id, rootPath.toString(),null);
    dataset.load();
    dataset.delete(line);
    dataset.saveMods();
    res.setStatusCode(200);
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/indexline/{line}",
          method = "DELETE")
  @HamDoc(description = "Remove the indexline (aka the pointer to replayer row)",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")}
  )
  public void deleteSingleIndexLineData(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var line = Integer.parseInt(req.getPathParameter("line"));

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
            new ReplayerDataset( loggerBuilder, dataReorganizer, md5Tester);
    dataset.load(id, rootPath.toString(),null);
    dataset.load();
    dataset.deleteIndex(line);
    dataset.saveMods();
    res.setStatusCode(200);
  }

  private void cloneToRow(ReplayerRow destination, ReplayerRow source) {
    destination.getResponse().setBinaryResponse(source.getResponse().isBinaryResponse());
    destination.getResponse().setHeaders(source.getResponse().getHeaders());
    destination.getResponse().setStatusCode(source.getResponse().getStatusCode());

    destination.getRequest().setBinaryRequest(source.getRequest().isBinaryRequest());
    destination.getRequest().setHeaders(source.getRequest().getHeaders());
    destination.getRequest().setMethod(source.getRequest().getMethod());
    destination.getRequest().setProtocol(source.getRequest().getProtocol());
    destination.getRequest().setQuery(source.getRequest().getQuery());
    destination.getRequest().setHost(source.getRequest().getHost());
    destination.getRequest().setPath(source.getRequest().getPath());
    destination.getRequest().setPort(source.getRequest().getPort());
    destination.getRequest().setPostParameters(source.getRequest().getPostParameters());
    destination.getRequest().setStaticRequest(source.getRequest().isStaticRequest());
    destination.getRequest().setSoapRequest(source.getRequest().isSoapRequest());
    destination.setStimulatedTest(source.isStimulatedTest());
  }


  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/lineindex/{line}",
          method = "GET")
  @HamDoc(description = "Retrieves the indexline (aka the pointer to replayer row)",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")},
          responses = @HamResponse(
            body =  CallIndex.class
          )
  )
  public void retrieveSingleLineIndexData(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var line = Integer.parseInt(req.getPathParameter("line"));

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
            new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
    dataset.load(id, rootPath.toString(),null);
    var datasetContent = dataset.load();
    ListAllRecordList result = new ListAllRecordList(datasetContent, id,false);
    for (var singleLine : result.getIndexes()) {
      if (singleLine.getId() == line) {
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(singleLine));
        return;
      }
    }
    res.setStatusCode(404);
    res.setResponseText("Missing id " + id + " with line " + line);
  }



  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/lineindex/{line}",
          method = "PUT")
  @HamDoc(description = "Addes the indexline (aka the pointer to replayer row)",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")},
          requests = @HamRequest(
                  body =  CallIndex.class
          )
  )
  public void modifySingleLineIndexData(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var line = Integer.parseInt(req.getPathParameter("line"));

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
            new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
    dataset.load(id, rootPath.toString(),null);
    var datasetContent = dataset.load();
    for (var destination : datasetContent.getIndexes()) {
      if (destination.getId() == line) {
        var source = mapper.readValue(req.getRequestText(), CallIndex.class);
        cloneToIndex(destination, source);
        dataset.saveMods();
        return;
      }
    }
    res.setStatusCode(404);
    res.setResponseText("Missing id " + id + " with line " + line);
  }

  private void cloneToIndex(CallIndex destination, CallIndex source) {
    destination.setStimulatorTest(source.isStimulatorTest());
    destination.setPactTest(source.isPactTest());
    destination.setReference(source.getReference());
    destination.setDescription(source.getDescription());
  }
}
