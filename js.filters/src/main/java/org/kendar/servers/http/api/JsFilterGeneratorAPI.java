package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.http.generators.BaseGenerator;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class JsFilterGeneratorAPI implements FilteringClass {
    private final JsonConfiguration configuration;
    private final Logger logger;
    private final FileResourcesUtils fileResourcesUtils;
    private final EventQueue eventQueue;
    private final HashMap<String,BaseGenerator> generators;
    final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    public JsFilterGeneratorAPI(JsonConfiguration configuration,
                                FileResourcesUtils fileResourcesUtils,
                                LoggerBuilder loggerBuilder,
                                EventQueue eventQueue, List<BaseGenerator> generators) {

        this.generators = new HashMap<>();
        for (var bg :
                generators) {
            this.generators.put(bg.getType(),bg);
        }
        this.logger = loggerBuilder.build(JsFilterAPI.class);
        this.configuration = configuration;
        this.fileResourcesUtils = fileResourcesUtils;
        this.eventQueue = eventQueue;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/jsfiltergen/generate/{recording}/{line}/{type}",
            method = "GET",
            id = "1000a4b4-297id-11ec-9621-02galac130002")
    public boolean getJsFiltersList(Request req, Response res) throws JsonProcessingException {
        var type = req.getPathParameter("type").toLowerCase(Locale.ROOT);
        var recording = req.getPathParameter("recording");
        var line = Integer.parseInt(req.getPathParameter("line"));
        var fileName = recording+"."+line;
        var generator = this.generators.get(type);
        Request recordingLineRequest = null;
        Response recordingLineResponse = null;
        var result = generator.generate(recordingLineRequest,recordingLineResponse);
        res.setResponseText(result);
        res.addHeader("Content-Disposition",fileName);
        res.addHeader("content-type","text/plain");
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/jsfiltergen/generators",
            method = "GET",
            id = "1000a777297id-11ecfluk1-0242ac130002")
    public boolean getGenerators(Request req, Response res) throws JsonProcessingException {
        var result = new ArrayList<GeneratorModel>();
        for (var generator :
                this.generators.values()) {
            var gm = new GeneratorModel();
            gm.setDescription(generator.getDescription());
            gm.setType(generator.getType());
            result.add(gm);
        }

        res.setResponseText(mapper.writeValueAsString(result));
        res.addHeader("content-type","application/json");
        res.setStatusCode(200);
        return false;
    }
}
