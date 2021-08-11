package org.kendar.replayer.apis;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${replayer.address:replayer.local.org}",
        blocking = true)
public class ReplayerAPISingleLine implements FilteringClass {

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}",
            method = "GET")
    public boolean retrieveSingleLineData(Request req, Response res){
        var id = req.getPathParameter("id");
        var line = req.getPathParameter("line");
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}",
            method = "PUT")
    public boolean modifySingleLineData(Request req, Response res){
        var id = req.getPathParameter("id");
        var line = req.getPathParameter("line");
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}",
            method = "DELETE")
    public boolean deleteSingleLineData(Request req, Response res){
        var id = req.getPathParameter("id");
        var line = req.getPathParameter("line");
        return false;
    }
}
