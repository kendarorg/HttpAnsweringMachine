package org.kendar.servers.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.models.JsonFileData;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FullDownloadUploadService;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class SettingsAPI implements FilteringClass {
    private final JsonConfiguration configuration;
    private final FullDownloadUploadService downloadUploadService;

    public SettingsAPI(JsonConfiguration configuration, FullDownloadUploadService downloadUploadService) {

        this.configuration = configuration;
        this.downloadUploadService = downloadUploadService;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/utils/settings",
            method = "GET")
    @HamDoc(description = "Retrieve the current configuration",
            responses = @HamResponse(
                    body = String.class,
                    description = "The json formatted configuration"
            ),
            tags = {"base/utils"}
    )
    public void downloadSettings(Request req, Response res) throws Exception {
        var result = configuration.getConfigurationAsString();
        res.setResponseText(result);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setStatusCode(200);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/utils/settings/full",
            method = "GET")
    @HamDoc(description = "Retrieve the current configuration,recordings etc",
            responses = @HamResponse(
                    bodyType = ConstantsMime.ZIP,
                    description = "The zip with the settings"
            ),
            tags = {"base/utils"}
    )
    public void downloadFull(Request req, Response res) throws Exception {
        var data = downloadUploadService.retrieveItems();
        res.setResponseBytes(data);
        res.setBinaryResponse(true);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.ZIP);
        res.setStatusCode(200);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/utils/settings/full",
            method = "POST")
    @HamDoc(description = "Upload the full configuration,recordings etc",
            requests = @HamRequest(
                    accept = ConstantsMime.ZIP
            ),
            tags = {"base/utils"}
    )
    public void uploadFull(Request req, Response res) throws Exception {
        downloadUploadService.uploadItems(req.getRequestBytes());
        res.setStatusCode(200);
    }
    //curl -H "Content-Type:application/octet-stream" --data-binary "@full.zip" http://localhost/api/utils/settings/full

    static ObjectMapper mapper = new ObjectMapper();

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/utils/settings",
            method = "POST")
    @HamDoc(description = "Set the settings",

            requests = @HamRequest(
                    accept = ConstantsMime.JSON,
                    body = String.class
            ),
            tags = {"base/utils"}
    )
    public void setNewSettings(Request req, Response res) throws Exception {
        JsonFileData jsonFileData = mapper.readValue(req.getRequestText(), JsonFileData.class);
        var body = jsonFileData.readAsString();
        configuration.setConfigurationAsString(body);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setStatusCode(200);
    }
}

