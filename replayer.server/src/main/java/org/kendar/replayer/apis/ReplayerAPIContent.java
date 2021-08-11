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
public class ReplayerAPIContent implements FilteringClass {

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}/{requestOrResponse}",
            method = "GET")
    public boolean retrieveContent(Request req, Response res){
        var id = req.getPathParameter("id");
        var line = req.getPathParameter("line");
        var requestOrResponse = req.getPathParameter("requestOrResponse");
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}/{requestOrResponse}",
            method = "POST")
    public boolean modifyConent(Request req, Response res){
        var id = req.getPathParameter("id");
        var line = req.getPathParameter("line");
        var requestOrResponse = req.getPathParameter("requestOrResponse");
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}/{requestOrResponse}",
            method = "DELETE")
    public boolean deleteConent(Request req, Response res){
        var id = req.getPathParameter("id");
        var line = req.getPathParameter("line");
        var requestOrResponse = req.getPathParameter("requestOrResponse");
        return false;
    }
}
