package org.kendar.servers.logging;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.dns.configurations.DnsConfig;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}",
  blocking = true)
public class LoggingApi implements FilteringClass {
  private JsonConfiguration configuration;
  private LoggerBuilder loggerBuilder;
  ObjectMapper mapper = new ObjectMapper();

  public LoggingApi(JsonConfiguration configuration, LoggerBuilder loggerBuilder){

    this.configuration = configuration;
    this.loggerBuilder = loggerBuilder;
  }
  public void setLevelOfLog(String logger,Level level){
    var config = configuration.getConfiguration(GlobalConfig.class);
    config.getLogging().getLoggers().put(logger,level);
    loggerBuilder.setLevel(logger, level);
  }

  @HttpMethodFilter(phase = HttpFilterType.API,
    pathAddress = "/api/logger",
    method = "GET",id="1000aab4-277d-a1ef-5621-0242ac130002")
  public boolean getLoggers(Request req, Response res) throws JsonProcessingException {
    var config = configuration.getConfiguration(GlobalConfig.class);
    res.addHeader("Content-type", "application/json");
    res.setResponseText(mapper.writeValueAsString(config.getLogging().getLoggers()));
    return false;
  }

  @HttpMethodFilter(phase = HttpFilterType.API,
    pathAddress = "/api/logger/{id}",
    method = "GET",id="1000a4b-277d-a1ef-5621-0242ac130002")
  public boolean getLogger(Request req, Response res) throws JsonProcessingException {
    var config = configuration.getConfiguration(GlobalConfig.class);
    var id = req.getPathParameter("id");
    var logger = config.getLogging().getLoggers().get(id);
    res.addHeader("Content-type", "application/json");
    res.setResponseText(mapper.writeValueAsString(logger));
    return false;
  }

  @HttpMethodFilter(phase = HttpFilterType.API,
    pathAddress = "/api/logger/{id}",
    method = "DELETE",id="10d0a4b4-277d-a1ef-5621-0242ac130002")
  public boolean deleteLogger(Request req, Response res) throws JsonProcessingException {
    var config = configuration.getConfiguration(GlobalConfig.class);
    var id = req.getPathParameter("id");
    var level = req.getQuery("level").toUpperCase(Locale.ROOT);
    setLevelOfLog(id,Level.OFF);
    config.getLogging().getLoggers().remove(id);
    configuration.setConfiguration(config);
    return false;
  }

  @HttpMethodFilter(phase = HttpFilterType.API,
    pathAddress = "/api/logger/{id}",
    method = "POST",id="10c0a4b4-277d-a1ef-5621-0242ac130002")
  public boolean setLogger(Request req, Response res) throws JsonProcessingException {
    var config = configuration.getConfiguration(GlobalConfig.class);
    var id = req.getPathParameter("id");
    var level = req.getQuery("level").toUpperCase(Locale.ROOT);
    setLevelOfLog(id,Level.toLevel(level));
    config.getLogging().getLoggers().put(id,Level.toLevel(level));
    configuration.setConfiguration(config);
    res.addHeader("Content-type", "application/json");
    res.setResponseText(mapper.writeValueAsString(config.getLogging().getLoggers().get(id)));
    return false;
  }

  @Override public String getId() {
    return "org.kendar.servers.logging.LoggingApi";
  }
}
