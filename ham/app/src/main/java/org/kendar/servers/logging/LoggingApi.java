package org.kendar.servers.logging;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.Example;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.http.annotations.multi.QueryString;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.http.PluginsInitializer;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.http.SpecialLoggerDescriptor;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class LoggingApi implements FilteringClass {
    final ObjectMapper mapper = new ObjectMapper();
    private final JsonConfiguration configuration;
    private final LoggerBuilder loggerBuilder;
    private final PluginsInitializer pluginsInitializer;

    public LoggingApi(
            JsonConfiguration configuration,
            LoggerBuilder loggerBuilder,
            PluginsInitializer pluginsInitializer) {

        this.configuration = configuration;
        this.loggerBuilder = loggerBuilder;
        this.pluginsInitializer = pluginsInitializer;
    }

    public void setLevelOfLog(String logger, Level level) {
        var config = configuration.getConfiguration(GlobalConfig.class);
        config.getLogging().getLoggers().put(logger, level);
        loggerBuilder.setLevel(logger, level);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/log/logger",
            method = "GET",
            id = "1000aab4-277d-a1ef-5621-0242ac130002")
    @HamDoc(
            description = "List currently active loggers",
            responses = @HamResponse(
                    body = LogDTO[].class
            ),
            tags = {"base/utils"})
    public void getLoggers(Request req, Response res) throws JsonProcessingException {
        var config = configuration.getConfiguration(GlobalConfig.class);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        var loggers = convertLoggers(config.getLogging().getLoggers())
                .stream().filter(a -> !a.getValue().equalsIgnoreCase("OFF")).collect(Collectors.toList());
        res.setResponseText(mapper.writeValueAsString(loggers));
    }

    private List<LogDTO> convertLoggers(HashMap<String, Level> loggers) {
        var result = new ArrayList<LogDTO>();
        for (var kvp : loggers.entrySet()) {
            var dto = new LogDTO();
            dto.setKey(kvp.getKey());
            dto.setValue(kvp.getValue().levelStr);
            result.add(dto);
        }
        return result;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/log/special",
            method = "GET",
            id = "1000aab4-277d-a1tf-5621-0242ac130002")
    @HamDoc(
            description = "List all the special logging classes",
            responses = @HamResponse(
                    body = SpecialLoggerDescriptor.class
            ),
            tags = {"base/logs"})
    public void getSpecialLoggers(Request req, Response res) throws JsonProcessingException {
        var specialLoggers = pluginsInitializer.getSpecialLoggers();
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(specialLoggers));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/log/logger/{id}",
            method = "GET",
            id = "1000a4b-277d-a1ef-5621-0242ac130002")
    @HamDoc(description = "Retrieve the level of the specific logger",
            responses = @HamResponse(
                    body = String.class,
                    examples = @Example(example = "DEBUG")
            ),
            tags = {"base/logs"},
            path = @PathParameter(key = "id")
    )
    public void getLogger(Request req, Response res) throws JsonProcessingException {
        var config = configuration.getConfiguration(GlobalConfig.class);
        var id = req.getPathParameter("id");
        var logger = config.getLogging().getLoggers().get(id);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(logger));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/log/logger/{id}",
            method = "DELETE",
            id = "10d0a4b4-277d-a1ef-5621-0242ac130002")
    @HamDoc(description = "Set to OFF the specified logger",
            tags = {"base/logs"},
            path = @PathParameter(key = "id")
    )
    public void deleteLogger(Request req, Response res) {
        var config = configuration.getConfiguration(GlobalConfig.class);
        var id = req.getPathParameter("id");
        setLevelOfLog(id, Level.OFF);
        config.getLogging().getLoggers().remove(id);
        configuration.setConfiguration(config);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/log/logger/{id}",
            method = "POST",
            id = "10c0a4b4-277d-a1ef-5621-0242ac130002")
    @HamDoc(description = "Set the level of the logger",
            responses = @HamResponse(
                    body = String.class,
                    examples = @Example(example = "DEBUG")
            ),
            query = @QueryString(
                    key = "level",
                    example = "DEBUG"
            ),
            tags = {"base/logs"},
            path = @PathParameter(key = "id")
    )
    public void setLogger(Request req, Response res) throws JsonProcessingException {
        var config = configuration.getConfiguration(GlobalConfig.class);
        var id = req.getPathParameter("id");
        var level = req.getQuery("level").toUpperCase(Locale.ROOT);
        setLevelOfLog(id, Level.toLevel(level));
        config.getLogging().getLoggers().put(id, Level.toLevel(level));
        configuration.setConfiguration(config);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(config.getLogging().getLoggers().get(id)));
    }

    @Override
    public String getId() {
        return "org.kendar.servers.logging.LoggingApi";
    }
}
