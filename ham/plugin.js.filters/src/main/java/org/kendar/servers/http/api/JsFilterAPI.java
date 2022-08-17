package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.http.events.ScriptsModified;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.JsFilterConfig;
import org.kendar.servers.http.JsFilterDescriptor;
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
  private final EventQueue eventQueue;
  final ObjectMapper mapper = new ObjectMapper();

  public JsFilterAPI(JsonConfiguration configuration,
                     FileResourcesUtils fileResourcesUtils,
                     LoggerBuilder loggerBuilder,
                     EventQueue eventQueue) {

    this.logger = loggerBuilder.build(JsFilterAPI.class);
    this.configuration = configuration;
    this.fileResourcesUtils = fileResourcesUtils;
    this.eventQueue = eventQueue;
  }

  @Override
  public String getId() {
    return this.getClass().getName();
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/jsfilter/filters",
      method = "GET")
  @HamDoc(tags = {"plugin/js"},
          description = "List all js filters",
          responses = @HamResponse(
                  body = String[].class
          ))
  public void getJsFiltersList(Request req, Response res) throws JsonProcessingException {
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
    res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
    res.setResponseText(mapper.writeValueAsString(result));
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/filters/{filtername}",
          method = "GET")
  @HamDoc(tags = {"plugin/js"},
          description = "Get Single filter",
          path = @PathParameter(key = "filtername"),
          responses = @HamResponse(
                  body = JsFilterDescriptor.class
          ))
  public void getJsFilter(Request req, Response res) {
    var jsFilterPath = configuration.getConfiguration(JsFilterConfig.class).getPath();
    var jsFilterDescriptor = req.getPathParameter("filtername");


    // https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
    try {
      File f;
      var realPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor+".json");
      var result = loadSinglePlugin(realPath,jsFilterPath);

      res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
      res.setResponseText(mapper.writeValueAsString(result));
    } catch (Exception e) {
      logger.error("Error reading js filter " + jsFilterDescriptor, e);
    }
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/filters/{filtername}",
          method = "POST")

  @HamDoc(tags = {"plugin/js"},
          description = "Update Single filter",
          path = @PathParameter(key = "filtername"),
          requests = @HamRequest(
              body =  JsFilterConfig.class
          ))
  public void saveJsFilter(Request req, Response res) {
    var jsFilterPath = configuration.getConfiguration(JsFilterConfig.class).getPath();
    var jsFilterDescriptor = req.getPathParameter("filtername");


    // https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
    try {
      File f;
      var realPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor+".json");
      Path of = Path.of(jsFilterPath);
      if(!Files.exists(of)){
        Files.createDirectory(of);
      }
      JsFilterDescriptor result =mapper.readValue(req.getRequestText(),JsFilterDescriptor.class);
      Files.writeString(Path.of(realPath),req.getRequestText());
      res.setStatusCode(200);
      eventQueue.handle(new ScriptsModified());
    } catch (Exception e) {
      logger.error("Error reading js filter " + jsFilterDescriptor, e);
    }
  }



  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/filters/{filtername}",
          method = "DELETE")
  @HamDoc(tags = {"plugin/js"},
          description = "Delete Single filter",
          path = @PathParameter(key = "filtername"))
  public void deleteJsFilter(Request req, Response res) {
    var jsFilterPath = configuration.getConfiguration(JsFilterConfig.class).getPath();
    var jsFilterDescriptor = req.getPathParameter("filtername");


    // https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
    try {
      File f;
      var realPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor+".json");
      Path of = Path.of(realPath);
      System.out.println(of);
      if(Files.exists(of)){
        Files.deleteIfExists(of);
      }
      res.setResponseText("OK");
      res.setStatusCode(200);
      eventQueue.handle(new ScriptsModified());
    } catch (Exception e) {
      logger.error("Error reading js filter " + jsFilterDescriptor, e);
    }
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/filters",
          method = "POST")
  @HamDoc(tags = {"plugin/js"},
          description = "Create Single filter",
          requests = @HamRequest(
                  body =  JsFilterConfig.class
          ))
  public void uploadJsFilter(Request req, Response res) throws JsonProcessingException {
    var jsFilterPath = configuration.getConfiguration(JsFilterConfig.class).getPath();
    JsonFileData jsonFileData = mapper.readValue(req.getRequestText(), JsonFileData.class);
    String fileFullPath = jsonFileData.getName();

    var scriptName = fileFullPath.substring(0, fileFullPath.lastIndexOf('.'));
    var realScript = mapper.readValue(jsonFileData.readAsString(), JsFilterDescriptor.class);
    scriptName = realScript.getId();
    // https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
    try {
      File f;
      var realPath = fileResourcesUtils.buildPath(jsFilterPath,scriptName+".json");
      Path of = Path.of(jsFilterPath);
      if(!Files.exists(of)){
        Files.createDirectory(of);
      }
      Files.writeString(Path.of(realPath),jsonFileData.readAsString());
      res.setResponseText(realScript.getId());
      res.setStatusCode(200);
      eventQueue.handle(new ScriptsModified());
    } catch (Exception e) {
      logger.error("Error uploading js filter " + realScript, e);
    }
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
          pathAddress = "/api/plugins/jsfilter/filters/{filtername}/{file}",
          method = "GET")
  @HamDoc(tags = {"plugin/js"},
          description = "Retrieve the content of a filter associated file",
          path = {@PathParameter(key = "filtername"),
                  @PathParameter(key = "file")},
          responses = @HamResponse(
                  body = String.class
          )
  )
  public void getJsFilterFile(Request req, Response res) {
    var jsFilterPath = configuration.getConfiguration(JsFilterConfig.class).getPath();
    var jsFilterDescriptor = req.getPathParameter("filtername");
    var fileId = req.getPathParameter("file");
    if(fileId==null || fileId.isEmpty()){
      res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.TEXT);
      res.setResponseText("");
      return;
    }


    // https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
    try {
      File f;
      var realPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor+".json");
      var subPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor,fileId);
      //var result = loadSinglePlugin(realPath,jsFilterPath);
      var result = Files.readString(Path.of(subPath));
      res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.TEXT);
      res.setResponseText(result);
    } catch (Exception e) {
      logger.error("Error reading js filter " + jsFilterDescriptor, e);
    }
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/filters/{filtername}/{file}",
          method = "POST")

  @HamDoc(tags = {"plugin/js"},
          description = "Set the content of a filter associated file",
          path = {@PathParameter(key = "filtername"),
                  @PathParameter(key = "file")},
          requests = @HamRequest(
                  body = String.class
          )
  )
  public void putJsFilterFile(Request req, Response res) {
    var jsFilterPath = configuration.getConfiguration(JsFilterConfig.class).getPath();
    var jsFilterDescriptor = req.getPathParameter("filtername");
    var fileId = req.getPathParameter("file");


    // https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
    try {
      File f;
      var realPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor+".json");
      var subPath = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor);
      Path of = Path.of(realPath);
      var result = mapper.readValue(Files.readString(of),JsFilterDescriptor.class);

      Path path = Path.of(subPath);
      if(!Files.exists(path)){
        Files.createDirectory(path);
      }
      var founded = false;
      if(result.getRequires()!=null){
        for (var require :
                result.getRequires()) {
          if(require.equalsIgnoreCase(fileId)){
            founded=true;
            break;
          }
        }
      }else{
        result.setRequires(new ArrayList<>());
      }
      if(!founded){
        result.getRequires().add(fileId);
        Files.writeString(of,mapper.writeValueAsString(result));
      }
      var subPathSubFile = fileResourcesUtils.buildPath(jsFilterPath,jsFilterDescriptor,fileId);
      //var result = loadSinglePlugin(realPath,jsFilterPath);
      var content = req.getRequestText();
      Files.writeString(Path.of(subPathSubFile),content);
      res.setStatusCode(200);
      eventQueue.handle(new ScriptsModified());
    } catch (Exception e) {
      logger.error("Error reading js filter " + jsFilterDescriptor, e);
    }
  }
}
