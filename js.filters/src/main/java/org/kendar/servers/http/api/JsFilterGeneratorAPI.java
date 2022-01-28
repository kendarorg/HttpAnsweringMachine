package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.config.HttpsWebServerConfig;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.http.generators.BaseGenerator;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class JsFilterGeneratorAPI implements FilteringClass {
    private final Logger logger;
    private final HashMap<String, BaseGenerator> generators;
    final ObjectMapper mapper = new ObjectMapper();
    private JsonConfiguration configuration;


    @Override
    public String getId() {
        return this.getClass().getName();
    }

    public JsFilterGeneratorAPI(JsonConfiguration configuration,
                                FileResourcesUtils fileResourcesUtils,
                                LoggerBuilder loggerBuilder,
                                EventQueue eventQueue, List<BaseGenerator> generators) {
        this.configuration = configuration;

        this.generators = new HashMap<>();
        for (var bg :
                generators) {
            this.generators.put(bg.getType(), bg);
        }
        this.logger = loggerBuilder.build(JsFilterAPI.class);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/jsfiltergen/generate/{recording}/{line}/{type}",
            method = "GET",
            id = "1000a4b4-297id-11ec-9621-02galac130002")
    public void getJsFiltersList(Request req, Response res) throws IOException, InterruptedException {
        var type = req.getPathParameter("type").toLowerCase(Locale.ROOT);
        var recording = req.getPathParameter("recording");
        var line = Integer.parseInt(req.getPathParameter("line"));

        var recordingLineRequest = getRecordingLineRequest(recording, line);
        var recordingLineResponse = getRecordingLineResponse(recording, line);
        var fileName = recording + "." + line;
        var generator = this.generators.get(type);
        var result = generator.generate(recordingLineRequest, recordingLineResponse);
        res.setResponseText(result);
        res.addHeader("Content-Disposition", fileName);
        res.addHeader("content-type", "text/plain");
        res.setStatusCode(200);
    }

    private Request getRecordingLineRequest(String recording, int line) throws IOException, InterruptedException {
        HttpResponse<String> response = retrieveRecordingLine(recording, line);

        var jsonNode = mapper.readValue(response.body(), JsonNode.class);
        var jsonNodeRequest = jsonNode.get("request");
        var stringRequest = mapper.writeValueAsString(jsonNodeRequest);
        var req = mapper.readValue(stringRequest,Request.class);
        // the response:
        return req;
    }

    private HttpResponse<String> retrieveRecordingLine(String recording, int line) throws IOException, InterruptedException {
        var config = configuration.getConfiguration(GlobalConfig.class);
        var targetAddress = "http://" + config.getLocalAddress() + "/api/plugins/replayer/recording/" + recording + "/line/" + line;
        // create a client
        var client = HttpClient.newHttpClient();

        // create a request
        var request = HttpRequest.newBuilder(
                        URI.create(targetAddress))
                .header("accept", "application/json")
                .build();

        // use the client to send the request
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }


    private Response getRecordingLineResponse(String recording, int line) throws IOException, InterruptedException {
        HttpResponse<String> response = retrieveRecordingLine(recording, line);

        var jsonNode = mapper.readValue(response.body(), JsonNode.class);
        var jsonNodeResponse = jsonNode.get("response");
        var stringResponse = mapper.writeValueAsString(jsonNodeResponse);
        var res = mapper.readValue(stringResponse,Response.class);
        // the response:
        return res;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/jsfiltergen/generators",
            method = "GET",
            id = "1000a777297id-11ecfluk1-0242ac130002")
    public void getGenerators(Request req, Response res) throws JsonProcessingException {
        var result = new ArrayList<GeneratorModel>();
        for (var generator :
                this.generators.values()) {
            var gm = new GeneratorModel();
            gm.setDescription(generator.getDescription());
            gm.setType(generator.getType());
            result.add(gm);
        }

        res.setResponseText(mapper.writeValueAsString(result));
        res.addHeader("content-type", "application/json");
        res.setStatusCode(200);
    }
}
