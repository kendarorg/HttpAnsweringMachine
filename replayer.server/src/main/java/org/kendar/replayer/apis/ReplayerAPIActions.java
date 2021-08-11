package org.kendar.replayer.apis;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${replayer.address:replayer.local.org}",
        blocking = true)
public class ReplayerAPIActions implements FilteringClass {

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/record/{action}",
            method = "GET")
    public boolean recording(Request req, Response res){
        var id = req.getPathParameter("id");
        var action = req.getPathParameter("action");
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/replay/{action}",
            method = "GET")
    public boolean replaying(Request req, Response res){
        var id = req.getPathParameter("id");
        var action = req.getPathParameter("action");
        return false;
    }
}
