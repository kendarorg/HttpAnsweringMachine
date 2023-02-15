package org.kendar.servers.http.api;

import org.kendar.Main;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.Example;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}",
        blocking = true)
public class HealthController implements FilteringClass {
    @Override
    public String getId() {
        return "org.kendar.servers.http.api.HealthController";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/health",
            method = "GET")
    @HamDoc(
            tags = {"base/utils"},
            description = "Retrieve the application status",
            responses = @HamResponse(
                    body = String.class,
                     examples = @Example(example = "OK")
            ))
    public void getStatus(Request req, Response res) {
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.TEXT);
        res.setResponseText("OK");
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/shutdown",
            method = "GET")
    @HamDoc(
            tags = {"base/utils"},
            description = "Shutdown the application",
            responses = @HamResponse(
                    body = String.class,
                    examples = @Example(example = "OK")
            ))
    public void doShutdown(Request req, Response res) {
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.TEXT);
        res.setResponseText("OK");
        Main.shutdown();
    }
}
