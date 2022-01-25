package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.*;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class JsFilterAPI implements FilteringClass {
  private final JsonConfiguration configuration;
  private final Logger logger;
  private final FileResourcesUtils fileResourcesUtils;
  final ObjectMapper mapper = new ObjectMapper();

  public JsFilterAPI(JsonConfiguration configuration,
                     FileResourcesUtils fileResourcesUtils,
                     LoggerBuilder loggerBuilder) {

    this.logger = loggerBuilder.build(JsFilterAPI.class);
    this.configuration = configuration;
    this.fileResourcesUtils = fileResourcesUtils;
  }

  @Override
  public String getId() {
    return "org.kendar.servers.http.api.JsFilterController";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/jsfilter",
      method = "GET",
      id = "1000a4b4-297id-11ec-9621-0242ac130002")
  public boolean getJsFiltersList(Request req, Response res) throws JsonProcessingException {
    var jsFilterPath = configuration.getConfiguration(JsFilterConfig.class).getPath();

    var result = new ArrayList<String>();
    String currentPath = "";
    try {
      File f;
      var realPath = fileResourcesUtils.buildPath(jsFilterPath);
      f = new File(realPath);
      if (f.exists()) {
        var pathnames = f.list();
        if (pathnames != null) {
          // For each pathname in the pathnames array
          for (String pathname : pathnames) {
            var fullPath = fileResourcesUtils.buildPath(jsFilterPath, pathname);
            currentPath = fullPath;

            var descriptor = loadScriptId( realPath, fullPath);
            if(descriptor!=null)result.add(descriptor);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error reading js filter " + currentPath, e);
    }
    res.addHeader("Content-type", "application/json");
    res.setResponseText(mapper.writeValueAsString(result));
    return false;
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/{filtername}",
          method = "GET",
          id = "1000a4b4-297id-11ec-9777-0242ac130002")
  public boolean getJsFilter(Request req, Response res) {
    var jsFilterPath = configuration.getConfiguration(JsFilterConfig.class).getPath();
    var jsFilterDescriptor = req.getPathParameter("filtername");


    // https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
    try {
      File f;
      var realPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor+".json");
      var subPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor);
      var result = loadSinglePlugin(realPath,jsFilterPath);

      res.addHeader("Content-type", "application/json");
      res.setResponseText(mapper.writeValueAsString(result));
    } catch (Exception e) {
      logger.error("Error reading js filter " + jsFilterDescriptor, e);
    }
    return false;
  }

  private String loadScriptId(String realPath, String fullPath){
    var newFile = new File(fullPath);
    if (newFile.isFile()) {
      var path = Path.of(fullPath);
      var fname= path.getFileName().toString();

      int pos = fname.lastIndexOf(".");
      if(!fname.toLowerCase(Locale.ROOT).endsWith(".json")){
        return null;
      }
      if (pos > 0) {
        fname = fname.substring(0, pos);
      }
      return fname;
    }
    return null;
  }

  private JsFilterDescriptor loadSinglePlugin(String realPath,String jspluginsPath) throws IOException {
    var newFile = new File(realPath);
    if (newFile.isFile()) {
      var data = Files.readString(Path.of(realPath));
      var filterDescriptor = mapper.readValue(data, JsFilterDescriptor.class);
      filterDescriptor.setRoot(jspluginsPath);
      return filterDescriptor;
    }
    return null;
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/{filtername}/{file}",
          method = "GET",
          id = "1000a4b4-47id-11ec-9777-0242ac130002")
  public boolean getJsFilterFile(Request req, Response res) {
    var jsFilterPath = configuration.getConfiguration(JsFilterConfig.class).getPath();
    var jsFilterDescriptor = req.getPathParameter("filtername");
    var fileId = req.getPathParameter("file");
    if(fileId==null || fileId.isEmpty()){
      res.addHeader("Content-type", "text/plain");
      res.setResponseText("");
      return false;
    }


    // https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
    try {
      File f;
      var realPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor+".json");
      var subPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor,fileId);
      //var result = loadSinglePlugin(realPath,jsFilterPath);
      var result = Files.readString(Path.of(subPath));
      res.addHeader("Content-type", "text/plain");
      res.setResponseText(result);
    } catch (Exception e) {
      logger.error("Error reading js filter " + jsFilterDescriptor, e);
    }
    return false;
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/{filtername}/{file}",
          method = "POST",
          id = "10iyh4b4-47id-11ec-9777-0242ac130002")
  public boolean putJsFilterFile(Request req, Response res) {
    var jsFilterPath = configuration.getConfiguration(JsFilterConfig.class).getPath();
    var jsFilterDescriptor = req.getPathParameter("filtername");
    var fileId = req.getPathParameter("file");


    // https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
    try {
      File f;
      var realPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor+".json");
      var subPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor);
      var result = mapper.readValue(Files.readString(Path.of(realPath)),JsFilterDescriptor.class);

      var founded = false;
      if(result.getRequires()!=null){
        for (var require :
                result.getRequires()) {
          if(require.equalsIgnoreCase(fileId)){
            founded=true;
          }
        }
      }else{
        result.setRequires(new ArrayList<>());
      }
      if(!founded){
        result.getRequires().add(fileId);
        Files.writeString(Path.of(realPath),mapper.writeValueAsString(result));
      }
      var subPathSubFile = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor,fileId);
      //var result = loadSinglePlugin(realPath,jsFilterPath);
      var content = req.getRequestText();
      Files.writeString(Path.of(subPathSubFile),content);
      res.setStatusCode(200);
    } catch (Exception e) {
      logger.error("Error reading js filter " + jsFilterDescriptor, e);
    }
    return false;
  }
}
