package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.apis.models.ListAllRecordList;
import org.kendar.replayer.storage.DataReorganizer;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

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
      method = "GET",
      id = "5000daa6-277f-11ec-9621-0242ac1afe002")
  public boolean retrieveSingleLineData(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var line = Integer.parseInt(req.getPathParameter("line"));

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
        new ReplayerDataset(
            id, rootPath.toString(), null, loggerBuilder, dataReorganizer, md5Tester);
    var datasetContent = dataset.load();
    ListAllRecordList result = new ListAllRecordList(datasetContent, id);
    for (var singleLine : result.getLines()) {
      if (singleLine.getId() == line) {
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(singleLine));
        return true;
      }
    }
    res.setStatusCode(404);
    res.setResponseText("Missing id " + id + " with line " + line);

    return false;
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}",
      method = "PUT",
      id = "5001daa6-277f-11ec-9621-0242ac1afe002")
  public boolean modifySingleLineData(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var line = Integer.parseInt(req.getPathParameter("line"));

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
        new ReplayerDataset(
            id, rootPath.toString(), null, loggerBuilder, dataReorganizer, md5Tester);
    var datasetContent = dataset.load();
    for (var destination : datasetContent.getStaticRequests()) {
      if (destination.getId() == line) {
        var source = mapper.readValue(req.getRequestText(), ReplayerRow.class);
        cloneToRow(destination, source);
        dataset.saveMods();
        return true;
      }
    }
    for (var destination : datasetContent.getDynamicRequests()) {
      if (destination.getId() == line) {
        var source = mapper.readValue(req.getRequestText(), ReplayerRow.class);
        cloneToRow(destination, source);
        dataset.saveMods();
        return true;
      }
    }
    res.setStatusCode(404);
    res.setResponseText("Missing id " + id + " with line " + line);
    return false;
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}",
      method = "POST",
      id = "5002daa6-277f-11ec-9621-0242ac1afe002")
  public boolean addLineData(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var line = Integer.parseInt(req.getPathParameter("line"));

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
        new ReplayerDataset(
            id, rootPath.toString(), null, loggerBuilder, dataReorganizer, md5Tester);
    var datasetContent = dataset.load();
    for (var destination : datasetContent.getStaticRequests()) {
      if (destination.getId() == line) {
        res.setStatusCode(409);
        res.setResponseText("Duplicate id");
        return true;
      }
    }
    for (var destination : datasetContent.getDynamicRequests()) {
      if (destination.getId() == line) {
        res.setStatusCode(409);
        res.setResponseText("Duplicate id");
        return true;
      }
    }

    var source = mapper.readValue(req.getRequestText(), ReplayerRow.class);
    datasetContent.getDynamicRequests().add(source);
    dataset.saveMods();
    res.setStatusCode(200);
    return false;
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}",
      method = "DELETE",
      id = "5003daa6-277f-11ec-9621-0242ac1afe002")
  public boolean deleteSingleLineData(Request req, Response res) throws IOException {
    var id = req.getPathParameter("id");
    var line = Integer.parseInt(req.getPathParameter("line"));

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
        new ReplayerDataset(
            id, rootPath.toString(), null, loggerBuilder, dataReorganizer, md5Tester);
    dataset.load();
    dataset.delete(line);
    dataset.saveMods();
    res.setStatusCode(200);
    return false;
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
  }
}
