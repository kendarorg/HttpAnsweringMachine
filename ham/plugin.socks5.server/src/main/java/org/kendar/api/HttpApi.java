package org.kendar.api;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.QueryString;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.socks5.Socks5Config;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class HttpApi implements FilteringClass {
    private final JsonConfiguration configuration;

    public HttpApi(JsonConfiguration configuration){

        this.configuration = configuration;
    }
    @Override
    public String getId() {
        return getClass().getName();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/socks5/http",
            method = "GET")
    @HamDoc(description = "Retrieves all the replayer results", tags = {"plugin/replayer"},
            query = @QueryString(key = "captureAllHttp", description = "Tells to set/unset to capture all http data " +
                    "without setting the dns"))
    public void captureAllHttpRequests(Request request, Response response) throws Exception {
        var socks5Config = configuration.getConfiguration(Socks5Config.class).copy();
        var queryParam = request.getQuery("captureAllHttp");
        var captureAll = false;
        if(queryParam!=null){
            captureAll = Boolean.getBoolean(queryParam);
        }
        socks5Config.setInterceptAllHttp(captureAll);
        configuration.setConfiguration(socks5Config);
        response.setStatusCode(200);
    }
}
