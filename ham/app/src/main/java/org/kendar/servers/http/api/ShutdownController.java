package org.kendar.servers.http.api;

import org.kendar.Main;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.Example;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "127.0.0.1",
        blocking = true)
public class ShutdownController implements FilteringClass {
    @Override
    public String getId() {
        return "org.kendar.servers.http.api.ShutdownController";
    }


    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/shutdown",
            method = "GET")
    @HamDoc(
            tags = {"base/utils"},
            description = "Kills gracefully the application",
            responses = @HamResponse(
                    body = String.class,
                    examples = @Example(example = "OK")
            ))
    public void shutdown(Request req, Response res) {
        Main.doRun.set(false);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.TEXT);
        res.setResponseText("OK");
    }
}
