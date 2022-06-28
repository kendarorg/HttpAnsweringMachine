package org.kendar.servers.utils;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.http.annotations.multi.QueryString;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class SettingsAPI  implements FilteringClass {
    private JsonConfiguration configuration;

    public SettingsAPI(JsonConfiguration configuration){

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
        res.addHeader("content-type","application/json");
        res.setStatusCode(200);
    }
}
