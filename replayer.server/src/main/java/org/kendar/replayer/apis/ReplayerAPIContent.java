package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.storage.DataReorganizer;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.models.JsonFileData;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPIContent implements FilteringClass {
  private final FileResourcesUtils fileResourcesUtils;
  private final LoggerBuilder loggerBuilder;
  private final DataReorganizer dataReorganizer;
  private final Md5Tester md5Tester;
  private final String replayerData;
  ObjectMapper mapper = new ObjectMapper();

  public ReplayerAPIContent(
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
    return "org.kendar.replayer.apis.ReplayerAPIContent";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}/{requestOrResponse}",
      method = "GET",
      id = "3004daa6-277f-11ec-9621-0242ac1afe002")
  public void retrieveContent(Request req, Response res) throws IOException {
    var id = getPathParameter(req, "id");
    var line = Integer.parseInt(getPathParameter(req, "line"));
    var requestOrResponse = getPathParameter(req, "requestOrResponse");

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
        new ReplayerDataset(
            id, rootPath.toString(), null, loggerBuilder, dataReorganizer, md5Tester);
    var datasetContent = dataset.load();

    for (var singleLine : datasetContent.getStaticRequests()) {
      if (sendBackContent(res, line, requestOrResponse, singleLine)) {
        return;
      }
    }
    for (var singleLine : datasetContent.getDynamicRequests()) {
      if (sendBackContent(res, line, requestOrResponse, singleLine)) {
        return;
      }
    }
    res.setStatusCode(404);
    res.setResponseText("Missing id " + id + " with line " + line);
  }

  private String getPathParameter(Request req, String id) {
    return req.getPathParameter(id);
  }

  private boolean sendBackContent(
      Response res, int line, String requestOrResponse, ReplayerRow singleLine) {
    if (singleLine.getId() == line) {
      var allTypes= MimeTypes.getDefaultMimeTypes();
      if ("request".equalsIgnoreCase(requestOrResponse)) {
        var contentType =singleLine.getRequest().getHeader("Content-Type");
        res.addHeader("Content-Type", contentType);
        res.setBinaryResponse(singleLine.getRequest().isBinaryRequest());
        if (singleLine.getRequest().isBinaryRequest()) {
          res.setResponseBytes(singleLine.getRequest().getRequestBytes());
        } else {
          res.setResponseText(singleLine.getRequest().getRequestText());
        }

        setResultContentType(res, line, allTypes, contentType);

      } else if ("response".equalsIgnoreCase(requestOrResponse)) {
        var contentType = singleLine.getResponse().getHeader("Content-Type");
        res.addHeader("Content-Type", contentType);
        res.setBinaryResponse(singleLine.getResponse().isBinaryResponse());
        if (singleLine.getResponse().isBinaryResponse()) {
          res.setResponseBytes(singleLine.getResponse().getResponseBytes());
        } else {
          res.setResponseText(singleLine.getResponse().getResponseText());
        }
        setResultContentType(res, line, allTypes, contentType);
      }
      if(res.getHeader("Content-Type") == null) {
        if (res.isBinaryResponse()) {
          res.addHeader("Content-Type", "application/octet-stream");
          res.addHeader("Content-Disposition","request."+ line +".bin");
        } else {
          res.addHeader("Content-Type", "text/plain");
          res.addHeader("Content-Disposition","request."+ line +".txt");
        }
      }
      return true;
    }
    return false;
  }

  private void setResultContentType(Response res, int line, MimeTypes allTypes, String contentType) {
    try {
      MimeType mimeType = allTypes.forName(contentType);
      String ext = mimeType.getExtension();
      res.addHeader("Content-Disposition","request."+ line +ext);
    } catch (MimeTypeException e) {
      res.addHeader("Content-Disposition","request."+ line +".bin");
    }
  }

  private boolean deleted(
      Response res, int line, String requestOrResponse, ReplayerRow singleLine) {
    if (singleLine.getId() == line) {
      if ("request".equalsIgnoreCase(requestOrResponse)) {
        singleLine.getRequest().setRequestBytes(null);
        singleLine.getRequest().setRequestText(null);
        singleLine.setRequestHash("0");
      } else if ("response".equalsIgnoreCase(requestOrResponse)) {
        singleLine.getResponse().setResponseText(null);
        singleLine.getResponse().setResponseBytes(null);
        singleLine.setResponseHash("0");
      }
      return true;
    }
    return false;
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}/{requestOrResponse}",
      method = "DELETE",
      id = "3005daa6-277f-11ec-9621-0242ac1afe002")
  public void deleteConent(Request req, Response res) throws IOException {
    var id = getPathParameter(req, "id");
    var line = Integer.parseInt(getPathParameter(req, "line"));
    var requestOrResponse = getPathParameter(req, "requestOrResponse");

    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

    var dataset =
        new ReplayerDataset(
            id, rootPath.toString(), null, loggerBuilder, dataReorganizer, md5Tester);
    var datasetContent = dataset.load();

    for (var singleLine : datasetContent.getStaticRequests()) {
      if (deleted(res, line, requestOrResponse, singleLine)) {
        dataset.saveMods();
        return;
      }
    }
    for (var singleLine : datasetContent.getDynamicRequests()) {
      if (deleted(res, line, requestOrResponse, singleLine)) {
        dataset.saveMods();
        return;
      }
    }
    res.setStatusCode(404);
    res.setResponseText("Missing id " + id + " with line " + line);
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}/{requestOrResponse}",
      method = "POST",
      id = "3006daa6-277f-11ec-9621-0242ac1afe002")
  public void modifyConent(Request req, Response res)
      throws IOException, NoSuchAlgorithmException {
    var id = getPathParameter(req, "id");
    var line = Integer.parseInt(getPathParameter(req, "line"));
    var requestOrResponse = getPathParameter(req, "requestOrResponse");
    var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));
    var data = mapper.readValue(req.getRequestText(), JsonFileData.class);

    var dataset =
        new ReplayerDataset(
            id, rootPath.toString(), null, loggerBuilder, dataReorganizer, md5Tester);
    var datasetContent = dataset.load();

    for (var singleLine : datasetContent.getStaticRequests()) {
      if (updated( line, requestOrResponse, singleLine, data)) {
        dataset.saveMods();
        return;
      }
    }
    for (var singleLine : datasetContent.getDynamicRequests()) {
      if (updated( line, requestOrResponse, singleLine, data)) {
        dataset.saveMods();
        return;
      }
    }
    res.setStatusCode(404);
    res.setResponseText("Missing id " + id + " with line " + line);
  }

  private boolean updated(
      int line,
      String requestOrResponse,
      ReplayerRow singleLine,
      JsonFileData data)
      throws NoSuchAlgorithmException {
    if (singleLine.getId() == line) {
      if ("request".equalsIgnoreCase(requestOrResponse)) {
        singleLine.setRequestHash(md5Tester.calculateMd5(data.readAsByte()));
        if (!data.matchContentType("text/plain")) {
          singleLine.getRequest().setRequestBytes(data.readAsByte());
          singleLine.getRequest().setBinaryRequest(true);
        }else{
          singleLine.getRequest().setRequestText(data.readAsString());
          singleLine.getRequest().setBinaryRequest(false);
        }

      } else if ("response".equalsIgnoreCase(requestOrResponse)) {
        singleLine.setResponseHash(md5Tester.calculateMd5(data.readAsByte()));
        if (!data.matchContentType("text/plain")) {
          singleLine.getResponse().setResponseBytes(data.readAsByte());
          singleLine.getResponse().setBinaryResponse(true);
        }else{
          singleLine.getResponse().setResponseText(data.readAsString());
          singleLine.getResponse().setBinaryResponse(false);
        }
      }
      return true;
    }
    return false;
  }
}
