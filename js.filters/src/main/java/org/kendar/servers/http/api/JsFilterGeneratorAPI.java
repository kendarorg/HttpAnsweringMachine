package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class JsFilterGeneratorAPI implements FilteringClass {
    private final JsonConfiguration configuration;
    private final Logger logger;
    private final FileResourcesUtils fileResourcesUtils;
    private final EventQueue eventQueue;

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    public JsFilterGeneratorAPI(JsonConfiguration configuration,
                       FileResourcesUtils fileResourcesUtils,
                       LoggerBuilder loggerBuilder,
                       EventQueue eventQueue) {

        this.logger = loggerBuilder.build(JsFilterAPI.class);
        this.configuration = configuration;
        this.fileResourcesUtils = fileResourcesUtils;
        this.eventQueue = eventQueue;
    }
    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/jsfiltergen/{recording}/{line}/{type}",
            method = "GET",
            id = "1000a4b4-297id-11ec-9621-0242ac130002")
    public boolean getJsFiltersList(Request req, Response res) throws JsonProcessingException {
        var type = req.getPathParameter("type").toLowerCase(Locale.ROOT);
        var recording = req.getPathParameter("recording");
        var line = Integer.parseInt(req.getPathParameter("line"));
        var fileName = recording+"."+line;
        if(type.equalsIgnoreCase("curl")){
            fileName+=".sh";
        }else if(type.equalsIgnoreCase("jsfilter")){
            fileName+=".js";
        }else if(type.equalsIgnoreCase("junit")){
            fileName+=".java";
        }
        res.setResponseText(fileName);
        res.addHeader("Content-Disposition",fileName);
        res.addHeader("content-type","text/plain");
        res.setStatusCode(200);
        return false;
    }
}
