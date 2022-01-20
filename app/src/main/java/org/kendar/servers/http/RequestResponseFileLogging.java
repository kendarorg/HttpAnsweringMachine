package org.kendar.servers.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@HttpTypeFilter(hostAddress = "*")
public class RequestResponseFileLogging implements FilteringClass {
  private static final ObjectMapper mapper = new ObjectMapper();
  private final Logger responseLogger;
  private final Logger requestLogger;
  private final Logger staticLogger;
  private final Logger dynamicLogger;
  private final FileResourcesUtils fileResourcesUtils;
  private final JsonConfiguration configuration;
  private String roundtripsPath;

  public RequestResponseFileLogging(
      FileResourcesUtils fileResourcesUtils,
      LoggerBuilder loggerBuilder,
      JsonConfiguration configuration) {

    this.fileResourcesUtils = fileResourcesUtils;
    this.responseLogger = loggerBuilder.build(Response.class);
    this.requestLogger = loggerBuilder.build(Request.class);
    this.staticLogger = loggerBuilder.build(StaticRequest.class);
    this.dynamicLogger = loggerBuilder.build(DynamicReqest.class);
    this.configuration = configuration;
  }

  @Override
  public String getId() {
    return "org.kendar.servers.http.RequestResponseFileLogging";
  }

  @PostConstruct
  public void init() {
    try {
      var config = configuration.getConfiguration(GlobalConfig.class);
      roundtripsPath = fileResourcesUtils.buildPath(config.getLogging().getLogRoundtripsPath());
      var np = Path.of(roundtripsPath);
      var dirPath = new File(np.toString());
      if(!dirPath.exists()){
        dirPath.mkdir();
      }
      if (!Files.isDirectory(np)) {
        Files.createDirectory(np);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @HttpMethodFilter(
      phase = HttpFilterType.POST_RENDER,
      pathAddress = "*",
      method = "*",
      id = "1001a4b4-277d-11ec-9621-0242ac130002")
  public boolean doLog(Request serReq, Response serRes) {
    if (serReq.isStaticRequest() && !staticLogger.isDebugEnabled()) return false;
    if (!serReq.isStaticRequest() && !dynamicLogger.isDebugEnabled()) return false;
    var rt = serReq.getRequestText();
    var rb = serReq.getRequestBytes();
    var st = serRes.getResponseText();
    var sb = serRes.getResponseBytes();

    //TODO ERROR RESETTING req/res
    if (requestLogger.isDebugEnabled()
        && serReq.getRequestText() != null
        && serReq.getRequestText().length() > 100) {
      serReq.setRequestText(serReq.getRequestText().substring(0, 100));
    } else if (!requestLogger.isTraceEnabled()) {
      serReq.setRequestText(null);
    }
    serReq.setRequestBytes(null);

    if (responseLogger.isDebugEnabled()
        && serRes.getResponseText() != null
        && serRes.getResponseText().length() > 100) {
      serRes.setResponseText(serRes.getResponseText().substring(0, 100));
    } else if (!responseLogger.isTraceEnabled()) {
      serRes.setResponseText(null);
    }
    serRes.setResponseBytes(null);
    var extension = getOptionalExtension(serReq.getPath());
    var filePath =
        roundtripsPath
            + File.separator
            + cleanUp(serReq.getMs() + "_" + serReq.getHost() + "_" + serReq.getPath());
    if (extension != null) {
      filePath += "." + extension;
    }
    filePath += ".log";

    try {
      FileWriter myWriter = new FileWriter(filePath);
      myWriter.write(
          serReq.getMethod()
              + " "
              + serReq.getProtocol()
              + " "
              + serReq.getHost()
              + " "
              + serReq.getPath()
              + "\n");
      myWriter.write("==========================\n");
      myWriter.write("REQUEST:\n");
      myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(serReq) + "\n");
      myWriter.write("==========================\n");
      myWriter.write("RESPONSE:\n");
      myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(serRes) + "\n");
      myWriter.write("==========================\n");
      myWriter.close();
    } catch (Exception ex) {

    }
    serReq.setRequestBytes(rb);
    serReq.setRequestText(rt);
    serRes.setResponseText(st);
    serRes.setResponseBytes(sb);
    return false;
  }

  private String getOptionalExtension(String filePath) {
    String extension = null;
    int i = filePath.lastIndexOf('.');
    int p = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

    if (i > p) {
      extension = filePath.substring(i + 1);
    }
    return extension;
  }

  private String cleanUp(String s) {
    StringBuilder result = new StringBuilder();
    for (var c : s.toCharArray()) {
      if (c == '.') c = '-';
      if (c == '\\') c = '-';
      if (c == '/') c = '-';
      if (c == '`') c = '-';
      if (c == ':') c = '-';
      result.append(c);
    }
    return result.toString();
  }
}
