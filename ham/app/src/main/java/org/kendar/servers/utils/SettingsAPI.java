package org.kendar.servers.utils;

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
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class SettingsAPI implements FilteringClass {
    private final JsonConfiguration configuration;

    public SettingsAPI(JsonConfiguration configuration) {

        this.configuration = configuration;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/utils/settings",
            method = "GET",
            id = "GET:/api/utils/settings")
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
            pathAddress = "/api/utils/settings",
            method = "POST",
            id = "POST:/api/utils/settings")
    @HamDoc(description = "Set the settings",

            requests = @HamRequest(
                    accept = ConstantsMime.JSON,
                    body = String.class
            ),
            tags = {"base/utils"}
    )
    public void setNewSettings(Request req, Response res) throws Exception {
        var body = req.getRequestText();
        configuration.setConfigurationAsString(body);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setStatusCode(200);
    }
}
